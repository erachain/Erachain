package org.erachain.gui.items.accounts;

import java.math.BigDecimal;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.transaction.Send_RecordDetailsFrame;
import org.erachain.lang.Lang;

//import org.erachain.settings.Settings;

@SuppressWarnings("serial")

public class AccountTakeHoldPanel extends AssetSendPanel {

    public AccountTakeHoldPanel(AssetCls asset, Account accountFrom, Account accountTo, PersonCls person) {
        super(asset, TransactionAmount.ACTION_HOLD, accountFrom, accountTo);

        this.jLabel_Title.setText(Lang.getInstance().translate("If You want to take on hold issued asset %asset%, fill in this form")
                .replace("%asset%", asset.viewName()));

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
        IssueConfirmDialog dd = new IssueConfirmDialog(null, true, transaction,
                Lang.getInstance().translate("Holden"), (int) (this.getWidth() / 1.2), (int) (this.getHeight() / 1.2), Status_text,
                Lang.getInstance().translate("Confirmation Transaction"));
        Send_RecordDetailsFrame ww = new Send_RecordDetailsFrame((RSend) transaction);
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


