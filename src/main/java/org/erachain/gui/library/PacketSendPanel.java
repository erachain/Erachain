package org.erachain.gui.library;


import org.erachain.core.account.Account;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLinkAddress;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetType;
import org.erachain.lang.Lang;
import org.mapdb.Fun;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.validation.constraints.Null;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class PacketSendPanel extends JPanel {
    public final TableModel assetsTableModel;
    private final MTable jTableAssets;
    private JScrollPane jScrollPaneAssets;
    private JButton jButtonRemoveAsset;
    private GridBagConstraints gridBagConstraints;
    public JCheckBox defaultCheck;

    protected JComboBox<Account> ownerComboBox;
    protected JComboBox<AssetType> assetTypeJComboBox;

    private static int DESCR_COL = 1;

    public PacketSendPanel() {

        super();

        this.setName(Lang.T("Assets Package"));

        this.ownerComboBox = ownerComboBox;
        this.assetTypeJComboBox = assetTypeJComboBox;

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

        assetsTableModel = new TableModel(0);
        jTableAssets = new MTable(assetsTableModel);
        jScrollPaneAssets.setViewportView(jTableAssets);
        TableColumn columnNo = jTableAssets.getColumnModel().getColumn(DESCR_COL + 1);
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
        this.add(defaultCheck, gridBagConstraints);

        reset();

    }

    private void reset() {
        jScrollPaneAssets.setVisible(!defaultCheck.isSelected());
        jButtonRemoveAsset.setVisible(!defaultCheck.isSelected());
        this.setMinimumSize(new Dimension(0, !defaultCheck.isSelected() ? 130 : 30));

        if (!defaultCheck.isSelected()) {
            ExLinkAddress[] items = AssetCls.getDefaultDEXAwards(((AssetType) assetTypeJComboBox.getSelectedItem()).getId(),
                    (Account) ownerComboBox.getSelectedItem());
            if (items == null) {
                assetsTableModel.clearRows(true);
            } else {
                assetsTableModel.setRows(items);
            }
        }

    }

    // table model class

    @SuppressWarnings("serial")
    public
    class TableModel extends DefaultTableModel {

        public TableModel(int rows) {
            super(new Object[]{Lang.T("Address"), Lang.T("Information"), Lang.T("Royalty") + " %",
                            Lang.T("Memo")
                    },
                    rows);
            this.addEmpty();

        }

        private void addEmpty() {
            this.addRow(new Object[]{"", "", 1.000d, ""});
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (column != DESCR_COL)
                return true;
            return false;
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
                String address = (String) aValue;
                if (!address.isEmpty()) {
                    //CHECK IF LAST ROW
                    if (row == this.getRowCount() - 1) {
                        this.addEmpty();
                    }

                    Fun.Tuple2<Account, String> result = Account.tryMakeAccount(address);
                    if (result.a == null) {
                        super.setValueAt(Lang.T(result.b), row, DESCR_COL);
                    } else {
                        super.setValueAt(
                                Lang.T(Account.getDetailsForEncrypt(address, AssetCls.FEE_KEY, false, true)),
                                row, DESCR_COL);
                    }

                    super.setValueAt(aValue, row, column);
                }
            } else {
                super.setValueAt(aValue, row, column);

                //CHECK IF LAST ROW
                if (row == this.getRowCount() - 1) {
                    this.addEmpty();
                }
            }
        }

        public void setRows(ExLinkAddress[] items) {
            clearRows(false);

            for (int i = 0; i < items.length; ++i) {
                addRow(new Object[]{items[i].getAccount().getAddress(),
                        Lang.T(Account.getDetailsForEncrypt(items[i].getAccount().getAddress(), AssetCls.FEE_KEY, false, true)),
                        items[i].getValue1() / 1000d, items[i].getMemo()});
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

        public ExLinkAddress[] getRows() {
            if (defaultCheck.isSelected())
                return null;

            ArrayList<ExLinkAddress> list = new ArrayList<>();
            for (int i = 0; i < getRowCount(); i++) {
                String address = (String) this.getValueAt(i, 0);
                if (address == null || address.isEmpty())
                    continue;
                try {
                    //ORDINARY ASSET
                    if (Crypto.getInstance().isValidAddress(address)) {
                        list.add(new ExLinkAddress(new Account(address),
                                (int) ((double) this.getValueAt(i, 2) * 1000.0d),
                                (String) this.getValueAt(i, 3)));
                    }
                } catch (Exception e) {
                }
            }

            return list.toArray(new ExLinkAddress[0]);
        }

    }

}

