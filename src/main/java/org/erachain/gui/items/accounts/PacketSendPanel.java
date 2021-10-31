package org.erachain.gui.items.accounts;


import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.RenderComboBoxViewBalance;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.Vector;

public class PacketSendPanel extends JPanel {
    public final TableModel assetsTableModel;
    private final MTable jTableAssets;
    private JScrollPane jScrollPaneAssets;
    private JButton jButtonRemoveAsset;
    private GridBagConstraints gridBC;
    public JCheckBox defaultCheck;
    public JComboBox<Integer> jComboBoxAction;

    public PacketSendPanel() {

        super();

        this.setName(Lang.T("Assets Package"));

        jScrollPaneAssets = new JScrollPane();
        jButtonRemoveAsset = new JButton();

        jComboBoxAction = new JComboBox<>();
        jComboBoxAction.setModel(new DefaultComboBoxModel(new Integer[]{
                TransactionAmount.ACTION_SEND,
                TransactionAmount.ACTION_DEBT,
                TransactionAmount.ACTION_HOLD,
                TransactionAmount.ACTION_SPEND,
                TransactionAmount.ACTION_PLEDGE,
        }));
        jComboBoxAction.setRenderer(new RenderComboBoxViewBalance());

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
        //jTableAssets.setDefaultRenderer(Object.class, new RendererRight());
        jScrollPaneAssets.setViewportView(jTableAssets);
        TableColumn columnNo = jTableAssets.getColumnModel().getColumn(TableModel.NO_COL);
        columnNo.setMinWidth(30);
        columnNo.setMaxWidth(70);
        columnNo.setPreferredWidth(50);
        columnNo.setWidth(50);
        columnNo.sizeWidthToFit();

        TableColumn columnKey = jTableAssets.getColumnModel().getColumn(TableModel.ASSET_COL - 1);
        columnKey.setMinWidth(40);
        columnKey.setMaxWidth(70);
        columnKey.setPreferredWidth(60);
        columnKey.setWidth(60);
        columnKey.sizeWidthToFit();

        TableColumn columnAsset = jTableAssets.getColumnModel().getColumn(TableModel.ASSET_COL);
        columnAsset.setMinWidth(150);
        columnAsset.setPreferredWidth(200);
        columnAsset.setWidth(170);
        columnAsset.sizeWidthToFit();

        int gridy = 0;
        gridBC = new GridBagConstraints();
        JLabel actionLabel = new JLabel(Lang.T("Action") + ":");
        gridBC.gridx = 1;
        this.add(actionLabel, gridBC);

        ++gridBC.gridx;
        this.add(jComboBoxAction, gridBC);

        jButtonRemoveAsset.setText(Lang.T("Remove Row"));
        ++gridBC.gridx;
        gridBC.insets = new Insets(8, 28, 8, 8);
        this.add(jButtonRemoveAsset, gridBC);

        gridBC = new GridBagConstraints();
        gridBC.gridwidth = 9;
        gridBC.gridheight = 3;
        gridBC.weightx = 0.2;
        gridBC.weighty = 0.2;
        gridBC.gridx = 0;
        gridBC.fill = GridBagConstraints.BOTH;
        gridBC.gridy = ++gridy;
        this.add(jScrollPaneAssets, gridBC);

        if (false) {
            gridBC = new GridBagConstraints();
            gridBC.gridx = 2;
            gridBC.insets = new Insets(8, 8, 8, 8);
            this.add(defaultCheck, gridBC);
        }

        //reset();
        this.setMinimumSize(new Dimension(0, 160));


    }

    private void reset() {
    }

    // table model class

    @SuppressWarnings("serial")
    public
    class TableModel extends DefaultTableModel {

        static final int NO_COL = 0;
        static final int ASSET_COL = 2;
        static final int MEMO_COL = 8;

        public TableModel() {
            super(new Object[]{Lang.T("No."), Lang.T("Key"), Lang.T("Asset"), Lang.T("Volume"), Lang.T("Price"), Lang.T("Discounted Price"),
                    Lang.T("Tax # налог") + " %", Lang.T("Fee"), Lang.T("Memo")
            }, 0);
            this.addEmpty();

        }

        private void addEmpty() {
            this.addRow(new Object[]{0, 2L, BlockChain.FEE_ASSET, null, null, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, ""});
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == NO_COL || column == ASSET_COL)
                return false;

            return true;
        }

        public Class<? extends Object> getColumnClass(int c) {     // set column type

            switch (c) {
                case NO_COL:
                    return Integer.class;
                case ASSET_COL - 1:
                    return Long.class;
                case ASSET_COL:
                    return AssetCls.class;
                case MEMO_COL:
                    return String.class;
                default:
                    return BigDecimal.class;
            }
        }


        public Object getValueAt(int row, int col) {

            if (this.getRowCount() < row || this.getRowCount() == 0)
                return null;

            if (col == NO_COL)
                return row + 1;

            return super.getValueAt(row, col);


        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {

            if (column == ASSET_COL - 1) {
                Long key = (Long) aValue;
                super.setValueAt(Controller.getInstance().getAsset(key), row, ASSET_COL);
            }

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

            Vector lastRow = (Vector) this.getDataVector().get(getRowCount() - 1);
            int len = lastRow.get(ASSET_COL) == null || lastRow.get(ASSET_COL + 2) == null ? getRowCount() - 1 : getRowCount();
            Object[][] rows = new Object[len][];
            for (int i = 0; i < len; i++) {
                Vector dataRow = (Vector) this.getDataVector().get(i);
                Object[] row = new Object[8];
                row[0] = dataRow.get(ASSET_COL - 1);
                row[1] = dataRow.get(ASSET_COL + 1);
                row[2] = dataRow.get(ASSET_COL + 2);
                row[3] = dataRow.get(ASSET_COL + 3);
                row[4] = dataRow.get(ASSET_COL + 4);
                row[5] = dataRow.get(ASSET_COL + 5);
                row[6] = dataRow.get(ASSET_COL + 6);
                row[7] = dataRow.get(ASSET_COL);
                rows[i] = row;
            }

            return rows;
        }

    }

}

