package com.udpmail.common.model;

public record Email(String from, String to, String subject, String sentTime, String content) {}
