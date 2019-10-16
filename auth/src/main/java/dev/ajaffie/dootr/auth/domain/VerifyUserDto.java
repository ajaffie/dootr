package dev.ajaffie.dootr.auth.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class VerifyUserDto {
    public final String email;
    public final String key;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public VerifyUserDto(@JsonProperty("email") String email, @JsonProperty("key") String key) {
        this.email = email;
        this.key = key;
    }
}
