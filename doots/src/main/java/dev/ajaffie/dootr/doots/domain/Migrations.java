package dev.ajaffie.dootr.doots.domain;

import io.vertx.axle.mysqlclient.MySQLPool;

public class Migrations {
    private static boolean isMigrationDone = false;
    private static final Object lock = new Object();

    public static void runMigrations(MySQLPool client) {
        synchronized (lock) {
            if (isMigrationDone) {
                return;
            }
            client.query(
                    "CREATE TABLE IF NOT EXISTS Doots (\n" +
                            "Id BIGINT(19) UNSIGNED PRIMARY KEY,\n" +
                            "Username MEDIUMTEXT NOT NULL,\n" +
                            "UserId INT NOT NULL,\n" +
                            "Parent BIGINT(19) UNSIGNED,\n" +
                            "ChildType TEXT(30),\n" +
                            "Content TEXT(4096) CHARACTER SET utf8mb4 NOT NULL,\n" +
                            "`Timestamp` BIGINT(19) UNSIGNED NOT NULL,\n" +
                            "FULLTEXT(Content)" +
                            ");"
            )
                    .thenCompose(rs -> client.query(
                            "CREATE INDEX IF NOT EXISTS time_index " +
                                    "ON Doots (`Timestamp` DESC)"
                    ))
                    .thenCompose(rs -> client.query(
                            "CREATE INDEX IF NOT EXISTS parent_index " +
                                    "ON Doots (Parent DESC, ChildType(30))"
                    ))
                    .thenCompose(rs -> client.query(
                            "CREATE TABLE IF NOT EXISTS Likes (\n" +
                                    "DootId BIGINT(19) UNSIGNED,\n" +
                                    "UserId BIGINT(19) UNSIGNED,\n" +
                                    "Username MEDIUMTEXT,\n" +
                                    "PRIMARY KEY (DootId, UserId),\n" +
                                    "FOREIGN KEY (DootId) REFERENCES Doots(Id) ON DELETE CASCADE\n" +
                                    ");"
                    ))
                    .thenCompose(rs -> client.query(
                            "CREATE TABLE IF NOT EXISTS Follows (\n" +
                                    "FollowedName MEDIUMTEXT,\n" +
                                    "FollowerName MEDIUMTEXT,\n" +
                                    "PRIMARY KEY (FollowedName(500), FollowerName(500))" +
                                    ");"
                    ))
                    .thenCompose(rs -> client.query(
                            "CREATE TABLE IF NOT EXISTS Media (\n" +
                                    "DootId BIGINT(19) UNSIGNED,\n" +
                                    "MediaId BIGINT(19) UNSIGNED,\n" +
                                    "PRIMARY KEY (DootId, MediaId)" +
                                    ");"
                    ))
                    .toCompletableFuture().join();
            isMigrationDone = true;
        }
    }
}
