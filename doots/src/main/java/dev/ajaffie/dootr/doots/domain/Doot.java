package dev.ajaffie.dootr.doots.domain;

import io.vertx.axle.sqlclient.Row;
import io.vertx.axle.sqlclient.Tuple;

import java.util.List;

public class Doot {
    public long id;
    public String username;
    public long userId;
    public Properties property;
    public Long retweeted;
    public Long parent;
    public String childType;
    public String content;
    public long timestamp;

    public Doot(long id, String username, long userId, Long likes, Long retweeted, String content, long timestamp, Long parent, String childType) {
        this.id = id;
        this.username = username;
        this.userId = userId;
        this.property = new Properties(likes);
        this.retweeted = retweeted;
        this.content = content;
        this.timestamp = timestamp;
        this.parent = parent;
        this.childType = childType;
    }

    private Doot() {

    }

    public static Doot from(Row row) {
        if (row == null) {
            return null;
        }
        Doot created = new Doot();
        created.id = row.getLong("Id");
        created.username = row.getString("Username");
        created.userId = row.getLong("UserId");
        created.property = new Properties(0L);
        created.retweeted = 0L;
        created.parent = row.getLong("Parent");
        created.childType = row.getString("ChildType");
        created.content = row.getString("Content");
        created.timestamp = row.getLong("Timestamp");
        return created;
    }

    public Tuple toRow() {
        return Tuple.of(username, userId, parent, childType, content, timestamp);
    }

    public Doot withLikes(long likes) {
        this.property = new Properties(likes);
        return this;
    }

    public Doot withRetweets(long retweets) {
        this.retweeted = retweets;
        return this;
    }

    static class Properties {
        public Long likes;

        public Properties(Long likes) {
            this.likes = likes;
        }
    }

    static class ChildType {
        public static final String REPLY = "reply";
        public static final String RETWEET = "retweet";
    }
}
