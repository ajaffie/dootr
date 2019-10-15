package dev.ajaffie.dootr.auth.domain;

import io.vertx.axle.sqlclient.Row;
import io.vertx.axle.sqlclient.Tuple;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

public class User {
    private static final Random RANDOM = new SecureRandom();
    private long id;
    private String email;
    private String username;
    // These are hex encoded.
    private String salt;
    private String passwordHash;

    public User(String email, String username, String password) {
        this.email = email;
        this.username = username;
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        this.salt = DatatypeConverter.printHexBinary(salt);
        this.passwordHash = hashPassword(password, salt);
    }

    private User() {
    }

    public static User from(Row row) {
        User user = new User();

        user.id = row.getLong("Id");
        user.email = row.getString("Email");
        user.username = row.getString("Username");
        user.salt = row.getString("Salt");
        user.passwordHash = row.getString("PasswordHash");
        return user;
    }

    public Tuple row() {
        return Row.of(username, email, salt, passwordHash);
    }


    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getSalt() {
        return salt;
    }

    public byte[] getSaltAsBytes() {
        return DatatypeConverter.parseHexBinary(salt);
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public byte[] getPasswordHashAsBytes() {
        return DatatypeConverter.parseHexBinary(passwordHash);
    }

    public static String hashPassword(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 50000, 256);
        try {
            return DatatypeConverter.printHexBinary(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error hashing password: " + e.getMessage());
        } finally {
            spec.clearPassword();
        }
    }
}
