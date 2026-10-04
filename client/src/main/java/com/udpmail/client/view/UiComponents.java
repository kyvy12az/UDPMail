package com.udpmail.client.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;

final class RoundedPanel extends JPanel {
    private final Color fill;
    private final int arc;
    private Color outline;

    RoundedPanel(LayoutManager layout, Color fill, int arc) {
        super(layout); this.fill = fill; this.arc = arc; setOpaque(false);
    }
    RoundedPanel outline(Color color) { outline = color; return this; }
    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(fill); g.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);
        if (outline != null) { g.setColor(outline); g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc); }
        g.dispose(); super.paintComponent(graphics);
    }
}

final class RoundedButton extends JButton {
    private final Color normal, borderColor;
    private boolean hovering;
    RoundedButton(String text, Color background, Color foreground) { this(text, background, foreground, background); }
    RoundedButton(String text, Color background, Color foreground, Color borderColor) {
        super(text); normal=background; this.borderColor=borderColor; setForeground(foreground);
        setFont(new Font("Segoe UI",Font.BOLD,14)); setBorder(BorderFactory.createEmptyBorder(10,18,10,18));
        setPreferredSize(new Dimension(150,46)); setContentAreaFilled(false); setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter(){ public void mouseEntered(MouseEvent e){hovering=true;repaint();} public void mouseExited(MouseEvent e){hovering=false;repaint();} });
    }
    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g=(Graphics2D)graphics.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill=isEnabled()?(hovering?normal.brighter():normal):new Color(0xB8C4D4);
        g.setColor(fill); g.fillRoundRect(0,0,getWidth()-1,getHeight()-1,11,11); g.setColor(isEnabled()?borderColor:new Color(0xB8C4D4));
        g.drawRoundRect(0,0,getWidth()-1,getHeight()-1,11,11); g.dispose(); super.paintComponent(graphics);
    }
}

final class PasswordFieldWithToggle extends JPanel {
    private final JPasswordField field=new JPasswordField();
    private final JButton toggle=new JButton();
    private final char echo;
    PasswordFieldWithToggle() {
        super(new BorderLayout()); setOpaque(false); ViewStyles.styleInput(field); field.setBorder(BorderFactory.createEmptyBorder(9,13,9,6)); echo=field.getEchoChar();
        field.setEchoChar((char) 0);
        field.putClientProperty("JTextField.leadingIcon",IconManager.load("lock-keyhole",20,20,ViewStyles.MUTED));
        toggle.setIcon(IconManager.load("eye",20,20,ViewStyles.MUTED)); toggle.setBorder(BorderFactory.createEmptyBorder(0,10,0,12));
        toggle.setContentAreaFilled(false); toggle.setFocusPainted(false); toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(ViewStyles.compoundBorder(0,0,0,0)); add(field,BorderLayout.CENTER); add(toggle,BorderLayout.EAST);
        toggle.addActionListener(e->{boolean show=field.getEchoChar()!=0;field.setEchoChar(show?(char)0:echo);toggle.setIcon(IconManager.load(show?"eye":"eye-off",20,20,ViewStyles.MUTED));});
    }
    char[] getPassword(){return field.getPassword();}
    void setPasswordEnabled(boolean enabled){field.setEnabled(enabled);toggle.setEnabled(enabled);}
    JPasswordField field(){return field;}
}

final class BrandPanel extends JPanel {
    private final Icon mailIcon=IconManager.load("mail",150,150,Color.WHITE);
    private final Icon accentIcon=IconManager.load("pencil",34,34,ViewStyles.CYAN);
    BrandPanel(){setOpaque(false);setPreferredSize(new Dimension(450,650));}
    @Override protected void paintComponent(Graphics graphics){
        Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0,0,ViewStyles.NAVY_DARK,getWidth(),getHeight(),new Color(0x0860C7)));g.fillRect(0,0,getWidth(),getHeight());
        g.setColor(new Color(255,255,255,18));g.fillOval(-130,-120,360,360);g.fillOval(getWidth()-210,getHeight()-230,380,380);
        int cx=getWidth()/2,cy=Math.max(190,getHeight()/2-70);g.setColor(new Color(0x10A9F5));g.setStroke(new BasicStroke(3f));g.drawArc(cx-155,cy-90,310,180,15,310);
        mailIcon.paintIcon(this,g,cx-75,cy-58);
        draw(g,"UDP",cx-42,cy+130,new Font("Segoe UI",Font.BOLD,43),Color.WHITE);draw(g,"Mail",cx+70,cy+130,new Font("Segoe UI",Font.BOLD,43),ViewStyles.CYAN);
        draw(g,"Kết nối và gửi thư an toàn",cx,cy+176,new Font("Segoe UI",Font.PLAIN,20),new Color(220,235,255));
        g.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{6,8},0));g.setColor(new Color(0x40C8FF));
        Path2D route=new Path2D.Double();route.moveTo(35,getHeight()-100);route.curveTo(cx-100,getHeight()-180,cx+50,getHeight()-60,getWidth()-45,getHeight()-135);g.draw(route);
        accentIcon.paintIcon(this,g,getWidth()-83,getHeight()-170);g.dispose();
    }
    private static void draw(Graphics2D g,String text,int x,int y,Font font,Color color){g.setFont(font);g.setColor(color);g.drawString(text,x-g.getFontMetrics().stringWidth(text)/2,y);}
}
