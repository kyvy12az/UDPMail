package com.udpmail.client.view;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public final class ViewStyles {
    static final Color NAVY = new Color(0x08, 0x2E, 0x63);
    static final Color NAVY_DARK = new Color(0x04, 0x1E, 0x47);
    static final Color PRIMARY = new Color(0x14, 0x77, 0xF8);
    static final Color CYAN = new Color(0x16, 0xC7, 0xF4);
    static final Color BACKGROUND = new Color(0xF5, 0xF8, 0xFC);
    static final Color TEXT = new Color(0x07, 0x1B, 0x41);
    static final Color MUTED = new Color(0x64, 0x74, 0x8B);
    static final Color SUCCESS = new Color(0x16, 0xA3, 0x4A);
    static final Color ERROR = new Color(0xEF, 0x33, 0x40);
    static final Color BORDER = new Color(0xD7, 0xE1, 0xEF);
    static final Color PRIMARY_PALE = new Color(0xE7, 0xF2, 0xFF);
    static final Font PLAIN = new Font("Segoe UI", Font.PLAIN, 14);

    private ViewStyles() {}

    public static void installDefaults() {
        UIManager.put("defaultFont", PLAIN);
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 9);
        UIManager.put("ScrollBar.width", 11);
    }

    static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 28));
        label.setForeground(TEXT);
        return label;
    }

    static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setFont(PLAIN);
        label.setForeground(MUTED);
        return label;
    }

    static JButton primary(String text) { return new RoundedButton(text, PRIMARY, Color.WHITE); }
    static JButton secondary(String text) { return new RoundedButton(text, Color.WHITE, PRIMARY, BORDER); }

    static JPanel field(String text, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, 7));
        panel.setOpaque(false);
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(label, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }

    static void styleInput(JComponent input) {
        input.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        input.setPreferredSize(new Dimension(100, 46));
        input.setMinimumSize(new Dimension(80, 46));
        input.setBorder(compoundBorder(10, 13, 10, 13));
        input.setBackground(Color.WHITE);
        input.setForeground(TEXT);
    }

    static Border compoundBorder(int top, int left, int bottom, int right) {
        return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(top, left, bottom, right));
    }

    static String serverAddress() {
        return System.getProperty("udp.mail.host", "127.0.0.1") + ":" + Integer.getInteger("udp.mail.port", 2006);
    }
}
