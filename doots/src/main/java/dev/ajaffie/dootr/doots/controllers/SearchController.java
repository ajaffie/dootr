package dev.ajaffie.dootr.doots.controllers;

import dev.ajaffie.dootr.doots.domain.ErrorDto;
import dev.ajaffie.dootr.doots.domain.OkWithItemListDto;
import dev.ajaffie.dootr.doots.domain.QueryDto;
import dev.ajaffie.dootr.doots.domain.User;
import dev.ajaffie.dootr.doots.services.SearchService;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
public class SearchController {
    private SearchService searchService;
    private JWTAuthContextInfo jwtAuthContextInfo;
    private JWTCallerPrincipalFactory jwtFactory;

    @Inject
    public SearchController(SearchService searchService, JWTAuthContextInfo jwtAuthContextInfo) {
        this.searchService = searchService;
        this.jwtAuthContextInfo = jwtAuthContextInfo;
        this.jwtFactory = JWTCallerPrincipalFactory.instance();
    }

    @POST
    @Path("/search")
    public CompletionStage<Response> search(@CookieParam("session") Cookie sessionCookie, @RequestBody QueryDto query) {
        return searchService.search(query, getUserFromCookie(sessionCookie))
                .thenApply(doots -> {
                    if (doots == null) {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(new ErrorDto("There was an error searching. Try again later.")).build();
                    }
                    return Response.ok(OkWithItemListDto.fromDoots(doots)).build();
                });
    }

    private User getUserFromCookie(Cookie sessionCookie) {
        try {
            JsonWebToken jwt = jwtFactory.parse(sessionCookie.getValue(), jwtAuthContextInfo);
            return User.fromJwt(jwt);
        } catch (Exception e) {
            return null;
        }
    }
}
