package example;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import java.security.Principal;

@Controller("/user")
public class UserController {

    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Produces(MediaType.TEXT_PLAIN)
    @Get
    String index(Principal principal) {
        return principal.getName();
    }
}
