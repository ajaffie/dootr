package dev.ajaffie.dootr.auth.controllers;

import dev.ajaffie.dootr.auth.domain.AddUserDto;
import dev.ajaffie.dootr.auth.domain.BasicResponse;
import dev.ajaffie.dootr.auth.services.UserService;
import io.vertx.axle.core.Vertx;
import io.vertx.core.json.Json;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {

    private UserService userService;

    @Inject
    public UserController(UserService userService) {
        this.userService = userService;
    }

    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @POST
    @Path("/adduser")
    public CompletionStage<Response> addUser(@RequestBody AddUserDto addUserRequest) {
        logger.info("Add user request received for username {}.", addUserRequest.username);
        logger.debug("Full request: {}", Json.encodePrettily(addUserRequest));
        return userService.addUser(addUserRequest);
    }


}
