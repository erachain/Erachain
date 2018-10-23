package gui.items.accounts;

import controller.Controller;
import core.account.Account;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.transaction.R_Send;
import core.transaction.Transaction;
import gui.library.Issue_Confirm_Dialog;
import gui.library.My_JFileChooser;
import gui.library.library;
import gui.transaction.Send_RecordDetailsFrame;
import lang.Lang;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileView;

import java.awt.Container;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


@SuppressWarnings("serial")

public class Account_Send_Panel extends AssetSendPanel {
    
    //private Account_Send_Panel th;
    public boolean noRecive;

    public Account_Send_Panel(AssetCls asset, Account account, Account account_To, PersonCls person) {
        super(asset, account, account_To, person);
        String a;
        //th = this;

        this.jLabel_Title.setText(Lang.getInstance().translate("If You want to send asset %asset%, fill in this form").
                replace("%asset%", asset.viewName()));

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
                Lang.getInstance().translate("Send"),
                (int) (this.getWidth() / 1.2), (int) (this.getHeight() / 1.2), Status_text,
                Lang.getInstance().translate("Confirmation Transaction"), !noRecive);
        Send_RecordDetailsFrame ww = new Send_RecordDetailsFrame((R_Send) transaction);
        dd.jScrollPane1.setViewportView(ww);
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
