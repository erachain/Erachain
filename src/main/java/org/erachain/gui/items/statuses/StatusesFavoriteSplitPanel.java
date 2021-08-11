package org.erachain.gui.items.statuses;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.gui.items.ItemSplitPanel;

import java.awt.*;

public class StatusesFavoriteSplitPanel extends ItemSplitPanel {

    public static String NAME = "StatusesFavoriteSplitPanel";
    public static String TITLE = "Favorite Statuses";

    private static final long serialVersionUID = 2717571093561259483L;

    public StatusesFavoriteSplitPanel() {
        super(new FavoriteStatusesTableModel(), NAME, TITLE);
        iconName = "favorite.png";

        /*
        JMenuItem vouch_menu = new JMenuItem(Lang.T("Sign / Vouch"));
        vouch_menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DCSet db = DCSet.getInstance();
                Transaction trans = db.getTransactionFinalMap().get(itemTableSelected.getReference());
                new VouchTransactionDialog(trans.getBlockHeight(), trans.getSeqNo());

            }
        });
        menuTable.add(vouch_menu);

         */

    }

    // show details
    @Override
    public Component getShow(ItemCls item) {
        return new StatusInfo((StatusCls) item);
    }
}