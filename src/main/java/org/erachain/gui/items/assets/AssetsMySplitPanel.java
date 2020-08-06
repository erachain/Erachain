package org.erachain.gui.items.assets;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.models.WalletItemAssetsTableModel;
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


public class AssetsMySplitPanel extends ItemSplitPanel {
    private static final long serialVersionUID = 2717571093561259483L;

    public AssetsMySplitPanel() {
        super(new WalletItemAssetsTableModel(), "AssetsMySplitPanel", "My Assets");

        //      add items in menu
        JMenuItem sell = new JMenuItem(Lang.getInstance().translate("To sell"));
        sell.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = (AssetCls) itemTableSelected;
                MainPanel.getInstance().insertTab(
                        new ExchangePanel(asset, null, "To sell", ""));

            }
        });

        JMenuItem excahge = new JMenuItem(Lang.getInstance().translate("Exchange"));
        excahge.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = (AssetCls) itemTableSelected;
                MainPanel.getInstance().insertTab(
                        new ExchangePanel(asset, null, "", ""));
            }
        });

        JMenuItem buy = new JMenuItem(Lang.getInstance().translate("Buy"));
        buy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = (AssetCls) itemTableSelected;
                MainPanel.getInstance().insertTab(
                        new ExchangePanel(asset, null, "Buy", ""));

            }
        });

        this.menuTable.addSeparator();
        this.menuTable.add(excahge);
        this.menuTable.add(buy);
        this.menuTable.add(sell);

        this.menuTable.addSeparator();

        JMenuItem set_Status_Item = new JMenuItem(Lang.getInstance().translate("Set Status to Asset"));

        set_Status_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                @SuppressWarnings("unused")
                AssetSetStatusDialog fm = new AssetSetStatusDialog((AssetCls) itemTableSelected);

            }
        });
        this.menuTable.add(set_Status_Item);

        JMenuItem vouchAsset_Item = new JMenuItem(Lang.getInstance().translate("Vouch the Asset Info"));
        vouchAsset_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                AssetCls asset = (AssetCls) itemTableSelected;
                byte[] ref = asset.getReference();
                Transaction transaction = Transaction.findByDBRef(DCSet.getInstance(), ref);
                int blockNo = transaction.getBlockHeight();
                int recNo = transaction.getSeqNo();
                new VouchRecordDialog(blockNo, recNo);

            }
        });
        this.menuTable.add(vouchAsset_Item);

        menuTable.addSeparator();

        JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
        details.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = (AssetCls) itemTableSelected;
                //			new AssetFrame(asset);
            }
        });
        //	assetsMenu.add(details);

        JMenuItem dividend = new JMenuItem(Lang.getInstance().translate("Pay dividend"));
        dividend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = (AssetCls) itemTableSelected;
                new PayDividendFrame(asset);
            }
        });
        menuTable.add(dividend);

        menuTable.addSeparator();

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

        menuTable.add(setSeeInBlockexplorer);

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
