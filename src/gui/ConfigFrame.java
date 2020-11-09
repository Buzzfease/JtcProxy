/*
 * Created by JFormDesigner on Fri Nov 06 14:22:44 CST 2020
 */

package gui;

import config.AppConfig;
import config.Config;
import entity.WhiteListResult;
import network.Network;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author unknown
 */
public class ConfigFrame extends JFrame {
    public ConfigFrame() {
        initComponents();
        AppConfig config = Config.INSTANCE.getAppConfig();
        String queryMs = String.valueOf(config.getQueryTime());
        String ip = config.getIp();
        String appKey = config.getAppKey();
        textFiledQureyMs.setText(queryMs);
        textFiledIp.setText(ip);
        textFeildAppKey.setText(appKey);
    }

    private void okButtonActionPerformed(ActionEvent e) {
        String queryMs = textFiledQureyMs.getText();
        String ip = textFiledIp.getText();
        String appKey = textFeildAppKey.getText();
        AppConfig config = new AppConfig();

        if (!queryMs.isEmpty() && !ip.isEmpty() && !appKey.isEmpty()){
            try {
                long qms = Long.parseLong(queryMs);
                config.setAppKey(appKey);
                config.setQueryTime(qms);
                config.setIp(ip);
            }catch (Exception exception){
                exception.printStackTrace();
                labelHint.setText("请输入正确的数值");
            }finally {
                Config.INSTANCE.saveAppConfig(config);
            }

            Network.INSTANCE.getWhiteList(new Network.BaseCallBack<WhiteListResult>() {
                @Override
                public void requestSuccess(WhiteListResult whiteListResult, int successCount, int failedCount) {
                    String id = null;
                    if (!whiteListResult.getData().isEmpty()){
                        id = whiteListResult.getData().get(0).getId();
                    }
                    Network.INSTANCE.addWhiteList(id, new Network.BaseCallBack<WhiteListResult>() {
                        @Override
                        public void requestSuccess(WhiteListResult whiteListResult, int successCount, int failedCount) {
                            //关闭窗口
                            //setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            ConfigFrame.this.dispose();
                        }

                        @Override
                        public void requestFail(String message) {
                            JOptionPane.showMessageDialog(ConfigFrame.this, "同步白名单失败，请检查您的豌豆Appkey", "失败",JOptionPane.WARNING_MESSAGE);
                        }

                        @Override
                        public void requestOnGoing(int count) {

                        }
                    });
                }

                @Override
                public void requestFail(String message) {
                    JOptionPane.showMessageDialog(ConfigFrame.this, "同步白名单失败，请检查您的豌豆Appkey", "失败",JOptionPane.WARNING_MESSAGE);
                }

                @Override
                public void requestOnGoing(int count) {

                }
            });
        }else{
            labelHint.setText("请输入正确的数值");
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - unknown
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        panel3 = new JPanel();
        panel5 = new JPanel();
        label2 = new JLabel();
        textFiledQureyMs = new JTextField();
        panel6 = new JPanel();
        label3 = new JLabel();
        textFiledIp = new JTextField();
        panel7 = new JPanel();
        label4 = new JLabel();
        textFeildAppKey = new JTextField();
        panel8 = new JPanel();
        label5 = new JLabel();
        buttonUrl = new JButton();
        labelHint = new JLabel();
        buttonBar = new JPanel();
        okButton = new JButton();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setBorder (new javax. swing. border. CompoundBorder( new javax .swing .border .TitledBorder (new javax. swing.
            border. EmptyBorder( 0, 0, 0, 0) , "JF\u006frmDes\u0069gner \u0045valua\u0074ion", javax. swing. border. TitledBorder. CENTER
            , javax. swing. border. TitledBorder. BOTTOM, new java .awt .Font ("D\u0069alog" ,java .awt .Font
            .BOLD ,12 ), java. awt. Color. red) ,dialogPane. getBorder( )) ); dialogPane. addPropertyChangeListener (
            new java. beans. PropertyChangeListener( ){ @Override public void propertyChange (java .beans .PropertyChangeEvent e) {if ("\u0062order"
            .equals (e .getPropertyName () )) throw new RuntimeException( ); }} );
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new GridLayout());

                //======== panel3 ========
                {
                    panel3.setLayout(new VerticalLayout(10));

                    //======== panel5 ========
                    {
                        panel5.setLayout(new GridLayout(1, 2));

                        //---- label2 ----
                        label2.setText("\u67e5\u8be2\u95f4\u9694(ms)");
                        panel5.add(label2);

                        //---- textFiledQureyMs ----
                        textFiledQureyMs.setText("100");
                        panel5.add(textFiledQureyMs);
                    }
                    panel3.add(panel5);

                    //======== panel6 ========
                    {
                        panel6.setLayout(new GridLayout(1, 2));

                        //---- label3 ----
                        label3.setText("\u672c\u673aIP");
                        panel6.add(label3);

                        //---- textFiledIp ----
                        textFiledIp.setText("117.35.132.242");
                        panel6.add(textFiledIp);
                    }
                    panel3.add(panel6);

                    //======== panel7 ========
                    {
                        panel7.setLayout(new GridLayout(1, 2));

                        //---- label4 ----
                        label4.setText("\u8c4c\u8c46AppKey");
                        panel7.add(label4);

                        //---- textFeildAppKey ----
                        textFeildAppKey.setText("dc7f645191c1ee1eb679d922d0885ac5");
                        panel7.add(textFeildAppKey);
                    }
                    panel3.add(panel7);

                    //======== panel8 ========
                    {
                        panel8.setLayout(new GridLayout(1, 2));

                        //---- label5 ----
                        label5.setText(" ");
                        panel8.add(label5);

                        //---- buttonUrl ----
                        buttonUrl.setText("\u8c4c\u8c46\u5b98\u7f51");
                        panel8.add(buttonUrl);
                    }
                    panel3.add(panel8);

                    //---- labelHint ----
                    labelHint.setText(" ");
                    panel3.add(labelHint);
                }
                contentPanel.add(panel3);
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0};

                //---- okButton ----
                okButton.setText("\u4fdd\u5b58");
                okButton.addActionListener(e -> okButtonActionPerformed(e));
                buttonBar.add(okButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - unknown
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JPanel panel3;
    private JPanel panel5;
    private JLabel label2;
    private JTextField textFiledQureyMs;
    private JPanel panel6;
    private JLabel label3;
    private JTextField textFiledIp;
    private JPanel panel7;
    private JLabel label4;
    private JTextField textFeildAppKey;
    private JPanel panel8;
    private JLabel label5;
    private JButton buttonUrl;
    private JLabel labelHint;
    private JPanel buttonBar;
    private JButton okButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
