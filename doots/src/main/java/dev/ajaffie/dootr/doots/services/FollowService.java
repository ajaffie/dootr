package dev.ajaffie.dootr.doots.services;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface FollowService {
    CompletionStage<Boolean> follow(String follower, String followed);

    CompletionStage<Boolean> unFollow(String follower, String followed);

    CompletionStage<List<String>> followers(String followed, int limit);

    CompletionStage<List<String>> following(String follower, int limit);

    CompletionStage<Integer> numFollowers(String username);

    CompletionStage<Integer> numFollowing(String username);
}
