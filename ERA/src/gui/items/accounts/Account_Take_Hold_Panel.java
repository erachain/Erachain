package gui.items.accounts;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
import core.transaction.TransactionAmount;
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

//import settings.Settings;

@SuppressWarnings("serial")

public class Account_Take_Hold_Panel extends AssetSendPanel {
    //private final MessagesTableModel messagesTableModel;


    //private Account_Take_Hold_Panel th;


    public Account_Take_Hold_Panel(AssetCls asset, Account account,   Account account_To, PersonCls person) {
        super(asset,account,account_To, person);
        String a;
        //th = this;
        if (asset == null) {
            a = "";
            asset = Controller.getInstance().getAsset(1);
        }
        else a = asset.viewName();

        this.jLabel_Title.setText(Lang.getInstance().translate("If You want to take on hold issued asset %asset%, fill in this form").replace("%asset%", a));

        //	icon.setIcon(null);
        this.jButton_ok.setText(Lang.getInstance().translate("Hold Asset"));
        this.jLabel_To.setText(Lang.getInstance().translate("Vendor Account") + ":");
        this.jLabel_Recive_Detail.setText(Lang.getInstance().translate("Vendor Details") + ":");
      
    }

    @Override
    public void onSendClick() {
       
       
        // confirm params
       if (!cheskError()) return;
       
        //CREATE TX MESSAGE
        // HOLD on STOCK - with BACKWARD flag
        Transaction transaction = Controller.getInstance().r_Send(
                (byte) 2, TransactionAmount.BACKWARD_MASK, (byte) 0,
                Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()), feePow, recipient,
                key, BigDecimal.ZERO.subtract(amount),
                head, messageBytes, isTextByte, encrypted);
        // test result = new Pair<Transaction, Integer>(null, Transaction.VALIDATE_OK);

        String Status_text = "";
        Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(null, true, transaction,
                Lang.getInstance().translate("Holden"), (int) (this.getWidth() / 1.2), (int) (this.getHeight() / 1.2), Status_text,
                Lang.getInstance().translate("Confirmation Transaction"));
        Send_RecordDetailsFrame ww = new Send_RecordDetailsFrame((R_Send) transaction);
        dd.jScrollPane1.setViewportView(ww);
        dd.pack();
        dd.setLocationRelativeTo(this);
        dd.setVisible(true);

        //	JOptionPane.OK_OPTION
        if (dd.isConfirm) {

            result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);
            confirmaftecreatetransaction();

        }
        //ENABLE
        this.jButton_ok.setEnabled(true);
    }

}


