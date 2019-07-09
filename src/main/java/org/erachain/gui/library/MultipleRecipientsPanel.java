package org.erachain.gui.library;


import org.erachain.lang.Lang;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.validation.constraints.Null;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class MultipleRecipientsPanel extends JPanel {
    private final Table_Model recipientsTableModel;
    private final MTable jTableRecipients;
    private JScrollPane jScrollPaneRecipients;
    private JButton jButtonAddRecipient;
    private JButton jButtonRemoveRecipient;
    private GridBagConstraints gridBagConstraints;
    private JCheckBox  allCheckBox;
    private JCheckBox  encryptCheckBox;

    public MultipleRecipientsPanel() {

        super();
        this.setName(Lang.getInstance().translate("Recipients"));
        jButtonAddRecipient = new JButton();
        jScrollPaneRecipients = new JScrollPane();
        jButtonRemoveRecipient = new JButton();
        allCheckBox = new JCheckBox();
        allCheckBox.setText(Lang.getInstance().translate("Everything"));
        allCheckBox.setSelected(true);
        encryptCheckBox = new JCheckBox();
        encryptCheckBox.setText(Lang.getInstance().translate("Encrypt"));
        encryptCheckBox.setSelected(false);
        jButtonAddRecipient.setVisible(false);
        encryptCheckBox.setVisible(false);
        jButtonRemoveRecipient.setVisible(false);


        allCheckBox.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                jButtonRemoveRecipient.setVisible(!allCheckBox.isSelected());
                jTableRecipients.setVisible(!allCheckBox.isSelected());
                encryptCheckBox.setVisible(!allCheckBox.isSelected());
            }
        });

        this.jButtonRemoveRecipient.addActionListener(new ActionListener() {
            // delete row
            @Override
            public void actionPerformed(ActionEvent e) {
                int interval=0;
                if (recipientsTableModel.getRowCount() > 0) {
                    int selRow = jTableRecipients.getSelectedRow();
                    if (selRow != -1 && recipientsTableModel.getRowCount() >= selRow) {
                        ((DefaultTableModel) recipientsTableModel).removeRow(selRow);
                        interval = selRow-1;
                        if (interval<0) interval =0;
                    }
                }
                if (recipientsTableModel.getRowCount()<1) {
                    recipientsTableModel.addRow(new Object[]{"", ""});
                    interval = 0;
                }

                jTableRecipients.setRowSelectionInterval(interval, interval);
                recipientsTableModel.fireTableDataChanged();
            }
        });
        this.setLayout(new GridBagLayout());

        jScrollPaneRecipients.setOpaque(false);
        jScrollPaneRecipients.setPreferredSize(new Dimension(0, 0));

        recipientsTableModel = new Table_Model(0);
        jTableRecipients = new MTable(recipientsTableModel);
        jTableRecipients.setVisible(false);
        jScrollPaneRecipients.setViewportView(jTableRecipients);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        this.add(allCheckBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        this.add(encryptCheckBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        this.add(jScrollPaneRecipients, gridBagConstraints);

        jButtonAddRecipient.setText(Lang.getInstance().translate("Add"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        this.add(jButtonAddRecipient, gridBagConstraints);

        jButtonRemoveRecipient.setText(Lang.getInstance().translate("Remove"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        this.add(jButtonRemoveRecipient, gridBagConstraints);


    }


    // table model class

    @SuppressWarnings("serial")
    class Table_Model extends DefaultTableModel {

        public Table_Model(int rows) {
            super(new Object[]{Lang.getInstance().translate("Address"),
                    Lang.getInstance().translate("Description")}, rows);
            this.addRow(new Object[]{"", ""});
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            //	if (column == 0) {
            return true;
            //	}
            //       return false;
        }

        public Class<? extends Object> getColumnClass(int c) {     // set column type
            Object o = getValueAt(0, c);
            return o == null ? Null.class : o.getClass();
        }


        public Object getValueAt(int row, int col) {

            if (this.getRowCount() < row || this.getRowCount() == 0) return null;


            return super.getValueAt(row, col);


        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            //IF STRING
            if (aValue instanceof String) {
                //CHECK IF NOT EMPTY
                if (((String) aValue).length() > 0) {
                    //CHECK IF LAST ROW
                    if (row == this.getRowCount() - 1) {
                        this.addRow(new Object[]{"", ""});
                    }

                    super.setValueAt(aValue, row, column);
                }
            } else {
                super.setValueAt(aValue, row, column);

                //CHECK IF LAST ROW
                if (row == this.getRowCount() - 1) {
                    this.addRow(new Object[]{"", ""});
                }
            }
        }

        public java.util.List<String> getValues(int column) {
            List<String> values = new ArrayList<String>();

            for (int i = 0; i < this.getRowCount(); i++) {
                String value = String.valueOf(this.getValueAt(i, column));

                if (value.length() > 24) values.add(value);

            }

            return values;
        }

    }

}

