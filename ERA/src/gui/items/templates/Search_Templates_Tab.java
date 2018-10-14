package gui.items.templates;

import core.item.ItemCls;
import core.item.templates.TemplateCls;
import core.transaction.Transaction;
import datachain.DCSet;
import gui.items.Item_Search_SplitPanel;
import gui.records.VouchRecordDialog;
import lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class Search_Templates_Tab extends Item_Search_SplitPanel {
    private static TableModelTemplates tableModelTemplates = new TableModelTemplates();
    private Search_Templates_Tab th;


    public Search_Templates_Tab() {
        super(tableModelTemplates, "Search_Templates_Tab", "Search_Templates_Tab");
        this.th = this;
        setName(Lang.getInstance().translate("Search Templates"));
        JMenuItem vouch_Item = new JMenuItem(Lang.getInstance().translate("Vouch"));

        vouch_Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                TemplateCls template = (TemplateCls) th.item_Menu;
                if (template == null) return;
                Transaction trans = DCSet.getInstance().getTransactionFinalMap().getBySignature(template.getReference());
                new VouchRecordDialog(trans.getBlockHeight(), trans.getSeqNo(DCSet.getInstance()));
            }
        });
        this.menu_Table.add(vouch_Item);
    }


    //show details
    @Override
    protected Component get_show(ItemCls item) {
        return new Info_Templates((TemplateCls) item);

    }


}
