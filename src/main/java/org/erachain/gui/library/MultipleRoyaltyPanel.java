package org.erachain.gui.library;


import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.lang.Lang;
import org.mapdb.Fun;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.validation.constraints.Null;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MultipleRoyaltyPanel extends JPanel {
    public final Table_Model recipientsTableModel;
    private final MTable jTableRecipients;
    private JScrollPane jScrollPaneRecipients;
    private JButton jButtonAddRecipient;
    private JButton jButtonRemoveRecipient;
    private GridBagConstraints gridBagConstraints;
    private JCheckBox withoutCheckBox;
    public JCheckBox signCanRecipientsCheckBox;

    public MultipleRoyaltyPanel() {

        super();
        this.setName(Lang.T("Recipients"));
        jButtonAddRecipient = new JButton();
        jScrollPaneRecipients = new JScrollPane();
        jButtonRemoveRecipient = new JButton();

        withoutCheckBox = new JCheckBox();
        withoutCheckBox.setText(Lang.T("Without Recipients"));
        withoutCheckBox.setSelected(false);
        withoutCheckBox.setVisible(false);

        signCanRecipientsCheckBox = new JCheckBox(Lang.T("-"));
        signCanRecipientsCheckBox.setSelected(true);
        signCanRecipientsCheckBox.setVisible(true);
        signCanRecipientsCheckBox.setVisible(false);

        jButtonAddRecipient.setVisible(false);
        jButtonRemoveRecipient.setVisible(true);

        withoutCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jButtonRemoveRecipient.setVisible(!withoutCheckBox.isSelected());
                jTableRecipients.setVisible(!withoutCheckBox.isSelected());
                signCanRecipientsCheckBox.setVisible(!withoutCheckBox.isSelected());
                jButtonRemoveRecipient.setVisible(!withoutCheckBox.isSelected());
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
        jTableRecipients.setVisible(true);
        jTableRecipients.setMinimumSize(new Dimension(0, 100));
        jScrollPaneRecipients.setViewportView(jTableRecipients);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        this.add(withoutCheckBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        this.add(signCanRecipientsCheckBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.2;
        this.add(jScrollPaneRecipients, gridBagConstraints);

        jButtonAddRecipient.setText(Lang.T("Add"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        this.add(jButtonAddRecipient, gridBagConstraints);

        jButtonRemoveRecipient.setText(Lang.T("Remove"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        this.add(jButtonRemoveRecipient, gridBagConstraints);


    }


    // table model class

    @SuppressWarnings("serial")
    public
    class Table_Model extends DefaultTableModel {

        public Table_Model(int rows) {
            super(new Object[]{Lang.T("Address"),
                            Lang.T("Description")
                    },
                    rows);
            this.addEmpty();

        }

        private void addEmpty() {
            this.addRow(new Object[]{"", ""});
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == 0)
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
                        super.setValueAt(Lang.T(result.b), row, column + 1);
                    } else {
                        super.setValueAt(
                                Lang.T(Account.getDetailsForEncrypt(address, AssetCls.FEE_KEY, true, true)),
                                row, column + 1);
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

        public void setRecipients(Account[] recipients) {
            clearRecipients();

            for (int i = 0; i < recipients.length; ++i) {
                addRow(new Object[]{recipients[i].getAddress(), ""});
            }
        }

        public void clearRecipients() {
            while (getRowCount() > 0) {
                this.removeRow(getRowCount() - 1);
            }
        }

        public Account[] getRecipients() {
            if (withoutCheckBox.isSelected() || getRowCount() == 0)
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

            return temp.toArray(new Account[0]);
        }

    }

}

