package com.udpmail.client.view;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

public class LoginFrame extends JFrame {
    private final JTextField username=new JTextField();
    private final PasswordFieldWithToggle password=new PasswordFieldWithToggle();
    private final JButton login=ViewStyles.primary("Đăng nhập");
    private final JButton register=new JButton("Tạo tài khoản mới");

    public LoginFrame(BiConsumer<String,String> onLogin,Runnable onRegister){
        super("UDP Mail - Đăng nhập");
        Image windowIcon=IconManager.image("mail",32,32,ViewStyles.PRIMARY);if(windowIcon!=null)setIconImage(windowIcon);
        setDefaultCloseOperation(EXIT_ON_CLOSE);setSize(1100,700);setMinimumSize(new Dimension(900,600));setLocationRelativeTo(null);
        JPanel root=new JPanel(new GridBagLayout());root.setBackground(Color.WHITE);setContentPane(root);
        GridBagConstraints c=new GridBagConstraints();c.gridy=0;c.fill=GridBagConstraints.BOTH;c.weighty=1;
        c.gridx=0;c.weightx=.47;root.add(new BrandPanel(),c);
        c.gridx=1;c.weightx=.53;root.add(form(onLogin,onRegister),c);
        getRootPane().setDefaultButton(login);
    }

    private JComponent form(BiConsumer<String,String> onLogin,Runnable onRegister){
        JPanel outer=new JPanel(new GridBagLayout());outer.setBackground(Color.WHITE);outer.setBorder(BorderFactory.createEmptyBorder(25,45,25,45));
        JPanel form=new JPanel();form.setOpaque(false);form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));form.setPreferredSize(new Dimension(430,550));
        JLabel title=ViewStyles.title("Đăng nhập");title.setFont(title.getFont().deriveFont(38f));title.setAlignmentX(Component.LEFT_ALIGNMENT);form.add(title);
        form.add(Box.createVerticalStrut(8));JLabel subtitle=ViewStyles.muted("Đăng nhập để sử dụng UDP Mail");subtitle.setFont(subtitle.getFont().deriveFont(17f));subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);form.add(subtitle);
        form.add(Box.createVerticalStrut(34));ViewStyles.styleInput(username);username.putClientProperty("JTextField.leadingIcon",IconManager.load("user",20,20,ViewStyles.MUTED));username.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));
        password.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));form.add(ViewStyles.field("Tên đăng nhập",username));form.add(Box.createVerticalStrut(16));form.add(ViewStyles.field("Mật khẩu",password));
        form.add(Box.createVerticalStrut(24));login.setMaximumSize(new Dimension(Integer.MAX_VALUE,48));login.setAlignmentX(Component.LEFT_ALIGNMENT);login.setIcon(IconManager.load("log-in",20,20,Color.WHITE));login.setIconTextGap(10);form.add(login);
        form.add(Box.createVerticalStrut(20));form.add(separator());form.add(Box.createVerticalStrut(12));
        register.setAlignmentX(Component.LEFT_ALIGNMENT);register.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));register.setForeground(ViewStyles.PRIMARY);register.setFont(new Font("Segoe UI",Font.BOLD,14));register.setContentAreaFilled(false);register.setBorderPainted(false);register.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));form.add(register);
//        form.add(Box.createVerticalGlue());form.add(serverStatus());
        outer.add(form);login.addActionListener(e->{String user=username.getText().trim(),pass=new String(password.getPassword());if(user.isEmpty()){validation("Vui lòng nhập tên đăng nhập.",username);return;}if(pass.isEmpty()){validation("Vui lòng nhập mật khẩu.",password.field());return;}onLogin.accept(user,pass);});
        register.addActionListener(e->onRegister.run());return outer;
    }

    private JComponent separator(){
        JPanel panel=new JPanel(new GridBagLayout());panel.setOpaque(false);panel.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));GridBagConstraints c=new GridBagConstraints();c.gridy=0;c.fill=GridBagConstraints.HORIZONTAL;c.weightx=1;
        panel.add(new JSeparator(),c);c.weightx=0;c.insets=new Insets(0,14,0,14);panel.add(ViewStyles.muted("hoặc"),c);c.weightx=1;c.insets=new Insets(0,0,0,0);panel.add(new JSeparator(),c);panel.setAlignmentX(Component.LEFT_ALIGNMENT);return panel;
    }

    private JComponent serverStatus(){
        RoundedPanel panel=new RoundedPanel(new FlowLayout(FlowLayout.CENTER,9,12),new Color(0xECF9F1),12).outline(new Color(0xB9E8CB));panel.setMaximumSize(new Dimension(Integer.MAX_VALUE,52));panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel stateIcon=new JLabel(IconManager.load("circle-check-big",17,17,ViewStyles.SUCCESS));JLabel text=new JLabel("Server: "+ViewStyles.serverAddress()+"  •  Sẵn sàng kết nối");text.setForeground(new Color(0x11883D));text.setFont(new Font("Segoe UI",Font.BOLD,13));panel.add(stateIcon);panel.add(text);return panel;
    }

    private void validation(String message,JComponent focus){JOptionPane.showMessageDialog(this,message,"Thiếu thông tin",JOptionPane.WARNING_MESSAGE);focus.requestFocusInWindow();}
    public void setBusy(boolean busy){username.setEnabled(!busy);password.setPasswordEnabled(!busy);login.setEnabled(!busy);register.setEnabled(!busy);login.setText(busy?"Đang đăng nhập...":"Đăng nhập");}
}
