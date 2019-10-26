package dev.ajaffie.dootr.auth.services;

import com.sendgrid.*;
import dev.ajaffie.dootr.auth.domain.Email;
import dev.ajaffie.dootr.auth.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.ConfigurationException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class EmailServiceImpl implements EmailService {
    private static final com.sendgrid.Email FROM = new com.sendgrid.Email("no-reply@dootr.ajaffie.dev", "Dootr");

    private final SendGrid sendgrid;
    private final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);


    @Inject
    public EmailServiceImpl() throws ConfigurationException {
        final String apikey = System.getenv().getOrDefault("SENDGRID_API_KEY", null);
        if (apikey == null) {
            throw new ConfigurationException("Please supply sendgrid API key in environment as 'SENDGRID_API_KEY'.");
        }
        this.sendgrid = new SendGrid(apikey);
    }

    @Override
    @Transactional
    public CompletionStage<Boolean> sendEmail(Email email) {
        com.sendgrid.Email dest = new com.sendgrid.Email(email.dest, email.destName);
        Content content = new Content("text/plain", email.content);
        Mail mail = new Mail(FROM, email.subject, dest, content);

        Request req = new Request();

        try {
            req.setMethod(Method.POST);
            req.setEndpoint("mail/send");
            req.setBody(mail.build());
            Response resp = sendgrid.api(req);
        } catch (IOException ioe) {
            logger.error("There was an error sending an email: {}", ioe.getMessage());
            return CompletableFuture.completedFuture(false);
        }
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletionStage<Boolean> sendVerificationEmail(User user) {
        return sendEmail(Email.verificationEmail(user))
                .handle((success, err) -> err == null && success);
    }
}
