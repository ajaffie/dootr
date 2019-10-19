package dev.ajaffie.dootr.auth.services;

import dev.ajaffie.dootr.auth.domain.Email;
import dev.ajaffie.dootr.auth.domain.User;

import java.util.concurrent.CompletionStage;

public interface EmailService {

    CompletionStage<Void> sendEmail(Email email);

    CompletionStage<Boolean> sendVerificationEmail(User user);
}
