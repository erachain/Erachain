package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.voting.Poll;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.PollMap;
import org.erachain.utils.ObserverMessage;

import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class PollsTableModel extends TableModelCls<String, Poll> implements Observer {
    public static final int COLUMN_NAME = 0;
    public static final int COLUMN_VOTES = 2;
    private static final int COLUMN_CREATOR = 1;
    private AssetCls asset;

    private SortableList<String, Poll> polls;
    private PollMap db;

    public PollsTableModel() {
        super(DCSet.getInstance().getPollMap(), "PollsTableModel", 2000,
                new String[]{"Name", "Creator", "Total Votes"}, null, false);
    }

    public void setAsset(AssetCls asset) {
        this.asset = asset;
        this.fireTableDataChanged();
    }

    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    @Override
    public SortableList<String, Poll> getSortableList() {
        return this.polls;
    }

    public Poll getPoll(int row) {
        return this.polls.get(row).getB();
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

        Poll poll = this.polls.get(row).getB();

        switch (column) {
            case COLUMN_NAME:

                String key = poll.getName();

                //CHECK IF ENDING ON A SPACE
                if (key.endsWith(" ")) {
                    key = key.substring(0, key.length() - 1);
                    key += ".";
                }

                return key;

            case COLUMN_CREATOR:

                return poll.getCreator().getPersonAsString();

            case COLUMN_VOTES:

                BigDecimal amo = poll.getTotalVotes(this.asset.getKey(DCSet.getInstance()));
                if (amo == null)
                    return BigDecimal.ZERO;
                return amo;


        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.LIST_POLL_TYPE) {
            if (this.polls == null) {
                this.polls = (SortableList<String, Poll>) message.getValue();
                this.polls.registerObserver();
            }

            this.fireTableDataChanged();
        }

        //CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.ADD_POLL_TYPE || message.getType() == ObserverMessage.REMOVE_POLL_TYPE) {
            this.fireTableDataChanged();
        }
    }

    public void addObservers() {
        this.asset = Controller.getInstance().getAsset(AssetCls.FEE_KEY);
        //Controller.getInstance().addObserver(this);
        db = DCSet.getInstance().getPollMap();
        polls = db.getList();
    }

    public void deleteObservers() {
        //if(this.polls!=null)this.polls.removeObserver();
        //DCSet.getInstance().getPollMap().deleteObserver(this);
    }

    @Override
    public Poll getItem(int k) {
        // TODO Auto-generated method stub
        return this.polls.get(k).getB();
    }

    public void getIntervalThis(long startBack, long endBack) {
    }

}
