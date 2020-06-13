package org.erachain.gui.transaction;

import org.erachain.core.transaction.CancelSellNameTransaction;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class CancelSellNameDetailsFrame extends RecDetailsFrame {
    public CancelSellNameDetailsFrame(CancelSellNameTransaction cancelNameSale) {
        super(cancelNameSale, true);

        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        ++detailGBC.gridy;
        JTextField name = new JTextField(cancelNameSale.getName());
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, detailGBC);

        //PACK
        //	this.pack();
        //    this.setResizable(false);
        //    this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
