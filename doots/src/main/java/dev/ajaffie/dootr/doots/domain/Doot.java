package dev.ajaffie.dootr.doots.domain;

import io.vertx.axle.sqlclient.Row;
import io.vertx.axle.sqlclient.Tuple;

public class Doot {
    public long id;
    public String username;
    public long userId;
    public Properties property;
    public long retweeted;
    public String content;
    public long timestamp;

    public Doot(long id, String username, long userId, long likes, long retweeted, String content, long timestamp) {
        this.id = id;
        this.username = username;
        this.userId = userId;
        this.property = new Properties(likes);
        this.retweeted = retweeted;
        this.content = content;
        this.timestamp = timestamp;
    }

    private Doot() {

    }

    public static Doot from(Row row) {
        Doot created = new Doot();
        created.id = row.getLong("Id");
        created.username = row.getString("Username");
        created.userId = row.getLong("UserId");
        created.property = new Properties(row.getLong("Likes"));
        created.retweeted = row.getLong("Retweeted");
        created.content = row.getString("Content");
        created.timestamp = row.getLong("Timestamp");
        return created;
    }

    public Tuple toRow() {
        return Tuple.of(username, userId, property.likes, retweeted, content, timestamp);
    }

    static class Properties {
        public long likes;

        public Properties(long likes) {
            this.likes = likes;
        }
    }
}
