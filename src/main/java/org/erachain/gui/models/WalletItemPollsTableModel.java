package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.polls.PollCls;
import org.erachain.database.SortableList;
import org.erachain.database.wallet.PollMap;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;

import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletItemPollsTableModel extends TableModelCls<Tuple2<String, String>, PollCls> implements Observer {
    public static final int COLUMN_NAME = 0;
    public static final int COLUMN_ADDRESS = 1;
    public static final int COLUMN_TOTAL_VOTES = 2;
    private static final int COLUMN_CONFIRMED = 3;

    private SortableList<Tuple2<String, String>, PollCls> polls;

    public WalletItemPollsTableModel() {
        super("WalletItemPollsTableModel", 1000,
                new String[]{"Name", "Creator", "Total Votes", "Confirmed"});
    }

    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    @Override
    public SortableList<Tuple2<String, String>, PollCls> getSortableList() {
        return polls;
    }

    public PollCls getPoll(int row) {
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

        Pair<Tuple2<String, String>, PollCls> data = this.polls.get(row);

        if (data == null || data.getB() == null) {
            return -1;
        }
        PollCls poll = data.getB();

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

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            //GUI ERROR
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.LIST_POLL_TYPE) {
            if (this.polls == null) {
                this.polls = (SortableList<Tuple2<String, String>, PollCls>) message.getValue();
                this.polls.registerObserver();
                this.polls.sort(PollMap.NAME_INDEX);
            }

            this.fireTableDataChanged();
        }

        //CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.ADD_POLL_TYPE || message.getType() == ObserverMessage.REMOVE_POLL_TYPE) {
            this.fireTableDataChanged();
        }
    }

    public void addObserversThis() {
        Controller.getInstance().addWalletListener(this);
    }

    public void removeObserversThis() {
        Controller.getInstance().deleteObserver(this);
    }

    @Override
    public Object getItem(int k) {
        // TODO Auto-generated method stub
        return polls.get(k).getB();
    }
}
