package com.udpmail.server.storage;

import java.nio.file.Path;

public final class StorageConfig {
    private StorageConfig() {}
    public static Path usersRoot() {
        return Path.of(System.getProperty("udp.mail.data", "server_data"), "users").toAbsolutePath().normalize();
    }
}
