package org.erachain.gui.items.polls;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PollsFavoriteSplitPanel extends ItemSplitPanel {

    public static String NAME = "PollsFavoriteSplitPanel";
    public static String TITLE = "Favorite Polls";

    private static final long serialVersionUID = 2717571093561259483L;

    public PollsFavoriteSplitPanel() {
        super(new FavoritePollsTableModel(), NAME, TITLE);
        iconName = "favorite.png";

        JMenuItem setVote_Menu = new JMenuItem(Lang.getInstance().translate("To Vote"));
        setVote_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                PollCls poll = (PollCls) (itemTableSelected);
                AssetCls AssetCls = DCSet.getInstance().getItemAssetMap().get((long) (1));
                new PollsDialog(poll, 0, AssetCls);
            }
        });
        menuTable.add(setVote_Menu);

        menuTable.addSeparator();

        JMenuItem setStatus_Menu = new JMenuItem(Lang.getInstance().translate("Set status"));
        setStatus_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //			new UnionSetStatusDialog(th, (UnionCls) itemMenu);
            }
        });
        this.menuTable.add(setStatus_Menu);

    }

    // show details
    @Override
    public Component getShow(ItemCls item) {
        AssetCls AssetCls = DCSet.getInstance().getItemAssetMap().get((long) (1));
        PollsDetailPanel pollInfo = new PollsDetailPanel((PollCls) item, AssetCls);

        return pollInfo;
    }

}