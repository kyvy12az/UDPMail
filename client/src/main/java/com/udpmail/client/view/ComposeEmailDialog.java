package com.udpmail.client.view;

import com.udpmail.client.session.ClientSession;
import javax.swing.*;
import java.awt.*;

public class ComposeEmailDialog extends JDialog {
    public interface Handler{void send(String to,String subject,String content)throws Exception;}
    private final JTextField to=new JTextField(),subject=new JTextField();
    private final JTextArea content=new JTextArea();
    private final JButton send=ViewStyles.primary("Gửi email"),cancel=ViewStyles.secondary("Hủy");
    private final Handler handler;

    public ComposeEmailDialog(JFrame owner,Handler handler){
        super(owner,"Soạn thư mới",true);this.handler=handler;Image windowIcon=IconManager.image("mail",32,32,ViewStyles.PRIMARY);if(windowIcon!=null)setIconImage(windowIcon);setSize(680,590);setMinimumSize(new Dimension(560,500));setLocationRelativeTo(owner);
        JPanel root=new JPanel(new BorderLayout(0,16));root.setBackground(ViewStyles.BACKGROUND);root.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));setContentPane(root);
        JPanel header=new JPanel(new BorderLayout(12,0));header.setOpaque(false);JLabel icon=new JLabel(IconManager.load("pencil",30,30,ViewStyles.PRIMARY));header.add(icon,BorderLayout.WEST);
        JPanel titles=new JPanel();titles.setOpaque(false);titles.setLayout(new BoxLayout(titles,BoxLayout.Y_AXIS));titles.add(ViewStyles.title("Soạn thư mới"));JLabel sender=ViewStyles.muted("Người gửi: "+ClientSession.username());sender.setIcon(IconManager.load("user",17,17,ViewStyles.MUTED));sender.setIconTextGap(8);titles.add(sender);header.add(titles,BorderLayout.CENTER);root.add(header,BorderLayout.NORTH);
        RoundedPanel card=new RoundedPanel(new BorderLayout(0,14),Color.WHITE,14).outline(ViewStyles.BORDER);card.setBorder(BorderFactory.createEmptyBorder(18,20,18,20));
        ViewStyles.styleInput(to);to.putClientProperty("JTextField.leadingIcon",IconManager.load("user",20,20,ViewStyles.MUTED));ViewStyles.styleInput(subject);subject.putClientProperty("JTextField.leadingIcon",IconManager.load("file-text",20,20,ViewStyles.MUTED));JPanel fields=new JPanel(new GridLayout(2,1,0,12));fields.setOpaque(false);fields.add(ViewStyles.field("Người nhận",to));fields.add(ViewStyles.field("Tiêu đề",subject));card.add(fields,BorderLayout.NORTH);
        content.setLineWrap(true);content.setWrapStyleWord(true);content.setFont(new Font("Segoe UI",Font.PLAIN,15));content.setForeground(ViewStyles.TEXT);content.setBorder(BorderFactory.createEmptyBorder(10,12,10,12));JScrollPane scroll=new JScrollPane(content);scroll.setBorder(BorderFactory.createLineBorder(ViewStyles.BORDER));card.add(ViewStyles.field("Nội dung",scroll),BorderLayout.CENTER);root.add(card,BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));actions.setOpaque(false);send.setIcon(IconManager.load("mail",20,20,Color.WHITE));send.setIconTextGap(10);actions.add(cancel);actions.add(send);root.add(actions,BorderLayout.SOUTH);cancel.addActionListener(e->dispose());send.addActionListener(e->submit());getRootPane().setDefaultButton(send);
    }

    private void submit(){
        String receiver=to.getText().trim(),body=content.getText();if(receiver.isEmpty()){warn("Vui lòng nhập người nhận.",to);return;}if(body.isBlank()){warn("Nội dung email không được để trống.",content);return;}
        setBusy(true);new SwingWorker<Void,Void>(){
            protected Void doInBackground()throws Exception{handler.send(receiver,subject.getText().trim(),body);return null;}
            protected void done(){try{get();JOptionPane.showMessageDialog(ComposeEmailDialog.this,"Email đã được gửi thành công.","Gửi email",JOptionPane.INFORMATION_MESSAGE);dispose();}catch(Exception ex){JOptionPane.showMessageDialog(ComposeEmailDialog.this,message(ex),"Không thể gửi email",JOptionPane.ERROR_MESSAGE);setBusy(false);}}
        }.execute();
    }
    private void setBusy(boolean busy){to.setEnabled(!busy);subject.setEnabled(!busy);content.setEnabled(!busy);send.setEnabled(!busy);cancel.setEnabled(!busy);send.setText(busy?"Đang gửi...":"Gửi email");}
    private void warn(String text,JComponent focus){JOptionPane.showMessageDialog(this,text,"Thông tin chưa hợp lệ",JOptionPane.WARNING_MESSAGE);focus.requestFocusInWindow();}
    private static String message(Exception ex){Throwable cause=ex.getCause();return cause==null?ex.getMessage():cause.getMessage();}
}
