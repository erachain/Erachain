package gui.items.accounts;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.AEScrypto;
import core.crypto.Base58;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.transaction.R_Send;
import core.transaction.Transaction;
import gui.AccountRenderer;
import gui.MainFrame;
import gui.PasswordPane;
import gui.items.assets.AssetsComboBoxModel;
import gui.library.Issue_Confirm_Dialog;
import gui.models.AccountsComboBoxModel;
import gui.transaction.OnDealClick;
import gui.transaction.Send_RecordDetailsFrame;
import lang.Lang;
import utils.Converter;

//import settings.Settings;

@SuppressWarnings("serial")

public class Account_Confiscate_Debt_Panel extends Class_Account_Transaction_Panel {
    // private final MessagesTableModel messagesTableModel;

    private Transaction transaction;

    public Account_Confiscate_Debt_Panel(AssetCls asset, Account account,   Account account_To, PersonCls person) {
        super(asset,account,account_To, person);
        String a;
        if (asset == null)
            a = "";
        else
            a = asset.viewName();

        sendButton.setText(Lang.getInstance().translate(asset.isOutsideType()? "Подтвердить погашение требования" : "Confiscate Debt"));
        jTextArea_Title.setText(Lang.getInstance()
                .translate(asset.isOutsideType()? "Если Вы хотите подтвердить погашение требования %asset%, заполните эту форму"
                        : "If You want to confiscate in debt issued asset %asset%, fill in this form")
                .replace("%asset%", a));

        // icon.setIcon(null);

        toLabel.setText(Lang.getInstance().translate(asset.isOutsideType()?"Счет эмитента" : "Debtor Account") + ":");
        recDetailsLabel.setText(Lang.getInstance().translate(asset.isOutsideType()?"Детали эмитента" : "Debtor Details") + ":");

      }

    @Override
    public void onSendClick() {
  
        // confirm params
        if (!cheskError()) return;

        // CREATE TX MESSAGE
        transaction = Controller.getInstance().r_Send((byte) 2, core.transaction.TransactionAmount.BACKWARD_MASK,
                (byte) 0, Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()), feePow,
                recipient, -key, amount, head, messageBytes, isTextByte, encrypted);

        String Status_text = "<HTML>" + Lang.getInstance().translate("Size") + ":&nbsp;" + transaction.viewSize(false)
                + " Bytes, ";
        Status_text += "<b>" + Lang.getInstance().translate("Fee") + ":&nbsp;" + transaction.getFee().toString()
                + " COMPU</b><br></body></HTML>";

        Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(MainFrame.getInstance(), true,
                Lang.getInstance().translate("Send Mail"), (int) (this.getWidth() / 1.2),
                (int) (this.getHeight() / 1.2), Status_text, Lang.getInstance().translate("Confirmation Transaction"));
        Send_RecordDetailsFrame ww = new Send_RecordDetailsFrame((R_Send) transaction);
        dd.jScrollPane1.setViewportView(ww);
        dd.setLocationRelativeTo(this);
        dd.setVisible(true);

        // JOptionPane.OK_OPTION
        if (dd.isConfirm) {

            result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, false);

            confirmaftecreatetransaction();
        }
        // ENABLE
        this.sendButton.setEnabled(true);
    }

}
