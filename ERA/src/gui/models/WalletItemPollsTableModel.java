package gui.models;

import controller.Controller;
import core.item.polls.PollCls;
import database.wallet.PollMap;
import datachain.DCSet;
import datachain.SortableList;
import lang.Lang;
import org.mapdb.Fun.Tuple2;
import utils.ObserverMessage;
import utils.Pair;

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

    private String[] columnNames = Lang.getInstance().translate(new String[]{"Name", "Creator", "Total Votes", "Confirmed"});

    public WalletItemPollsTableModel() {

        addObservers();
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
    public int getColumnCount() {
        return this.columnNames.length;
    }

    @Override
    public String getColumnName(int index) {
        return this.columnNames[index];
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

    public void removeObservers() {

        Controller.getInstance().deleteObserver(this);

    }

    public void addObservers() {
        Controller.getInstance().addWalletListener(this);
    }

    @Override
    public Object getItem(int k) {
        // TODO Auto-generated method stub
        return polls.get(k).getB();
    }
}
