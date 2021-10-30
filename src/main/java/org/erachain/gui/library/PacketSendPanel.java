package org.erachain.gui.library;


import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.validation.constraints.Null;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

public class PacketSendPanel extends JPanel {
    public final TableModel assetsTableModel;
    private final MTable jTableAssets;
    private JScrollPane jScrollPaneAssets;
    private JButton jButtonRemoveAsset;
    private GridBagConstraints gridBagConstraints;
    public JCheckBox defaultCheck;

    private static int NO_COL = 0;

    public PacketSendPanel() {

        super();

        this.setName(Lang.T("Assets Package"));

        jScrollPaneAssets = new JScrollPane();
        jButtonRemoveAsset = new JButton();

        defaultCheck = new JCheckBox();

        defaultCheck.setText(Lang.T("Standard"));
        defaultCheck.setSelected(true);

        defaultCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });

        this.jButtonRemoveAsset.addActionListener(new ActionListener() {
            // delete row
            @Override
            public void actionPerformed(ActionEvent e) {
                int interval = 0;
                if (assetsTableModel.getRowCount() > 0) {
                    int selRow = jTableAssets.getSelectedRow();
                    if (selRow != -1 && assetsTableModel.getRowCount() >= selRow) {
                        ((DefaultTableModel) assetsTableModel).removeRow(selRow);
                        interval = selRow - 1;
                        if (interval < 0) interval = 0;
                    }
                }

                if (assetsTableModel.getRowCount() < 1) {
                    assetsTableModel.addEmpty();
                    interval = 0;
                }

                jTableAssets.setRowSelectionInterval(interval, interval);
                assetsTableModel.fireTableDataChanged();
            }
        });
        this.setLayout(new GridBagLayout());

        assetsTableModel = new TableModel();
        jTableAssets = new MTable(assetsTableModel);
        jScrollPaneAssets.setViewportView(jTableAssets);
        TableColumn columnNo = jTableAssets.getColumnModel().getColumn(NO_COL);
        columnNo.setMinWidth(30);
        columnNo.setMaxWidth(70);
        columnNo.setPreferredWidth(50);
        columnNo.setWidth(50);
        columnNo.sizeWidthToFit();

        TableColumn columnKey = jTableAssets.getColumnModel().getColumn(NO_COL + 1);
        columnKey.setMinWidth(40);
        columnKey.setMaxWidth(70);
        columnKey.setPreferredWidth(60);
        columnKey.setWidth(60);
        columnKey.sizeWidthToFit();

        TableColumn columnAsset = jTableAssets.getColumnModel().getColumn(NO_COL + 2);
        columnAsset.setMinWidth(150);
        columnAsset.setPreferredWidth(200);
        columnAsset.setWidth(170);
        columnAsset.sizeWidthToFit();

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        ++gridBagConstraints.gridy;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.2;
        this.add(jScrollPaneAssets, gridBagConstraints);

        jButtonRemoveAsset.setText(Lang.T("Remove"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        this.add(jButtonRemoveAsset, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        //this.add(defaultCheck, gridBagConstraints);

        //reset();
        this.setMinimumSize(new Dimension(0, 160));


    }

    private void reset() {
        jScrollPaneAssets.setVisible(!defaultCheck.isSelected());
        jButtonRemoveAsset.setVisible(!defaultCheck.isSelected());
        this.setMinimumSize(new Dimension(0, !defaultCheck.isSelected() ? 130 : 30));

    }

    // table model class

    @SuppressWarnings("serial")
    public
    class TableModel extends DefaultTableModel {

        public TableModel() {
            super(new Object[]{Lang.T("No."), Lang.T("Key"), Lang.T("Asset"), Lang.T("Volume"), Lang.T("Price"), Lang.T("Discounted Price"),
                    Lang.T("Tax") + " %", Lang.T("Fee"), Lang.T("Memo")
            }, 0);
            this.addEmpty();

        }

        private void addEmpty() {
            this.addRow(new Object[]{getRowCount() + 1, 2L, "", BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, ""});
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == NO_COL || column == NO_COL + 2)
                return false;

            return true;
        }

        public Class<? extends Object> getColumnClass_(int c) {     // set column type
            if (c == 1)
                return AssetCls.class;

            Object o = getValueAt(0, c);
            return o == null ? Null.class : o.getClass();
        }


        public Object getValueAt(int row, int col) {

            if (this.getRowCount() < row || this.getRowCount() == 0)
                return null;

            if (col == NO_COL)
                return row + 1;

            if (col == 2) {
                Object value = super.getValueAt(row, 1);
                if (value != null) {
                    try {
                        return Controller.getInstance().getAsset(Long.parseLong(value.toString()));
                    } catch (Exception e) {
                    }
                }
            }

            return super.getValueAt(row, col);


        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {

            super.setValueAt(aValue, row, column);

            //CHECK IF LAST ROW
            if (row == this.getRowCount() - 1) {
                this.addEmpty();
            }
        }

        public void setRows(Object[][] items) {
            clearRows(false);

            for (int i = 0; i < items.length; ++i) {
                addRow(items[i]);
            }
            addEmpty();
        }

        public void clearRows(boolean addEmpty) {
            while (getRowCount() > 0) {
                this.removeRow(getRowCount() - 1);
            }

            if (addEmpty)
                addEmpty();

        }

        /**
         * 0: (long) AssetKey, 1: Amount, 2: Price, 3: Discounted Price, 4: Tax as percent, 5: Fee as absolute value, 6: memo, 7: Asset (after setDC())
         */

        public Object[][] getRows() {
            if (defaultCheck.isSelected())
                return null;

            Object[][] rows = new Object[getRowCount()][];
            for (int i = 0; i < getRowCount(); i++) {
                Object[] row = new Object[8];
                System.arraycopy(this.getDataVector().get(i), 1, row, 0, 7);
                rows[i] = row;
            }

            return rows;
        }

    }

}

