package dev.ajaffie.dootr.doots.controllers;

import dev.ajaffie.dootr.doots.domain.*;
import dev.ajaffie.dootr.doots.services.DootService;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@Consumes(APPLICATION_JSON + ";charset=utf-8")
public class DootController {

    private DootService dootService;
    private Logger logger;

    private JWTAuthContextInfo jwtAuthContextInfo;
    private JWTCallerPrincipalFactory jwtFactory;

    @Inject
    public DootController(DootService dootService, JWTAuthContextInfo jwtAuthContextInfo) {
        this.dootService = dootService;
        this.jwtAuthContextInfo = jwtAuthContextInfo;
        this.jwtFactory = JWTCallerPrincipalFactory.instance();
        this.logger = LoggerFactory.getLogger(DootController.class);
    }

    @POST
    @Path("/additem")
    public CompletionStage<Response> createDoot(@CookieParam("session") Cookie sessionCookie, @RequestBody AddItemDto addItemDto) {
        User user = getUserFromCookie(sessionCookie);
        if (user == null) {
            return CompletableFuture.completedFuture(Response.status(Response.Status.OK).entity(new ErrorDto("User is not logged in.")).build());
        }
        if ((addItemDto.content == null || addItemDto.content.length() == 0) && !"retweet".equals(addItemDto.childType)) {
            return CompletableFuture.completedFuture(Response.status(Response.Status.OK).entity(new ErrorDto("No content supplied.")).build());
        }
        if (addItemDto.content != null && addItemDto.content.length() > 4096) {
            return CompletableFuture.completedFuture(Response.status(Response.Status.OK).entity(new ErrorDto("Doot is too long.")).build());
        }
        return dootService.createDoot(addItemDto, user)
                .handle((id, ex) -> {
                    if (ex != null) {
                        logger.error("Error creating doot: {}", ex.getMessage());
                    }
                    if (id == null) {
                        logger.error("Failed to create doot {} for user {}", addItemDto.content, user.username);
                        return Response.status(Response.Status.OK).entity(new ErrorDto("Doot not created. Try again later.")).build();
                    } else {
                        return Response.ok(new OkWithIdDto(id)).build();
                    }
                });
    }

    @GET
    @Path("/item/{id}")
    public CompletionStage<Response> getDoot(@PathParam("id") String id) {
        long longID;
        try {
            longID = Long.parseLong(id);
        } catch (NumberFormatException nfe) {
            return CompletableFuture.completedFuture(Response.status(Response.Status.OK).entity(new ErrorDto("Invalid id.")).build());
        }
        return dootService.getDoot(longID)
                .thenApply(doot -> {
                    if (doot == null) {
                        return Response.status(Response.Status.OK).entity(new ErrorDto("Doot not found.")).build();
                    }
                    return Response.ok(new OkWithItemDto(ItemDto.from(doot))).build();
                });
    }

    @DELETE
    @Path("/item/{id}")
    public CompletionStage<Response> deleteDoot(@CookieParam("session") Cookie sessionCookie, @PathParam("id") long id) {
        User user = getUserFromCookie(sessionCookie);
        if (user == null) {
            return CompletableFuture.completedFuture(Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorDto("User is not logged in.")).build());
        }
        return dootService.deleteDoot(id, user)
                .thenApply(s -> {
                    if (s == null) {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    } else if (s) {
                        return Response.ok().build();
                    } else {
                        return Response.status(Response.Status.UNAUTHORIZED).build();
                    }
                });
    }

    @POST
    @Path("/item/{id}/like")
    public CompletionStage<Response> likeDoot(@CookieParam("session") Cookie sessionCookie, @PathParam("id") long id, @RequestBody LikeDootDto likeDootDto) {
        User user = getUserFromCookie(sessionCookie);
        if (user == null) {
            return CompletableFuture.completedFuture(Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorDto("User is not logged in.")).build());
        }
        return dootService.likeDoot(id, user, likeDootDto.like)
                .thenApply(s -> {
                    if (s) {
                        return Response.ok(new OkDto()).build();
                    } else {
                        return Response.ok(new ErrorDto("There was an error liking/unliking the doot.")).build();
                    }
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
