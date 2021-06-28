package org.erachain.gui.transaction;

import org.erachain.core.transaction.ChangeOrderTransaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class UpdateOrderDetailsFrame extends RecDetailsFrame {
    public UpdateOrderDetailsFrame(ChangeOrderTransaction orderUpdate) {
        super(orderUpdate, true);

        orderUpdate.setDC(DCSet.getInstance(), true);

        //LABEL HAVE
        ++labelGBC.gridy;
        JLabel haveLabel = new JLabel(Lang.T("Have") + ":");
        this.add(haveLabel, labelGBC);

        //LABEL PRICE
        ++labelGBC.gridy;
        JLabel priceLabel = new JLabel(Lang.T("Price") + ":");
        this.add(priceLabel, labelGBC);

        //PRICE
        ++fieldGBC.gridy;
        JTextField price = new JTextField(orderUpdate.getPriceCalc().toPlainString()
                + " / " + orderUpdate.getPriceCalcReverse().toPlainString());
        price.setEditable(false);
        MenuPopupUtil.installContextMenu(price);
        this.add(price, fieldGBC);

        //PACK
        //		this.pack();
        //        this.setResizable(false);
        //       this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
