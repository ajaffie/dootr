package dev.ajaffie.dootr.doots.services;

import dev.ajaffie.dootr.doots.domain.Doot;
import dev.ajaffie.dootr.doots.domain.QueryDto;

import java.util.Collection;
import java.util.concurrent.CompletionStage;

public interface SearchService {
    CompletionStage<Collection<Doot>> search(QueryDto query);
}
