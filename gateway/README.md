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
1) mn create-app example.micronaut.gateway --build=gradle --lang=java
 
// build.gradle
annotationProcessor("io.micronaut.security:micronaut-security-annotations")
implementation("io.micronaut.security:micronaut-security-jwt")

// application.properties
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



---------------------------------------------------------------------------
DISCLAIMER: For educational purposes only, please refer to micronaut documentation
https://guides.micronaut.io/latest/micronaut-token-propagation-gradle-java.html
for the original tutorial.