package gui.items.assets;

import core.item.assets.Order;
import datachain.DCSet;
import lang.Lang;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;
import utils.MenuPopupUtil;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.BigInteger;

@SuppressWarnings("serial")
public class Order_Info_Panel extends JPanel {

    public GridBagConstraints labelGBC = new GridBagConstraints();
    public GridBagConstraints detailGBC = new GridBagConstraints();

    public Order_Info_Panel(Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> orderREC) {

        // LAYOUT
        this.setLayout(new GridBagLayout());

        Order order = Order.fromDBrec(orderREC);
        order.setDC(DCSet.getInstance());

        // LABEL GBC
        labelGBC = new GridBagConstraints();
        labelGBC.insets = new Insets(0, 5, 5, 0);
        labelGBC.fill = GridBagConstraints.HORIZONTAL;
        labelGBC.anchor = GridBagConstraints.FIRST_LINE_START;// ..NORTHWEST;
        labelGBC.weightx = 0;
        labelGBC.gridx = 0;
        // LABEL HAVE
        ++labelGBC.gridy;
        JLabel haveLabel = new JLabel(Lang.getInstance().translate("Have") + ":");
        this.add(haveLabel, labelGBC);

        // HAVE
        ++detailGBC.gridy;
        JTextField have = new JTextField(
                order.getAmountHave().toPlainString() + " x " + String.valueOf(order.getHaveAsset().toString()));
        have.setEditable(false);
        MenuPopupUtil.installContextMenu(have);
        this.add(have, detailGBC);

        // LABEL WANT
        ++labelGBC.gridy;
        JLabel wantLabel = new JLabel(Lang.getInstance().translate("Want") + ":");
        this.add(wantLabel, labelGBC);

        // HAVE
        ++detailGBC.gridy;
        JTextField want = new JTextField(
                order.getAmountWant().toPlainString() + " x " + String.valueOf(order.getWantAsset().toString()));
        want.setEditable(false);
        MenuPopupUtil.installContextMenu(want);
        this.add(want, detailGBC);

        // LABEL PRICE
        ++labelGBC.gridy;
        JLabel priceLabel = new JLabel(Lang.getInstance().translate("Price") + ":");
        this.add(priceLabel, labelGBC);

        // PRICE
        ++detailGBC.gridy;
        JTextField price = new JTextField(order.getPrice().toPlainString());
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
