package dev.ajaffie.dootr.doots.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.axle.sqlclient.Row;

import java.util.ArrayList;
import java.util.List;

@RegisterForReflection
public class ItemDto {
    public final String id;
    public final String username;
    public final PropertiesDto property;
    public final long retweeted;
    public final String content;
    public final long timestamp;
    public final String childType;
    public final Long parent;
    public final List<Long> media;

    private ItemDto(long id, String username, PropertiesDto property, long retweeted, String content, long timestamp, String childType, Long parent, List<Long> media) {
        this.id = String.valueOf(id);
        this.username = username;
        this.property = property;
        this.retweeted = retweeted;
        this.content = content;
        this.timestamp = timestamp;
        this.childType = childType;
        this.parent = parent;
        this.media = media == null ? new ArrayList<>() : media;
    }

    public static ItemDto from(Doot doot) {
        return new ItemDto(doot.id, doot.username, new PropertiesDto(doot.property.likes),
                doot.retweeted, doot.content, doot.timestamp, null, null, null);
    }

    public static ItemDto from(Row row) {
        return new ItemDto(
                row.getLong("id"),
                row.getString("username"),
                new PropertiesDto(row.getLong("likes")),
                row.getLong("retweets"),
                row.getString("content"),
                row.getLong("timestamp"),
                null,
                null,
                null
        );
    }

    static class PropertiesDto {
        public final long likes;

        public PropertiesDto(long likes) {
            this.likes = likes;
        }
    }
}
