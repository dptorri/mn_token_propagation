package example;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;
import reactor.core.publisher.Mono;

@Client(id = "userecho")
//@Client annotation is used with a service id. We will reference the exact service id in the configuration

@Requires(notEnv = Environment.TEST)
//Expresses that the configuration will not load within the given environments.

public interface UserEchoClient extends UsernameFetcher {

    @Override
    @Consumes(MediaType.TEXT_PLAIN)
    @Get("/user")
    Mono<String> findUsername(@Header("Authorization") String authorization);
}
