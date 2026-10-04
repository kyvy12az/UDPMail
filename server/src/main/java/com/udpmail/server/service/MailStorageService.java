package com.udpmail.server.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailStorageService {
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern EMAIL_FILE = Pattern.compile("email_(\\d+)\\.txt");
    private final AccountService accounts;
    public MailStorageService(AccountService accounts) { this.accounts = accounts; }

    public List<String> listEmailFiles(String username) throws IOException {
        Path dir = accounts.userDirectory(username);
        if (!Files.isDirectory(dir)) throw new NoSuchFileException(username);
        try (var paths = Files.list(dir)) {
            return paths.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .sorted().toList();
        }
    }
    public String readEmail(String username, String filename) throws IOException {
        Path file = safeEmailPath(username, filename);
        if (!Files.isRegularFile(file)) throw new NoSuchFileException(filename);
        if (filename.equals("user.txt")) return readAccountInfo(username, file);
        return Files.readString(file, StandardCharsets.UTF_8);
    }
    public synchronized String saveEmail(String from, String to, String subject, String content, String senderIp) throws IOException {
        if (!accounts.exists(to)) throw new NoSuchFileException(to);
        Path receiverDirectory = accounts.userDirectory(to);
        Path senderDirectory = accounts.userDirectory(from);
        String receivedFilename = nextEmailFilename(receiverDirectory);
        String body = "From: " + from + "\nFromIP: "
                + cleanHeader(senderIp == null || senderIp.isBlank() ? "Không rõ" : senderIp)
                + "\nTo: " + to + "\nSubject: " + cleanHeader(subject) + "\nSentTime: " +
                LocalDateTime.now().format(DISPLAY_TIME) + "\n\n" + (content == null ? "" : content);
        Files.writeString(receiverDirectory.resolve(receivedFilename), body, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW);
        String sentFilename = nextEmailFilename(senderDirectory);
        Files.writeString(senderDirectory.resolve(sentFilename), body, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW);
        return receivedFilename;
    }
    private String nextEmailFilename(Path directory) throws IOException {
        int largestSequence = 0;
        try (var files = Files.list(directory)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                Matcher matcher = EMAIL_FILE.matcher(file.getFileName().toString());
                if (!matcher.matches()) continue;
                try {
                    largestSequence = Math.max(largestSequence, Integer.parseInt(matcher.group(1)));
                } catch (NumberFormatException ignored) {
                    // Skip a malformed sequence that is too large for an integer.
                }
            }
        }
        return "email_" + String.format("%03d", largestSequence + 1) + ".txt";
    }
    public long emailCount() throws IOException {
        long count = 0;
        try (var users = Files.list(com.udpmail.server.storage.StorageConfig.usersRoot())) {
            for (Path dir : users.filter(Files::isDirectory).toList()) {
                count += listEmailFiles(dir.getFileName().toString()).stream().filter(name -> !name.equals("user.txt")).count();
            }
        }
        return count;
    }
    private Path safeEmailPath(String username, String filename) {
        if (filename == null || !filename.matches("[A-Za-z0-9_.-]+\\.txt")) throw new IllegalArgumentException("Tên file không hợp lệ");
        Path directory = accounts.userDirectory(username);
        Path file = directory.resolve(filename).normalize();
        if (!file.startsWith(directory)) throw new IllegalArgumentException("Đường dẫn không hợp lệ");
        return file;
    }
    private String readAccountInfo(String username, Path file) throws IOException {
        String accountData = Files.readString(file, StandardCharsets.UTF_8);
        Properties properties = new Properties();
        try (var reader = new java.io.StringReader(accountData)) { properties.load(reader); }
        String registerTime = properties.getProperty("registerTime", "Không rõ");
//        String registerIp = properties.getProperty("registerIP", properties.getProperty("registerIp", "Không rõ"));
        String password = plainPassword(accountData);
        return "From: System\nTo: " + username + "\nSubject: Thông tin tài khoản\nSentTime: " + registerTime +
                "\n\nTên đăng nhập: " + properties.getProperty("username", username) +
                "\nMật khẩu: " + (password == null ? "Không thể hiển thị (tài khoản cũ lưu passwordHash)" : password) +
//                "\nIP đăng ký: " + registerIp +
                "\nThời gian đăng ký: " + registerTime +
                "\n";
    }
    private String plainPassword(String accountData) {
        for (String line : accountData.split("\\R", -1)) {
            if (line.startsWith("password: ")) return line.substring("password: ".length());
        }
        return null;
    }
    private String cleanHeader(String text) { return text == null ? "(Không có tiêu đề)" : text.replaceAll("[\\r\\n]", " "); }
}
