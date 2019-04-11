package org.erachain.gui.items.assets;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.SearchItemSplitPanel;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SearchAssetsSplitPanel extends SearchItemSplitPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    //private static ItemAssetsTableModel tableModelItemAssets = ;
    private SearchAssetsSplitPanel th;


    public SearchAssetsSplitPanel(boolean search_and_exchange) {
        super(new ItemAssetsTableModel(), "SearchAssetsSplitPanel", "SearchAssetsSplitPanel");
        th = this;
        setName(Lang.getInstance().translate("Search Assets"));


        // MENU


        JMenuItem sell = new JMenuItem(Lang.getInstance().translate("To sell"));
        sell.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ExchangeFrame((AssetCls) th.itemMenu, null, "To sell", "");
            }
        });


        JMenuItem excahge = new JMenuItem(Lang.getInstance().translate("Exchange"));
        excahge.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ExchangeFrame((AssetCls) th.itemMenu, null, "", "");
            }
        });


        JMenuItem buy = new JMenuItem(Lang.getInstance().translate("Buy"));
        buy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ExchangeFrame((AssetCls) th.itemMenu, null, "Buy", "");
            }
        });


        JMenuItem vouch_menu = new JMenuItem(Lang.getInstance().translate("Vouch"));
        vouch_menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DCSet db = DCSet.getInstance();
                Transaction trans = db.getTransactionFinalMap().get(th.itemMenu.getReference());

                new VouchRecordDialog(trans.getBlockHeight(), trans.getSeqNo());

            }
        });


//	nameSalesMenu.add(favorite);
        if (search_and_exchange) {
            this.menuTable.add(excahge);
            this.menuTable.addSeparator();
            this.menuTable.add(buy);

            this.menuTable.add(sell);
            this.menuTable.addSeparator();

            this.menuTable.addSeparator();

            this.menuTable.add(vouch_menu);
        } else {
            this.menuTable.remove(this.favorite_menu_items);
        }


    }


    //show details
    @Override
    protected Component getShow(ItemCls item) {

        return new AssetInfo((AssetCls) item);

    }

    // mouse 2 click
    @Override
    protected void table_mouse_2_Click(ItemCls item) {

        new ExchangeFrame((AssetCls) item, null, "", "");
    }

}
