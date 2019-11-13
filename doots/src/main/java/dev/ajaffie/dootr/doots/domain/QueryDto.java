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

    public QueryDto() {
        this(Instant.now().getEpochSecond(), null, null, null, true);
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public QueryDto(@JsonProperty("timestamp") Long timestamp,
                    @JsonProperty("limit") Integer limit,
                    @JsonProperty("q") String query,
                    @JsonProperty("username") String username,
                    @JsonProperty("following") Boolean following) {
        this.query = query == null || query.isEmpty() ? null : query;
        this.username = username;
        this.following = following == null ? true : following;
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
                    @JsonProperty("following") Boolean following) {
        this(timestamp == null ? null : timestamp.longValue(), limit, query, username, following);
    }
}
