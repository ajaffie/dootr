package dev.ajaffie.dootr.doots.services;

import dev.ajaffie.dootr.doots.domain.*;
import io.vertx.axle.mysqlclient.MySQLPool;
import io.vertx.axle.sqlclient.RowSet;
import io.vertx.axle.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class DootServiceImpl implements DootService {

    private MySQLPool client;
    private Logger logger;
    private SnowflakeGenerator snowflakeGenerator;

    @Inject
    public DootServiceImpl(MySQLPool mySQLPool, SnowflakeGenerator snowflakeGenerator) {
        this.client = mySQLPool;
        logger = LoggerFactory.getLogger(DootServiceImpl.class);
        this.snowflakeGenerator = snowflakeGenerator;
    }

    @Override
    @Transactional
    public CompletionStage<Long> createDoot(AddItemDto addItemDto, User user) {
        long time = Instant.now().getEpochSecond();
        logger.info("Inserting doot at timestamp {} from user {}", time, user.username);
        long newId = snowflakeGenerator.nextId();
        return client.preparedQuery("INSERT INTO Doots (Id, Username, UserId, Content, `Timestamp`) VALUES (?, ?, ?, ?, ?);",
                Tuple.of(newId, user.username, user.userId, addItemDto.content, time)
        )
                .thenApply(rs -> {
                    if (rs.rowCount() == 1) {
                        return true;
                    }
                    throw new DootException("There was an error creating the doot.");
                })
                .handle((success, err) -> {
                    if (err != null) {
                        logger.error("Error creating doot: " + err.getMessage());
                    }
                    logger.info("Doot id {} created for user {}", newId, user.username);
                    return newId;
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
                .thenCompose(this::addLikes)
                .handle((d, err) -> {
                    if (err != null || d == null) {
                        logger.error("There was an error loading the doot with id {}: {}", id, err != null ? err.getMessage() : "The doot does not exist.");
                    }
                    return d;
                });
    }

    @Override
    public CompletionStage<List<Long>> getDootsForUser(String username, int limit) {
        return client.preparedQuery("SELECT Id\n" +
                "FROM Doots\n" +
                "WHERE Username = ?\n" +
                "ORDER BY `Timestamp` DESC\n" +
                "LIMIT ?",
                Tuple.of(username, limit))
                .thenApply(RowSet::iterator)
                .thenApply(it -> {
                    List<Long> dootIds = new ArrayList<>(limit);
                    it.forEachRemaining(row -> dootIds.add(row.getLong("Id")));
                    return dootIds;
                })
                .handle((dootIds, ex) -> {
                    if (ex == null) {
                        return dootIds;
                    }
                    logger.error("Error getting dootIds for {}: {}", username, ex.getMessage());
                    return null;
                });
    }

    @Override
    public CompletionStage<Boolean> deleteDoot(long id, User user) {
        return getDoot(id)
                .thenApply(doot -> {
                    if (doot == null) {
                        return null;
                    }
                    return doot.userId == user.userId;
                })
                .thenCompose(proceed -> {
                    if (proceed == null) throw new DootException("Doot not found.");
                    if (proceed) {
                        return client.preparedQuery("DELETE FROM Doots WHERE Id = ?;", Tuple.of(id))
                                .thenApply(rs -> rs.rowCount() == 1);
                    }
                    return CompletableFuture.completedFuture(false);
                })
                .handle((s, ex) -> {
                    if (ex instanceof DootException) {
                        logger.error("Error deleting doot {}: {}", id, ex.getMessage());
                        return null;
                    }
                    return s;
                });
    }

    @Override
    public CompletionStage<Boolean> likeDoot(long id, User user, boolean like) {
        if (like) {
            return client.preparedQuery("INSERT IGNORE INTO Likes (DootId, UserId, Username) VALUES (?, ?, ?);",
                    Tuple.of(id, user.userId, user.username))
                    .handle((rs, err) -> {
                        if (err != null) {
                            logger.error("Error liking doot: {}", err.getMessage());
                            return false;
                        }
                        return true;
                    });
        } else {
            return client.preparedQuery("DELETE FROM Likes WHERE DootId = ? AND UserId = ? LIMIT 1;",
                    Tuple.of(id, user.userId))
                    .handle((rs, err) -> {
                        if (err != null) {
                            logger.error("Error unliking doot: {}", err.getMessage());
                            return false;
                        }
                        return true;
                    });
        }
    }


    public CompletionStage<Doot> addLikes(Doot doot) {
        return client.preparedQuery("SELECT COUNT(*) FROM Likes WHERE DootId = ?;", Tuple.of(doot.id))
                .thenApply(RowSet::iterator)
                .thenApply(it -> {
                    if (it.hasNext()) {
                        return doot.withLikes(it.next().getLong(0));
                    }
                    return doot.withLikes(0);
                })
                .handle((d, ex) -> {
                    if (ex == null) {
                        return d;
                    }
                    logger.error("There was an error adding likes to doot '{}': {}", doot.id, ex.getMessage());
                    return doot;
                });
    }

    @PostConstruct
    public void setup() {
        // run database migrations
        Migrations.runMigrations(client);
    }
}
