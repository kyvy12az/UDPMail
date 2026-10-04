package com.udpmail.client.service;

import com.udpmail.client.network.UDPClient;
import com.udpmail.common.protocol.*;
import java.io.IOException;
import java.util.*;

public class EmailService {
    private final UDPClient client;
    public EmailService(UDPClient client) { this.client = client; }
    public List<String> list(String username) throws IOException {
        Response<Object> r = client.send(new Request(ActionType.LIST_EMAILS, Map.of("username", username))); requireSuccess(r);
        if (!(r.getData() instanceof List<?> list)) return List.of(); return list.stream().map(Object::toString).toList();
    }
    public String read(String username, String filename) throws IOException {
        Response<Object> r = client.send(new Request(ActionType.READ_EMAIL, Map.of("username", username, "filename", filename))); requireSuccess(r);
        if (r.getData() instanceof Map<?,?> map) return Objects.toString(map.get("content"), ""); return "";
    }
    public void send(String from, String to, String subject, String content) throws IOException {
        Response<Object> r = client.send(new Request(ActionType.SEND_EMAIL, Map.of("from", from, "to", to, "subject", subject, "content", content))); requireSuccess(r);
    }
    private void requireSuccess(Response<?> r) throws IOException { if (!r.isSuccess()) throw new IOException(r.getMessage()); }
}
