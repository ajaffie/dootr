package dev.ajaffie.dootr.doots.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class OkWithIdDto extends OkDto {
    public final String id;

    public OkWithIdDto(long id) {
        this.id = String.valueOf(id);
    }
}
