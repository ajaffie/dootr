package dev.ajaffie.dootr.doots.controllers;

import dev.ajaffie.dootr.doots.domain.*;
import dev.ajaffie.dootr.doots.services.AuthService;
import dev.ajaffie.dootr.doots.services.DootService;
import dev.ajaffie.dootr.doots.services.FollowService;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RestClient;
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
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
public class UserController {
    private FollowService followService;
    private DootService dootService;
    private JWTAuthContextInfo jwtAuthContextInfo;
    private JWTCallerPrincipalFactory jwtFactory;
    private AuthService authService;
    private Logger logger;

    @Inject
    public UserController(FollowService followService, JWTAuthContextInfo jwtAuthContextInfo, DootService dootService, @RestClient AuthService authService) {
        this.followService = followService;
        this.jwtAuthContextInfo = jwtAuthContextInfo;
        this.jwtFactory = JWTCallerPrincipalFactory.instance();
        this.dootService = dootService;
        this.authService = authService;
        this.logger = LoggerFactory.getLogger(UserController.class);
    }

    @POST
    @Path("/follow")
    public CompletionStage<Response> follow(@CookieParam("session") Cookie sessionCookie, @RequestBody FollowDto followDto) {
        User user = getUserFromCookie(sessionCookie);
        if (user == null) {
            return CompletableFuture.completedFuture(Response.ok(new ErrorDto("Please log in.")).build());
        }
        if (followDto.username == null || followDto.username.isEmpty()) {
            return CompletableFuture.completedFuture(Response.ok(new ErrorDto("Supply a username.")).build());
        }
        if (followDto.follow) {
            logger.info("User {} following {}", user.username, followDto.username);
            return authService.getUser(followDto.username)
                    .handle((uDto, ex) -> {
                        if (ex == null) {
                            return true;
                        }
                        logger.error("User {} not found", followDto.username);
                        return false;
                    })
                    .thenCompose(exists -> exists ? followService.follow(user.username, followDto.username) : CompletableFuture.completedFuture(false))
                    .thenApply(success -> {
                        if (success) {
                            return Response.ok(new OkDto()).build();
                        } else {
                            return Response.ok(new ErrorDto("Error following user " + followDto.username)).build();
                        }
                    });
        } else {
            logger.info("User {} unfollowing {}", user.username, followDto.username);
            return followService.unFollow(user.username, followDto.username)
                    .thenApply(success -> {
                        if (success) {
                            return Response.ok(new OkDto()).build();
                        } else {
                            return Response.ok(new ErrorDto("Error unfollowing user " + followDto.username)).build();
                        }
                    });
        }
    }

    @GET
    @Path("/user/{username}/following")
    public CompletionStage<Response> getFollowing(@PathParam("username") String username, @QueryParam("limit") Integer limit) {
        limit = fixLimit(limit);
        if (username == null || username.isEmpty()) {
            return CompletableFuture.completedFuture(Response.ok(new ErrorDto("Supply a username.")).build());
        }
        Integer finalLimit = limit;
        return authService.getUser(username).handle((u, ex) -> {
            if (ex == null) {
                return u;
            }
            logger.error("Error getting user {}: {}", username, ex.getMessage());
            return null;
        })
                .thenCompose(uDto -> uDto != null ? followService.following(username, finalLimit) : null)
                .thenApply(usernames -> {
                    if (usernames != null) {
                        return Response.ok(new OkWithUsernameListDto(usernames)).build();
                    } else {
                        return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorDto("Error getting users followed by " + username)).build();
                    }
                });
    }

    @GET
    @Path("/user/{username}/followers")
    public CompletionStage<Response> getFollowers(@PathParam("username") String username, @QueryParam("limit") Integer limit) {
        limit = fixLimit(limit);
        if (username == null || username.isEmpty()) {
            return CompletableFuture.completedFuture(Response.status(Response.Status.BAD_REQUEST).entity(new ErrorDto("Supply a username.")).build());
        }
        Integer finalLimit = limit;
        return authService.getUser(username).handle((u, ex) -> {
            if (ex == null) {
                return u;
            }
            logger.error("Error getting user {}: {}", username, ex.getMessage());
            return null;
        })
                .thenCompose(uDto -> uDto != null ? followService.followers(username, finalLimit) : null)
                .thenApply(usernames -> {
                    if (usernames != null) {
                        return Response.ok(new OkWithUsernameListDto(usernames)).build();
                    } else {
                        return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorDto("Error getting followers of " + username)).build();
                    }
                });
    }

    @GET
    @Path("/user/{username}/posts")
    public CompletionStage<Response> getPosts(@PathParam("username") String username, @QueryParam("limit") Integer limit) {
        limit = fixLimit(limit);
        if (username == null || username.isEmpty()) {
            return CompletableFuture.completedFuture(Response.status(Response.Status.BAD_REQUEST).entity(new ErrorDto("Supply a username.")).build());
        }
        Integer finalLimit = limit;
        return authService.getUser(username).handle((u, ex) -> {
            if (ex == null) {
                return u;
            }
            logger.error("Error getting user {}: {}", username, ex.getMessage());
            return null;
        })
                .thenCompose(uDto -> uDto != null ? dootService.getDootsForUser(username, finalLimit) : null)
                .thenApply(dootIds -> {
                    if (dootIds == null) {
                        return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorDto("Error getting doots for " + username)).build();
                    }
                    return Response.ok(new OkWithIdListDto(dootIds)).build();
                });

    }

    @GET
    @Path("/user/{username}")
    public CompletionStage<Response> getUser(@PathParam("username") String username) {
        if (username == null || username.isEmpty()) {
            return CompletableFuture.completedFuture(Response.ok(new ErrorDto("Supply a username.")).build());
        }
        CompletableFuture<AuthResponseDto> userFuture = authService.getUser(username).handle((u, ex) -> {
            if (ex == null) {
                return u;
            }
            logger.error("Error getting user {}: {}", username, ex.getMessage());
            return null;
        }).toCompletableFuture();
        CompletableFuture<Integer> followersFuture = followService.numFollowers(username).toCompletableFuture();
        CompletableFuture<Integer> followingFuture = followService.numFollowing(username).toCompletableFuture();
        return CompletableFuture.allOf(userFuture, followersFuture, followingFuture)
                .thenApply(v -> {
                    AuthResponseDto uDto = userFuture.join();
                    if (uDto == null) {
                        return Response.status(Response.Status.NOT_FOUND).entity(new ErrorDto("User not found.")).build();
                    }
                    logger.info("got user dto {} with username {} email {}", uDto, uDto.username, uDto.email);
                    return Response.ok(new OkWithUserDto(uDto.email, followersFuture.join(), followingFuture.join())).build();
                });
    }

    private static int fixLimit(Integer limit) {
        if (limit == null || limit < 1) {
            limit = 50;
        } else if (limit > 200) {
            limit = 200;
        }
        return limit;
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
