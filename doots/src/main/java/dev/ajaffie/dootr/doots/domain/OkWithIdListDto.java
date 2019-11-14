package dev.ajaffie.dootr.doots.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;
import java.util.stream.Collectors;

@RegisterForReflection
public class OkWithIdListDto extends OkDto {
    public final List<String> items;

    public OkWithIdListDto(List<Long> items) {
        this.items = items.stream().map(String::valueOf).collect(Collectors.toList());
    }
}
