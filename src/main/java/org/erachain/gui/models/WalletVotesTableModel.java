package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.voting.Poll;
import org.erachain.database.SortableList;
import org.erachain.database.wallet.PollMap;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;

import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletVotesTableModel extends SortedListTableModelCls<Tuple2<String, String>, Poll> implements Observer {
    public static final int COLUMN_NAME = 0;
    public static final int COLUMN_ADDRESS = 1;
    public static final int COLUMN_TOTAL_VOTES = 2;
    private static final int COLUMN_CONFIRMED = 3;

    private SortableList<Tuple2<String, String>, Poll> polls;

    public WalletVotesTableModel() {
        super(new String[]{"Name", "Creator", "Total Votes", "Confirmed"}, true);
    }

    @Override
    public SortableList<Tuple2<String, String>, Poll> getSortableList() {
        return polls;
    }

    public Poll getPoll(int row) {
        return polls.get(row).getB();
    }

    @Override
    public int getRowCount() {

        return (this.polls == null) ? 0 : this.polls.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.polls == null || row > this.polls.size() - 1) {
            return null;
        }

        Pair<Tuple2<String, String>, Poll> data = this.polls.get(row);

        if (data == null || data.getB() == null) {
            return -1;
        }
        Poll poll = data.getB();

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

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.WALLET_LIST_POLL_TYPE) {
            if (this.polls == null) {
                this.polls = (SortableList<Tuple2<String, String>, Poll>) message.getValue();
                //this.polls.registerObserver();
                this.polls.sort(PollMap.NAME_INDEX);
            }

            this.fireTableDataChanged();
        }

        //CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.WALLET_ADD_POLL_TYPE || message.getType() == ObserverMessage.WALLET_REMOVE_POLL_TYPE || message.getType() == ObserverMessage.WALLET_RESET_POLL_TYPE) {
            this.fireTableDataChanged();
        }
    }


    public void addObservers() {

        super.addObservers();

        if (Controller.getInstance().doesWalletDatabaseExists())
            return;

        map.addObserver(this);

    }

    public void deleteObservers() {
        super.deleteObservers();

        if (Controller.getInstance().doesWalletDatabaseExists())
            return;

        map.deleteObserver(this);
    }

}
