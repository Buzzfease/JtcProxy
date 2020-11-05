import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import org.jdesktop.swingx.*;
/*
 * Created by JFormDesigner on Wed Nov 04 13:38:28 CST 2020
 */

/**
 * @author unknown
 */
public class RequestFrame extends JFrame {
    int requestCount = 1;
    int successCount = 0;
    int failedCount = 0;

    public RequestFrame() {
        initComponents();
    }

    private void buttonDoWorkActionPerformed(ActionEvent e) {
        requestCount = 1;
        successCount = 0;
        failedCount = 0;

        if (Config.INSTANCE.isLoop()){
            if (Network.INSTANCE.isLoopQuerying()){
                //stop
                Network.INSTANCE.setLoopQuerying(false);
                buttonDoWork.setText("查询");
            }else{
                //start
                String handleResult = handleCarNo(cardNumTextFeild.getText());
                if (handleResult.equals("")){
                    labelHint.setText("请输入正确的车牌号");
                }else{
                    ArrayList<String> carNoList = new ArrayList<>();
                    carNoList.add(handleResult);

                    Network.INSTANCE.setLoopQuerying(true);
                    buttonDoWork.setText("停止查询");
                    labelHint.setText("查询中...");
                    Network.INSTANCE.queryLoop(carNoList, new Network.BaseCallBack<ArrayList<CarResult>>() {
                        @Override
                        public void requestSuccess(ArrayList<CarResult> carResults, int times, int successCount, int failedCount) {
                            labelHint.setText("第"+times+"轮查询结果: "+successCount+"条查询成功,"+failedCount+"条查询失败");
                            for(CarResult bean:carResults){
                                DefaultTableModel model = (DefaultTableModel) table1.getModel();
                                model.setRowCount(0);

                                Vector<String> v = new Vector<>();
                                v.add(bean.getObj().getCarNo());
                                v.add(bean.getObj().getParkName());
                                v.add(String.valueOf(bean.getObj().getTotalFee()));

                                model.addRow(v);
                            }
                        }

                        @Override
                        public void requestFail(String message) {

                        }
                    });
                }
            }
        }else{
            String handleResult = handleCarNo(cardNumTextFeild.getText());
            if (handleResult.equals("")){
                labelHint.setText("请输入正确的车牌号");
            }else{
                labelHint.setText("查询中...");
                Network.INSTANCE.querySingle(handleResult, new Network.BaseCallBack<CarResult>() {
                    @Override
                    public void requestSuccess(CarResult carResult, int times, int successCount, int failedCount) {
                        labelHint.setText("查询成功");
                        DefaultTableModel model = (DefaultTableModel) table1.getModel();
                        model.setRowCount(0);

                        Vector<String> v = new Vector<>();
                        v.add(carResult.getObj().getCarNo());
                        v.add(carResult.getObj().getParkName());
                        v.add(String.valueOf(carResult.getObj().getTotalFee()));

                        model.addRow(v);
                    }

                    @Override
                    public void requestFail(String message) {
                        labelHint.setText(message);
                    }
                });
            }
        }
    }

    private String handleCarNo(String input){
        if (input.isEmpty() || input.length() < 5){
            return "";
        }
        StringBuilder sb = new StringBuilder(input);
        if (!input.contains("-")){
            sb.insert(1,"-");
        }
        return sb.toString();
    }

    private void checkBoxRepeatStateChanged(ChangeEvent e) {
        JCheckBoxMenuItem checkBox = (JCheckBoxMenuItem) e.getSource();
        if (checkBox.isSelected()){
            Config.INSTANCE.setLoop(true);
        }else {
            Config.INSTANCE.setLoop(false);
        }
    }

    private void checkBoxProxyStateChanged(ChangeEvent e) {
        JCheckBoxMenuItem checkBox = (JCheckBoxMenuItem) e.getSource();
        if (checkBox.isSelected()){
            Config.INSTANCE.setProxyOpen(true);
        }else {
            Config.INSTANCE.setProxyOpen(false);
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - unknown
        mainPanel = new JPanel();
        panel3 = new JPanel();
        inputPanel = new JPanel();
        cardNumTextFeild = new JTextField();
        buttonDoWork = new JButton();
        inputPanel2 = new JPanel();
        fileTextFiled = new JTextField();
        panel2 = new JPanel();
        buttonInport = new JButton();
        buttonDoManyWork = new JButton();
        optionPanel = new JPanel();
        checkBoxRepeat = new JCheckBoxMenuItem();
        checkBoxProxy = new JCheckBoxMenuItem();
        labelHint = new JLabel();
        dataPanel = new JScrollPane();
        table1 = new JTable();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== mainPanel ========
        {
            mainPanel.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.TitledBorder(new javax.swing
            .border.EmptyBorder(0,0,0,0), "JFor\u006dDesi\u0067ner \u0045valu\u0061tion",javax.swing.border.TitledBorder
            .CENTER,javax.swing.border.TitledBorder.BOTTOM,new java.awt.Font("Dia\u006cog",java.
            awt.Font.BOLD,12),java.awt.Color.red),mainPanel. getBorder()))
            ;mainPanel. addPropertyChangeListener(new java.beans.PropertyChangeListener(){@Override public void propertyChange(java.beans.PropertyChangeEvent e
            ){if("bord\u0065r".equals(e.getPropertyName()))throw new RuntimeException();}})
            ;
            mainPanel.setLayout(new VerticalLayout(5));

            //======== panel3 ========
            {
                panel3.setLayout(new GridLayout(1, 1));
            }
            mainPanel.add(panel3);

            //======== inputPanel ========
            {
                inputPanel.setLayout(new GridLayout(1, 2, 10, 0));
                inputPanel.add(cardNumTextFeild);

                //---- buttonDoWork ----
                buttonDoWork.setText("\u67e5\u8be2");
                buttonDoWork.addActionListener(e -> buttonDoWorkActionPerformed(e));
                inputPanel.add(buttonDoWork);
            }
            mainPanel.add(inputPanel);

            //======== inputPanel2 ========
            {
                inputPanel2.setLayout(new GridLayout(1, 2, 10, 0));

                //---- fileTextFiled ----
                fileTextFiled.setEditable(false);
                fileTextFiled.setEnabled(false);
                inputPanel2.add(fileTextFiled);

                //======== panel2 ========
                {
                    panel2.setLayout(new GridLayout(1, 2, 10, 0));

                    //---- buttonInport ----
                    buttonInport.setText("\u6279\u91cf\u5bfc\u5165");
                    panel2.add(buttonInport);

                    //---- buttonDoManyWork ----
                    buttonDoManyWork.setText("\u6279\u91cf\u67e5\u8be2");
                    buttonDoManyWork.setEnabled(false);
                    panel2.add(buttonDoManyWork);
                }
                inputPanel2.add(panel2);
            }
            mainPanel.add(inputPanel2);

            //======== optionPanel ========
            {
                optionPanel.setLayout(new GridLayout(1, 2));

                //---- checkBoxRepeat ----
                checkBoxRepeat.setText("\u8f6e\u8be2");
                checkBoxRepeat.addChangeListener(e -> checkBoxRepeatStateChanged(e));
                optionPanel.add(checkBoxRepeat);

                //---- checkBoxProxy ----
                checkBoxProxy.setText("\u5f00\u542f\u4ee3\u7406");
                checkBoxProxy.addChangeListener(e -> checkBoxProxyStateChanged(e));
                optionPanel.add(checkBoxProxy);
            }
            mainPanel.add(optionPanel);

            //---- labelHint ----
            labelHint.setText(" ");
            mainPanel.add(labelHint);

            //======== dataPanel ========
            {

                //---- table1 ----
                table1.setModel(new DefaultTableModel(
                    new Object[][] {
                    },
                    new String[] {
                        "\u8f66\u724c\u53f7", "\u505c\u8f66\u573a", "\u7f34\u8d39\u60c5\u51b5"
                    }
                ));
                dataPanel.setViewportView(table1);
            }
            mainPanel.add(dataPanel);
        }
        contentPane.add(mainPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - unknown
    private JPanel mainPanel;
    private JPanel panel3;
    private JPanel inputPanel;
    private JTextField cardNumTextFeild;
    private JButton buttonDoWork;
    private JPanel inputPanel2;
    private JTextField fileTextFiled;
    private JPanel panel2;
    private JButton buttonInport;
    private JButton buttonDoManyWork;
    private JPanel optionPanel;
    private JCheckBoxMenuItem checkBoxRepeat;
    private JCheckBoxMenuItem checkBoxProxy;
    private JLabel labelHint;
    private JScrollPane dataPanel;
    private JTable table1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
