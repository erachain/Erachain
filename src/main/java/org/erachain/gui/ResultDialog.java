package org.erachain.gui;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.items.assets.DepositExchange;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class ResultDialog {

    public static boolean make(Component parent, Transaction transaction, boolean tryFree) {

        int result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK, tryFree, false);

        //CHECK VALIDATE MESSAGE
        if (result == Transaction.VALIDATE_OK) {
            String message = Lang.T("Transaction \"%1\" has been sent").replace("%1",
                    Lang.T(transaction.viewFullTypeName())) + "!";
            JOptionPane.showMessageDialog(new JFrame(), message,
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);
            return true;
        } else if (result == Transaction.NOT_ENOUGH_FEE
                // и биржа включена
                && Settings.EXCHANGE_IN_OUT) {

            Object[] options = {Lang.T("Add funds to Your account"),
                    Lang.T("Cancel")};

            JLabel mess = new JLabel(Lang.T("ENOUGH_COMPU_AND_BUY"));

            int n = JOptionPane.showOptionDialog(
                    parent,
                    mess,
                    Lang.T(OnDealClick.resultMess(result)),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    null
            );

            if (n == JOptionPane.YES_OPTION) {
                AssetCls feeAsset = Controller.getInstance().getAsset(AssetCls.FEE_KEY);
                BigDecimal amountBuy = transaction.getFee();
                MainPanel.getInstance().insertNewTab(Lang.T("Deposit") + " COMPU", new DepositExchange(feeAsset, transaction.getCreator(),
                        amountBuy, feeAsset));
            }

        } else {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T(OnDealClick.resultMess(result))
                            + (transaction.errorValue == null ? "" : ": " + transaction.errorValue),
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
        }
        return false;

    }
}
