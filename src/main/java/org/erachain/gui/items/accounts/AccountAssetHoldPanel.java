package org.erachain.gui.items.accounts;

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

import java.awt.*;
import java.math.BigDecimal;

public class AccountAssetHoldPanel extends AccountAssetActionPanelCls  {

    private static String iconFile = "images/pageicons/AccountAssetHoldPanel.png";

    public AccountAssetHoldPanel(AssetCls assetIn, Account accountFrom, Account accountTo, PersonCls person) {
        super(true, null, assetIn, null, TransactionAmount.ACTION_HOLD, accountFrom, accountTo, null);

        //	icon.setIcon(null);

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

    public static Image getIcon() {
        {
            try {
                return Toolkit.getDefaultToolkit().getImage(iconFile);
            } catch (Exception e) {
                return null;
            }
        }
    }
}


