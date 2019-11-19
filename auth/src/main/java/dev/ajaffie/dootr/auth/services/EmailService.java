package dev.ajaffie.dootr.auth.services;

import dev.ajaffie.dootr.auth.domain.Email;
import dev.ajaffie.dootr.auth.domain.User;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

public interface EmailService {

    CompletionStage<Boolean> sendEmail(Email email) throws IOException;

}
