package com.udpmail.client.view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

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
    private final BufferedImage background=loadBackground();
    BrandPanel(){setOpaque(false);setPreferredSize(new Dimension(450,650));}
    @Override protected void paintComponent(Graphics graphics){
        super.paintComponent(graphics);
        Graphics2D g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        if(background==null){g.setColor(ViewStyles.NAVY_DARK);g.fillRect(0,0,getWidth(),getHeight());g.dispose();return;}
        double scale=Math.max((double)getWidth()/background.getWidth(),(double)getHeight()/background.getHeight());
        int width=(int)Math.ceil(background.getWidth()*scale),height=(int)Math.ceil(background.getHeight()*scale);
        int x=(getWidth()-width)/2,y=(getHeight()-height)/2;
        g.drawImage(background,x,y,width,height,null);g.dispose();
    }
    private static BufferedImage loadBackground(){
        try{
            var resource=BrandPanel.class.getResource("/background.png");
            if(resource==null){System.err.println("Không tìm thấy ảnh nền: /background.png");return null;}
            return ImageIO.read(resource);
        }catch(IOException e){System.err.println("Không thể tải ảnh nền /background.png: "+e.getMessage());return null;}
    }
}
