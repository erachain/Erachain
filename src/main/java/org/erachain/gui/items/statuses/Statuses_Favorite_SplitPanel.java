package org.erachain.gui.items.statuses;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.Item_SplitPanel;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Statuses_Favorite_SplitPanel extends Item_SplitPanel {
    private static final long serialVersionUID = 2717571093561259483L;
    private Statuses_Favorite_SplitPanel th;

    public Statuses_Favorite_SplitPanel() {
        super(new FavoriteStatusesTableModel(), "Statuses_Favorite_SplitPanel");
        this.setName(Lang.getInstance().translate("Favorite Statuses"));
        th = this;
        JMenuItem vouch_menu = new JMenuItem(Lang.getInstance().translate("Vouch"));
        vouch_menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DCSet db = DCSet.getInstance();
                Transaction trans = db.getTransactionFinalMap().get(((StatusCls) th.item_Menu).getReference());
                new VouchRecordDialog(trans.getBlockHeight(), trans.getSeqNo());

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

}