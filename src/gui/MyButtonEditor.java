package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * 自定义一个往列里边添加按钮的单元格编辑器。最好继承DefaultCellEditor，不然要实现的方法就太多了。
 * 
 */
public class MyButtonEditor extends DefaultCellEditor
{
 
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6546334664166791132L;
 
    private JPanel panel;
 
    private JButton button;
 
    public MyButtonEditor(JTable table)
    {
        // DefautlCellEditor有此构造器，需要传入一个，但这个不会使用到，直接new一个即可。
        super(new JTextField());
 
        // 设置点击几次激活编辑。
        this.setClickCountToStart(1);
 
        this.initButton(table);
 
        this.initPanel();
 
        // 添加按钮。
        this.panel.add(this.button);
    }
 
    private void initButton(JTable table)
    {
        this.button = new JButton();
        this.button.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(table.getModel().getValueAt(table.getSelectedRow(), 0));
                        sb.append("  ");
                        sb.append(table.getModel().getValueAt(table.getSelectedRow(), 1));
                        sb.append("  ");
                        sb.append(table.getModel().getValueAt(table.getSelectedRow(), 2));
                        System.out.println(sb.toString());
                    }
                });
 
    }
 
    private void initPanel()
    {
        this.panel = new JPanel();
        this.panel.setLayout(new GridLayout());
    }
 
 
    /**
     * 这里重写父类的编辑方法，返回一个JPanel对象即可（也可以直接返回一个Button对象，但是那样会填充满整个单元格）
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {

        //获得默认表格单元格控件
        JTextField editor = (JTextField) super.getTableCellEditorComponent(table, value, isSelected,
                row, column);
        editor.setHorizontalAlignment(SwingConstants.CENTER);
        // 只为按钮赋值即可。也可以作其它操作。
        this.button.setText("复制");
 
        return this.panel;
    }
 
    /**
     * 重写编辑单元格时获取的值。如果不重写，这里可能会为按钮设置错误的值。
     */
    @Override
    public Object getCellEditorValue()
    {
        return this.button.getText();
    }
 
}
