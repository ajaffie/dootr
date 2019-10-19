package dev.ajaffie.dootr.auth.services;

import dev.ajaffie.dootr.auth.domain.AddUserDto;
import dev.ajaffie.dootr.auth.domain.VerifyUserDto;

import javax.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;

public interface UserService {

    CompletionStage<Response> addUser(AddUserDto addUserRequest);

    CompletionStage<Response> verifyUser(VerifyUserDto verifyRequest);

    CompletionStage<Response> resendVerification(VerifyUserDto verifyRequest);
}
