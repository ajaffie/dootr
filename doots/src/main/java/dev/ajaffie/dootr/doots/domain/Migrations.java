package dev.ajaffie.dootr.doots.domain;

import io.vertx.axle.mysqlclient.MySQLPool;

public class Migrations {
    private static boolean isMigrationDone = false;
    private static Object lock = new Object();

    public static void runMigrations(MySQLPool client) {
        synchronized (lock) {
            if (isMigrationDone) {
                return;
            }
            client.query(
                    "CREATE TABLE IF NOT EXISTS Doots (\n" +
                            "Id BIGINT(19) UNSIGNED PRIMARY KEY AUTO_INCREMENT,\n" +
                            "Username MEDIUMTEXT NOT NULL,\n" +
                            "UserId INT NOT NULL,\n" +
                            "Likes BIGINT(19) UNSIGNED NOT NULL DEFAULT 0,\n" +
                            "Retweeted BIGINT(19) UNSIGNED NOT NULL DEFAULT 0,\n" +
                            "Content TEXT(280) NOT NULL,\n" +
                            "`Timestamp` BIGINT(19) UNSIGNED NOT NULL" +
                            ");"
            )
                    .thenCompose(rs -> client.query(
                            "CREATE INDEX IF NOT EXISTS time_index " +
                                    "ON Doots (`Timestamp` DESC)"
                    ))
                    .toCompletableFuture().join();
            isMigrationDone = true;
        }
    }
}
