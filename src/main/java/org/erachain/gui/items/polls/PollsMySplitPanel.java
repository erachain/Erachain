package org.erachain.gui.items.polls;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.models.WalletItemPollsTableModel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PollsMySplitPanel extends ItemSplitPanel {

    public static String NAME = "PollsMySplitPanel";
    public static String TITLE = "My Polls";

    public PollsMySplitPanel() {
        super(new WalletItemPollsTableModel(), NAME, TITLE);

        JMenuItem setVote_Menu = new JMenuItem(Lang.T("To Vote"));
        setVote_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                PollCls poll = (PollCls) (itemTableSelected);
                AssetCls AssetCls = DCSet.getInstance().getItemAssetMap().get((long) (1));
                new PollsDialog(poll, 0, AssetCls);
            }
        });
        menuTable.add(setVote_Menu);

        menuTable.addSeparator();

        JMenuItem setStatus_Menu = new JMenuItem(Lang.T("Set status"));
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