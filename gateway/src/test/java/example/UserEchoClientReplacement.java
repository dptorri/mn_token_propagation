package example;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

@Requires(env = Environment.TEST)
@Singleton
public class UserEchoClientReplacement implements UsernameFetcher {

    @Override
    public Mono<String> findUsername() {
        return Mono.just("sherlock");
    }
}