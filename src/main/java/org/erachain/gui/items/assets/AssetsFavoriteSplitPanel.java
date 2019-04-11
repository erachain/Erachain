package org.erachain.gui.items.assets;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AssetsFavoriteSplitPanel extends ItemSplitPanel {
    private static final long serialVersionUID = 2717571093561259483L;

    public AssetsFavoriteSplitPanel() {
        super(new FavoriteAssetsTableModel(), "AssetsFavoriteSplitPanel");
        setName(Lang.getInstance().translate("Favorite Assets"));

        JMenuItem sell = new JMenuItem(Lang.getInstance().translate("To sell"));

        sell.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ExchangeFrame((AssetCls) itemMenu, null, "To sell", "");
            }
        });

        JMenuItem exchange = new JMenuItem(Lang.getInstance().translate("Exchange"));
        exchange.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ExchangeFrame((AssetCls) itemMenu, null, "", "");
            }
        });

        JMenuItem buy = new JMenuItem(Lang.getInstance().translate("Buy"));
        buy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ExchangeFrame((AssetCls) itemMenu, null, "Buy", "");
            }
        });

        JMenuItem vouchMenu = new JMenuItem(Lang.getInstance().translate("Vouch"));
        vouchMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DCSet db = DCSet.getInstance();
                Transaction transaction = db.getTransactionFinalMap().get(itemMenu.getReference());
                new VouchRecordDialog(transaction.getBlockHeight(), transaction.getSeqNo());

            }
        });
        menuTable.addSeparator();
        menuTable.add(exchange);
        menuTable.addSeparator();
        menuTable.add(buy);
        menuTable.add(sell);
        menuTable.addSeparator();
        menuTable.add(vouchMenu);
    }

    // show details
    @Override
    public Component getShow(ItemCls item) {
        return new AssetInfo((AssetCls) item);
    }

}
