package com.udpmail.common.protocol;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class PacketCodec {
    public static final int MAX_PACKET_SIZE = 60_000;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private PacketCodec() {}
    public static byte[] encode(Object value) throws IOException { return MAPPER.writeValueAsString(value).getBytes(StandardCharsets.UTF_8); }
    public static Request decodeRequest(byte[] bytes, int length) throws IOException { return MAPPER.readValue(bytes, 0, length, Request.class); }
    public static Response<Object> decodeResponse(byte[] bytes, int length) throws IOException { return MAPPER.readValue(bytes, 0, length, new TypeReference<>() {}); }
}
