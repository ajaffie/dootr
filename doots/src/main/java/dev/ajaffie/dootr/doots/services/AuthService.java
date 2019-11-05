package dev.ajaffie.dootr.doots.services;

import dev.ajaffie.dootr.doots.domain.AuthResponseDto;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.CompletionStage;

@Path("/")
@RegisterRestClient
public interface AuthService {

    @GET
    @Path("/getuser")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<AuthResponseDto> getUser(@QueryParam("username") String username);
}
