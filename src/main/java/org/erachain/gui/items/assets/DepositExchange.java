package org.erachain.gui.items.assets;

import org.erachain.api.ApiErrorFactory;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.library.MButton;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.models.FundTokensComboBoxModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.erachain.ntp.NTP;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple4;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

//public class PersonConfirm extends JDialog { // InternalFrame  {
public class DepositExchange extends JPanel {

    // private JComboBox<Account> accountLBox;

    // private static final long serialVersionUID = 1L;
    private static final long serialVersionUID = 2717571093561259483L;
    // Variables declaration - do not modify
    private MButton jButton_Cansel;
    private MButton jButton_Confirm;
    private JComboBox<Account> jComboBox_YourAddress;
    public JComboBox<AssetCls> cbxAssets;
    private JTextField jFormattedTextField_Fee;
    private JTextField jTextField_addDays;
    private JLabel jLabel_Address;
    private JLabel jLabel_Adress_Check;
    private JLabel jLabel_Asset;
    private JScrollPane paneAssetInfo;
    private JLabel jLabel_Title;
    private JLabel jLabel_addDays;
    private JLabel jLabel_addDays_Check;
    private JLabel jLabel_YourAddress;
    //private JTextField jTextField_Address1;
    private JTextField jTextField_Address = new JTextField();

    public DepositExchange(AssetCls asset, Account account) {

        initComponents(asset, account);
        this.setVisible(true);
    }

    private void refreshReceiverDetails(JTextField pubKeyTxt, JLabel pubKeyDetails) {
        String toValue = pubKeyTxt.getText();

        // CHECK IF RECIPIENT IS VALID ADDRESS
        boolean isValid = false;
        try {
            isValid = !toValue.isEmpty() && PublicKeyAccount.isValidPublicKey(toValue);
        } catch (Exception e) {
        }

        if (!isValid) {
            pubKeyDetails.setText(ApiErrorFactory.getInstance().messageError(Transaction.INVALID_ADDRESS));
            return;
        }

        PublicKeyAccount account = new PublicKeyAccount(toValue);
        // SHOW PubKey for BANK
        String personDetails = "+" + account.getBase32() + "<br>";

        if (false && Controller.getInstance().getStatus() != Controller.STATUS_OK) {
            pubKeyDetails.setText("<html>" + personDetails
                    + Lang.getInstance().translate("Status must be OK to show public key details.") + "</html>");
            return;
        }

        // SHOW account for FEE asset
        Tuple4<Long, Integer, Integer, Integer> addressDuration = account.getPersonDuration(DCSet.getInstance());

        if (addressDuration == null) {
            personDetails += "<b>" + Lang.getInstance().translate("Ready for personalize") + "</b>";
        } else {
            // TEST TIME and EXPIRE TIME
            long current_time = NTP.getTime();

            // TEST TIME and EXPIRE TIME
            int daysLeft = addressDuration.b - (int) (current_time / (long) 86400000);
            if (daysLeft < 0)
                personDetails += Lang.getInstance().translate("Personalize ended %days% ago").replace("%days%",
                        "" + daysLeft);
            else
                personDetails += Lang.getInstance().translate("Personalize is valid for %days% days").replace("%days%",
                        "" + daysLeft);

            personDetails += "<br>" + Lang.getInstance().translate("Person is still alive");

        }
        pubKeyDetails.setText("<html>" + personDetails + "<br>" + account.toString(Transaction.FEE_KEY) + "</html>");

    }

    public void onGoClick(AssetCls asset, JButton Button_Confirm, JComboBox<Account> jComboBox_YourAddress,
                          JTextField pubKeyTxt, JTextField toDateTxt,
                          JTextField feePowTxt) {

        if (!OnDealClick.proccess1(Button_Confirm))
            return;

        Account creator = (Account) jComboBox_YourAddress.getSelectedItem();

        Button_Confirm.setEnabled(true);

    }

    private void initComponents(AssetCls asset_in, Account account) {

        AssetCls asset;
        if (asset_in == null) {
            asset = Controller.getInstance().getAsset(1l);
        } else {
            asset = asset_in;
        }

        GridBagConstraints gridBagConstraints;

        paneAssetInfo = new JScrollPane();
        jLabel_YourAddress = new JLabel();
        jComboBox_YourAddress = new JComboBox<>();
        jLabel_Address = new JLabel();
        jLabel_Asset = new JLabel();

        jLabel_Adress_Check = new JLabel();
        jLabel_addDays = new JLabel();
        jTextField_addDays = new JTextField();
        jFormattedTextField_Fee = new JTextField();
        jLabel_addDays_Check = new JLabel();
        jLabel_Title = new JLabel();

        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = new int[]{0, 9, 0, 9, 0, 9, 0};
        layout.rowHeights = new int[]{0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0};
        //getContentPane().setLayout(layout);
        this.setLayout(layout);

        paneAssetInfo.setBorder(BorderFactory.createEtchedBorder());
        AssetInfo info = new AssetInfo(asset, false); //new PersonInfo();
        //jScrollPane2.setViewportView(new AssetInfo(asset));
        //info.sho.show_001(asset);
        info.setFocusable(false);
        paneAssetInfo.setViewportView(info);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 9, 0, 9);
        add(paneAssetInfo, gridBagConstraints);

        jLabel_YourAddress.setText(Lang.getInstance().translate("Your account") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        // gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        gridBagConstraints.insets = new Insets(21, 27, 0, 0);
        add(jLabel_YourAddress, gridBagConstraints);

        jComboBox_YourAddress = new JComboBox<Account>(new AccountsComboBoxModel());

        //ON FAVORITES CHANGE

        jComboBox_YourAddress.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    //jTextField_Address.getDocument().remove(0, jTextField_Address1.getDocument().getLength());
                    //jTextField_Address1.getDocument().insertString(0,
                    //        (((Account) jComboBox_YourAddress.getSelectedItem())).getAddress(),
                    ///        null);
                } catch (Exception e1) {
                }

                jTextField_Address.setText(((Account) jComboBox_YourAddress.getSelectedItem()).getAddress());

            }
        });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        // gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 13);
        gridBagConstraints.insets = new Insets(21, 0, 0, 13);
        add(jComboBox_YourAddress, gridBagConstraints);

        jLabel_Asset.setText(Lang.getInstance().translate("Asset") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        // gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        gridBagConstraints.insets = new Insets(21, 27, 0, 0);
        add(jLabel_Asset, gridBagConstraints);

        //FAVORITES GBC
        GridBagConstraints favoritesGBC = new GridBagConstraints();
        favoritesGBC.insets = new Insets(21, 0, 0, 13);
        favoritesGBC.fill = GridBagConstraints.HORIZONTAL;
        favoritesGBC.anchor = GridBagConstraints.LINE_END;
        favoritesGBC.gridwidth = 3;
        favoritesGBC.gridx = 2;
        favoritesGBC.gridy = 1;

        //ASSET FAVORITES
        cbxAssets = new JComboBox<AssetCls>(new FundTokensComboBoxModel());
        this.add(cbxAssets, favoritesGBC);

        //ON FAVORITES CHANGE
        cbxAssets.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    AssetCls asset = (AssetCls) cbxAssets.getSelectedItem();
                    paneAssetInfo.setViewportView(new AssetInfo(asset, false));
                }
            }
        });

        jLabel_Address.setText(Lang.getInstance().translate("Account to Deposit") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 27, 0, 0);
        add(jLabel_Address, gridBagConstraints);

        jTextField_Address.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                refreshReceiverDetails(jTextField_Address, jLabel_Adress_Check);
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                refreshReceiverDetails(jTextField_Address, jLabel_Adress_Check);
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                refreshReceiverDetails(jTextField_Address, jLabel_Adress_Check);
            }
        });

        if (account == null || account.isPerson()) {
            jLabel_Adress_Check.setText(Lang.getInstance().translate("Insert Deposit Account"));
        } else {
            jTextField_Address.setText(account.getAddress());
        }

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.weightx = 0.1;
        // gridBagConstraints.gridheight =7;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        add(jLabel_Adress_Check, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        add(jTextField_Address, gridBagConstraints);

        jLabel_addDays.setText(Lang.getInstance().translate("Add Days") + ":");

        jTextField_addDays.setToolTipText("");
        jTextField_addDays.setText("0"); // NOI18N
        jTextField_addDays.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jFormattedTextField_ToDoActionPerformed(evt);
            }
        });

        jFormattedTextField_Fee.setHorizontalAlignment(JTextField.LEFT);
        jFormattedTextField_Fee.setText("0");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;

        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        add(jFormattedTextField_Fee, gridBagConstraints);

        jButton_Cansel = new MButton(Lang.getInstance().translate("Cancel"), 2);
        jButton_Cansel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            }
        });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new Insets(1, 0, 29, 0);
        add(jButton_Cansel, gridBagConstraints);

        jButton_Confirm = new MButton(Lang.getInstance().translate("Confirm"), 2);
        jButton_Confirm.setToolTipText("");
        jButton_Confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onGoClick(asset, jButton_Confirm, jComboBox_YourAddress,
                        jTextField_Address,
                        jTextField_addDays, jFormattedTextField_Fee);
            }
        });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        // gridBagConstraints.insets = new java.awt.Insets(12, 9, 11, 9);
        gridBagConstraints.insets = new Insets(12, 23, 0, 9);
        add(jLabel_Title, gridBagConstraints);
        jLabel_Title.setText(Lang.getInstance().translate("Information about the Asset"));
        add(jLabel_Title, gridBagConstraints);

    }

    private void jFormattedTextField_ToDoActionPerformed(ActionEvent evt) {
        // TODO add your handling code here:
    }

}
