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
import org.erachain.settings.Settings;

import java.awt.*;


@SuppressWarnings("serial")

public class AccountAssetSpendPanel extends AccountAssetActionPanelCls {

    public boolean noRecive;
    private static String iconFile = Settings.getInstance().getPatnIcons()+ "AccountAssetSpendPanel.png";
    public AccountAssetSpendPanel(AssetCls assetIn, Account accountFrom, Account accountTo, PersonCls person, String message) {
        super(false, null, assetIn, null, TransactionAmount.ACTION_SPEND, accountFrom, accountTo, message);

        //  icon.setIcon(null);

    }
    /*
    public AccountAssetSendPanel(AssetCls asset, Account account, Account account_To, PersonCls person, String message) {
        this(asset, account, account_To, person);
    }
    */


    @Override
    public void onSendClick() {

        // confirm params
        if (!cheskError()) return;

        // CREATE TX MESSAGE
        Transaction transaction = Controller.getInstance().r_Send(
                Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()), feePow, recipient, -key,
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

    public static  Image getIcon() {
        {
            try {
                return Toolkit.getDefaultToolkit().getImage(iconFile);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
