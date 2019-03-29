package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.transaction.Send_RecordDetailsFrame;
import org.erachain.lang.Lang;

//import org.erachain.settings.Settings;

@SuppressWarnings("serial")

public class AccountRepayDebtPanel extends AssetSendPanel {
    // private final MessagesTableModel messagesTableModel;

    public AccountRepayDebtPanel(AssetCls asset, Account account, Account account_To, PersonCls person) {
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
        IssueConfirmDialog dd = new IssueConfirmDialog(null, true, transaction,
                Lang.getInstance().translate("Send Mail"), (int) (this.getWidth() / 1.2),
                (int) (this.getHeight() / 1.2), Status_text, Lang.getInstance().translate("Confirmation Transaction"));
        Send_RecordDetailsFrame ww = new Send_RecordDetailsFrame((RSend) transaction);

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
