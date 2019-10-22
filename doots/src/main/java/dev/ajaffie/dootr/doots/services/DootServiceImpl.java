package dev.ajaffie.dootr.doots.services;

import dev.ajaffie.dootr.doots.domain.AddItemDto;
import dev.ajaffie.dootr.doots.domain.Doot;
import dev.ajaffie.dootr.doots.domain.DootException;
import dev.ajaffie.dootr.doots.domain.User;
import io.vertx.axle.mysqlclient.MySQLPool;
import io.vertx.axle.sqlclient.RowIterator;
import io.vertx.axle.sqlclient.RowSet;
import io.vertx.axle.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class DootServiceImpl implements DootService {

    private MySQLPool client;
    private Logger logger;

    @Inject
    public DootServiceImpl(MySQLPool mySQLPool) {
        this.client = mySQLPool;
        logger = LoggerFactory.getLogger(DootServiceImpl.class);
    }

    @Override
    @Transactional
    public CompletionStage<Long> createDoot(AddItemDto addItemDto, User user) {


        return client.preparedQuery("INSERT INTO Doots (Username, UserId, Content, `Timestamp`) VALUES (?, ?, ?, ?);",
                Tuple.of(user.username, user.userId, addItemDto.content, Instant.now().getEpochSecond())
        )
                .thenApply(rs -> {
                    if (rs.rowCount() == 1) {
                        return true;
                    }
                    throw new DootException("There was an error creating the doot.");
                })
                .thenCompose(s -> client.query("SELECT LAST_INSERT_ID();"))
                .thenApply(RowSet::iterator)
                .thenApply(it -> {
                    if (it.hasNext()) {
                        return it.next();
                    }
                    throw new DootException("There was an error creating the doot.");
                })
                .thenApply(r -> r.getLong(0))
                .handle((id, err) -> {
                    if (err != null) {
                        logger.error("Error creating doot: " + err.getMessage());
                    }
                    return id;
                });

    }

    @Override
    public CompletionStage<Doot> getDoot(long id) {
        return client.preparedQuery("SELECT * FROM Doots WHERE Id = ?;", Tuple.of(id))
                .thenApply(RowSet::iterator)
                .thenApply(it -> {
                    if (it.hasNext()) {
                        return Doot.from(it.next());
                    }
                    return null;
                })
                .handle((d, err) -> {
                    if (err != null || d == null) {
                        logger.error("There was an error loading the doot with id {}: {}", id, err != null ? err.getMessage() : "The doot does not exist.");
                    }
                    return d;
                });
    }

    @PostConstruct
    public void setup() {
        // run database migrations
        client.query(
                "CREATE TABLE IF NOT EXISTS Doots (\n" +
                        "Id BIGINT(19) UNSIGNED PRIMARY KEY AUTO_INCREMENT,\n" +
                        "Username MEDIUMTEXT NOT NULL,\n" +
                        "UserId INT NOT NULL,\n" +
                        "Likes BIGINT(19) UNSIGNED NOT NULL DEFAULT 0,\n" +
                        "Retweeted BIGINT(19) UNSIGNED NOT NULL DEFAULT 0,\n" +
                        "Content TEXT(280) NOT NULL,\n" +
                        "`Timestamp` BIGINT(19) UNSIGNED NOT NULL" +
                        ");"
        ).toCompletableFuture().join();
    }
}
