package dev.ajaffie.dootr.doots.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class FollowDto {
    public final String username;
    public final boolean follow;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public FollowDto(@JsonProperty("username") String username, @JsonProperty("follow") Boolean follow) {
        this.username = username;
        this.follow = follow == null || follow;
    }
}
