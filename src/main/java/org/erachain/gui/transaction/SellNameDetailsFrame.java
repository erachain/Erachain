package org.erachain.gui.transaction;

import org.erachain.core.transaction.SellNameTransaction;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class SellNameDetailsFrame extends RecDetailsFrame {
    public SellNameDetailsFrame(SellNameTransaction nameSale) {
        super(nameSale);

        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        ++detailGBC.gridy;
        JTextField name = new JTextField(nameSale.getNameSale().getKey());
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, detailGBC);

        //LABEL PRICE
        ++labelGBC.gridy;
        JLabel priceLabel = new JLabel(Lang.getInstance().translate("Price") + ":");
        this.add(priceLabel, labelGBC);

        //PRICE
        ++detailGBC.gridy;
        JTextField price = new JTextField(nameSale.getNameSale().getAmount().toPlainString());
        price.setEditable(false);
        MenuPopupUtil.installContextMenu(price);
        this.add(price, detailGBC);

        //PACK
//		this.pack();
//        this.setResizable(false);
//        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
