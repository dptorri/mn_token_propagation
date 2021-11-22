package example;

import reactor.core.publisher.Mono;

public interface UsernameFetcher {
    Mono<String> findUsername();
}
