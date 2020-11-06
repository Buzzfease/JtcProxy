/*
 * Created by JFormDesigner on Fri Nov 06 14:22:44 CST 2020
 */

package gui;

import config.Config;
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
    }

    private void okButtonActionPerformed(ActionEvent e) {
        String loopMs = textFieldLoopMs.getText();
        String queryMs = textFiledQureyMs.getText();
        if (!loopMs.isEmpty() && !queryMs.isEmpty()){
            try {
                long lms = Long.parseLong(loopMs);
                long qms = Long.parseLong(queryMs);
                Config.INSTANCE.setLoopTime(lms);
                Config.INSTANCE.setQueryTime(qms);
                setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            }catch (Exception exception){
                exception.printStackTrace();
                labelHint.setText("请输入正确的数值");
            }
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
        panel4 = new JPanel();
        label1 = new JLabel();
        textFieldLoopMs = new JTextField();
        panel5 = new JPanel();
        label2 = new JLabel();
        textFiledQureyMs = new JTextField();
        panel6 = new JPanel();
        label3 = new JLabel();
        buttonIp = new JButton();
        labelHint = new JLabel();
        buttonBar = new JPanel();
        okButton = new JButton();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.TitledBorder(new javax.swing.border.
            EmptyBorder(0,0,0,0), "JF\u006frmDesi\u0067ner Ev\u0061luatio\u006e",javax.swing.border.TitledBorder.CENTER,javax.swing
            .border.TitledBorder.BOTTOM,new java.awt.Font("Dialo\u0067",java.awt.Font.BOLD,12),
            java.awt.Color.red),dialogPane. getBorder()));dialogPane. addPropertyChangeListener(new java.beans.PropertyChangeListener()
            {@Override public void propertyChange(java.beans.PropertyChangeEvent e){if("borde\u0072".equals(e.getPropertyName()))
            throw new RuntimeException();}});
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new GridLayout());

                //======== panel3 ========
                {
                    panel3.setLayout(new VerticalLayout(10));

                    //======== panel4 ========
                    {
                        panel4.setLayout(new GridLayout(1, 2));

                        //---- label1 ----
                        label1.setText("\u8f6e\u8be2\u95f4\u9694(ms)");
                        panel4.add(label1);

                        //---- textFieldLoopMs ----
                        textFieldLoopMs.setText("5000");
                        panel4.add(textFieldLoopMs);
                    }
                    panel3.add(panel4);

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
                        label3.setText("\u540c\u6b65\u672c\u673aIP\u81f3\u767d\u540d\u5355");
                        panel6.add(label3);

                        //---- buttonIp ----
                        buttonIp.setText("\u5f00\u59cb\u540c\u6b65");
                        panel6.add(buttonIp);
                    }
                    panel3.add(panel6);

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
                okButton.setText("\u5b8c\u6210");
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
    private JPanel panel4;
    private JLabel label1;
    private JTextField textFieldLoopMs;
    private JPanel panel5;
    private JLabel label2;
    private JTextField textFiledQureyMs;
    private JPanel panel6;
    private JLabel label3;
    private JButton buttonIp;
    private JLabel labelHint;
    private JPanel buttonBar;
    private JButton okButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
