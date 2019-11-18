package dev.ajaffie.dootr.doots.controllers;

import dev.ajaffie.dootr.doots.domain.ErrorDto;
import dev.ajaffie.dootr.doots.domain.MediaUploadForm;
import dev.ajaffie.dootr.doots.domain.OkWithIdDto;
import dev.ajaffie.dootr.doots.domain.User;
import dev.ajaffie.dootr.doots.services.MediaService;
import dev.ajaffie.dootr.doots.services.SnowflakeGenerator;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Path("/")
@ApplicationScoped
public class MediaController {

    private MediaService mediaService;
    private SnowflakeGenerator snowflakeGenerator;
    private Logger logger;

    private JWTAuthContextInfo jwtAuthContextInfo;
    private JWTCallerPrincipalFactory jwtFactory;

    @Inject
    public MediaController(MediaService mediaService, JWTAuthContextInfo jwtAuthContextInfo, SnowflakeGenerator snowflakeGenerator) {
        this.mediaService = mediaService;
        this.jwtAuthContextInfo = jwtAuthContextInfo;
        this.jwtFactory = JWTCallerPrincipalFactory.instance();
        this.logger = LoggerFactory.getLogger(MediaController.class);
        this.snowflakeGenerator = snowflakeGenerator;
    }

    @POST
    @Path("/addmedia")
    @Consumes("multipart/form-data")
    @Produces("application/json")
    public CompletionStage<Response> addMedia(@CookieParam("session") Cookie sessionCookie, @MultipartForm MediaUploadForm uploadForm) {
        User user = getUserFromCookie(sessionCookie);
        if (user == null) {
            return CompletableFuture.completedFuture(Response.status(Response.Status.OK).entity(new ErrorDto("User is not logged in.")).build());
        }
        if (uploadForm == null || uploadForm.buffer == null) {
            return CompletableFuture.completedFuture(Response.ok(new ErrorDto("Content not correctly uploaded.")).build());
        }
        return mediaService.putMedia(user, snowflakeGenerator.nextId(), uploadForm.mime, uploadForm.buffer)
                .thenApply(id -> {
                    if (id == null) {
                        return Response.ok(new ErrorDto("Media not successfully uploaded.")).build();
                    }
                    return Response.ok(new OkWithIdDto(id)).build();
                });
    }

    @GET
    @Path("/media/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public CompletionStage<Response> getMedia(@PathParam("id") String id) {
        long longID;
        try {
            longID = Long.parseLong(id);
        } catch (NumberFormatException nfe) {
            return CompletableFuture.completedFuture(Response.status(Response.Status.OK).entity(new ErrorDto("Invalid id.")).build());
        }
        return mediaService.getMedia(longID)
                .thenApply(media -> {
                    if (media == null) {
                        return Response.status(Response.Status.OK).entity(new ErrorDto("Media not found.")).build();
                    }
                    return Response.ok(media.buffer.array()).type(media.mime).build();
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
