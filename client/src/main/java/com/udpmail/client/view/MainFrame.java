package com.udpmail.client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MainFrame extends JFrame {
    public interface Handler{List<String> refresh()throws Exception;String read(String filename)throws Exception;void send(String to,String subject,String content)throws Exception;void logout();}
    private final DefaultListModel<String> model=new DefaultListModel<>();
    private final JList<String> files=new JList<>(model);
    private final JLabel countMessage=new JLabel(),actionBadge=new JLabel("Chưa chọn email"),status=new JLabel(),countStatus=new JLabel();
    private final JLabel filenameValue=valueLabel(),fromValue=valueLabel(),fromIpValue=valueLabel(),toValue=valueLabel(),subjectValue=valueLabel(),timeValue=valueLabel();
    private final JTextArea body=new JTextArea();
    private final CardLayout detailCards=new CardLayout();
    private final JPanel detailBody=new JPanel(detailCards);
    private final JButton reload=ViewStyles.secondary("Tải lại");
    private final Handler handler;private final String username;

    public MainFrame(String username,List<String> initialFiles,Handler handler){
        super("UDP Mail - Client");this.username=username;this.handler=handler;Image windowIcon=IconManager.image("mail",32,32,ViewStyles.PRIMARY);if(windowIcon!=null)setIconImage(windowIcon);setDefaultCloseOperation(EXIT_ON_CLOSE);setSize(1300,800);setMinimumSize(new Dimension(1030,650));setLocationRelativeTo(null);
        JPanel root=new JPanel(new BorderLayout());root.setBackground(ViewStyles.BACKGROUND);setContentPane(root);root.add(header(),BorderLayout.NORTH);root.add(sidebar(),BorderLayout.WEST);
        JPanel workspace=new JPanel(new BorderLayout(12,0));workspace.setOpaque(false);workspace.setBorder(new EmptyBorder(14,14,14,14));
        JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,listPanel(),detailPanel());split.setBorder(null);split.setOpaque(false);split.setDividerSize(8);split.setResizeWeight(.42);workspace.add(split);root.add(workspace,BorderLayout.CENTER);root.add(statusBar(),BorderLayout.SOUTH);
        setFiles(initialFiles,"LOGIN_SUCCESS");files.addListSelectionListener(e->{if(!e.getValueIsAdjusting()&&files.getSelectedValue()!=null)readSelected(files.getSelectedValue());});
    }

    private JComponent header(){
        JPanel header=new JPanel(new BorderLayout());header.setBackground(ViewStyles.NAVY);header.setBorder(new EmptyBorder(14,22,14,22));
        JLabel logo=new JLabel("UDP Mail",IconManager.load("mail",34,34,Color.WHITE),SwingConstants.LEFT);logo.setForeground(Color.WHITE);logo.setFont(new Font("Segoe UI",Font.BOLD,25));logo.setIconTextGap(12);header.add(logo,BorderLayout.WEST);
        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,18,5));right.setOpaque(false);JLabel connected=new JLabel("Đã kết nối",IconManager.load("circle-check-big",17,17,new Color(0x37E48B)),SwingConstants.LEFT);connected.setIconTextGap(8);connected.setForeground(new Color(0x37E48B));connected.setFont(new Font("Segoe UI",Font.BOLD,14));JLabel user=new JLabel(username,IconManager.load("user",22,22,Color.WHITE),SwingConstants.LEFT);user.setForeground(Color.WHITE);user.setFont(new Font("Segoe UI",Font.BOLD,15));user.setIconTextGap(8);
        RoundedPanel toast=new RoundedPanel(new FlowLayout(FlowLayout.CENTER,8,5),new Color(24,132,105),10).outline(new Color(0x6BE5B4));toast.add(new JLabel(IconManager.load("circle-check-big",19,19,Color.WHITE)));JLabel toastText=new JLabel("Đăng nhập thành công");toastText.setForeground(Color.WHITE);toastText.setFont(new Font("Segoe UI",Font.BOLD,13));toast.add(toastText);right.add(connected);right.add(user);right.add(toast);header.add(right,BorderLayout.EAST);
        Timer timer=new Timer(3500,e->{toast.setVisible(false);header.revalidate();});timer.setRepeats(false);timer.start();return header;
    }

    private JComponent sidebar(){
        JPanel panel=new JPanel();panel.setBackground(ViewStyles.NAVY);panel.setPreferredSize(new Dimension(220,0));panel.setBorder(new EmptyBorder(18,12,18,12));panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        JButton inbox=sideButton("Hộp thư","inbox",true),compose=sideButton("Soạn thư","pencil",false),refresh=sideButton("Làm mới danh sách","refresh-cw",false),logout=sideButton("Đăng xuất","log-out",false);
        panel.add(inbox);panel.add(Box.createVerticalStrut(8));panel.add(compose);panel.add(Box.createVerticalStrut(8));panel.add(refresh);panel.add(Box.createVerticalStrut(8));panel.add(logout);panel.add(Box.createVerticalGlue());
        JButton large=new RoundedButton("Soạn email mới",ViewStyles.PRIMARY,Color.WHITE);large.setIcon(IconManager.load("pencil",22,22,Color.WHITE));large.setIconTextGap(10);large.setMaximumSize(new Dimension(Integer.MAX_VALUE,52));large.setAlignmentX(Component.LEFT_ALIGNMENT);panel.add(large);
        compose.addActionListener(e->openCompose());large.addActionListener(e->openCompose());refresh.addActionListener(e->refreshFiles());reload.addActionListener(e->refreshFiles());logout.addActionListener(e->logout());return panel;
    }

    private JButton sideButton(String text,String iconName,boolean selected){
        JButton button=new JButton(text,IconManager.load(iconName,22,22,Color.WHITE));button.setHorizontalAlignment(SwingConstants.LEFT);button.setIconTextGap(12);button.setForeground(Color.WHITE);button.setFont(new Font("Segoe UI",Font.BOLD,14));button.setFocusPainted(false);button.setBorder(new EmptyBorder(11,14,11,14));button.setContentAreaFilled(selected);button.setBackground(selected?new Color(0x1477F8):ViewStyles.NAVY);button.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));button.setAlignmentX(Component.LEFT_ALIGNMENT);button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return button;
    }

    private JComponent listPanel(){
        RoundedPanel card=new RoundedPanel(new BorderLayout(0,12),Color.WHITE,14).outline(ViewStyles.BORDER);card.setBorder(new EmptyBorder(18,18,18,18));
        JPanel heading=new JPanel(new BorderLayout(10,0));heading.setOpaque(false);JLabel title=new JLabel("Danh sách file email",IconManager.load("file-text",25,25,ViewStyles.PRIMARY),SwingConstants.LEFT);title.setIconTextGap(10);title.setForeground(ViewStyles.TEXT);title.setFont(new Font("Segoe UI",Font.BOLD,21));heading.add(title,BorderLayout.WEST);reload.setIcon(IconManager.load("refresh-cw",18,18,ViewStyles.PRIMARY));reload.setIconTextGap(8);reload.setPreferredSize(new Dimension(110,40));heading.add(reload,BorderLayout.EAST);
        JPanel north=new JPanel();north.setOpaque(false);north.setLayout(new BoxLayout(north,BoxLayout.Y_AXIS));north.add(heading);north.add(Box.createVerticalStrut(10));JLabel path=ViewStyles.muted("Server đã mở: server_data/users/"+username+"/");path.setAlignmentX(Component.LEFT_ALIGNMENT);path.setHorizontalAlignment(SwingConstants.LEFT);path.setMaximumSize(new Dimension(Integer.MAX_VALUE,path.getPreferredSize().height));north.add(path);north.add(Box.createVerticalStrut(12));
        RoundedPanel success=new RoundedPanel(new BorderLayout(10,0),new Color(0xE7FAEF),10).outline(new Color(0xBCEBCF));success.setBorder(new EmptyBorder(10,12,10,12));success.add(new JLabel(IconManager.load("circle-check-big",22,22,ViewStyles.SUCCESS)),BorderLayout.WEST);countMessage.setForeground(new Color(0x127A39));countMessage.setFont(new Font("Segoe UI",Font.BOLD,14));success.add(countMessage,BorderLayout.CENTER);north.add(success);card.add(north,BorderLayout.NORTH);
        files.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);files.setFixedCellHeight(48);files.setBackground(Color.WHITE);files.setCellRenderer(new FileRenderer());JScrollPane scroll=new JScrollPane(files);scroll.setBorder(BorderFactory.createLineBorder(ViewStyles.BORDER));card.add(scroll,BorderLayout.CENTER);return card;
    }

    private JComponent detailPanel(){
        RoundedPanel card=new RoundedPanel(new BorderLayout(0,14),Color.WHITE,14).outline(ViewStyles.BORDER);card.setBorder(new EmptyBorder(18,18,18,18));
        JPanel heading=new JPanel(new BorderLayout(10,0));heading.setOpaque(false);JLabel title=new JLabel("Nội dung email",IconManager.load("mail",25,25,ViewStyles.NAVY),SwingConstants.LEFT);title.setIconTextGap(10);title.setFont(new Font("Segoe UI",Font.BOLD,21));title.setForeground(ViewStyles.TEXT);heading.add(title,BorderLayout.WEST);actionBadge.setOpaque(true);actionBadge.setBackground(ViewStyles.PRIMARY_PALE);actionBadge.setForeground(ViewStyles.PRIMARY);actionBadge.setBorder(new EmptyBorder(7,11,7,11));heading.add(actionBadge,BorderLayout.EAST);card.add(heading,BorderLayout.NORTH);
        detailBody.setOpaque(false);detailBody.add(emptyState("Chọn một file email để xem nội dung"),"empty");detailBody.add(emptyState("Đang tải nội dung email..."),"loading");detailBody.add(emailView(),"content");card.add(detailBody,BorderLayout.CENTER);detailCards.show(detailBody,"empty");return card;
    }

    private JComponent emailView(){
        JPanel panel=new JPanel(new BorderLayout(0,14));panel.setOpaque(false);JPanel meta=new JPanel(new GridBagLayout());meta.setOpaque(false);meta.setBorder(BorderFactory.createMatteBorder(1,0,1,0,ViewStyles.BORDER));GridBagConstraints c=new GridBagConstraints();c.gridy=0;c.insets=new Insets(7,5,3,10);c.anchor=GridBagConstraints.WEST;c.fill=GridBagConstraints.HORIZONTAL;
        addMeta(meta,c,"Tên file",filenameValue);addMeta(meta,c,"Từ",fromValue);addMeta(meta,c,"IP gửi",fromIpValue);addMeta(meta,c,"Đến",toValue);addMeta(meta,c,"Tiêu đề",subjectValue);addMeta(meta,c,"Thời gian",timeValue);panel.add(meta,BorderLayout.NORTH);
        body.setEditable(false);body.setLineWrap(true);body.setWrapStyleWord(true);body.setFont(new Font("Segoe UI",Font.PLAIN,15));body.setForeground(ViewStyles.TEXT);body.setBorder(new EmptyBorder(14,14,14,14));JScrollPane scroll=new JScrollPane(body);scroll.setBorder(BorderFactory.createLineBorder(ViewStyles.BORDER));panel.add(scroll,BorderLayout.CENTER);return panel;
    }

    private void addMeta(JPanel panel,GridBagConstraints c,String name,JLabel value){c.gridx=0;c.weightx=0;JLabel label=ViewStyles.muted(name+":");panel.add(label,c);c.gridx=1;c.weightx=1;panel.add(value,c);c.gridy++;c.insets=new Insets(3,5,3,10);}
    private JComponent emptyState(String text){JPanel panel=new JPanel(new GridBagLayout());panel.setOpaque(false);JLabel label=new JLabel(text,IconManager.load("mail",52,52,ViewStyles.BORDER),SwingConstants.CENTER);label.setHorizontalTextPosition(SwingConstants.CENTER);label.setVerticalTextPosition(SwingConstants.BOTTOM);label.setIconTextGap(14);label.setForeground(ViewStyles.MUTED);label.setFont(new Font("Segoe UI",Font.PLAIN,16));panel.add(label);return panel;}

    private JComponent statusBar(){JPanel bar=new JPanel(new BorderLayout());bar.setBackground(ViewStyles.NAVY);bar.setBorder(new EmptyBorder(9,18,9,18));status.setForeground(Color.WHITE);status.setIcon(IconManager.load("info",17,17,Color.WHITE));status.setIconTextGap(9);countStatus.setForeground(new Color(0xD3E6FF));bar.add(status,BorderLayout.WEST);bar.add(countStatus,BorderLayout.EAST);return bar;}
    private void setFiles(List<String> names,String action){model.clear();names.forEach(model::addElement);countMessage.setText("Đã nhận "+model.size()+" tên file từ Server");status.setText(action+"  •  Server trả về "+model.size()+" tên file  •  "+ViewStyles.serverAddress());countStatus.setText(model.size()+" file");}

    private void readSelected(String selected){
        actionBadge.setText("READ_EMAIL | "+selected);detailCards.show(detailBody,"loading");files.setEnabled(false);
        new SwingWorker<String,Void>(){
            protected String doInBackground()throws Exception{return handler.read(selected);}
            protected void done(){try{String raw=get();if(selected.equals(files.getSelectedValue()))showEmail(selected,raw);}catch(Exception e){actionBadge.setText("READ_EMAIL thất bại");detailCards.show(detailBody,"empty");JOptionPane.showMessageDialog(MainFrame.this,message(e),"Không thể đọc email",JOptionPane.ERROR_MESSAGE);}finally{files.setEnabled(true);}}
        }.execute();
    }
    private void showEmail(String filename,String raw){Map<String,String> values=parse(raw);filenameValue.setText(filename);fromValue.setText(values.getOrDefault("From","Không rõ"));fromIpValue.setText(values.getOrDefault("FromIP","Không rõ"));toValue.setText(values.getOrDefault("To",username));subjectValue.setText(values.getOrDefault("Subject","(Không có tiêu đề)"));timeValue.setText(values.getOrDefault("SentTime","Không rõ"));body.setText(values.getOrDefault("Body",""));body.setCaretPosition(0);detailCards.show(detailBody,"content");status.setText("READ_EMAIL  •  "+filename+"  •  "+ViewStyles.serverAddress());}
    private Map<String,String> parse(String raw){Map<String,String> result=new LinkedHashMap<>();String normalized=raw==null?"":raw.replace("\r\n","\n");int split=normalized.indexOf("\n\n");String headers=split>=0?normalized.substring(0,split):normalized;result.put("Body",split>=0?normalized.substring(split+2):"");for(String line:headers.split("\n")){int colon=line.indexOf(':');if(colon>0)result.put(line.substring(0,colon).trim(),line.substring(colon+1).trim());}return result;}

    private void refreshFiles(){reload.setEnabled(false);reload.setText("Đang tải...");runAsync(handler::refresh,names->{setFiles(names,"LIST_EMAILS");reload.setText("Tải lại");reload.setEnabled(true);},()->{reload.setText("Tải lại");reload.setEnabled(true);});}
    private void openCompose(){ComposeEmailDialog dialog=new ComposeEmailDialog(this,handler::send);dialog.setVisible(true);refreshFiles();}
    private void logout(){status.setText("Đang đăng xuất...");runAsync(()->{handler.logout();return null;},ignored->{},()->{});}
    private <T>void runAsync(Task<T> task,Consumer<T> done,Runnable finished){new SwingWorker<T,Void>(){protected T doInBackground()throws Exception{return task.run();}protected void done(){try{done.accept(get());}catch(Exception e){JOptionPane.showMessageDialog(MainFrame.this,message(e),"Lỗi",JOptionPane.ERROR_MESSAGE);}finally{finished.run();}}}.execute();}
    private static String message(Exception e){Throwable c=e.getCause();return c==null?e.getMessage():c.getMessage();}
    private static JLabel valueLabel(){JLabel label=new JLabel("—");label.setForeground(ViewStyles.TEXT);label.setFont(new Font("Segoe UI",Font.BOLD,14));return label;}
    private interface Task<T>{T run()throws Exception;}

    private static final class FileRenderer extends DefaultListCellRenderer{
        public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus){JLabel label=(JLabel)super.getListCellRendererComponent(list,value,index,selected,focus);label.setBorder(new EmptyBorder(0,13,0,13));String iconName="user.txt".equals(value)?"user":"file-text";label.setIcon(IconManager.load(iconName,22,22,selected?Color.WHITE:new Color(0x32608E)));label.setIconTextGap(11);label.setFont(new Font("Segoe UI",selected?Font.BOLD:Font.PLAIN,14));label.setBackground(selected?ViewStyles.PRIMARY:Color.WHITE);label.setForeground(selected?Color.WHITE:ViewStyles.TEXT);return label;}
    }
}
