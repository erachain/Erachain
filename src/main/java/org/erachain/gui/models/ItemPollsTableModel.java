package org.erachain.gui.models;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.datachain.DCSet;

import java.math.BigDecimal;
import java.util.Observer;

@SuppressWarnings("serial")
public class ItemPollsTableModel extends TimerTableModelCls<ItemCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    private static final int COLUMN_CREATOR = 2;
    public static final int COLUMN_VOTES = 3;
    private AssetCls asset;

    public ItemPollsTableModel() {
        super(DCSet.getInstance().getItemPollMap(),
                new String[]{"Key", "Name", "Creator", "Total Votes"}, true);
    }

    public void setAsset(AssetCls asset) {
        this.asset = asset;

        this.fireTableDataChanged();
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

            case COLUMN_CREATOR:

                return poll.getMaker().getPersonAsString();

            case COLUMN_VOTES:

                BigDecimal amo = poll.getTotalVotes(DCSet.getInstance(), this.asset.getKey());
                if (amo == null)
                    return BigDecimal.ZERO;
                return amo;


        }

        return null;
    }

}
