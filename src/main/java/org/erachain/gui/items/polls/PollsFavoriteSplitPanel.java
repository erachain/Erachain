package org.erachain.gui.items.polls;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PollsFavoriteSplitPanel extends ItemSplitPanel {
    private static final long serialVersionUID = 2717571093561259483L;
    private PollsFavoriteSplitPanel th;

    public PollsFavoriteSplitPanel() {
        super(new FavoritePollsTableModel(), "PollsFavoriteSplitPanel");
        this.setName(Lang.getInstance().translate("Favorite Polls"));
        th = this;
        JMenuItem vouch_menu = new JMenuItem(Lang.getInstance().translate("Vouch"));
        vouch_menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DCSet db = DCSet.getInstance();
                Transaction trans = db.getTransactionFinalMap().get(((TemplateCls) th.itemMenu).getReference());
                new VouchRecordDialog(trans.getBlockHeight(), trans.getSeqNo());

            }
        });
        th.menuTable.add(vouch_menu);
        JMenuItem setVote_Menu = new JMenuItem(Lang.getInstance().translate("Voting"));
        setVote_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//				new UnionSetStatusDialog(th, (UnionCls) itemMenu);

                PollCls poll = (PollCls) (item_Table_Selected);
                AssetCls AssetCls = DCSet.getInstance().getItemAssetMap().get((long) (1));
                new PollsDialog(poll, 0, AssetCls);
            }
        });
        th.menuTable.add(setVote_Menu);
    }

    // show details
    @Override
    public Component getShow(ItemCls item) {
        AssetCls AssetCls = DCSet.getInstance().getItemAssetMap().get((long) (1));
        PollsDetailPanel pollInfo = new PollsDetailPanel((PollCls) item, AssetCls);

        return pollInfo;
    }

}
