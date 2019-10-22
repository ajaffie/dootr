package dev.ajaffie.dootr.doots.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ErrorDto {
    public final String status = "error";
    public final String error;

    public ErrorDto(String error) {
        this.error = error;
    }
}
