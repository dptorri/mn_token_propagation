## mn_token_propagation
Proper token propagation in microservices using micronaut

### 0. Architecture
#### Gateway (Customer facing)
A microservice secured via JWT which exposes an endpoint /user.
The output of that endpoint is the result of consuming the `userecho` endpoint.


#### userecho (External/Internal service)
A microservice secured via JWT which exposes an endpoint `/user` which
responds with the username of the authenticated user.

### 1. Create Gateway service
```
1) mn create-app example.gateway --build=gradle --lang=java
 
// build.gradle
annotationProcessor("io.micronaut.security:micronaut-security-annotations")
implementation("io.micronaut.security:micronaut-security-jwt")

// application.yml
micronaut:
  application:
    name: gateway
  server:
    port: 1111

```
### 2. Create Mock AuthenticationProviderUserPassword
Simulates the user authentication request and pass 2 mock users.
```
@Singleton 
public class AuthenticationProviderUserPassword implements AuthenticationProvider { 
    
    @Override
    public Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> authenticationRequest) {
        return Flux.create(emitter -> {
            if ((authenticationRequest.getIdentity().equals("sherlock") || authenticationRequest.getIdentity().equals("watson")) &&
                    authenticationRequest.getSecret().equals("password")) {
                emitter.next(AuthenticationResponse.success((String) authenticationRequest.getIdentity()));
                emitter.complete();
            } else {
                emitter.error(AuthenticationResponse.exception());
            }
        }, FluxSink.OverflowStrategy.ERROR);

    }
    
    
❯ curl http://localhost:1111 -v
...
> 
< HTTP/1.1 401 Unauthorized
...
* Closing connection 0
```
### 3. Create a class UserController which exposes /user endpoint.

```
@Controller("/user") 
public class UserController {

    private final UsernameFetcher usernameFetcher;

    public UserController(UsernameFetcher usernameFetcher) {  
        this.usernameFetcher = usernameFetcher;
    }

    @Secured(SecurityRule.IS_AUTHENTICATED)  
    @Produces(MediaType.TEXT_PLAIN) 
    @Get 
    Mono<String> index(@Header("Authorization") String authorization) {  
        return usernameFetcher.findUsername(authorization);
    }
}
```

### 4. Create an interface to encapsulate the collaboration with the `userecho` microservice.

```
package example;

import io.micronaut.http.annotation.Header;
import reactor.core.publisher.Mono;

public interface UsernameFetcher {
    Mono<String> findUsername(@Header("Authorization") String authorization);
}
```

### 5. Create a Micronaut HTTP Declarative @Client `userecho`
```
@Client(id = "userecho") 
//@Client annotation is used with a service id. We will reference in application.yml

@Requires(notEnv = Environment.TEST) 
//Expresses that the configuration will not load within the given environments.

public interface UserEchoClient extends UsernameFetcher {

    @Override
    @Consumes(MediaType.TEXT_PLAIN)
    @Get("/user")
    Mono<String> findUsername(@Header("Authorization") String authorization);
}

// application.yml
micronaut:
  http:
    services:
      userecho:
        urls:
          - "http://localhost:2222"

  security:
    authentication: bearer
    token:
      jwt:
        signatures:
          secret:
            generator:
              secret: '"${JWT_GENERATOR_SIGNATURE_SECRET:pleaseChangeThisSecretForANewOne}"'
```
Set authentication to bearer to receive a JSON response from the login endpoint.
You can create a SecretSignatureConfiguration named generator via configuration as illustrated above.
The generator signature is used to sign the issued JWT claims.

### 6. Provide a UsernameFetcher bean replacement for the Test environment
Basically mocks the username
```
package example;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.http.annotation.Header;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

@Requires(env = Environment.TEST)
@Singleton
public class UserEchoClientReplacement implements UsernameFetcher {

    @Override
    public Mono<String> findUsername(@Header("Authorization") String authorization) {
        return Mono.just("sherlock");
    }
}
```

### 7. Create tests to verify the application is secured
```
@MicronautTest 
public class UserControllerTest {

    @Inject
    @Client("/")
    HttpClient client; 

    @Test
    public void testUserEndpointIsSecured() { 
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.GET("/user"));
        });

        assertEquals(HttpStatus.UNAUTHORIZED, thrown.getResponse().getStatus());
    }
```
### 8. Create test user can fetch username after login
```
@MicronautTest 
public class UserControllerTest {

    @Inject
    @Client("/")
    HttpClient client; 

    @Test
    public void testCanFetchUsernameAfterLogin() {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("sherlock", "password");
        HttpRequest request = HttpRequest.POST("/login", credentials);

        BearerAccessRefreshToken bearerAccessRefreshToken = client.toBlocking().retrieve(request, BearerAccessRefreshToken.class);

        String username = client.toBlocking().retrieve(HttpRequest.GET("/user")
                .header("Authorization", "Bearer " + bearerAccessRefreshToken.getAccessToken()), String.class);

        assertEquals("sherlock", username);
    }
```
### 9. Create userecho service
```
1) mn create-app example.userecho --build=gradle --lang=java
 
// build.gradle
annotationProcessor("io.micronaut.security:micronaut-security-annotations")
implementation("io.micronaut.security:micronaut-security-jwt")
```
---------------------------------------------------------------------------
DISCLAIMER: For educational purposes only, please refer to micronaut documentation
https://guides.micronaut.io/latest/micronaut-token-propagation-gradle-java.html
for the original tutorial.