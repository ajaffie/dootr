package dev.ajaffie.dootr.auth.services;

import com.nimbusds.jose.JOSEException;
import dev.ajaffie.dootr.auth.domain.User;

import java.time.Duration;

public interface JWTService {
    Duration TOKEN_EXP_DURATION = Duration.ofDays(1);

    String genJwt(User user) throws JOSEException;

    String getJwks();
}
