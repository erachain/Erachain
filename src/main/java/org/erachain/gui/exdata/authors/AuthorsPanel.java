package org.erachain.gui.exdata.authors;


import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.library.MTable;
import org.erachain.lang.Lang;
import org.mapdb.Fun;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.validation.constraints.Null;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class AuthorsPanel extends JPanel {
    public final Table_Model AuthorsTableModel;
    private final MTable jTableAuthors;
    private JScrollPane jScrollPaneAuthors;
    private JButton jButtonAddAuthor;
    private JButton jButtonRemoveAuthor;
    private GridBagConstraints gridBagConstraints;


    public AuthorsPanel() {

        super();
        this.setName(Lang.getInstance().translate("Authors"));
        jButtonAddAuthor = new JButton();
        jScrollPaneAuthors = new JScrollPane();
        jButtonRemoveAuthor = new JButton();
        jButtonAddAuthor.setVisible(false);
        jButtonRemoveAuthor.setVisible(true);




        this.jButtonRemoveAuthor.addActionListener(new ActionListener() {
            // delete row
            @Override
            public void actionPerformed(ActionEvent e) {
                int interval = 0;
                if (AuthorsTableModel.getRowCount() > 0) {
                    int selRow = jTableAuthors.getSelectedRow();
                    if (selRow != -1 && AuthorsTableModel.getRowCount() >= selRow) {
                        ((DefaultTableModel) AuthorsTableModel).removeRow(selRow);
                        interval = selRow - 1;
                        if (interval < 0) interval = 0;
                    }
                }

                if (AuthorsTableModel.getRowCount() < 1) {
                    AuthorsTableModel.addRow(new Object[]{"", ""});
                    interval = 0;
                }

                jTableAuthors.setRowSelectionInterval(interval, interval);
                AuthorsTableModel.fireTableDataChanged();
            }
        });
        this.setLayout(new GridBagLayout());

        jScrollPaneAuthors.setOpaque(false);
        jScrollPaneAuthors.setPreferredSize(new Dimension(0, 0));

        AuthorsTableModel = new Table_Model(0);
        jTableAuthors = new MTable(AuthorsTableModel);
        jTableAuthors.setVisible(true);
        jScrollPaneAuthors.setViewportView(jTableAuthors);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        this.add(jScrollPaneAuthors, gridBagConstraints);

        jButtonAddAuthor.setText(Lang.getInstance().translate("Add"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        this.add(jButtonAddAuthor, gridBagConstraints);

        jButtonRemoveAuthor.setText(Lang.getInstance().translate("Remove"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        this.add(jButtonRemoveAuthor, gridBagConstraints);


    }


    // table model class

    @SuppressWarnings("serial")
    public
    class Table_Model extends DefaultTableModel {

        public Table_Model(int rows) {
            super(new Object[]{Lang.getInstance().translate("Address"),
                            Lang.getInstance().translate("Description")
                    },
                    rows);
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
                        this.addRow(new Object[]{"", ""});
                    }

                    Fun.Tuple2<Account, String> result = Account.tryMakeAccount(address);
                    if (result.a == null) {
                        super.setValueAt(Lang.getInstance().translate(result.b), row, column + 1);
                    } else {
                        super.setValueAt(
                                Lang.getInstance().translate(Account.getDetailsForEncrypt(address, AssetCls.FEE_KEY, true)),
                                row, column + 1);
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

        public void setAuthors(Account[] Authors) {
            clearAuthors();

            for (int i = 0; i < Authors.length; ++i) {
                addRow(new Object[]{Authors[i].getAddress(), ""});
            }
        }

        public void clearAuthors() {
            while (getRowCount() > 0) {
                this.removeRow(getRowCount() - 1);
            }
            //this.addRow(new Object[]{"", ""});
        }

        public Account[] getAuthors() {
            if (getRowCount() == 0)
                return new Account[0];

            ArrayList<Account> temp = new ArrayList<>();
            for (int i = 0; i < getRowCount(); i++) {
                try {
                    //ORDINARY Author
                    String AuthorAddress = this.getValueAt(i, 0).toString();
                    if (Crypto.getInstance().isValidAddress(AuthorAddress)) {
                        temp.add(new Account(AuthorAddress));
                    } else {
                        if (PublicKeyAccount.isValidPublicKey(AuthorAddress)) {
                            temp.add(new PublicKeyAccount(AuthorAddress));
                        }
                    }
                } catch (Exception e) {
                    break;
                }
            }

            return temp.toArray(new Account[0]);
        }

    }

}

