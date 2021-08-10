package org.erachain.gui.items.polls;

import org.erachain.core.item.polls.PollCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.SearchItemsTableModel;

@SuppressWarnings("serial")
public class PollsItemsTableModel extends SearchItemsTableModel {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_TOTAL_VOTES = 3;
    public static final int COLUMN_FAVORITE = 4;

    public PollsItemsTableModel() {
        super(DCSet.getInstance().getItemPollMap(), new String[]{"Key", "Name", "Creator", "Votes", "Favorite"},
                null, COLUMN_FAVORITE);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        PollCls poll = (PollCls) this.list.get(row);

        switch (column) {
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
