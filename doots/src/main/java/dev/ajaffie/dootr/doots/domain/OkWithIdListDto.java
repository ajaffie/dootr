package dev.ajaffie.dootr.doots.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public class OkWithIdListDto extends OkDto {
    public final List<Long> items;

    public OkWithIdListDto(List<Long> items) {
        this.items = items;
    }
}
