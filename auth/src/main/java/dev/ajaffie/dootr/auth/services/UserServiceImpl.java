package dev.ajaffie.dootr.auth.services;

import dev.ajaffie.dootr.auth.domain.AddUserDto;
import dev.ajaffie.dootr.auth.domain.BasicResponse;
import dev.ajaffie.dootr.auth.domain.User;
import io.vertx.axle.mysqlclient.MySQLPool;
import io.vertx.axle.sqlclient.RowSet;
import io.vertx.axle.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;

@ApplicationScoped
public class UserServiceImpl implements UserService {

    private final MySQLPool client;
    private final boolean createSchema;

    @Inject
    public UserServiceImpl(MySQLPool client){
        this.client = client;
        this.createSchema = System.getenv().containsKey("DOOTR_CREATE_SCHEMA");
    }

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Override
    public CompletionStage<Response> addUser(AddUserDto addUserRequest) {
        CompletableFuture<Response> future = new CompletableFuture<>();
        if (!isEmailValid(addUserRequest.email)) {
            future.complete(BasicResponse.error("Invalid email address."));
        }
        if (getByEmail(addUserRequest.email).toCompletableFuture().join() != null
        || getByUsername(addUserRequest.username).toCompletableFuture().join() != null) {
            future.complete(BasicResponse.error("A user with the provided email or username already exists."));
        }
        User newUser = new User(addUserRequest.email, addUserRequest.username, addUserRequest.password);
        saveUser(newUser)
                .thenAccept(result -> future.complete(result ? BasicResponse.ok() : BasicResponse.error("An error occurred while saving the user.")));

        return future;
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
                "INSERT INTO Users (Username, Email, Salt, PasswordHash) VALUES (?, ?, ?, ?)",
                    user.row())
                .thenApply(RowSet::rowCount)
                .thenApply(num -> num == 1);
    }


    @PostConstruct
    void config() {
        if (createSchema) {
            doCreateSchema();
        }
    }

    private void doCreateSchema() {
        logger.info("Creating database schema...");
        client.query("CREATE TABLE IF NOT EXISTS Users (\n" +
                "    Id INT PRIMARY KEY AUTO_INCREMENT,\n" +
                "    Username MEDIUMTEXT NOT NULL,\n" +
                "    Email MEDIUMTEXT NOT NULL,\n" +
                "    Salt TINYTEXT NOT NULL,\n" +
                "    PasswordHash MEDIUMTEXT NOT NULL\n" +
                ");")
                .toCompletableFuture()
                .join();

        logger.info("Schema creation complete.");
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
}
