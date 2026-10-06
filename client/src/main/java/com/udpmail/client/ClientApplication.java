package com.udpmail.client;

import com.formdev.flatlaf.FlatLightLaf;
import com.udpmail.client.network.UDPClient;
import com.udpmail.client.service.AuthService;
import com.udpmail.client.service.EmailService;
import com.udpmail.client.session.ClientSession;
import com.udpmail.client.view.LoginFrame;
import com.udpmail.client.view.MainFrame;
import com.udpmail.client.view.RegisterFrame;
import com.udpmail.client.view.ViewStyles;
import com.udpmail.common.protocol.Response;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.util.List;

public class ClientApplication {

    private final UDPClient udp;
    private final AuthService auth;
    private final EmailService email;

    private JFrame current;

    private ClientApplication() throws Exception {
        udp = new UDPClient(
                System.getProperty("udp.mail.host", "172.20.10.7"),
                Integer.getInteger("udp.mail.port", 2006)
        );

        auth = new AuthService(udp);
        email = new EmailService(udp);
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        ViewStyles.installDefaults();

        SwingUtilities.invokeLater(() -> {
            try {
                new ClientApplication().showLogin();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        null,
                        e.getMessage(),
                        "Không thể mở Client",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    private void swap(JFrame next) {
        if (current != null) {
            current.dispose();
        }

        current = next;
        current.setVisible(true);
    }

    private void showLogin() {
        LoginFrame frame = new LoginFrame(
                (username, password) ->
                        login(frameRef(), username, password),
                this::showRegister
        );

        swap(frame);
    }

    private LoginFrame frameRef() {
        return (LoginFrame) current;
    }

    private void login(
            LoginFrame frame,
            String username,
            String password
    ) {
        if (username.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Hãy nhập đầy đủ tài khoản và mật khẩu"
            );
            return;
        }

        frame.setBusy(true);

        new SwingWorker<Response<Object>, Void>() {

            @Override
            protected Response<Object> doInBackground() throws Exception {
                return auth.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    Response<Object> response = get();

                    if (!response.isSuccess()) {
                        throw new Exception(response.getMessage());
                    }

                    ClientSession.login(username);

                    showMain(
                            username,
                            auth.loginFiles(response)
                    );
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            frame,
                            message(e),
                            "Đăng nhập thất bại",
                            JOptionPane.ERROR_MESSAGE
                    );

                    frame.setBusy(false);
                }
            }
        }.execute();
    }

    private void showRegister() {
        RegisterFrame frame = new RegisterFrame(
                (username, password) ->
                        register(frameRefRegister(), username, password),
                this::showLogin
        );

        swap(frame);
    }

    private RegisterFrame frameRefRegister() {
        return (RegisterFrame) current;
    }

    private void register(
            RegisterFrame frame,
            String username,
            String password
    ) {
        frame.setBusy(true);

        new SwingWorker<Response<Object>, Void>() {

            @Override
            protected Response<Object> doInBackground() throws Exception {
                return auth.register(username, password);
            }

            @Override
            protected void done() {
                try {
                    Response<Object> response = get();

                    if (!response.isSuccess()) {
                        throw new Exception(response.getMessage());
                    }

                    JOptionPane.showMessageDialog(
                            frame,
                            "Đăng ký thành công. "
                                    + "Server đã tạo thư mục tài khoản."
                    );

                    showLogin();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            frame,
                            message(e),
                            "Đăng ký thất bại",
                            JOptionPane.ERROR_MESSAGE
                    );

                    frame.setBusy(false);
                }
            }
        }.execute();
    }

    private void showMain(
            String username,
            List<String> files
    ) {
        MainFrame frame = new MainFrame(
                username,
                files,
                new MainFrame.Handler() {

                    @Override
                    public List<String> refresh() throws Exception {
                        return email.list(username);
                    }

                    @Override
                    public String read(String filename) throws Exception {
                        return email.read(username, filename);
                    }

                    @Override
                    public void send(
                            String to,
                            String subject,
                            String content
                    ) throws Exception {
                        email.send(
                                username,
                                to,
                                subject,
                                content
                        );
                    }

                    @Override
                    public void logout() {
                        try {
                            auth.logout(username);
                        } catch (Exception ignored) {
                        }

                        ClientSession.clear();

                        SwingUtilities.invokeLater(
                                ClientApplication.this::showLogin
                        );
                    }
                }
        );

        swap(frame);
    }

    private static String message(Exception exception) {
        Throwable cause = exception.getCause();

        return cause == null
                ? exception.getMessage()
                : cause.getMessage();
    }
}