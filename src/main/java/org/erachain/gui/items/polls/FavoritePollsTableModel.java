package org.erachain.gui.items.polls;

import org.erachain.controller.Controller;
import org.erachain.core.item.polls.PollCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.utils.ObserverMessage;

import java.util.*;

@SuppressWarnings("serial")
public class FavoritePollsTableModel extends FavoriteItemModelTable implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    public FavoritePollsTableModel() {
        super(DCSet.getInstance().getItemPollMap(),
                Controller.getInstance().wallet.database.getPollFavoritesSet(),
                new String[]{"Key", "Name", "Publisher", "Confirmed", "Favorite"},
                new Boolean[]{false, true, true, false, false},
                ObserverMessage.WALLET_RESET_POLL_FAVORITES_TYPE,
                ObserverMessage.WALLET_ADD_POLL_FAVORITES_TYPE,
                ObserverMessage.WALLET_DELETE_POLL_FAVORITES_TYPE,
                ObserverMessage.WALLET_LIST_POLL_FAVORITES_TYPE,
                COLUMN_FAVORITE);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        PollCls status = (PollCls) this.list.get(row);
        if (status == null)
            return null;


        switch (column) {
            case COLUMN_KEY:

                return status.getKey(DCSet.getInstance());

            case COLUMN_NAME:

                return status.viewName();

            case COLUMN_ADDRESS:

                return status.getOwner().getPersonAsString();

            case COLUMN_CONFIRMED:

                return status.isConfirmed();

            case COLUMN_FAVORITE:

                return status.isFavorite();

        }

        return null;
    }

}
