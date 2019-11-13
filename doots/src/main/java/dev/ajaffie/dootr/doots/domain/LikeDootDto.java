package dev.ajaffie.dootr.doots.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LikeDootDto {
    public final boolean like;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public LikeDootDto(@JsonProperty("like") boolean like) {
        this.like = like;
    }
}
