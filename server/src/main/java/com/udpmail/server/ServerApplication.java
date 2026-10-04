package com.udpmail.server;

import com.formdev.flatlaf.FlatLightLaf;
import com.udpmail.server.network.*;
import com.udpmail.server.service.*;
import com.udpmail.server.view.ServerFrame;
import javax.swing.*;

public class ServerApplication {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        final AccountService accounts;
        try { accounts = new AccountService(); }
        catch (Exception e) { JOptionPane.showMessageDialog(null, e.getMessage(), "Không thể khởi tạo Server", JOptionPane.ERROR_MESSAGE); return; }
        SwingUtilities.invokeLater(() -> {
            try {
                ServerFrame frame = new ServerFrame();
                MailStorageService mail = new MailStorageService(accounts);
                RequestHandler handler = new RequestHandler(accounts, mail, frame);
                UDPServer server = new UDPServer(2006, handler, frame);
                frame.setActions(() -> { try { server.start(); frame.onLog("SERVER", "Server bắt đầu lắng nghe"); } catch (Exception e) { throw new IllegalStateException(e); } }, server::stop, handler::publishState);
                frame.setVisible(true);
                new Thread(handler::publishState, "server-dashboard-loader").start();
            } catch (Exception e) { JOptionPane.showMessageDialog(null, e.getMessage(), "Không thể khởi tạo Server", JOptionPane.ERROR_MESSAGE); }
        });
    }
}
