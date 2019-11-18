package dev.ajaffie.dootr.doots.domain;

import java.nio.ByteBuffer;

public class Media {
    public final ByteBuffer buffer;
    public final String mime;

    public Media(ByteBuffer buffer, String mime) {
        this.buffer = buffer;
        this.mime = mime;
    }
}
