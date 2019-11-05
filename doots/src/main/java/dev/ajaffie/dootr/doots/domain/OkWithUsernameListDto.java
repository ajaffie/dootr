package dev.ajaffie.dootr.doots.domain;

import java.util.List;

public class OkWithUsernameListDto extends OkDto {
    public List<String> users;

    public OkWithUsernameListDto(List<String> usernames) {
        this.users = usernames;
    }
}
