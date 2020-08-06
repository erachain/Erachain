package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.Library;
import org.erachain.gui.transaction.Send_RecordDetailsFrame;
import org.erachain.lang.Lang;


@SuppressWarnings("serial")

public class AccountAssetSpendPanel extends AccountAssetActionPanelCls {

    public boolean noRecive;

    public AccountAssetSpendPanel(AssetCls assetIn, Account accountFrom, Account accountTo, PersonCls person, String message) {
        super(null, null, false, assetIn, TransactionAmount.ACTION_SPEND, accountFrom, accountTo, message);

        iconName = "AccountAssetSpendPanel";
    }


    @Override
    public void onSendClick() {

        // confirm params
        if (!cheskError()) return;

        // CREATE TX MESSAGE
        Transaction transaction = Controller.getInstance().r_Send(
                Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress()), feePow, recipient, -key,
                amount.negate(), head, messageBytes, isTextByte, encrypted, 0);
        // test result = new Pair<Transaction, Integer>(null,
        // Transaction.VALIDATE_OK);

        String Status_text = "";
        IssueConfirmDialog dd = new IssueConfirmDialog(null, true, transaction,
                Lang.getInstance().translate("Spend"),
                (int) (this.getWidth() / 1.2), (int) (this.getHeight() / 1.2), Status_text,
                Lang.getInstance().translate("Confirmation Transaction"), !noRecive);
        Send_RecordDetailsFrame ww = new Send_RecordDetailsFrame((RSend) transaction);
        dd.jScrollPane1.setViewportView(ww);
        dd.pack();
        dd.setLocationRelativeTo(this);
        dd.setVisible(true);

        // JOptionPane.OK_OPTION
        if (dd.isConfirm) {

            if (noRecive) {

                // save
                Library.saveJSONStringToEraFile(getParent(), transaction.toJson().toJSONString());


            } else {

                result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);
                confirmaftecreatetransaction();
            }
        }

        // ENABLE
        this.jButton_ok.setEnabled(true);
    }


}