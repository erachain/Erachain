package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.voting.Poll;

import java.math.BigDecimal;

@SuppressWarnings("serial")
public class WalletVotesTableModel extends WalletTableModel<Poll> {
    public static final int COLUMN_NAME = 0;
    public static final int COLUMN_ADDRESS = 1;
    public static final int COLUMN_TOTAL_VOTES = 2;
    private static final int COLUMN_CONFIRMED = 3;

    public WalletVotesTableModel() {
        super(Controller.getInstance().getWallet().dwSet.getPollMap(),
                new String[]{"Name", "Creator", "Total Votes", "Confirmed"}, null, true, -1);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        Poll poll = this.list.get(row);

        switch (column) {
            case COLUMN_NAME:

                return poll.getName();

            case COLUMN_ADDRESS:

                return poll.getCreator().getPersonAsString();

            case COLUMN_TOTAL_VOTES:

                BigDecimal amo = poll.getTotalVotes();
                if (amo == null)
                    return BigDecimal.ZERO;
                return amo;


            case COLUMN_CONFIRMED:

                return poll.isConfirmed();

        }

        return null;
    }

}
