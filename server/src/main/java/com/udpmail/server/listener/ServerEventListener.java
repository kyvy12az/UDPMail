package com.udpmail.server.listener;

import java.util.List;

public interface ServerEventListener {
    record UserStatus(String username, String ipAddress, boolean online, String loginTime) {}
    void onLog(String event, String information);
    void onStatsChanged(int accounts, int onlineClients, int emails);
    void onUsersChanged(List<UserStatus> users);
    void onStateChanged(boolean running, int port);
}
