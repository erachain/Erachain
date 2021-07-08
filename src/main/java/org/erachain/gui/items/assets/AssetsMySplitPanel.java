package org.erachain.gui.items.assets;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.items.statement.IssueDocumentPanel;
import org.erachain.gui.models.WalletItemAssetsTableModel;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class AssetsMySplitPanel extends ItemSplitPanel {

    public static String NAME = "AssetsMySplitPanel";
    public static String TITLE = "My Assets";

    private static final long serialVersionUID = 2717571093561259483L;

    public AssetsMySplitPanel() {
        super(new WalletItemAssetsTableModel(), NAME, TITLE);

        //      add items in menu
        JMenuItem sell = new JMenuItem(Lang.T("To sell"));
        sell.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = (AssetCls) itemTableSelected;
                MainPanel.getInstance().insertTab(
                        new ExchangePanel(asset, null, "To sell", ""));

            }
        });

        JMenuItem excahge = new JMenuItem(Lang.T("Exchange"));
        excahge.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = (AssetCls) itemTableSelected;
                MainPanel.getInstance().insertTab(
                        new ExchangePanel(asset, null, "", ""));
            }
        });

        JMenuItem buy = new JMenuItem(Lang.T("Buy"));
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

        JMenuItem set_Status_Item = new JMenuItem(Lang.T("Set Status to Asset"));

        set_Status_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                @SuppressWarnings("unused")
                AssetSetStatusDialog fm = new AssetSetStatusDialog((AssetCls) itemTableSelected);

            }
        });
        this.menuTable.add(set_Status_Item);

        JMenuItem details = new JMenuItem(Lang.T("Details"));
        details.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = (AssetCls) itemTableSelected;
                //			new AssetFrame(asset);
            }
        });
        menuTable.add(details);

        JMenuItem accruals = new JMenuItem(Lang.T("Make Accruals"));
        accruals.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = (AssetCls) itemTableSelected;
                IssueDocumentPanel panel = new IssueDocumentPanel(asset.getMaker(), asset);
                panel.selectAccruals(asset, null);
                MainPanel.getInstance().insertNewTab(Lang.T("Make Accruals"), panel);
            }
        });
        menuTable.add(accruals);

        JMenuItem dividend = new JMenuItem(Lang.T("Pay Dividend"));
        dividend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = (AssetCls) itemTableSelected;
                IssueDocumentPanel panel = new IssueDocumentPanel(asset.getMaker(), null);
                panel.selectAccruals(null, asset);
                MainPanel.getInstance().insertNewTab(Lang.T("Pay Dividend"), panel);
            }
        });
        menuTable.add(dividend);

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
