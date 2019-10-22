package dev.ajaffie.dootr.doots.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class QueryDto {
    public final long timestamp;
    public final int limit;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public QueryDto(@JsonProperty("timestamp") Long timestamp, @JsonProperty("limit") Integer limit) {
        if (timestamp == null || timestamp > Instant.now().getEpochSecond()) {
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
}
