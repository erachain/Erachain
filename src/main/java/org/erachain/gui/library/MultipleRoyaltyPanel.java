package org.erachain.gui.library;


import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
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

public class MultipleRoyaltyPanel extends JPanel {
    public final Table_Model recipientsTableModel;
    private final MTable jTableRecipients;
    private JScrollPane jScrollPaneRecipients;
    private JButton jButtonRemoveRecipient;
    private GridBagConstraints gridBagConstraints;
    private JCheckBox defaultCheck;

    private static int DESCR_COL = 2;

    public MultipleRoyaltyPanel() {

        super();
        this.setName(Lang.T("Recipients"));
        jScrollPaneRecipients = new JScrollPane();
        jButtonRemoveRecipient = new JButton();

        defaultCheck = new JCheckBox();
        defaultCheck.setText(Lang.T("Use Default Royalty: author's award is %1").replace("%1", "10%"));
        defaultCheck.setSelected(true);
        defaultCheck.setEnabled(false);

        defaultCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });

        this.jButtonRemoveRecipient.addActionListener(new ActionListener() {
            // delete row
            @Override
            public void actionPerformed(ActionEvent e) {
                int interval = 0;
                if (recipientsTableModel.getRowCount() > 0) {
                    int selRow = jTableRecipients.getSelectedRow();
                    if (selRow != -1 && recipientsTableModel.getRowCount() >= selRow) {
                        ((DefaultTableModel) recipientsTableModel).removeRow(selRow);
                        interval = selRow - 1;
                        if (interval < 0) interval = 0;
                    }
                }

                if (recipientsTableModel.getRowCount() < 1) {
                    recipientsTableModel.addEmpty();
                    interval = 0;
                }

                jTableRecipients.setRowSelectionInterval(interval, interval);
                recipientsTableModel.fireTableDataChanged();
            }
        });
        this.setLayout(new GridBagLayout());

        recipientsTableModel = new Table_Model(0);
        jTableRecipients = new MTable(recipientsTableModel);
        jScrollPaneRecipients.setViewportView(jTableRecipients);
        TableColumn columnNo = jTableRecipients.getColumnModel().getColumn(1);
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
        this.add(jScrollPaneRecipients, gridBagConstraints);

        jButtonRemoveRecipient.setText(Lang.T("Remove"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        this.add(jButtonRemoveRecipient, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        this.add(defaultCheck, gridBagConstraints);

        reset();

    }

    private void reset() {
        jScrollPaneRecipients.setVisible(!defaultCheck.isSelected());
        jButtonRemoveRecipient.setVisible(!defaultCheck.isSelected());

        this.setMinimumSize(new Dimension(0, !defaultCheck.isSelected() ? 130 : 30));

    }

    // table model class

    @SuppressWarnings("serial")
    public
    class Table_Model extends DefaultTableModel {

        public Table_Model(int rows) {
            super(new Object[]{Lang.T("Address"), Lang.T("Royalty") + " %", Lang.T("Information")
                    },
                    rows);
            this.addEmpty();

        }

        private void addEmpty() {
            this.addRow(new Object[]{"", 1.0, ""});
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

        public void setRecipients(Object[][] items) {
            clearRecipients();

            for (int i = 0; i < items.length; ++i) {
                addRow(items[i]);
            }
        }

        public void clearRecipients() {
            while (getRowCount() > 0) {
                this.removeRow(getRowCount() - 1);
            }
        }

        public Object[] getRecipients() {
            if (defaultCheck.isSelected())
                return null;

            ArrayList<Account> temp = new ArrayList<>();
            for (int i = 0; i < getRowCount(); i++) {
                try {
                    //ORDINARY RECIPIENT
                    String recipientAddress = this.getValueAt(i, 0).toString();
                    if (Crypto.getInstance().isValidAddress(recipientAddress)) {
                        temp.add(new Account(recipientAddress));
                    } else {
                        if (PublicKeyAccount.isValidPublicKey(recipientAddress)) {
                            temp.add(new PublicKeyAccount(recipientAddress));
                        }
                    }
                } catch (Exception e) {
                }
            }

            Object[] list = new Object[temp.size()];
            for (int i = 0; i < getRowCount(); i++) {
                list[i] = new Fun.Tuple2(temp.get(i), this.getValueAt(i, 1));
            }
            return list;
        }

    }

}

