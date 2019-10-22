package dev.ajaffie.dootr.auth.controllers;

import dev.ajaffie.dootr.auth.domain.AddUserDto;
import dev.ajaffie.dootr.auth.domain.UserCreds;
import dev.ajaffie.dootr.auth.domain.VerifyUserDto;
import dev.ajaffie.dootr.auth.services.JWTService;
import dev.ajaffie.dootr.auth.services.UserService;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {

    private static Logger logger = LoggerFactory.getLogger(UserController.class);
    private UserService userService;
    private JWTService jwtService;

    @Inject
    public UserController(UserService userService, JWTService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @POST
    @Path("/adduser")
    public CompletionStage<Response> addUser(@RequestBody AddUserDto addUserRequest) {
        logger.info("Add user request received for username {}.", addUserRequest.username);
        return userService.addUser(addUserRequest);
    }

    @POST
    @Path("/verify")
    public CompletionStage<Response> verifyUser(@RequestBody VerifyUserDto verifyRequest) {
        logger.info("Verify user request received for {} with code {}", verifyRequest.email, verifyRequest.key);
        return userService.verifyUser(verifyRequest);
    }

    @POST
    @Path("/resendVerification")
    public CompletionStage<Response> resendVerify(@RequestBody VerifyUserDto verifyRequest) {
        logger.info("Resend verification request received for '{}'", verifyRequest.email);
        return userService.resendVerification(verifyRequest);
    }

    @GET
    @Path("/.well-known/jwks.json")
    public String jwks() {
        return jwtService.getJwks();
    }

    @POST
    @Path("/login")
    public CompletionStage<Response> login(@RequestBody UserCreds creds) {
        return userService.login(creds);
    }

    @POST
    @Path("/logout")
    public CompletionStage<Response> logout() {
        return userService.logout();
    }


}
