package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.utils.NumberAsString;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class BalanceRenderer implements ListCellRenderer<Pair<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> {
    private DefaultListCellRenderer defaultRenderer;

    public BalanceRenderer() {
        this.defaultRenderer = new DefaultListCellRenderer();
    }

    public Component getListCellRendererComponent(JList<? extends Pair<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> list, Pair<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel renderer = (JLabel) this.defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value != null) {
            AssetCls asset = Controller.getInstance().getAsset(value.getA().b);
            renderer.setText(asset.toString() + " - " + NumberAsString.formatAsString(value.getB().a.b, asset.getScale()));
        }

        return renderer;
    }
}