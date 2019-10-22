package dev.ajaffie.dootr.doots.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class OkWithItemDto extends OkDto {
    public final ItemDto item;

    public OkWithItemDto(ItemDto doot) {
        this.item = doot;
    }
}
