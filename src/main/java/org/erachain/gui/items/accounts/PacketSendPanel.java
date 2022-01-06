package org.erachain.gui.items.accounts;


import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.gui.items.assets.ComboBoxAssetsModel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.FavoriteComboBoxModel;
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
    public JCheckBox jCheck_Backward;
    public JComboBox<Integer> jComboBoxAction;
    public JComboBox<ItemCls> jComboBox_PriceAsset;
    private AccountAssetActionPanelCls parentPanel;
    private RSend parentTX;

    public PacketSendPanel(AccountAssetActionPanelCls parentPanel, RSend parentTX) {

        super();

        this.parentPanel = parentPanel;
        this.parentTX = parentTX;

        int balancePos;
        boolean backward;
        if (parentTX == null) {
            balancePos = parentPanel.action;
            backward = parentPanel.backward;

        } else {
            balancePos = parentTX.balancePosition();
            backward = parentTX.isBackward();
        }

        this.setName(Lang.T("list of Assets"));

        setBorder(BorderFactory.createEtchedBorder());

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

        if (balancePos != 0) {
            jComboBoxAction.setSelectedItem(balancePos);
            jComboBoxAction.setEnabled(false);
        }

        jCheck_Backward = new JCheckBox();

        jCheck_Backward.setText(Lang.T("Backward"));
        jCheck_Backward.setSelected(backward);
        jCheck_Backward.setEnabled(false);

        jCheck_Backward.addActionListener(new ActionListener() {
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

        TableColumn columnAction = jTableAssets.getColumnModel().getColumn(TableModel.ACTION_COL);
        columnAction.setMinWidth(130);
        columnAction.setPreferredWidth(170);
        columnAction.setWidth(150);
        columnAction.sizeWidthToFit();

        int gridy = 0;
        JLabel actionLabel = new JLabel(Lang.T("Action") + ":");
        gridBC = new GridBagConstraints();
        gridBC.gridx = 0;
        gridBC.anchor = GridBagConstraints.EAST;
        this.add(actionLabel, gridBC);

        ++gridBC.gridx;
        gridBC.anchor = GridBagConstraints.WEST;
        this.add(jComboBoxAction, gridBC);

        ++gridBC.gridx;
        gridBC.insets = new Insets(0, 28, 0, 8);
        this.add(jCheck_Backward, gridBC);

        jButtonRemoveAsset.setText(Lang.T("Remove Row"));
        ++gridBC.gridx;
        gridBC.anchor = GridBagConstraints.CENTER;
        gridBC.insets = new Insets(0, 28, 0, 8);
        this.add(jButtonRemoveAsset, gridBC);

        JLabel jLabel_PriceAsset = new JLabel(Lang.T("Price Asset") + ":");
        gridBC.gridy = ++gridy;
        gridBC.gridx = 0;
        gridBC.anchor = GridBagConstraints.EAST;
        this.add(jLabel_PriceAsset, gridBC);

        // favorite combo box
        jComboBox_PriceAsset = new JComboBox<>();
        jComboBox_PriceAsset.setModel(new ComboBoxAssetsModel(AssetCls.FEE_KEY));
        jComboBox_PriceAsset.setRenderer(new FavoriteComboBoxModel.IconListRenderer());
        ++gridBC.gridx;
        gridBC.gridwidth = 4;
        gridBC.weightx = 0.7;
        gridBC.anchor = GridBagConstraints.WEST;
        gridBC.fill = GridBagConstraints.BOTH;
        this.add(jComboBox_PriceAsset, gridBC);

        gridBC = new GridBagConstraints();
        gridBC.gridwidth = 9;
        gridBC.gridheight = 3;
        gridBC.weightx = 0.2;
        gridBC.weighty = 0.2;
        gridBC.gridx = 0;
        gridBC.fill = GridBagConstraints.BOTH;
        gridBC.gridy = ++gridy;
        this.add(jScrollPaneAssets, gridBC);

        if (parentTX != null) {
            for (Object[] packetRow : parentTX.getPacket()) {
                Object[] row = new Object[10];
                row[1] = packetRow[0];
                row[2] = packetRow[7];
                row[4] = packetRow[1];
                row[5] = packetRow[2];
                row[6] = packetRow[3];
                row[7] = packetRow[4];
                row[8] = packetRow[5];
                row[9] = packetRow[6];
                assetsTableModel.addRow(row);
            }

            jComboBox_PriceAsset.setSelectedItem(parentTX.getAsset());
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
        static final int ACTION_COL = 3;
        static final int MEMO_COL = 9;

        public TableModel() {
            super(new Object[]{Lang.T("No."), Lang.T("Key"), Lang.T("Asset"), Lang.T("Action"), Lang.T("Volume"), Lang.T("Price"), Lang.T("Discounted Price"),
                    Lang.T("Tax # налог") + " %", Lang.T("Fee"), Lang.T("Memo")
            }, 0);
            this.addEmpty();

        }

        private void addEmpty() {
            if (parentTX == null)
                this.addRow(new Object[]{0, 2L, BlockChain.FEE_ASSET, "", null, null, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, ""});
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (parentTX != null || column == NO_COL || column == ASSET_COL || column == ACTION_COL)
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
                case ACTION_COL:
                case MEMO_COL:
                    return String.class;
                default:
                    return BigDecimal.class;
            }
        }

        public Object getValueAt(int row, int col) {

            if (this.getRowCount() < row || this.getRowCount() == 0)
                return null;

            switch (col) {
                case NO_COL:
                    return row + 1;
                case ACTION_COL: {
                    AssetCls asset = (AssetCls) getValueAt(row, ASSET_COL);
                    if (asset == null)
                        return null;

                    return Lang.T(asset.viewAssetTypeAction(
                            jCheck_Backward.isSelected(), (int) jComboBoxAction.getSelectedItem(),
                            (parentTX == null ? parentPanel.creator : parentTX.getCreator())
                                    .equals(asset.getMaker())));
                }
            }

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

        /**
         * 0: (long) AssetKey, 1: Amount, 2: Price, 3: Discounted Price, 4: Tax as percent, 5: Fee as absolute value, 6: memo, 7: Asset (after setDC())
         */

        public Object[][] getRows() {

            Vector lastRow = (Vector) this.getDataVector().get(getRowCount() - 1);
            BigDecimal volumeLast = (BigDecimal) lastRow.get(ACTION_COL + 1);
            int len = lastRow.get(ASSET_COL) == null || volumeLast == null || volumeLast.signum() == 0 ? getRowCount() - 1 : getRowCount();
            Object[][] rows = new Object[len][];
            for (int i = 0; i < len; i++) {
                Vector dataRow = (Vector) this.getDataVector().get(i);
                Object[] row = new Object[8];
                row[0] = dataRow.get(ASSET_COL - 1);
                row[1] = dataRow.get(ACTION_COL + 1);
                row[2] = dataRow.get(ACTION_COL + 2);
                row[3] = dataRow.get(ACTION_COL + 3);
                row[4] = dataRow.get(ACTION_COL + 4);
                row[5] = dataRow.get(ACTION_COL + 5);
                row[6] = dataRow.get(ACTION_COL + 6);
                row[7] = dataRow.get(ASSET_COL);
                rows[i] = row;
            }

            return rows;
        }

    }

}

