package dev.ajaffie.dootr.doots.services;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.google.common.collect.ImmutableMap;
import dev.ajaffie.dootr.doots.domain.Media;
import dev.ajaffie.dootr.doots.domain.Migrations;
import dev.ajaffie.dootr.doots.domain.User;
import io.vertx.axle.mysqlclient.MySQLPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class MediaServiceImpl implements MediaService {

    private Logger logger;
    private CqlSession cassandra;

    @Inject
    public MediaServiceImpl() {
        cassandra = CassandraClusterFactory.getSession();
        logger = LoggerFactory.getLogger(MediaServiceImpl.class);
    }

    @Override
    public CompletionStage<Long> putMedia(User owner, Long id, String mime, ByteBuffer buffer) {
        return cassandra.executeAsync(SimpleStatement.newInstance("INSERT INTO Media.Media (Id, UserId, Mime, Content) VALUES (:Id, :UserId, :Mime, :Content);",
                ImmutableMap.of(
                        "Id", id,
                        "UserId", owner.userId,
                        "Mime", mime,
                        "Content", buffer
                )))
                .handle((asr, err) -> {
                    if (err != null) {
                        logger.error("Error uploading media: {}", err.getMessage());
                        return null;
                    }
                    return id;
                });
    }

    @Override
    public CompletionStage<Boolean> mediaBelongsToUser(Long mediaId, User user) {
        return cassandra.executeAsync(SimpleStatement.newInstance("SELECT Id, UserId FROM Media.Media WHERE Id = ? LIMIT 1;", mediaId))
                .thenApply(AsyncResultSet::one)
                .thenApply(r -> r != null && r.getLong("UserId") == user.userId)
                .handle((b, err) -> {
                    if (err != null) {
                        logger.error("Error checking ownership of media: {}", err.getMessage());
                        return null;
                    }
                    return b;
                });
    }

    @Override
    public CompletionStage<Media> getMedia(Long id) {
        return cassandra.executeAsync(SimpleStatement.newInstance("SELECT Mime, Content FROM Media.Media WHERE Id = ?", id))
                .thenApply(AsyncResultSet::one)
                .thenApply(row -> row != null ? new Media(row.getByteBuffer("Content"), row.getString("Mime")) : null)
                .handle((m, err) -> {
                    if (err != null) {
                        logger.error("Error getting media: {}", err.getMessage());
                        return null;
                    }
                    return m;
                });
    }

    @PostConstruct
    @Inject
    public void setup(MySQLPool client) {
        // run database migrations
        Migrations.runMigrations(client, CassandraClusterFactory.getSession());
    }
}
