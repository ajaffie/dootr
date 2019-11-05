package dev.ajaffie.dootr.doots.domain;

import org.eclipse.microprofile.jwt.JsonWebToken;

public class User {
    public final String username;
    public final long userId;

    public User(String username, long userId) {
        this.username = username;
        this.userId = userId;
    }

    public static User fromJwt(JsonWebToken jwt) {
        return new User(
                jwt.getClaim("upn"),
                Long.parseLong(jwt.getSubject())
        );
    }
}
