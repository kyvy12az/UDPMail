package com.udpmail.server.network;

import com.udpmail.common.protocol.*;
import com.udpmail.server.listener.ServerEventListener;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.*;

public class UDPServer {
    private final int port;
    private final RequestHandler handler;
    private final ServerEventListener listener;
    private ExecutorService workers;
    private volatile boolean running;
    private DatagramSocket socket;

    public UDPServer(int port, RequestHandler handler, ServerEventListener listener) { this.port = port; this.handler = handler; this.listener = listener; }
    public void start() throws SocketException {
        if (running) return;
        socket = new DatagramSocket(port); workers = Executors.newFixedThreadPool(8); running = true;
        if (listener != null) listener.onStateChanged(true, port);
        Thread receiverThread = new Thread(
                this::receiveLoop,
                "udp-server-receiver"
        );

        receiverThread.start();
    }
    private void receiveLoop() {
        while (running) {
            try {
                byte[] buffer = new byte[PacketCodec.MAX_PACKET_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                byte[] payload = Arrays.copyOf(packet.getData(), packet.getLength());
                InetSocketAddress remote = new InetSocketAddress(packet.getAddress(), packet.getPort());
                workers.submit(() -> process(payload, remote));
            } catch (SocketException e) { if (running) log("ERROR", e.getMessage()); }
              catch (IOException e) { log("ERROR", e.getMessage()); }
        }
    }
    private void process(byte[] payload, InetSocketAddress remote) {
        try {
            Request request = PacketCodec.decodeRequest(payload, payload.length);
            byte[] response = PacketCodec.encode(handler.handle(request, remote));
            socket.send(new DatagramPacket(response, response.length, remote.getAddress(), remote.getPort()));
        } catch (Exception e) { log("BAD_PACKET", e.getMessage()); }
    }
    public void stop() {
        running = false; if (socket != null) socket.close(); workers.shutdownNow();
        if (listener != null) listener.onStateChanged(false, port);
    }
    public boolean isRunning() { return running; }
    private void log(String event, String info) { if (listener != null) listener.onLog(event, info); }
}
