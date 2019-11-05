package dev.ajaffie.dootr.doots.domain;

public class OkWithUserDto extends OkDto {
    public final UserWithFollows user;

    public OkWithUserDto(String email, int followers, int following) {
        this.user = new UserWithFollows(email, followers, following);
    }


    static class UserWithFollows {
        public final String email;
        public final int followers;
        public final int following;

        UserWithFollows(String email, int followers, int following) {
            this.email = email;
            this.followers = followers;
            this.following = following;
        }
    }
}
