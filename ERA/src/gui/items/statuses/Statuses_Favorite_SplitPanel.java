package gui.items.statuses;

import core.item.ItemCls;
import core.item.statuses.StatusCls;
import core.transaction.Transaction;
import datachain.DCSet;
import gui.items.Item_SplitPanel;
import gui.records.VouchRecordDialog;
import lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Statuses_Favorite_SplitPanel extends Item_SplitPanel {
    private static final long serialVersionUID = 2717571093561259483L;
    private static Statuses_Favorite_TableModel table_Model = new Statuses_Favorite_TableModel();
    private Statuses_Favorite_SplitPanel th;

    public Statuses_Favorite_SplitPanel() {
        super(table_Model, "Statuses_Favorite_SplitPanel");
        this.setName(Lang.getInstance().translate("Favorite Statuses"));
        th = this;
        JMenuItem vouch_menu = new JMenuItem(Lang.getInstance().translate("Vouch"));
        vouch_menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DCSet db = DCSet.getInstance();
                Transaction trans = db.getTransactionFinalMap().getBySignature(((StatusCls) th.item_Menu).getReference());
                new VouchRecordDialog(trans.getBlockHeight(), trans.getSeqNo(db));

            }
        });
        th.menu_Table.add(vouch_menu);
    }

    // show details
    @Override
    public Component get_show(ItemCls item) {
        Status_Info info = new Status_Info();
        info.show_001((StatusCls) item);
        return info;
    }

    @Override
    protected void splitClose() {
        table_Model.removeObservers();

    }
}
