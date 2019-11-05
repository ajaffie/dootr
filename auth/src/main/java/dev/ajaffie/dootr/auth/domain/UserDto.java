package dev.ajaffie.dootr.auth.domain;

public class UserDto {
    public final long id;
    public final String username;
    public final String email;

    public UserDto(long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
}
