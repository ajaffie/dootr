package dev.ajaffie.dootr.doots.services;

import dev.ajaffie.dootr.doots.domain.Media;
import dev.ajaffie.dootr.doots.domain.User;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;

public interface MediaService {

    CompletionStage<Long> putMedia(User owner, Long id, String mime, ByteBuffer buffer);

    CompletionStage<Boolean> mediaBelongsToUser(Long mediaId, User user);

    CompletionStage<Media> getMedia(Long id);

}
