package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;

import config.Config;
import entity.InfoResult;
import network.Network;
import org.jdesktop.swingx.*;
import utils.CommonUtil;
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
    ArrayList<String> carNoList;

    public RequestFrame() {
        initComponents();
    }

    private void setStateReady(){
        buttonDoWork.setEnabled(true);
        buttonDoWork.setText("查询");
        buttonDoManyWork.setEnabled(true);
        buttonDoManyWork.setText("批量查询");
        buttonImport.setEnabled(true);
    }

    private void setStateDisable(){
        buttonDoWork.setEnabled(false);
        buttonDoWork.setText("查询");
        buttonDoManyWork.setEnabled(false);
        buttonDoManyWork.setText("批量查询");
        buttonImport.setEnabled(false);
    }

    private void buttonDoWorkActionPerformed(ActionEvent e) {
        requestCount = 1;
        successCount = 0;
        failedCount = 0;
        final boolean isLimit = Config.INSTANCE.isLimit24();
        String handleResult = CommonUtil.INSTANCE.handleCarNo(cardNumTextFeild.getText());
        if (handleResult.equals("")){
            labelHint.setText("请输入正确的车牌号");
            return;
        }
        //start
        labelHint.setText("查询中...");
        Network.INSTANCE.querySingle(handleResult, new Network.BaseCallBack<InfoResult>() {
            @Override
            public void requestSuccess(InfoResult infoResult, int successCount, int failedCount) {
                labelHint.setText("查询成功");
                DefaultTableModel model = (DefaultTableModel) table1.getModel();
                model.setRowCount(0);

                Vector<String> v = new Vector<>();

                if (infoResult.getResultCode() == -1){
                    v.add(infoResult.getConstCarNo());
                    v.add("未入场");
                    v.add("未停车");
                    v.add("null");
                    model.addRow(v);
                }else{
                    if (isLimit){
                        long parseTime = CommonUtil.INSTANCE.parseTime(infoResult.getDataItems().get(0).getAttributes().getStartTime());
                        if (parseTime != -1 && System.currentTimeMillis() - parseTime <= 60*60*24*1000){
                            v.add(infoResult.getDataItems().get(0).getAttributes().getCarNo());
                            v.add(infoResult.getDataItems().get(0).getAttributes().getParkName());
                            v.add(infoResult.getDataItems().get(0).getAttributes().getStartTime());
                            v.add("null");
                            model.addRow(v);
                        }
                    }else{
                        v.add(infoResult.getDataItems().get(0).getAttributes().getCarNo());
                        v.add(infoResult.getDataItems().get(0).getAttributes().getParkName());
                        v.add(infoResult.getDataItems().get(0).getAttributes().getStartTime());
                        v.add("null");
                        model.addRow(v);
                    }

                }

                table1.getColumnModel().getColumn(3).setCellRenderer(new MyButtonRender());
                table1.getColumnModel().getColumn(3).setCellEditor(new MyButtonEditor(table1));
            }

            @Override
            public void requestFail(String message) {
                labelHint.setText(message);
            }

            @Override
            public void requestOnGoing(int count) {

            }
        });
    }

    private void buttonDoManyWorkActionPerformed(ActionEvent e) {
        requestCount = 1;
        successCount = 0;
        failedCount = 0;
        final boolean isLimit = Config.INSTANCE.isLimit24();
        if (carNoList == null ||carNoList.isEmpty()) {
            labelHint.setText("请导入正确的车牌数据");
            return;
        }
        //start
        labelHint.setText("查询中...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Network.INSTANCE.queryMany(carNoList, new Network.BaseCallBack<ArrayList<InfoResult>>() {
                    @Override
                    public void requestSuccess(ArrayList<InfoResult> infoResults, int successCount, int failedCount) {
                        labelHint.setText("共查询"+carNoList.size()+"条数据: "+successCount+"条查询成功,"+failedCount+"条查询失败");
                        DefaultTableModel model = (DefaultTableModel) table1.getModel();
                        model.setRowCount(0);

                        for(InfoResult bean:infoResults){
                            Vector<String> v = new Vector<>();
                            if (bean.getResultCode() == -1){
                                v.add(bean.getConstCarNo());
                                v.add("未入场");
                                v.add("未停车");
                                v.add("null");
                                model.addRow(v);
                            }else{
                                if (isLimit){
                                    long parseTime = CommonUtil.INSTANCE.parseTime(bean.getDataItems().get(0).getAttributes().getStartTime());
                                    if (parseTime != -1 && System.currentTimeMillis() - parseTime <= 60*60*24*1000){
                                        v.add(bean.getDataItems().get(0).getAttributes().getCarNo());
                                        v.add(bean.getDataItems().get(0).getAttributes().getParkName());
                                        v.add(bean.getDataItems().get(0).getAttributes().getStartTime());
                                        v.add("null");
                                        model.addRow(v);
                                    }
                                }else{
                                    v.add(bean.getDataItems().get(0).getAttributes().getCarNo());
                                    v.add(bean.getDataItems().get(0).getAttributes().getParkName());
                                    v.add(bean.getDataItems().get(0).getAttributes().getStartTime());
                                    v.add("null");
                                    model.addRow(v);
                                }
                            }
                        }
                        table1.getColumnModel().getColumn(3).setCellRenderer(new MyButtonRender());
                        table1.getColumnModel().getColumn(3).setCellEditor(new MyButtonEditor(table1));
                    }

                    @Override
                    public void requestOnGoing(int count) {
                        labelHint.setText("共"+carNoList.size()+"条数据: 已查询,"+count+"条数据");
                    }

                    @Override
                    public void requestFail(String message) {

                    }
                });
            }
        }).start();
    }

    private void checkBoxProxyStateChanged(ChangeEvent e) {
        JCheckBox checkBox = (JCheckBox) e.getSource();
        if (checkBox.isSelected()){
            Config.INSTANCE.setProxyOpen(true);
        }else {
            Config.INSTANCE.setProxyOpen(false);
        }
    }

    private void checkBoxLimitStateChanged(ChangeEvent e) {
        JCheckBox checkBox = (JCheckBox) e.getSource();
        if (checkBox.isSelected()){
            Config.INSTANCE.setLimit24(true);
        }else {
            Config.INSTANCE.setLimit24(false);
        }
    }

    /*
     * 打开文件
     */
    private void showFileOpenDialog(Component parent) {
        // 创建一个默认的文件选取器
        JFileChooser fileChooser = new JFileChooser();

        // 设置默认显示的文件夹为当前文件夹
        fileChooser.setCurrentDirectory(new File("."));

        // 设置文件选择的模式（只选文件、只选文件夹、文件和文件均可选）
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        // 设置是否允许多选
        fileChooser.setMultiSelectionEnabled(true);

        // 添加可用的文件过滤器（FileNameExtensionFilter 的第一个参数是描述, 后面是需要过滤的文件扩展名 可变参数）
        //fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("excel文件(*.xlsx)", "xlsx"));
        // 设置默认使用的文件过滤器
        fileChooser.setFileFilter(new FileNameExtensionFilter("excel文件(*.xlsx, *.xls)", "xlsx", "xls"));

        // 打开文件选择框（线程将被阻塞, 直到选择框被关闭）
        int result = fileChooser.showOpenDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            // 如果点击了"确定", 则获取选择的文件路径
            File file = fileChooser.getSelectedFile();
            setStateDisable();

            if (file.getAbsolutePath().endsWith("xlsx")){
                Thread dialogThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        carNoList = CommonUtil.INSTANCE.readExcels(file.getAbsolutePath());
                        setStateReady();
                        fileTextFiled.setText(file.getAbsolutePath());
                        
                        StringBuilder sb = new StringBuilder();
                        for(String num:carNoList){
                            sb.append(num);
                            sb.append("\n");
                        }
                        textAreaImport.setText(sb.toString());
                    }
                });
                dialogThread.start();
            }else{
                Thread dialogThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        carNoList = CommonUtil.INSTANCE.readExcel(file.getAbsolutePath());
                        setStateReady();
                        fileTextFiled.setText(file.getAbsolutePath());

                        StringBuilder sb = new StringBuilder();
                        for(String num:carNoList){
                            sb.append(num);
                            sb.append("\n");
                        }
                        textAreaImport.setText(sb.toString());
                    }
                });
                dialogThread.start();
            }
        }
    }

    private void buttonImportActionPerformed(ActionEvent e) {
        showFileOpenDialog(this);
    }

    private void buttonConfigActionPerformed(ActionEvent e) {
        ConfigFrame frame = new ConfigFrame();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - unknown
        label1 = new JLabel();
        westPanel = new JPanel();
        scrollPane1 = new JScrollPane();
        textAreaImport = new JTextArea();
        mainPanel = new JPanel();
        inputPanel = new JPanel();
        cardNumTextFeild = new JTextField();
        buttonDoWork = new JButton();
        inputPanel2 = new JPanel();
        fileTextFiled = new JTextField();
        panel2 = new JPanel();
        buttonImport = new JButton();
        buttonDoManyWork = new JButton();
        optionPanel = new JPanel();
        panel4 = new JPanel();
        checkBoxProxy = new JCheckBox();
        checkBoxLimit = new JCheckBox();
        buttonConfig = new JButton();
        labelHint = new JLabel();
        dataPanel = new JScrollPane();
        table1 = new JTable();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //---- label1 ----
        label1.setText(" ");
        contentPane.add(label1, BorderLayout.NORTH);

        //======== westPanel ========
        {
            westPanel.setMinimumSize(new Dimension(100, 200));
            westPanel.setPreferredSize(new Dimension(100, 200));
            westPanel.setBorder (new javax. swing. border. CompoundBorder( new javax .swing .border .TitledBorder (new javax. swing. border
            . EmptyBorder( 0, 0, 0, 0) , "JF\u006frm\u0044es\u0069gn\u0065r \u0045va\u006cua\u0074io\u006e", javax. swing. border. TitledBorder. CENTER, javax
            . swing. border. TitledBorder. BOTTOM, new java .awt .Font ("D\u0069al\u006fg" ,java .awt .Font .BOLD ,
            12 ), java. awt. Color. red) ,westPanel. getBorder( )) ); westPanel. addPropertyChangeListener (new java. beans
            . PropertyChangeListener( ){ @Override public void propertyChange (java .beans .PropertyChangeEvent e) {if ("\u0062or\u0064er" .equals (e .
            getPropertyName () )) throw new RuntimeException( ); }} );
            westPanel.setLayout(new GridLayout());

            //======== scrollPane1 ========
            {
                scrollPane1.setViewportView(textAreaImport);
            }
            westPanel.add(scrollPane1);
        }
        contentPane.add(westPanel, BorderLayout.WEST);

        //======== mainPanel ========
        {
            mainPanel.setLayout(new VerticalLayout(5));

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

                    //---- buttonImport ----
                    buttonImport.setText("\u6279\u91cf\u5bfc\u5165");
                    buttonImport.addActionListener(e -> buttonImportActionPerformed(e));
                    panel2.add(buttonImport);

                    //---- buttonDoManyWork ----
                    buttonDoManyWork.setText("\u6279\u91cf\u67e5\u8be2");
                    buttonDoManyWork.addActionListener(e -> buttonDoManyWorkActionPerformed(e));
                    panel2.add(buttonDoManyWork);
                }
                inputPanel2.add(panel2);
            }
            mainPanel.add(inputPanel2);

            //======== optionPanel ========
            {
                optionPanel.setLayout(new GridLayout(1, 2, 10, 0));

                //======== panel4 ========
                {
                    panel4.setLayout(new GridLayout(1, 2, 10, 0));

                    //---- checkBoxProxy ----
                    checkBoxProxy.setText("\u5f00\u542f\u4ee3\u7406");
                    checkBoxProxy.addChangeListener(e -> checkBoxProxyStateChanged(e));
                    panel4.add(checkBoxProxy);

                    //---- checkBoxLimit ----
                    checkBoxLimit.setText("\u965024\u5c0f\u65f6");
                    checkBoxLimit.addChangeListener(e -> checkBoxLimitStateChanged(e));
                    panel4.add(checkBoxLimit);
                }
                optionPanel.add(panel4);

                //---- buttonConfig ----
                buttonConfig.setText("\u914d\u7f6e");
                buttonConfig.addActionListener(e -> buttonConfigActionPerformed(e));
                optionPanel.add(buttonConfig);
            }
            mainPanel.add(optionPanel);

            //---- labelHint ----
            labelHint.setText(" ");
            mainPanel.add(labelHint);

            //======== dataPanel ========
            {
                dataPanel.setMinimumSize(new Dimension(16, 0));
                dataPanel.setPreferredSize(new Dimension(452, 300));

                //---- table1 ----
                table1.setModel(new DefaultTableModel(
                    new Object[][] {
                    },
                    new String[] {
                        "\u8f66\u724c\u53f7", "\u505c\u8f66\u573a", "\u5165\u573a\u65f6\u95f4", "\u64cd\u4f5c"
                    }
                ));
                table1.setRowSelectionAllowed(false);
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
    private JLabel label1;
    private JPanel westPanel;
    private JScrollPane scrollPane1;
    private JTextArea textAreaImport;
    private JPanel mainPanel;
    private JPanel inputPanel;
    private JTextField cardNumTextFeild;
    private JButton buttonDoWork;
    private JPanel inputPanel2;
    private JTextField fileTextFiled;
    private JPanel panel2;
    private JButton buttonImport;
    private JButton buttonDoManyWork;
    private JPanel optionPanel;
    private JPanel panel4;
    private JCheckBox checkBoxProxy;
    private JCheckBox checkBoxLimit;
    private JButton buttonConfig;
    private JLabel labelHint;
    private JScrollPane dataPanel;
    private JTable table1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
