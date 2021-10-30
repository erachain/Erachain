package org.erachain.gui.library;


import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.items.assets.ComboBoxAssetsModel;
import org.erachain.gui.models.FavoriteComboBoxModel;
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

        defaultCheck.setText(Lang.T("Use Assets Package"));
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
        TableColumn columnNo = jTableAssets.getColumnModel().getColumn(NO_COL + 1);
        columnNo.setMinWidth(80);
        columnNo.setMaxWidth(120);
        columnNo.setPreferredWidth(100);
        columnNo.setWidth(100);
        columnNo.sizeWidthToFit();

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

        reset();

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
            super(new Object[]{Lang.T("#"), Lang.T("Asset"), Lang.T("Volume"), Lang.T("Price"), Lang.T("Disconted Price"),
                    Lang.T("Tax") + " %", Lang.T("Fee"), Lang.T("Memo")
            }, 0);
            this.addEmpty();

        }

        private void addEmpty() {
            JComboBox<ItemCls> jComboBox_Asset = new JComboBox<>();
            jComboBox_Asset.setModel(new ComboBoxAssetsModel(AssetCls.FEE_KEY));
            jComboBox_Asset.setRenderer(new FavoriteComboBoxModel.IconListRenderer());

            this.addRow(new Object[]{getRowCount() + 1, jComboBox_Asset, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, ""});
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (column != NO_COL)
                return true;
            return false;
        }

        public Class<? extends Object> getColumnClass(int c) {     // set column type
            Object o = getValueAt(0, c);
            return o == null ? Null.class : o.getClass();
        }


        public Object getValueAt(int row, int col) {

            if (this.getRowCount() < row || this.getRowCount() == 0)
                return null;

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

