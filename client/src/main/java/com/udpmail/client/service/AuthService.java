package com.udpmail.client.service;

import com.udpmail.client.network.UDPClient;
import com.udpmail.common.protocol.*;
import java.io.IOException;
import java.util.*;

public class AuthService {
    private final UDPClient client;
    public AuthService(UDPClient client) { this.client = client; }
    public Response<Object> register(String username, String password) throws IOException { return client.send(new Request(ActionType.REGISTER, Map.of("username", username, "password", password))); }
    public Response<Object> login(String username, String password) throws IOException { return client.send(new Request(ActionType.LOGIN, Map.of("username", username, "password", password))); }
    public Response<Object> logout(String username) throws IOException { return client.send(new Request(ActionType.LOGOUT, Map.of("username", username))); }
    @SuppressWarnings("unchecked")
    public List<String> loginFiles(Response<Object> response) {
        if (!(response.getData() instanceof Map<?,?> map)) return List.of();
        Object files = map.get("emailFiles"); if (!(files instanceof List<?> list)) return List.of();
        return list.stream().map(Object::toString).toList();
    }
}
