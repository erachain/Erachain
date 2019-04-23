package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.database.SortableList;
import org.erachain.database.wallet.PollMap;
import org.erachain.datachain.DCSet;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;

import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletItemPollsTableModel extends WalletAutoKeyTableModel<Tuple2<Long, Long>, Tuple2<Long, PollCls>> {
    public static final int COLUMN_NAME = 0;
    public static final int COLUMN_ADDRESS = 1;
    public static final int COLUMN_TOTAL_VOTES = 2;
    private static final int COLUMN_CONFIRMED = 3;

    public WalletItemPollsTableModel() {
        super(Controller.getInstance().wallet.database.getPollMap(),
                new String[]{"Name", "Creator", "Total Votes", "Confirmed"},
                null,true);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.listSorted == null || row > this.listSorted.size() - 1) {
            return null;
        }

        Pair<Tuple2<Long , Long>, Tuple2<Long, PollCls>> pair = this.listSorted.get(row);
        if (pair == null) {
            return null;
        }

        PollCls poll = pair.getB().b;

        switch (column) {
            case COLUMN_NAME:

                return poll.getName();

            case COLUMN_ADDRESS:

                return poll.getOwner().getPersonAsString();

            case COLUMN_TOTAL_VOTES:

                BigDecimal amo = poll.getTotalVotes(DCSet.getInstance());
                if (amo == null)
                    return BigDecimal.ZERO;
                return amo;


            case COLUMN_CONFIRMED:

                return poll.isConfirmed();

        }

        return null;
    }

}
