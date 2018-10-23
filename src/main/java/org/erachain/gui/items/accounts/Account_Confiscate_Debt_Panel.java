package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.R_Send;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.gui.library.Issue_Confirm_Dialog;
import org.erachain.gui.transaction.Send_RecordDetailsFrame;
import org.erachain.lang.Lang;

//import org.erachain.settings.Settings;

@SuppressWarnings("serial")

public class Account_Confiscate_Debt_Panel extends AssetSendPanel {

    public Account_Confiscate_Debt_Panel(AssetCls asset, Account account,   Account account_To, PersonCls person) {
        super(asset,account,account_To, person);

        this.jButton_ok.setText(Lang.getInstance().translate(asset.isOutsideType()? "Подтвердить погашение требования" : "Confiscate Debt"));
        this.jLabel_Title.setText(Lang.getInstance()
                .translate(asset.isOutsideType()? "Если Вы хотите подтвердить погашение требования %asset%, заполните эту форму"
                        : "If You want to confiscate in debt issued asset %asset%, fill in this form")
                .replace("%asset%", asset.viewName()));

        // icon.setIcon(null);

        this.jLabel_To.setText(Lang.getInstance().translate(asset.isOutsideType()?"Счет эмитента" : "Debtor Account") + ":");
        this.jLabel_Recive_Detail.setText(Lang.getInstance().translate(asset.isOutsideType()?"Детали эмитента" : "Debtor Details") + ":");

      }

    @Override
    public void onSendClick() {
  
        // confirm params
        if (!cheskError()) return;

        // CREATE TX MESSAGE
        Transaction transaction = Controller.getInstance().r_Send((byte) 2, TransactionAmount.BACKWARD_MASK,
                (byte) 0, Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()), feePow,
                recipient, -key, amount, head, messageBytes, isTextByte, encrypted);

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
