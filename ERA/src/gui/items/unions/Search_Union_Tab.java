package gui.items.unions;

import core.item.ItemCls;
import core.item.unions.UnionCls;
import gui.items.Item_Search_SplitPanel;
import lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Search_Union_Tab extends Item_Search_SplitPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static TableModelUnions tableModelUnions = new TableModelUnions();
    private Search_Union_Tab th;

    public Search_Union_Tab() {
        super(tableModelUnions, "Search_Union_Tab", "Search_Union_Tab");
        th = this;

        // ADD MENU ITEMS
        JMenuItem confirm_Menu = new JMenuItem(Lang.getInstance().translate("Confirm"));
        confirm_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new UnionConfirmDialog(th, (UnionCls) item_Menu);
            }
        });
        this.menu_Table.add(confirm_Menu);

        JMenuItem setStatus_Menu = new JMenuItem(Lang.getInstance().translate("Set Status"));
        setStatus_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new UnionSetStatusDialog(th, (UnionCls) item_Menu);
            }
        });
        this.menu_Table.add(setStatus_Menu);

    }

    // show details
    @Override
    public Component get_show(ItemCls item) {

        Union_Info union_Info = new Union_Info();
        union_Info.show_Union_001((UnionCls) item);
        return union_Info;

    }

}
