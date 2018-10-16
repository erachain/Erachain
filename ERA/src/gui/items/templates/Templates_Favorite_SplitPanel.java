package gui.items.templates;

import core.item.ItemCls;
import core.item.templates.TemplateCls;
import core.transaction.Transaction;
import datachain.DCSet;
import gui.items.Item_SplitPanel;
import gui.records.VouchRecordDialog;
import lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Templates_Favorite_SplitPanel extends Item_SplitPanel {
    private static final long serialVersionUID = 2717571093561259483L;
    private static Templates_Favorite_TableModel table_Model = new Templates_Favorite_TableModel();
    private Templates_Favorite_SplitPanel th;

    public Templates_Favorite_SplitPanel() {
        super(table_Model, "Templates_Favorite_SplitPanel");
        this.setName(Lang.getInstance().translate("Favorite Templates"));
        th = this;
        JMenuItem vouch_menu = new JMenuItem(Lang.getInstance().translate("Vouch"));
        vouch_menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DCSet db = DCSet.getInstance();
                Transaction trans = db.getTransactionFinalMap().get(((TemplateCls) th.item_Menu).getReference());
                new VouchRecordDialog(trans.getBlockHeight(), trans.getSeqNo(db));

            }
        });
        th.menu_Table.add(vouch_menu);
    }

    // show details
    @Override
    public Component get_show(ItemCls item) {
        return new Info_Templates((TemplateCls) item);
    }

    @Override
    protected void splitClose() {
        table_Model.removeObservers();

    }
}
