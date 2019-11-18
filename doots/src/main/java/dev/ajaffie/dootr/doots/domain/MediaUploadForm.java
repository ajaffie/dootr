package dev.ajaffie.dootr.doots.domain;

import org.apache.tika.Tika;

import javax.ws.rs.FormParam;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaUploadForm {
    public MediaUploadForm() {
    }

    public ByteBuffer buffer;
    public String mime;

    @FormParam("content")
    public void setContent(byte[] content) throws IOException {
        this.buffer = ByteBuffer.wrap(content);
        this.mime = new Tika().detect(buffer.array());
    }
}
