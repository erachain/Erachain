package gui.items.assets;

import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.transaction.Transaction;
import datachain.DCSet;
import gui.items.Item_SplitPanel;
import gui.records.VouchRecordDialog;
import lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Assets_Favorite_SplitPanel extends Item_SplitPanel {
    private static final long serialVersionUID = 2717571093561259483L;
    private static TableModelItemAssetsFavorute table_Model = new TableModelItemAssetsFavorute();
    private Assets_Favorite_SplitPanel th;

    public Assets_Favorite_SplitPanel() {
        super(table_Model, "Assets_Favorite_SplitPanel");
        this.setName(Lang.getInstance().translate("Favorite Persons"));
        th = this;
        JMenuItem sell = new JMenuItem(Lang.getInstance().translate("To sell"));
        sell.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ExchangeFrame((AssetCls) th.item_Menu, null, "To sell", "");
            }
        });

        JMenuItem excahge = new JMenuItem(Lang.getInstance().translate("Exchange"));
        excahge.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ExchangeFrame((AssetCls) th.item_Menu, null, "", "");
            }
        });

        JMenuItem buy = new JMenuItem(Lang.getInstance().translate("Buy"));
        buy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ExchangeFrame((AssetCls) th.item_Menu, null, "Buy", "");
            }
        });

        JMenuItem vouch_menu = new JMenuItem(Lang.getInstance().translate("Vouch"));
        vouch_menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DCSet db = DCSet.getInstance();
                Transaction trans = db.getTransactionFinalMap()
                        .getTransaction(((AssetCls) th.item_Menu).getReference());
                new VouchRecordDialog(trans.getBlockHeight(db), trans.getSeqNo(db));

            }
        });
        th.menu_Table.addSeparator();
        th.menu_Table.add(excahge);
        th.menu_Table.addSeparator();
        th.menu_Table.add(buy);
        th.menu_Table.add(sell);
        th.menu_Table.addSeparator();
        th.menu_Table.add(vouch_menu);
    }

    // show details
    @Override
    public Component get_show(ItemCls item) {
        return new Asset_Info((AssetCls) item);
    }

    @Override
    protected void splitClose() {
        table_Model.removeObservers();

    }

}
