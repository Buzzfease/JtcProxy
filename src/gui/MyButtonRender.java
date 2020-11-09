package gui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;


/**
 * @author FeianLing
 * @date 2019/9/10
 */
public class MyButtonRender implements TableCellRenderer {
  private JPanel jPanel;
  private JButton jButton;

  public MyButtonRender() {
    initJPanel();
    initButton();
      jPanel.add(jButton);
  }

  private void initButton() {
      jButton = new JButton();
  }

  private void initJPanel() {
      jPanel = new JPanel();
      jPanel.setLayout(new GridLayout());
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      jButton.setText("复制");
    return jPanel;
  }

}