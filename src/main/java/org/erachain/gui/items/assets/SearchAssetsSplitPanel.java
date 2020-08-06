package org.erachain.gui.items.assets;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.SearchItemSplitPanel;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.URLViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

public class SearchAssetsSplitPanel extends SearchItemSplitPanel {

    public static String NAME = "SearchAssetsSplitPanel";
    public static String TITLE = "Search Assets";

    private static final long serialVersionUID = 1L;

    public SearchAssetsSplitPanel(boolean search_and_exchange) {
        super(new ItemAssetsTableModel(), NAME, TITLE);

        // MENU

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.getInstance().translate("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    URLViewer.openWebpage(new URL(Settings.getInstance().getBlockexplorerURL()
                            + "/index/blockexplorer.html"
                            + "?asset=" + itemTableSelected.getKey()));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        });

        JMenuItem sell = new JMenuItem(Lang.getInstance().translate("To sell"));
        sell.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //new ExchangeFrame((AssetCls) th.itemMenu, null, "To sell", "");
                MainPanel.getInstance().insertTab(new ExchangePanel((AssetCls) itemTableSelected, null, "To sell", ""));

            }
        });

        JMenuItem excahge = new JMenuItem(Lang.getInstance().translate("Exchange"));
        excahge.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainPanel.getInstance().insertTab(new ExchangePanel((AssetCls) itemTableSelected, null, "", ""));

            }
        });

        JMenuItem buy = new JMenuItem(Lang.getInstance().translate("Buy"));
        buy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainPanel.getInstance().insertTab(new ExchangePanel((AssetCls) itemTableSelected, null, "Buy", ""));

            }
        });

        JMenuItem vouch_menu = new JMenuItem(Lang.getInstance().translate("Vouch"));
        vouch_menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DCSet db = DCSet.getInstance();
                Transaction trans = db.getTransactionFinalMap().get(itemTableSelected.getReference());

                new VouchRecordDialog(trans.getBlockHeight(), trans.getSeqNo());

            }
        });

        if (search_and_exchange) {
            this.menuTable.add(excahge);
            this.menuTable.addSeparator();
            this.menuTable.add(buy);

            this.menuTable.add(sell);
            this.menuTable.addSeparator();

            this.menuTable.addSeparator();

            this.menuTable.add(vouch_menu);

            menuTable.addSeparator();
            menuTable.add(setSeeInBlockexplorer);
        } else {
            this.menuTable.remove(this.favoriteMenuItems);

            menuTable.addSeparator();
            menuTable.add(setSeeInBlockexplorer);
        }

    }


    //show details
    @Override
    protected Component getShow(ItemCls item) {

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
