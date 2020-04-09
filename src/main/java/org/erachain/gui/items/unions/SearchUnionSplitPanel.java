package org.erachain.gui.items.unions;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.gui.items.SearchItemSplitPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.URLViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

public class SearchUnionSplitPanel extends SearchItemSplitPanel {
    /**
     *
     */
    private static String iconFile = Settings.getInstance().getPatnIcons() + "SearchUnionSplitPanel.png";
    private static final long serialVersionUID = 1L;
    private static TableModelUnionsItemsTableModel tableModelUnions = new TableModelUnionsItemsTableModel();
    private SearchUnionSplitPanel th;

    public SearchUnionSplitPanel() {
        super(tableModelUnions, "SearchUnionSplitPanel", "SearchUnionSplitPanel");
        th = this;

        JMenuItem vouch_Item = new JMenuItem(Lang.getInstance().translate("Vouch"));

        // ADD MENU ITEMS
        JMenuItem confirm_Menu = new JMenuItem(Lang.getInstance().translate("Confirm"));
        confirm_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new UnionConfirmDialog(th, (UnionCls) itemTableSelected);
            }
        });
        this.menuTable.add(confirm_Menu);

        JMenuItem setStatus_Menu = new JMenuItem(Lang.getInstance().translate("Set status"));
        setStatus_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new UnionSetStatusDialog(th, (UnionCls) itemTableSelected);
            }
        });
        this.menuTable.add(setStatus_Menu);

        menuTable.addSeparator();

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.getInstance().translate("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    URLViewer.openWebpage(new URL("http://" + Settings.getInstance().getBlockexplorerURL()
                            + ":" + Settings.getInstance().getWebPort() + "/index/blockexplorer.html"
                            + "?union=" + itemTableSelected.getKey()));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        });
        menuTable.add(setSeeInBlockexplorer);

    }

    // show details
    @Override
    public Component getShow(ItemCls item) {

        UnionInfo union_Info = new UnionInfo();
        union_Info.show_Union_001((UnionCls) item);
        return union_Info;

    }

    public static Image getIcon() {
        {
            try {
                return Toolkit.getDefaultToolkit().getImage(iconFile);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
