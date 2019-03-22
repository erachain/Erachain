package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.utils.ObserverMessage;

import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class ItemPollsTableModel extends TableModelCls<Long, ItemCls> implements Observer {
    public static final int COLUMN_NAME = 0;
    public static final int COLUMN_VOTES = 2;
    private static final int COLUMN_CREATOR = 1;
    private AssetCls asset;

    private SortableList<Long, ItemCls> polls;

    public ItemPollsTableModel() {
        super(DCSet.getInstance().getItemPollMap(),
                new String[]{"Name", "Creator", "Total Votes"}, true);
    }

    public void setAsset(AssetCls asset) {
        this.asset = asset;
        this.fireTableDataChanged();
    }

    @Override
    public SortableList<Long, ItemCls> getSortableList() {
        return this.polls;
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

        PollCls poll = (PollCls) this.polls.get(row).getB();

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

                return poll.getOwner().getPersonAsString();

            case COLUMN_VOTES:

                BigDecimal amo = poll.getTotalVotes(DCSet.getInstance(), this.asset.getKey(DCSet.getInstance()));
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
                this.polls = (SortableList<Long, ItemCls>) message.getValue();
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
        polls = map.getList();
    }

    public void deleteObservers() {
        //if(this.polls!=null)this.polls.removeObserver();
        //DCSet.getInstance().getPollMap().deleteObserver(this);
    }

    @Override
    public ItemCls getItem(int k) {
        return this.polls.get(k).getB();
    }
}
