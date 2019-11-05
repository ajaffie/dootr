package dev.ajaffie.dootr.doots.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class HasLimitDto {
    public final int limit;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public HasLimitDto(@JsonProperty("limit") Integer limit) {
        this.limit = limit == null ? 50 : Math.min(limit, 200);
    }
}
