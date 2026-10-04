package com.udpmail.server.network;

import com.udpmail.common.protocol.*;
import com.udpmail.server.listener.ServerEventListener;
import com.udpmail.server.service.AccountService;
import com.udpmail.server.service.MailStorageService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.InetSocketAddress;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RequestHandler {
    private final AccountService accounts;
    private final MailStorageService mail;
    private final ServerEventListener listener;
    private final Map<String, InetSocketAddress> onlineUsers = new ConcurrentHashMap<>();
    private final Map<String, String> loginTimes = new ConcurrentHashMap<>();
    private final Map<String, Response<?>> processedRequests = new ConcurrentHashMap<>();

    public RequestHandler(AccountService accounts, MailStorageService mail, ServerEventListener listener) {
        this.accounts = accounts; this.mail = mail; this.listener = listener;
    }
    public Response<?> handle(Request request, InetSocketAddress remote) {
        if (request != null && request.getRequestId() != null) {
            Response<?> cached = processedRequests.get(request.getRequestId());
            if (cached != null) return cached;
        }
        Response<?> response = process(request, remote);
        if (request != null && request.getRequestId() != null) {
            if (processedRequests.size() > 10_000) processedRequests.clear();
            processedRequests.putIfAbsent(request.getRequestId(), response);
        }
        return response;
    }
    private Response<?> process(Request request, InetSocketAddress remote) {
        String id = request == null ? null : request.getRequestId();
        try {
            if (request == null || request.getAction() == null) return Response.error(id, StatusCode.BAD_REQUEST, "Yêu cầu không hợp lệ");
            return switch (request.getAction()) {
                case REGISTER -> register(request, remote);
                case LOGIN -> login(request, remote);
                case LIST_EMAILS -> list(request);
                case READ_EMAIL -> read(request);
                case SEND_EMAIL -> send(request, remote);
                case LOGOUT -> logout(request);
                case PING -> Response.success(id, "PONG", Map.of("serverTime", System.currentTimeMillis()));
            };
        } catch (AccountService.AccountExistsException e) {
            return Response.error(id, StatusCode.ACCOUNT_EXISTS, "Tên đăng nhập đã tồn tại");
        } catch (NoSuchFileException e) {
            return Response.error(id, StatusCode.EMAIL_NOT_FOUND, "Không tìm thấy tài khoản hoặc email");
        } catch (IllegalArgumentException e) {
            return Response.error(id, StatusCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log("ERROR", e.getMessage());
            return Response.error(id, StatusCode.SERVER_ERROR, "Server không thể xử lý yêu cầu");
        }
    }
    private Response<?> register(Request r, InetSocketAddress remote) throws Exception {
        String username = required(r, "username"), password = required(r, "password");
        accounts.register(username, password);
        log("REGISTER", "Tạo " + username + " tại server_data/users/" + username + "/"); updateDashboard();
        return Response.success(r.getRequestId(), "Đăng ký thành công", null);
    }
    private Response<?> login(Request r, InetSocketAddress remote) throws Exception {
        String username = required(r, "username"), password = required(r, "password");
        if (!accounts.exists(username)) return Response.error(r.getRequestId(), StatusCode.ACCOUNT_NOT_FOUND, "Tài khoản không tồn tại");
        if (!accounts.authenticate(username, password)) return Response.error(r.getRequestId(), StatusCode.INVALID_PASSWORD, "Mật khẩu không đúng");
        onlineUsers.put(username, remote);
        loginTimes.put(username, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        List<String> files = mail.listEmailFiles(username);
        log("LOGIN", username + " đăng nhập từ " + remote.getAddress().getHostAddress()); updateDashboard();
        return Response.success(r.getRequestId(), "Đăng nhập thành công", Map.of("username", username, "emailFiles", files));
    }
    private Response<?> list(Request r) throws Exception {
        String username = required(r, "username"); requireOnline(username);
        return Response.success(r.getRequestId(), "Đã tải danh sách email", mail.listEmailFiles(username));
    }
    private Response<?> read(Request r) throws Exception {
        String username = required(r, "username"); requireOnline(username);
        String filename = required(r, "filename");
        log("READ_EMAIL", username + " đọc " + filename);
        return Response.success(r.getRequestId(), "Đã đọc email", Map.of("filename", filename, "content", mail.readEmail(username, filename)));
    }
    private Response<?> send(Request r, InetSocketAddress remote) throws Exception {
        String from = required(r, "from"), to = required(r, "to"); requireOnline(from);
        if (!accounts.exists(to)) return Response.error(r.getRequestId(), StatusCode.RECEIVER_NOT_FOUND, "Tài khoản người nhận không tồn tại");
        String senderIp = remote.getAddress().getHostAddress();
        String filename = mail.saveEmail(from, to, r.string("subject"), r.string("content"), senderIp);
        log("SEND_EMAIL", from + " → " + to + " | " + filename); updateDashboard();
        return Response.success(r.getRequestId(), "Gửi email thành công", Map.of("filename", filename));
    }
    private Response<?> logout(Request r) throws Exception {
        String username = required(r, "username"); onlineUsers.remove(username); log("LOGOUT", username + " đã đăng xuất"); updateDashboard();
        return Response.success(r.getRequestId(), "Đăng xuất thành công", null);
    }
    private String required(Request r, String key) {
        String value = r.string(key); if (value == null || value.isBlank()) throw new IllegalArgumentException("Thiếu dữ liệu: " + key); return value.trim();
    }
    private void requireOnline(String username) { if (!onlineUsers.containsKey(username)) throw new IllegalArgumentException("Phiên đăng nhập không hợp lệ"); }
    private void log(String event, String info) { if (listener != null) listener.onLog(event, info); }
    public void publishState() { updateDashboard(); }
    private void updateDashboard() {
        try {
            if (listener == null) return;
            listener.onStatsChanged((int) accounts.accountCount(), onlineUsers.size(), (int) mail.emailCount());
            List<ServerEventListener.UserStatus> users = accounts.listUsers().stream().map(user -> {
                InetSocketAddress address = onlineUsers.get(user.username());
                String ip = address == null ? user.registerIp() : address.getAddress().getHostAddress();
                return new ServerEventListener.UserStatus(user.username(), ip, address != null, loginTimes.getOrDefault(user.username(), "—"));
            }).toList();
            listener.onUsersChanged(users);
        } catch (Exception e) { log("ERROR", "Không thể cập nhật dashboard: " + e.getMessage()); }
    }
}
