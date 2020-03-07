package org.erachain.gui.items.assets;

import com.sun.javafx.binding.SelectBinding;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.library.MainPanelInterface;
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

public class AssetsFavoriteSplitPanel extends ItemSplitPanel  implements MainPanelInterface {
    private static final long serialVersionUID = 2717571093561259483L;
    private String iconFile = "images/pageicons/AssetsFavoriteSplitPanel.png";

    public AssetsFavoriteSplitPanel() {
        super(new FavoriteAssetsTableModel(), "AssetsFavoriteSplitPanel");
        setName(Lang.getInstance().translate("Favorite Assets"));

        JMenuItem sell = new JMenuItem(Lang.getInstance().translate("To sell"));

        sell.addActionListener(e ->
                //new ExchangeFrame((AssetCls) itemMenu, null, "To sell", "")
                MainPanel.getInstance().insertTab(new ExchangePanel((AssetCls) itemTableSelected, null, "To sell", ""))
        );

        JMenuItem exchange = new JMenuItem(Lang.getInstance().translate("Exchange"));
        exchange.addActionListener(e ->
                MainPanel.getInstance().insertTab(new ExchangePanel((AssetCls) itemTableSelected, null, "", ""))
        );

        JMenuItem buy = new JMenuItem(Lang.getInstance().translate("Buy"));
        buy.addActionListener(e ->
                //new ExchangeFrame((AssetCls) itemMenu, null, "Buy", "")
                MainPanel.getInstance().insertTab(new ExchangePanel((AssetCls) itemTableSelected, null, "Buy", ""))
        );

        JMenuItem vouchMenu = new JMenuItem(Lang.getInstance().translate("Vouch"));
        vouchMenu.addActionListener(e -> {
            DCSet db = DCSet.getInstance();
            Transaction transaction = db.getTransactionFinalMap().get(itemTableSelected.getReference());
            new VouchRecordDialog(transaction.getBlockHeight(), transaction.getSeqNo());

        });

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.getInstance().translate("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) {
                    return;
                }

                try {
                    URLViewer.openWebpage(new URL("http://" + Settings.getInstance().getBlockexplorerURL()
                            + ":" + Settings.getInstance().getWebPort() + "/index/blockexplorer.html"
                            + "?asset=" + itemTableSelected.getKey()));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        });

        menuTable.add(exchange);
        menuTable.addSeparator();
        menuTable.add(buy);
        menuTable.add(sell);
        menuTable.addSeparator();
        menuTable.add(vouchMenu);

        menuTable.addSeparator();
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
        MainPanel.getInstance().insertTab(panel);
    }
    @Override
    public Icon getIcon() {
        {
            try {
                return new ImageIcon(Toolkit.getDefaultToolkit().getImage(iconFile));
            } catch (Exception e) {
                return null;
            }
        }
    }
}
