package gui.items.accounts;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.AEScrypto;
import core.crypto.Base58;
import core.crypto.Crypto;
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
import utils.NameUtils;
import utils.NameUtils.NameResult;
import utils.Pair;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

//import settings.Settings;

@SuppressWarnings("serial")

public class Account_Repay_Debt_Panel extends AssetSendPanel {
    // private final MessagesTableModel messagesTableModel;

    public Account_Repay_Debt_Panel(AssetCls asset, Account account,   Account account_To, PersonCls person) {
        super(asset, account, account_To, person);

        this.jLabel_Title.setText(Lang.getInstance()
                .translate("If You want to give the borrowed asset %asset%, fill in this form").replace("%asset%", asset.viewName()));

        // icon.setIcon(null);
        this.jButton_ok.setText(Lang.getInstance().translate("Repay Debt"));
        this.jLabel_To.setText(Lang.getInstance().translate("Lender Account") + ":");
        this.jLabel_Recive_Detail.setText(Lang.getInstance().translate("Lender Details") + ":");

    }

    @Override
    public void onSendClick() {
        // confirm params
     
        if (!cheskError()) return;
        // CREATE TX MESSAGE
        Transaction transaction = Controller.getInstance().r_Send(
                Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()), feePow, recipient, -key,
                amount, head, messageBytes, isTextByte, encrypted);

        String Status_text = "";
        Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(null, true, transaction,
                Lang.getInstance().translate("Send Mail"), (int) (this.getWidth() / 1.2),
                (int) (this.getHeight() / 1.2), Status_text, Lang.getInstance().translate("Confirmation Transaction"));
        Send_RecordDetailsFrame ww = new Send_RecordDetailsFrame((R_Send) transaction);

        dd.jScrollPane1.setViewportView(ww);
        dd.pack();
        dd.setLocationRelativeTo(this);
        dd.setVisible(true);

        // JOptionPane.OK_OPTION
        if (dd.isConfirm) {

            result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);

            confirmaftecreatetransaction();
        }

        // ENABLE
        this.jButton_ok.setEnabled(true);
    }

}
