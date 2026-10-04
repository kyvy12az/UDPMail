package com.udpmail.client.session;

public final class ClientSession {
    private static String username;
    private ClientSession() {}
    public static String username() { return username; }
    public static void login(String value) { username = value; }
    public static void clear() { username = null; }
}
