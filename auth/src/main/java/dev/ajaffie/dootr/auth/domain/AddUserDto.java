package dev.ajaffie.dootr.auth.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class AddUserDto {
    public final String username;
    public final String email;
    public final String password;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public AddUserDto(
            @JsonProperty("username") String username,
            @JsonProperty("email") String email,
            @JsonProperty("password") String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
