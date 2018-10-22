package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.R_Send;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.library.Issue_Confirm_Dialog;
import org.erachain.gui.library.My_JFileChooser;
import org.erachain.gui.library.library;
import org.erachain.gui.transaction.Send_RecordDetailsFrame;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileView;

import java.awt.Container;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


@SuppressWarnings("serial")

public class Account_Send_Panel extends AssetSendPanel {
    
    private Account_Send_Panel th;
    public boolean noRecive;

    public Account_Send_Panel(AssetCls asset, Account account, Account account_To, PersonCls person) {
        super(asset,account,account_To, person);
        String a;
        th = this;
        if (asset == null) a = "";
        else a = asset.viewName();

        this.jLabel_Title.setText(Lang.getInstance().translate("If You want to send asset %asset%, fill in this form").replace("%asset%", a));

        //  icon.setIcon(null);
        this.jButton_ok.setText(Lang.getInstance().translate("Send"));
        this.jLabel_To.setText(Lang.getInstance().translate("To: (address or name)") + ":");
        this.jLabel_Recive_Detail.setText(Lang.getInstance().translate("Receiver details") + ":");
        this.jComboBox_Asset.setEnabled(false);
    }

   
    @Override
    public void onSendClick() {
        
     // confirm params
        if (!cheskError()) return;

        // CREATE TX MESSAGE
        Transaction transaction = Controller.getInstance().r_Send(
                Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()), feePow, recipient, key,
                amount, head, messageBytes, isTextByte, encrypted);
        // test result = new Pair<Transaction, Integer>(null,
        // Transaction.VALIDATE_OK);

        String Status_text = "";
        Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(null, true, transaction,
                Lang.getInstance().translate("Send Mail"),
                (int) (this.getWidth() / 1.2), (int) (this.getHeight() / 1.2), Status_text,
                Lang.getInstance().translate("Confirmation Transaction"), !noRecive);
        Send_RecordDetailsFrame ww = new Send_RecordDetailsFrame((R_Send) transaction);
        dd.jScrollPane1.setViewportView(ww);
        dd.pack();
        dd.setLocationRelativeTo(this);
        dd.setVisible(true);

        // JOptionPane.OK_OPTION
        if (dd.isConfirm) {

            if (noRecive) {

                // save
                library.saveJSONStringToEraFile(getParent(), transaction.toJson().toJSONString());
               

            } else {

                result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);
                confirmaftecreatetransaction();
            }
        }
        // ENABLE
        this.jButton_ok.setEnabled(true);
    }

}
