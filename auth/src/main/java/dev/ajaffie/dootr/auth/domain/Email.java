package dev.ajaffie.dootr.auth.domain;

import java.text.MessageFormat;

public class Email {
    public final String dest;
    public final String subject;
    public final String content;
    public final String destName;

    public Email(String dest, String destName, String subject, String content) {
        this.dest = dest;
        this.subject = subject;
        this.content = content;
        this.destName = destName;
    }

    public static Email verificationEmail(User user) {
        final String template =
                "Hello {0},\n" +
                        "Welcome to Dootr!\n\n" +
                        "You are receiving this email because a new account has been created with this email address ({1}).\n" +
                        "If you made an account, great! Go to dootr.ajaffie.dev/verify and enter the below code (between the brackets):\n" +
                        "Your validation key: <{2}>\n\n" +
                        "If you did not make an account with us, fear not! Whoever created an account with your email address will " +
                        "be locked out unless you enter the above code, so they can't masquerade around with your email :)\n\n" +
                        "Happy Dooting,\n" +
                        "Andrew";
        String content = MessageFormat.format(template, user.getUsername(), user.getEmail(), user.getVerifyCode());
        String subject = MessageFormat.format("Welcome to Dootr, {0}!", user.getUsername());
        return new Email(user.getEmail(), user.getUsername(), subject, content);
    }
}
