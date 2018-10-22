package org.erachain.gui.items.accounts;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.AEScrypto;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.R_Send;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.gui.AccountRenderer;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.items.assets.AssetsComboBoxModel;
import org.erachain.gui.library.Issue_Confirm_Dialog;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.gui.transaction.Send_RecordDetailsFrame;
import org.erachain.lang.Lang;
import org.erachain.utils.Converter;
import org.erachain.utils.NameUtils;
import org.erachain.utils.NameUtils.NameResult;
import org.erachain.utils.Pair;

//import org.erachain.settings.Settings;

@SuppressWarnings("serial")

public class Account_Take_Hold_Panel extends AssetSendPanel {
    //private final MessagesTableModel messagesTableModel;


    private Account_Take_Hold_Panel th;


    public Account_Take_Hold_Panel(AssetCls asset, Account account,   Account account_To, PersonCls person) {
        super(asset,account,account_To, person);
        String a;
        th = this;
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
        Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(MainFrame.getInstance(), true, transaction,
                Lang.getInstance().translate("Send Mail"), (int) (th.getWidth() / 1.2), (int) (th.getHeight() / 1.2), Status_text, Lang.getInstance().translate("Confirmation Transaction"));
        Send_RecordDetailsFrame ww = new Send_RecordDetailsFrame((R_Send) transaction);
        dd.jScrollPane1.setViewportView(ww);
        dd.setLocationRelativeTo(th);
        dd.setVisible(true);

        //	JOptionPane.OK_OPTION
        if (dd.isConfirm) {

           
            result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);

        }
        //ENABLE
        this.jButton_ok.setEnabled(true);
    }

}


