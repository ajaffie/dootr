package dev.ajaffie.dootr.auth.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.sendgrid.*;
import dev.ajaffie.dootr.auth.domain.Email;
import dev.ajaffie.dootr.auth.domain.User;
import dev.ajaffie.dootr.auth.domain.UserException;
import io.smallrye.reactive.messaging.annotations.Merge;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class EmailServiceImpl implements EmailService {
    private static final com.sendgrid.Email FROM = new com.sendgrid.Email("no-reply@dootr.ajaffie.dev", "Dootr");

    private final SendGrid sendgrid;
    private final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private static ObjectReader userReader = new ObjectMapper().readerFor(User.class);


    @Inject
    public EmailServiceImpl() throws ConfigurationException {
        final String apikey = System.getenv().getOrDefault("SENDGRID_API_KEY", null);
        if (apikey == null) {
            throw new ConfigurationException("Please supply sendgrid API key in environment as 'SENDGRID_API_KEY'.");
        }
        this.sendgrid = new SendGrid(apikey);
    }

    @Override
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

    @Incoming("sendverification")
    @Merge(value = Merge.Mode.MERGE)
    @Acknowledgment(value = Acknowledgment.Strategy.POST_PROCESSING)
    public CompletionStage<Boolean> sendVerificationEmail(String serializedUser) {
        User user;
        try {
            user = userReader.readValue(serializedUser);
        } catch (IOException e) {
            logger.error("couldn't read user: {}", e.getMessage());
            throw new UserException("couldn't read user");
        }
        return sendEmail(Email.verificationEmail(user))
                .handle((success, err) -> err == null && success);
    }
}
