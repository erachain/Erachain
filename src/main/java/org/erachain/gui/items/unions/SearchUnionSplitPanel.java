package org.erachain.gui.items.unions;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.gui.items.SearchItemSplitPanel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SearchUnionSplitPanel extends SearchItemSplitPanel {

    public static String NAME = "SearchUnionSplitPanel";
    public static String TITLE = "Search Unions";

    private static final long serialVersionUID = 1L;
    private SearchUnionSplitPanel th;

    public SearchUnionSplitPanel() {
        super(new TableModelUnionsItemsTableModel(), NAME, TITLE);
        th = this;

        JMenuItem vouch_Item = new JMenuItem(Lang.T("Sign / Vouch"));

        // ADD MENU ITEMS
        JMenuItem confirm_Menu = new JMenuItem(Lang.T("Confirm"));
        confirm_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new UnionConfirmDialog(th, (UnionCls) itemTableSelected);
            }
        });
        this.menuTable.add(confirm_Menu);

        JMenuItem setStatus_Menu = new JMenuItem(Lang.T("Set status"));
        setStatus_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new UnionSetStatusDialog(th, (UnionCls) itemTableSelected);
            }
        });
        this.menuTable.add(setStatus_Menu);

    }

    // show details
    @Override
    public Component getShow(ItemCls item) {

        UnionInfo union_Info = new UnionInfo();
        union_Info.show_Union_001((UnionCls) item);
        return union_Info;

    }

}