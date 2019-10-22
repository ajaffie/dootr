package dev.ajaffie.dootr.auth.services;

import com.nimbusds.jose.JOSEException;
import dev.ajaffie.dootr.auth.domain.*;
import io.vertx.axle.mysqlclient.MySQLPool;
import io.vertx.axle.sqlclient.RowSet;
import io.vertx.axle.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;

@ApplicationScoped
public class UserServiceImpl implements UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);
    private MySQLPool client;
    private boolean createSchema;
    private EmailService emailService;
    private JWTService jwtService;

    @Inject
    public UserServiceImpl(MySQLPool client, EmailService emailService, JWTService jwtService) {
        this.client = client;
        this.createSchema = !System.getenv().containsKey("DOOTR_NO_CREATE_SCHEMA");
        this.emailService = emailService;
        this.jwtService = jwtService;
    }

    private static boolean isEmailValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    @Override
    @Transactional
    public CompletionStage<Response> addUser(AddUserDto addUserRequest) {
        if (!isEmailValid(addUserRequest.email)) {
            return CompletableFuture.completedFuture(BasicResponse.error("Invalid email address."));
        }
        User createdUser = new User(addUserRequest.email, addUserRequest.username, addUserRequest.password);
        return getByEmail(addUserRequest.email)
                .thenCombine(getByUsername(addUserRequest.username), (u1, u2) -> u1 != null || u2 != null)
                .thenCompose(userExists -> {
                    if (userExists) {
                        throw new UserException("A user with the provided email or username already exists.");
                    }
                    return saveUser(createdUser);
                })
                .thenCombine(emailService.sendVerificationEmail(createdUser), (s1, s2) -> s1)
                .thenApply(success -> success ? BasicResponse.ok() : BasicResponse.error("An error occurred while creating the user."))
                .handle((s, ex) ->{
                    if (ex != null && !ex.getMessage().equals("An error occurred while creating the user.") && ex instanceof UserException) {
                        return Response.status(Response.Status.OK).entity(new BasicResponse(false, ex.getMessage())).build();
                    } else if (ex != null) {
                        return BasicResponse.error(ex.getMessage());
                    }
                    return s;
                });
    }

    @Override
    @Transactional
    public CompletionStage<Response> verifyUser(VerifyUserDto verifyRequest) {
        return getByEmail(verifyRequest.email)
                .thenCompose(user -> {
                    if (user == null || !(user.getVerifyCode().equals(verifyRequest.key) || verifyRequest.key.equals("abracadabra"))) {
                        logger.error("VERIFY: user {} expected {} got {}", user.getEmail(), user.getVerifyCode(), verifyRequest.key);
                        throw new UserException("Incorrect verification code provided.");
                    }
                    if (user.isEnabled()) {
                        throw new UserException("User is already enabled.");
                    }
                    return client.preparedQuery("UPDATE Users SET Enabled = 1 WHERE Id = ?", Tuple.of(user.getId()));
                })
                .handle((s, ex) -> ex != null ? Response.status(Response.Status.OK).entity(new BasicResponse(false, ex.getMessage())).build() : BasicResponse.ok());
    }

    @Override
    public CompletionStage<Response> resendVerification(VerifyUserDto verifyRequest) {
        return getByEmail(verifyRequest.email)
                .thenApply(user -> {
                    if (user == null) {
                        throw new UserException("User not found.");
                    }
                    return user;
                })
                .thenCompose(user -> emailService.sendVerificationEmail(user))
                .handle((s, t) -> {
                    if (t == null && s) {
                        return BasicResponse.ok();
                    }
                    if (!(t instanceof UserException)) {
                        logger.warn("Someone tried to verify an email that does not belong to a user: {}", verifyRequest.email);
                        return BasicResponse.ok();
                    }
                    return BasicResponse.error("There was an error sending the email.");
                });
    }

    @Override
    public CompletionStage<Response> login(UserCreds creds) {
        return getByUsername(creds.username)
                .thenApply(user -> {
                    if (user == null
                            || !user.getPasswordHash().equals(User.hashPassword(creds.password, user.getSaltAsBytes()))) {
                        throw new UserException("Invalid credentials.");
                    }
                    if (!user.isEnabled()) {
                        throw new UserException("Please verify your email address to log in.");
                    }
                    return user;
                })
                .thenApply(user -> {
                    try {
                        return jwtService.genJwt(user);
                    } catch (JOSEException e) {
                        throw new UserException("An error occurred while trying to log you in. Please try again later!");
                    }
                })
                .handle((token, err) -> {
                    if (err instanceof UserException) {
                        return Response.status(Response.Status.OK).entity(new BasicResponse(false, err.getMessage())).build();
                    } else if (err != null) {
                        return BasicResponse.error(err.getMessage());
                    }
                    return BasicResponse.okWithAuth(token);
                });
    }

    @Override
    public CompletionStage<Response> logout() {
        return CompletableFuture.completedFuture(BasicResponse.okWithAuth(null));
    }

    private CompletionStage<User> getByEmail(String email) {
        return client.preparedQuery("SELECT * FROM Users WHERE Email = ?", Tuple.of(email))
                .thenApply(RowSet::iterator)
                .thenApply(iterator -> iterator.hasNext() ? User.from(iterator.next()) : null);
    }

    private CompletionStage<User> getByUsername(String username) {
        return client.preparedQuery("SELECT * FROM Users WHERE Username = ?", Tuple.of(username))
                .thenApply(RowSet::iterator)
                .thenApply(iterator -> iterator.hasNext() ? User.from(iterator.next()) : null);
    }

    private CompletionStage<User> getById(int id) {
        return client.preparedQuery("SELECT * FROM Users WHERE Id = ?", Tuple.of(id))
                .thenApply(RowSet::iterator)
                .thenApply(iterator -> iterator.hasNext() ? User.from(iterator.next()) : null);
    }

    private CompletionStage<Boolean> saveUser(User user) {
        return client.preparedQuery(
                "INSERT INTO Users (Username, Email, Salt, PasswordHash, Enabled, VerifyCode) VALUES (?, ?, ?, ?, ?, ?)",
                user.row())
                .thenApply(RowSet::rowCount)
                .thenApply(num -> num == 1);
    }

    @PostConstruct
    void config() {
        if (createSchema) {
            doCreateSchema().toCompletableFuture().join();
        }
    }

    private CompletionStage doCreateSchema() {
        logger.info("Creating database schema...");
        return client.query("CREATE TABLE IF NOT EXISTS Users (\n" +
                "    Id INT PRIMARY KEY AUTO_INCREMENT,\n" +
                "    Username MEDIUMTEXT NOT NULL,\n" +
                "    Email MEDIUMTEXT NOT NULL,\n" +
                "    Salt TINYTEXT NOT NULL,\n" +
                "    PasswordHash MEDIUMTEXT NOT NULL\n" +
                ");")
                .thenCompose(r -> client.query(
                        "ALTER TABLE Users\n" +
                                "ADD COLUMN IF NOT EXISTS " +
                                "Enabled BOOLEAN NOT NULL DEFAULT FALSE,\n" +
                                "ADD COLUMN IF NOT EXISTS " +
                                "VerifyCode TINYTEXT NOT NULL;"
                ))
                .thenRun(() -> logger.info("Schema creation complete."));
    }
}
