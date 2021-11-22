package example;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import reactor.core.publisher.Mono;

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
