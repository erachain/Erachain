package org.erachain.gui.models;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;


public class Renderer_Account_Panel extends JPanel implements TableCellRenderer {
    private static final long serialVersionUID = 1L;
    JLabel jLabel1;

    public Renderer_Account_Panel() {
        //   setLineWrap(true);
        //   setWrapStyleWord(true);
        setOpaque(true);
        JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        JPanel jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        //
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.1;
        jPanel1.add(jLabel1, gridBagConstraints);

        jScrollPane1.setViewportView(jPanel1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(jScrollPane1, gridBagConstraints);


    }

    //  @Override
    //  public Component getTableCellRendererComponent(JTable table,
    //         Object value, boolean isSelected, boolean hasFocus, int row, int column)
    //  {

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        java.awt.GridBagConstraints gridBagConstraints;
        jLabel1.setText(value.toString());

        return this;
    }
}