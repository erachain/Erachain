package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class OrderInfoPanel extends JPanel {

    public GridBagConstraints labelGBC;
    public GridBagConstraints detailGBC;

    public OrderInfoPanel(Order order) {

        // LAYOUT
        this.setLayout(new GridBagLayout());

        AssetCls haveAsset = Controller.getInstance().getAsset(order.getHaveAssetKey());
        AssetCls wantAsset = Controller.getInstance().getAsset(order.getWantAssetKey());

        // LABEL GBC
        labelGBC = new GridBagConstraints();
        labelGBC.insets = new Insets(0, 5, 5, 0);
        labelGBC.fill = GridBagConstraints.HORIZONTAL;
        labelGBC.anchor = GridBagConstraints.FIRST_LINE_START;// ..NORTHWEST;
        labelGBC.weightx = 0;
        labelGBC.gridx = 0;

        // DETAIL GBC
        detailGBC = new GridBagConstraints();
        detailGBC.insets = new Insets(0, 5, 5, 0);
        detailGBC.fill = GridBagConstraints.BOTH;
        detailGBC.anchor = GridBagConstraints.FIRST_LINE_START;// ..NORTHWEST;
        detailGBC.weightx = 0;
        detailGBC.gridx = 1;

        // LABEL HAVE
        ++labelGBC.gridy;
        JLabel haveLabel = new JLabel(Lang.T("Have") + ":");
        this.add(haveLabel, labelGBC);

        // HAVE
        ++detailGBC.gridy;
        JTextField have = new JTextField(
                order.getAmountHave().toPlainString() + " x " + haveAsset.getShortName());
        have.setEditable(false);
        MenuPopupUtil.installContextMenu(have);
        this.add(have, detailGBC);

        // LABEL WANT
        ++labelGBC.gridy;
        JLabel wantLabel = new JLabel(Lang.T("Want") + ":");
        this.add(wantLabel, labelGBC);

        // HAVE
        ++detailGBC.gridy;
        JTextField want = new JTextField(
                order.getAmountWant().toPlainString() + " x " + wantAsset.getShortName());
        want.setEditable(false);
        MenuPopupUtil.installContextMenu(want);
        this.add(want, detailGBC);

        // LABEL PRICE
        ++labelGBC.gridy;
        JLabel priceLabel = new JLabel(Lang.T("Price") + ":");
        this.add(priceLabel, labelGBC);

        // PRICE
        ++detailGBC.gridy;
        JTextField price = new JTextField(order.getPrice().toPlainString() + " / " + order.calcPriceReverse().toPlainString());
        //+ " / " + order.getPriceCalcReverse().toPlainString());
        price.setEditable(false);
        MenuPopupUtil.installContextMenu(price);
        this.add(price, detailGBC);

        // PACK
        // this.pack();
        // this.setResizable(false);
        // this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
