package dev.ajaffie.dootr.doots.services;

import dev.ajaffie.dootr.doots.domain.AddItemDto;
import dev.ajaffie.dootr.doots.domain.Doot;
import dev.ajaffie.dootr.doots.domain.User;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface DootService {
    CompletionStage<Long> createDoot(AddItemDto addItemDto, User user);

    CompletionStage<Doot> getDoot(long id);

    CompletionStage<List<Long>> getDootsForUser(String username, int limit);

    CompletionStage<Boolean> deleteDoot(long id, User user);

    CompletionStage<Boolean> likeDoot(long id, User user, boolean like);

    CompletionStage<Doot> addLikes(Doot doot);
}
