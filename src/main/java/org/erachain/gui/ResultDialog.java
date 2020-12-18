package org.erachain.gui;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.items.assets.DepositExchange;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class ResultDialog {

    public static boolean make(Component parent, Transaction transaction, String message, boolean addMess) {

        int result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);

        //CHECK VALIDATE MESSAGE
        if (result == Transaction.VALIDATE_OK) {
            JOptionPane.showMessageDialog(new JFrame(), (message == null ? transaction.viewFullTypeName() : Lang.getInstance().translate(message))
                            + (addMess ? " " + Lang.getInstance().translate("- was made") + "!" : ""),
                    Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
            return true;
        } else if (BlockChain.MAIN_MODE && result == Transaction.NOT_ENOUGH_FEE) {

            Object[] options = {Lang.getInstance().translate("Add funds to Your account"),
                    Lang.getInstance().translate("Cancel")};

            JLabel mess = new JLabel(Lang.getInstance().translate("ENOUGH_COMPU_AND_BUY"));

            int n = JOptionPane.showOptionDialog(
                    parent,
                    mess,
                    Lang.getInstance().translate(OnDealClick.resultMess(result)),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    null
            );

            if (n == JOptionPane.YES_OPTION) {
                AssetCls feeAsset = Controller.getInstance().getAsset(AssetCls.FEE_KEY);
                BigDecimal amountBuy = transaction.getFee();
                MainPanel.getInstance().insertNewTab(Lang.getInstance().translate("Deposit") + " COMPU", new DepositExchange(feeAsset, transaction.getCreator(),
                        amountBuy, feeAsset));
            }

        } else {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.getInstance().translate(OnDealClick.resultMess(result))
                            + (transaction.errorValue == null ? "" : ": " + transaction.errorValue),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        }
        return false;

    }
}
