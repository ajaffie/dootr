package dev.ajaffie.dootr.doots.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class QueryDto {
    public final long timestamp;
    public final int limit;
    public final String query;
    public final String username;
    public final boolean following;
    public final String rank;
    public final Long parent;
    public final boolean replies;
    public final boolean hasMedia;

    public QueryDto() {
        this(Instant.now().getEpochSecond(), null, null, null, true, "interest", null, true, false);
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public QueryDto(@JsonProperty("timestamp") Long timestamp,
                    @JsonProperty("limit") Integer limit,
                    @JsonProperty("q") String query,
                    @JsonProperty("username") String username,
                    @JsonProperty("following") Boolean following,
                    @JsonProperty("rank") String rank,
                    @JsonProperty("parent") String parent,
                    @JsonProperty("replies") Boolean replies,
                    @JsonProperty("hasMedia") Boolean hasMedia) {
        this.query = query == null || query.isEmpty() ? null : query;
        this.username = username;
        this.following = following == null ? true : following;
        this.rank = rank == null || rank.isEmpty() ? "interest" : rank;
        this.replies = replies == null ? true : replies;
        if (this.replies) {
            this.parent = parent == null || parent.isEmpty() ? null : Long.parseLong(parent);
        } else {
            this.parent = null;
        }
        this.hasMedia = hasMedia == null ? false : hasMedia;
        if (timestamp == null) {
            this.timestamp = Instant.now().getEpochSecond();
        } else {
            this.timestamp = timestamp;
        }
        if (limit == null || limit < 0) {
            this.limit = 25;
        } else {
            this.limit = Math.min(limit, 100);
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public QueryDto(@JsonProperty("timestamp") Float timestamp,
                    @JsonProperty("limit") Integer limit,
                    @JsonProperty("q") String query,
                    @JsonProperty("username") String username,
                    @JsonProperty("following") Boolean following,
                    @JsonProperty("rank") String rank,
                    @JsonProperty("hasMedia") Boolean hasMedia,
                    @JsonProperty("replies") Boolean replies,
                    @JsonProperty("parent") String parent) {
        this(timestamp == null ? null : timestamp.longValue(), limit, query, username, following, rank, parent, replies, hasMedia);
    }
}
