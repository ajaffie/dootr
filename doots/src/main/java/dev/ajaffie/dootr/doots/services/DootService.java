package dev.ajaffie.dootr.doots.services;

import dev.ajaffie.dootr.doots.domain.AddItemDto;
import dev.ajaffie.dootr.doots.domain.Doot;
import dev.ajaffie.dootr.doots.domain.User;

import java.util.concurrent.CompletionStage;

public interface DootService {
    CompletionStage<Long> createDoot(AddItemDto addItemDto, User user);

    CompletionStage<Doot> getDoot(long id);
}
