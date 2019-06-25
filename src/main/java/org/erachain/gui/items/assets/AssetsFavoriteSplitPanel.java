package org.erachain.gui.items.assets;

import com.sun.javafx.binding.SelectBinding;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;

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

        sell.addActionListener(e ->
                //new ExchangeFrame((AssetCls) itemMenu, null, "To sell", "")
                MainPanel.getInstance().insertTab(new ExchangePanel((AssetCls) itemMenu, null, "To sell", ""))
        );

        JMenuItem exchange = new JMenuItem(Lang.getInstance().translate("Exchange"));
        exchange.addActionListener(e ->
                MainPanel.getInstance().insertTab(new ExchangePanel((AssetCls) itemMenu, null, "", ""))
        );

        JMenuItem buy = new JMenuItem(Lang.getInstance().translate("Buy"));
        buy.addActionListener(e ->
                //new ExchangeFrame((AssetCls) itemMenu, null, "Buy", "")
                MainPanel.getInstance().insertTab(new ExchangePanel((AssetCls) itemMenu, null, "Buy", ""))
        );

        JMenuItem vouchMenu = new JMenuItem(Lang.getInstance().translate("Vouch"));
        vouchMenu.addActionListener(e -> {
            DCSet db = DCSet.getInstance();
            Transaction transaction = db.getTransactionFinalMap().get(itemMenu.getReference());
            new VouchRecordDialog(transaction.getBlockHeight(), transaction.getSeqNo());

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
        return new AssetInfo((AssetCls) item, true);
    }

    @Override
    protected void tableMouse2Click(ItemCls item) {

        AssetCls asset = (AssetCls) item;
        AssetCls assetSell = Settings.getInstance().getDefaultPairAsset();
        String action = null;
        ExchangePanel panel = new ExchangePanel(asset, assetSell, action, "");
        panel.setName(asset.getTickerName() + "/" + assetSell.getTickerName());
        MainPanel.getInstance().insertTab(panel);
    }

}
