package gui.transaction;

import core.transaction.SellNameTransaction;
import lang.Lang;
import utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class SellNameDetailsFrame extends Rec_DetailsFrame {
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
