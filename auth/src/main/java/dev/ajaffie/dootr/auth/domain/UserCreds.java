package dev.ajaffie.dootr.auth.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class UserCreds {
    public final String username;
    public final String password;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public UserCreds(@JsonProperty("username") String username, @JsonProperty("password") String password) {
        this.username = username;
        this.password = password;
    }
}
