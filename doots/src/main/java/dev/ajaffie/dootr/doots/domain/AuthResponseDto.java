package dev.ajaffie.dootr.doots.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponseDto {
    public final long id;
    public final String username;
    public final String email;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public AuthResponseDto(@JsonProperty("id") long id, @JsonProperty("username") String username, @JsonProperty("email") String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
}
