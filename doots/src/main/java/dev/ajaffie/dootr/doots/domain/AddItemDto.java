package dev.ajaffie.dootr.doots.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AddItemDto {
    public final String content;
    public final String childType;
    public final Long parent;
    public final List<Long> media;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public AddItemDto(
            @JsonProperty("content") String content,
            @JsonProperty("childType") String childType,
            @JsonProperty("parent") long parent,
            @JsonProperty("media") List<Long> media
    ) {
        this.content = content;
        this.childType = childType;
        this.media = media;
        this.parent = parent;
    }
}
