package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.polls.PollCls;
import org.erachain.datachain.DCSet;
import org.mapdb.Fun;

import java.math.BigDecimal;

@SuppressWarnings("serial")
public class WalletItemPollsTableModel extends WalletTableModel<PollCls> {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_TOTAL_VOTES = 3;
    public static final int COLUMN_FAVORITE = 4;

    private Fun.Tuple4<Integer, long[], BigDecimal, BigDecimal[]> votesWithPersons;

    public WalletItemPollsTableModel() {
        super(Controller.getInstance().wallet.database.getPollMap(),
                new String[]{"Key", "Name", "Creator", "Total Votes", "Favorite"},
                null, true, COLUMN_FAVORITE);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        PollCls poll = this.list.get(row);

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
