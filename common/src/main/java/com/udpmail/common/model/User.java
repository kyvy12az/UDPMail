package com.udpmail.common.model;

public record User(String username, String passwordHash, String registerTime, String registerIp) {}
