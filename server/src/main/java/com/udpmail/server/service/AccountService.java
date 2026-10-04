package com.udpmail.server.service;

import com.udpmail.server.storage.StorageConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import com.udpmail.common.model.User;

public class AccountService {
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_]{3,30}");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AccountService() throws IOException { Files.createDirectories(StorageConfig.usersRoot()); }
    public synchronized void register(String username, String password, String ip) throws IOException {
        validate(username, password);
        if (password.indexOf('\n') >= 0 || password.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("Mật khẩu không được chứa ký tự xuống dòng");
        }
        Path directory = userDirectory(username);
        if (Files.exists(directory)) throw new AccountExistsException();
        Files.createDirectory(directory);
        String accountData = "username: " + username + "\n"
                + "password: " + password + "\n"
                + "registerIP: " + ip + "\n"
                + "registerTime: " + LocalDateTime.now().format(TIME) + "\n";
        Files.writeString(directory.resolve("user.txt"), accountData, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW);
    }
    public boolean authenticate(String username, String password) throws IOException {
        Path file = userDirectory(username).resolve("user.txt");
        if (!Files.isRegularFile(file)) return false;
        String accountData = Files.readString(file, StandardCharsets.UTF_8);
        for (String line : accountData.split("\\R", -1)) {
            if (line.startsWith("password: ")) return line.substring("password: ".length()).equals(password);
        }
        Properties properties = new Properties();
        try (var reader = new java.io.StringReader(accountData)) { properties.load(reader); }
        String legacyHash = properties.getProperty("passwordHash");
        return legacyHash != null && PasswordUtils.hash(password).equals(legacyHash);
    }
    public boolean exists(String username) { return Files.isDirectory(userDirectory(username)); }
    public long accountCount() throws IOException {
        try (var paths = Files.list(StorageConfig.usersRoot())) { return paths.filter(Files::isDirectory).count(); }
    }
    public List<User> listUsers() throws IOException {
        try (var paths = Files.list(StorageConfig.usersRoot())) {
            return paths.filter(Files::isDirectory).map(path -> {
                Properties properties = new Properties();
                try (var reader = Files.newBufferedReader(path.resolve("user.txt"), StandardCharsets.UTF_8)) { properties.load(reader); }
                catch (IOException ignored) { return null; }
                return new User(properties.getProperty("username", path.getFileName().toString()), "",
                        properties.getProperty("registerTime", "—"),
                        properties.getProperty("registerIP", properties.getProperty("registerIp", "—")));
            }).filter(java.util.Objects::nonNull).sorted(java.util.Comparator.comparing(User::username)).toList();
        }
    }
    public Path userDirectory(String username) {
        if (username == null || !USERNAME.matcher(username).matches()) throw new IllegalArgumentException("Tên đăng nhập chỉ gồm chữ, số, dấu gạch dưới (3-30 ký tự)");
        Path path = StorageConfig.usersRoot().resolve(username).normalize();
        if (!path.startsWith(StorageConfig.usersRoot())) throw new IllegalArgumentException("Tên đăng nhập không hợp lệ");
        return path;
    }
    private void validate(String username, String password) {
        userDirectory(username);
        if (password == null || password.length() < 4) throw new IllegalArgumentException("Mật khẩu phải có ít nhất 4 ký tự");
    }
    public static final class AccountExistsException extends IOException {}
}
