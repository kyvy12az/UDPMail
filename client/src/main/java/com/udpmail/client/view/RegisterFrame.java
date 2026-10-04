package com.udpmail.client.view;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {
    public interface Handler{void register(String username,String password);}
    private final JTextField username=new JTextField();
    private final PasswordFieldWithToggle password=new PasswordFieldWithToggle(),confirm=new PasswordFieldWithToggle();
    private final JButton submit=ViewStyles.primary("Đăng ký"),backButton=new JButton("Quay lại đăng nhập");

    public RegisterFrame(Handler handler,Runnable back){
        super("UDP Mail - Tạo tài khoản");Image windowIcon=IconManager.image("mail",32,32,ViewStyles.PRIMARY);if(windowIcon!=null)setIconImage(windowIcon);setDefaultCloseOperation(EXIT_ON_CLOSE);setSize(1100,700);setMinimumSize(new Dimension(900,630));setLocationRelativeTo(null);
        JPanel root=new JPanel(new GridBagLayout());root.setBackground(Color.WHITE);setContentPane(root);GridBagConstraints c=new GridBagConstraints();c.gridy=0;c.fill=GridBagConstraints.BOTH;c.weighty=1;
        c.gridx=0;c.weightx=.47;root.add(new BrandPanel(),c);c.gridx=1;c.weightx=.53;root.add(form(handler,back),c);getRootPane().setDefaultButton(submit);
    }

    private JComponent form(Handler handler,Runnable back){
        JPanel outer=new JPanel(new GridBagLayout());outer.setBackground(Color.WHITE);outer.setBorder(BorderFactory.createEmptyBorder(20,42,20,42));
        JPanel form=new JPanel();form.setOpaque(false);form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));form.setPreferredSize(new Dimension(440,610));
        JLabel title=ViewStyles.title("Tạo tài khoản");title.setFont(title.getFont().deriveFont(36f));title.setAlignmentX(Component.LEFT_ALIGNMENT);form.add(title);form.add(Box.createVerticalStrut(6));
        JLabel subtitle=ViewStyles.muted("Tạo tài khoản mới để sử dụng UDP Mail");subtitle.setFont(subtitle.getFont().deriveFont(16f));subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);form.add(subtitle);form.add(Box.createVerticalStrut(24));
        ViewStyles.styleInput(username);username.putClientProperty("JTextField.leadingIcon",IconManager.load("user",20,20,ViewStyles.MUTED));username.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));password.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));confirm.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));
        form.add(ViewStyles.field("Tên đăng nhập",username));form.add(Box.createVerticalStrut(12));form.add(ViewStyles.field("Mật khẩu",password));form.add(Box.createVerticalStrut(12));form.add(ViewStyles.field("Xác nhận mật khẩu",confirm));form.add(Box.createVerticalStrut(20));
        submit.setMaximumSize(new Dimension(Integer.MAX_VALUE,48));submit.setAlignmentX(Component.LEFT_ALIGNMENT);submit.setIcon(IconManager.load("user",20,20,Color.WHITE));submit.setIconTextGap(10);form.add(submit);form.add(Box.createVerticalStrut(8));
        backButton.setAlignmentX(Component.LEFT_ALIGNMENT);backButton.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));backButton.setForeground(ViewStyles.PRIMARY);backButton.setFont(new Font("Segoe UI",Font.BOLD,14));backButton.setContentAreaFilled(false);backButton.setBorderPainted(false);backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));form.add(backButton);form.add(Box.createVerticalStrut(14));form.add(note());
        outer.add(form);submit.addActionListener(e->validateAndSubmit(handler));backButton.addActionListener(e->back.run());return outer;
    }

    private JComponent note(){
        RoundedPanel panel=new RoundedPanel(new BorderLayout(12,0),new Color(0xEDF5FF),12).outline(new Color(0xD3E5FB));panel.setBorder(BorderFactory.createEmptyBorder(14,16,14,16));panel.setMaximumSize(new Dimension(Integer.MAX_VALUE,78));panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(new JLabel(IconManager.load("info",27,27,ViewStyles.PRIMARY)),BorderLayout.WEST);JTextArea text=new JTextArea("Sau khi đăng ký, thư mục tài khoản của bạn sẽ được tạo trên Server để lưu trữ email.");text.setEditable(false);text.setLineWrap(true);text.setWrapStyleWord(true);text.setOpaque(false);text.setForeground(new Color(0x315E9D));text.setFont(ViewStyles.PLAIN);panel.add(text,BorderLayout.CENTER);return panel;
    }

    private void validateAndSubmit(Handler handler){
        String user=username.getText().trim(),pass=new String(password.getPassword()),confirmation=new String(confirm.getPassword());
        if(user.isEmpty()){validation("Tên đăng nhập không được để trống.",username);return;}if(pass.length()<4){validation("Mật khẩu phải có ít nhất 4 ký tự.",password.field());return;}if(!pass.equals(confirmation)){validation("Mật khẩu xác nhận không khớp.",confirm.field());return;}handler.register(user,pass);
    }
    private void validation(String message,JComponent focus){JOptionPane.showMessageDialog(this,message,"Thông tin chưa hợp lệ",JOptionPane.WARNING_MESSAGE);focus.requestFocusInWindow();}
    public void setBusy(boolean busy){username.setEnabled(!busy);password.setPasswordEnabled(!busy);confirm.setPasswordEnabled(!busy);submit.setEnabled(!busy);backButton.setEnabled(!busy);submit.setText(busy?"Đang đăng ký...":"Đăng ký");}
}
