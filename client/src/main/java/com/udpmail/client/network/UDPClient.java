package com.udpmail.client.network;

import com.udpmail.common.protocol.*;
import java.io.Closeable;
import java.io.IOException;
import java.net.*;

public class UDPClient implements Closeable {
    private final InetAddress serverAddress;
    private final int serverPort;
    private final DatagramSocket socket;
    public UDPClient(String host, int port) throws IOException {
        serverAddress = InetAddress.getByName(host); serverPort = port; socket = new DatagramSocket(); socket.setSoTimeout(1800);
    }
    public synchronized Response<Object> send(Request request) throws IOException {
        byte[] bytes = PacketCodec.encode(request);
        IOException last = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            socket.send(new DatagramPacket(bytes, bytes.length, serverAddress, serverPort));
            try {
                byte[] buffer = new byte[PacketCodec.MAX_PACKET_SIZE]; DatagramPacket response = new DatagramPacket(buffer, buffer.length); socket.receive(response);
                Response<Object> decoded = PacketCodec.decodeResponse(response.getData(), response.getLength());
                if (request.getRequestId().equals(decoded.getRequestId())) return decoded;
            } catch (SocketTimeoutException e) { last = new IOException("Server không phản hồi (lần " + attempt + ")", e); }
        }
        throw last == null ? new IOException("Không nhận được phản hồi") : last;
    }
    public void close() { socket.close(); }
}
