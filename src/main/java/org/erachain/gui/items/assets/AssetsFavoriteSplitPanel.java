package org.erachain.gui.items.assets;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AssetsFavoriteSplitPanel extends ItemSplitPanel {

    public static String NAME = "AssetsFavoriteSplitPanel";
    public static String TITLE = "Favorite Assets";

    private static final long serialVersionUID = 2717571093561259483L;

    public AssetsFavoriteSplitPanel() {
        super(new FavoriteAssetsTableModel(), NAME, TITLE);

        JMenuItem sell = new JMenuItem(Lang.T("To sell"));

        sell.addActionListener(e ->
                //new ExchangeFrame((AssetCls) itemMenu, null, "To sell", "")

                MainPanel.getInstance().insertTab(
                        new ExchangePanel((AssetCls) itemTableSelected, null, "To sell", "")));

        JMenuItem exchange = new JMenuItem(Lang.T("Exchange"));
        exchange.addActionListener(e ->
                MainPanel.getInstance().insertTab(
                        new ExchangePanel((AssetCls) itemTableSelected, null, "", "")));

        JMenuItem buy = new JMenuItem(Lang.T("Buy"));
        buy.addActionListener(e ->
                //new ExchangeFrame((AssetCls) itemMenu, null, "Buy", "")
                MainPanel.getInstance().insertTab(
                        new ExchangePanel((AssetCls) itemTableSelected, null, "Buy", "")));


        JMenuItem set_Status_Item = new JMenuItem(Lang.T("Set Status to Asset"));

        set_Status_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                @SuppressWarnings("unused")
                AssetSetStatusDialog fm = new AssetSetStatusDialog((AssetCls) itemTableSelected);

            }
        });
        this.menuTable.add(set_Status_Item);

        JMenuItem issueSeries = new JMenuItem(Lang.T("IssueSeries"));
        issueSeries.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                @SuppressWarnings("unused")
                IssueAssetSeriesDialog fm = new IssueAssetSeriesDialog((AssetCls) itemTableSelected);

            }
        });
        this.menuTable.add(issueSeries);

        menuTable.add(exchange);
        menuTable.addSeparator();
        menuTable.add(buy);
        menuTable.add(sell);

        menuTable.addSeparator();

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
        MainPanel.getInstance().insertTab(
                panel);
    }

}
