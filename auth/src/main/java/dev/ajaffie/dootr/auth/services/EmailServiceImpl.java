package dev.ajaffie.dootr.auth.services;

import dev.ajaffie.dootr.auth.domain.Email;
import dev.ajaffie.dootr.auth.domain.User;
import dev.ajaffie.dootr.auth.domain.UserException;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.ReactiveMailer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class EmailServiceImpl implements EmailService {

    private final ReactiveMailer mailer;

    @Inject
    public EmailServiceImpl(ReactiveMailer reactiveMailer){
        this.mailer = reactiveMailer;
    }

    @Override
    @Transactional
    public CompletionStage<Void> sendEmail(Email email) {
        return mailer.send(Mail.withText(email.dest, email.subject, email.content));
    }

    @Override
    public CompletionStage<Boolean> sendVerificationEmail(User user) {
        return sendEmail(Email.verificationEmail(user))
                .handle((v, t) -> t == null);
    }
}
