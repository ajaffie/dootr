package dev.ajaffie.dootr.doots.services;

import io.vertx.axle.mysqlclient.MySQLPool;
import io.vertx.axle.sqlclient.RowSet;
import io.vertx.axle.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class FollowServiceImpl implements FollowService {
    private MySQLPool client;
    private Logger logger;

    @Inject
    public FollowServiceImpl(MySQLPool pool) {
        this.client = pool;
        this.logger = LoggerFactory.getLogger(FollowServiceImpl.class);
    }

    @Override
    public CompletionStage<Boolean> follow(String follower, String followed) {
        return client.preparedQuery("INSERT IGNORE INTO Follows\n" +
                        "(FollowerName, FollowedName)\n" +
                        "VALUES (?, ?);",
                Tuple.of(follower, followed))
                .handle((rs, ex) -> {
                    if (ex == null) {
                        return true;
                    }
                    logger.error("Error user {} following user {}: {}", follower, followed, ex.getMessage());
                    return false;
                });
    }

    @Override
    public CompletionStage<Boolean> unFollow(String follower, String followed) {
        return client.preparedQuery("DELETE FROM Follows WHERE FollowerName = ? AND FollowedName = ?;",
                Tuple.of(follower, followed))
                .handle((rs, ex) -> {
                    if (ex == null) {
                        return true;
                    }
                    logger.error("Error user {} unfollowing user {}: {}", follower, followed, ex.getMessage());
                    return false;
                });
    }

    @Override
    public CompletionStage<List<String>> followers(String followed, int limit) {
        return client.preparedQuery("SELECT FollowerName FROM Follows WHERE FollowedName = ? LIMIT ?;",
                Tuple.of(followed, limit))
                .thenApply(RowSet::iterator)
                .thenApply(it -> {
                    List<String> users = new ArrayList<>(50);
                    it.forEachRemaining(row -> {
                        users.add(row.getString("FollowerName"));
                    });
                    return users;
                })
                .handle((users, ex) -> {
                    if (ex == null) {
                        return users;
                    }
                    logger.error("Error getting followers for {}: {}", followed, ex.getMessage());
                    return null;
                });
    }

    @Override
    public CompletionStage<List<String>> following(String follower, int limit) {
        return client.preparedQuery("SELECT FollowedName FROM Follows WHERE FollowerName = ? LIMIT ?;",
                Tuple.of(follower, limit))
                .thenApply(RowSet::iterator)
                .thenApply(it -> {
                    List<String> users = new ArrayList<>(50);
                    it.forEachRemaining(row -> {
                        users.add(row.getString("FollowedName"));
                    });
                    return users;
                })
                .handle((users, ex) -> {
                    if (ex == null) {
                        return users;
                    }
                    logger.error("Error getting users followed by {}: {}", follower, ex.getMessage());
                    return null;
                });
    }

    @Override
    public CompletionStage<Integer> numFollowers(String username) {
        return client.preparedQuery("SELECT COUNT(1) AS followers FROM Follows WHERE FollowedName = ?", Tuple.of(username))
                .thenApply(RowSet::iterator)
                .thenApply(Iterator::next)
                .thenApply(r -> r.getInteger("followers"))
                .handle((n, ex) -> {
                    if (ex == null) {
                        return n;
                    }
                    logger.error("Error getting number of followers of {}: {}", username, ex.getMessage());
                    return 0;
                });
    }

    @Override
    public CompletionStage<Integer> numFollowing(String username) {
        return client.preparedQuery("SELECT COUNT(1) AS following FROM Follows WHERE FollowerName = ?", Tuple.of(username))
                .thenApply(RowSet::iterator)
                .thenApply(Iterator::next)
                .thenApply(r -> r.getInteger("following"))
                .handle((n, ex) -> {
                    if (ex == null) {
                        return n;
                    }
                    logger.error("Error getting number of users followed by {}: {}", username, ex.getMessage());
                    return 0;
                });
    }


}
