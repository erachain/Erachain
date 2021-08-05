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

    public static boolean make(Component parent, Transaction transaction, boolean tryFree, String txName) {

        int result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK, tryFree, false);

        //CHECK VALIDATE MESSAGE
        if (result == Transaction.VALIDATE_OK) {
            String message = Lang.T("Transaction \"%1\" has been sent").replace("%1",
                    Lang.T(txName == null ? transaction.viewFullTypeName() : txName)) + "!";
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
                BigDecimal amountBuy = transaction.getFee().multiply(new BigDecimal(2));
                MainPanel.getInstance().insertNewTab(
                        Lang.T("Buy") + " " + AssetCls.FEE_ABBREV, new DepositExchange(AssetCls.FEE_ABBREV, transaction.getCreator(),
                                amountBuy));
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
