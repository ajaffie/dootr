package dev.ajaffie.dootr.doots.controllers;

import dev.ajaffie.dootr.doots.domain.*;
import dev.ajaffie.dootr.doots.services.DootService;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory;
import io.smallrye.jwt.auth.principal.ParseException;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@ApplicationScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class DootController {

    private DootService dootService;

    private JWTAuthContextInfo jwtAuthContextInfo;
    private JWTCallerPrincipalFactory jwtFactory;

    @Inject
    public DootController(DootService dootService, JWTAuthContextInfo jwtAuthContextInfo) {
        this.dootService = dootService;
        this.jwtAuthContextInfo = jwtAuthContextInfo;
        this.jwtFactory = JWTCallerPrincipalFactory.instance();
    }

    @POST
    @Path("/additem")
    public CompletionStage<Response> createDoot(@CookieParam("session") Cookie sessionCookie, @RequestBody AddItemDto addItemDto) {
        User user = getUserFromCookie(sessionCookie);
        if (user == null) {
            return CompletableFuture.completedFuture(Response.status(Response.Status.OK).entity(new ErrorDto("User is not logged in.")).build());
        }
        if (addItemDto.content == null || addItemDto.content.length() == 0) {
            return CompletableFuture.completedFuture(Response.status(Response.Status.OK).entity(new ErrorDto("No content supplied.")).build());
        }
        if (addItemDto.content.length() > 280) {
            return CompletableFuture.completedFuture(Response.status(Response.Status.OK).entity(new ErrorDto("Doot is too long.")).build());
        }
        return dootService.createDoot(addItemDto, user)
                .thenApply(id -> {
                    if (id == null) {
                        return Response.status(Response.Status.OK).entity(new ErrorDto("Doot not created. Try again later.")).build();
                    } else {
                        return Response.ok(new OkWithIdDto(id)).build();
                    }
                });
    }

    @GET
    @Path("/item/{id}")
    public CompletionStage<Response> getDoot(@PathParam("id") long id) {
        return dootService.getDoot(id)
                .thenApply(doot -> {
                    if (doot == null) {
                        return Response.status(Response.Status.OK).entity(new ErrorDto("Doot not found.")).build();
                    }
                    return Response.ok(new OkWithItemDto(ItemDto.from(doot))).build();
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
