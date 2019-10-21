package dev.ajaffie.dootr.auth.services;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import dev.ajaffie.dootr.auth.domain.User;

import javax.enterprise.context.ApplicationScoped;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@ApplicationScoped
public class JWTServiceImpl implements JWTService {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final JWKSet jwks;

    public JWTServiceImpl() throws Exception {
        this.privateKey = readPrivateKey("/META-INF/resources/privateKey.pem");
        this.publicKey = readPublicKey("/META-INF/resources/publicKey.pem");
        this.jwks = new JWKSet(
                new RSAKey.Builder(publicKey)
                        .privateKey(privateKey)
                        .keyUse(KeyUse.SIGNATURE)
                        .keyID("https://dootr.ajaffie.dev/key/0")
                        .build()
        );
    }

    private static long nowInSeconds() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    /**
     * Read a PEM encoded private key from the classpath
     *
     * @param pemResName - key file resource name
     * @return PrivateKey
     * @throws Exception on decode failure
     */
    private static RSAPrivateKey readPrivateKey(final String pemResName) throws Exception {
        InputStream contentIS = JWTServiceImpl.class.getResourceAsStream(pemResName);
        byte[] tmp = new byte[4096];
        int length = contentIS.read(tmp);
        return decodePrivateKey(new String(tmp, 0, length, StandardCharsets.UTF_8));
    }

    /**
     * Read a PEM encoded public key from the classpath
     *
     * @param pemResName - key file resource name
     * @return RSAPublicKey
     * @throws Exception on decode failure
     */
    private static RSAPublicKey readPublicKey(final String pemResName) throws Exception {
        InputStream contentIS = JWTServiceImpl.class.getResourceAsStream(pemResName);
        byte[] tmp = new byte[4096];
        int length = contentIS.read(tmp);
        return decodePublicKey(new String(tmp, 0, length, StandardCharsets.UTF_8));
    }

    /**
     * Decode a PEM encoded private key string to an RSA PrivateKey
     *
     * @param pemEncoded - PEM string for private key
     * @return PrivateKey
     * @throws Exception on decode failure
     */
    private static RSAPrivateKey decodePrivateKey(final String pemEncoded) throws Exception {
        byte[] encodedBytes = toEncodedBytes(pemEncoded);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(keySpec);
    }

    /**
     * Decode a PEM encoded public key string to an RSA PublicKey
     *
     * @param pemEncoded - PEM string for private key
     * @return PublicKey
     * @throws Exception on decode failure
     */

    private static RSAPublicKey decodePublicKey(String pemEncoded) throws Exception {
        byte[] encodedBytes = toEncodedBytes(pemEncoded);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(encodedBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(spec);
    }

    private static byte[] toEncodedBytes(final String pemEncoded) {
        final String normalizedPem = removeBeginEnd(pemEncoded);
        return Base64.getDecoder().decode(normalizedPem);
    }

    private static String removeBeginEnd(String pem) {
        pem = pem.replaceAll("-----BEGIN (.*)-----", "");
        pem = pem.replaceAll("-----END (.*)----", "");
        pem = pem.replaceAll("\r\n", "");
        pem = pem.replaceAll("\n", "");
        return pem.trim();
    }

    @Override
    public String genJwt(User user) throws JOSEException {
        JWSSigner signer = new RSASSASigner(privateKey);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getId()))
                .audience("https://dootr.ajaffie.dev")
                .expirationTime(Date.from(Instant.now().plus(TOKEN_EXP_DURATION)))
                .issuer("https://dootr.ajaffie.dev")
                .notBeforeTime(Date.from(Instant.now()))
                .issueTime(Date.from(Instant.now()))
                .claim("upn", user.getUsername())
                .build();
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .keyID(jwks.getKeys().get(0).getKeyID())
                        .build(),
                claimsSet
        );
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    @Override
    public String getJwks() {
        return jwks.toPublicJWKSet().toJSONObject(true).toJSONString();
    }
}
