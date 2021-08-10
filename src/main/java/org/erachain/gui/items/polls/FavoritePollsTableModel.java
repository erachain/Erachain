package org.erachain.gui.items.polls;

import org.erachain.controller.Controller;
import org.erachain.core.item.polls.PollCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.utils.ObserverMessage;

import java.util.Observer;

@SuppressWarnings("serial")
public class FavoritePollsTableModel extends FavoriteItemModelTable implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_TOTAL_VOTES = 3;
    public static final int COLUMN_FAVORITE = 4;


    public FavoritePollsTableModel() {
        super(DCSet.getInstance().getItemPollMap(),
                Controller.getInstance().getWallet().dwSet.getPollFavoritesSet(),
                new String[]{"Key", "Name", "Publisher", "Votes", "Favorite"},
                new Boolean[]{false, true, true, false, false},
                ObserverMessage.WALLET_RESET_POLL_FAVORITES_TYPE,
                ObserverMessage.WALLET_ADD_POLL_FAVORITES_TYPE,
                ObserverMessage.WALLET_DELETE_POLL_FAVORITES_TYPE,
                ObserverMessage.WALLET_LIST_POLL_FAVORITES_TYPE,
                COLUMN_FAVORITE);
    }

    @Override
    protected void updateMap() {
        favoriteMap = Controller.getInstance().getWallet().dwSet.getPollFavoritesSet();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        PollCls poll = (PollCls) this.list.get(row);
        if (poll == null)
            return null;


        switch (column) {
            case COLUMN_CONFIRMATIONS:
                return poll.getConfirmations(dcSet);

            case COLUMN_KEY:
                return poll.getKey();

            case COLUMN_NAME:
                return poll;

            case COLUMN_ADDRESS:
                return poll.getMaker().getPersonAsString();

            case COLUMN_TOTAL_VOTES:
                return DCSet.getInstance().getVoteOnItemPollMap().countVotes(poll.getKey());

            case COLUMN_FAVORITE:
                return poll.isFavorite();

        }

        return null;
    }

}
