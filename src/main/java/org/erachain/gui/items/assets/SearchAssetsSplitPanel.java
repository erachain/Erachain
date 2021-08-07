package org.erachain.gui.items.assets;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.items.SearchItemSplitPanel;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SearchAssetsSplitPanel extends SearchItemSplitPanel {

    public static String NAME = "SearchAssetsSplitPanel";
    public static String TITLE = "Search Assets";

    private static final long serialVersionUID = 1L;

    public SearchAssetsSplitPanel(boolean search_and_exchange) {
        super(new ItemAssetsTableModel(), NAME, TITLE);

        // MENU

        JMenuItem sell = new JMenuItem(Lang.T("To sell"));
        sell.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //new ExchangeFrame((AssetCls) th.itemMenu, null, "To sell", "");
                MainPanel.getInstance().insertTab(new ExchangePanel((AssetCls) itemTableSelected, null, "To sell", ""));

            }
        });

        JMenuItem excahge = new JMenuItem(Lang.T("Exchange"));
        excahge.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainPanel.getInstance().insertTab(new ExchangePanel((AssetCls) itemTableSelected, null, "", ""));

            }
        });

        JMenuItem buy = new JMenuItem(Lang.T("Buy"));
        buy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainPanel.getInstance().insertTab(new ExchangePanel((AssetCls) itemTableSelected, null, "Buy", ""));

            }
        });

        if (search_and_exchange) {
            this.menuTable.add(excahge);
            this.menuTable.addSeparator();
            this.menuTable.add(buy);

            this.menuTable.add(sell);
            this.menuTable.addSeparator();

            menuTable.addSeparator();
        } else {
            this.menuTable.remove(this.favoriteMenuItems);

            menuTable.addSeparator();
        }

    }


    //show details
    @Override
    public Component getShow(ItemCls item) {
        return new AssetInfo((AssetCls) item, false);

    }

    // mouse 2 click
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
