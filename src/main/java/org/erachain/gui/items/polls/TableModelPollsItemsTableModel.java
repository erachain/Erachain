package org.erachain.gui.items.polls;

import org.erachain.core.item.polls.PollCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.SearchItemsTableModel;

@SuppressWarnings("serial")
public class TableModelPollsItemsTableModel extends SearchItemsTableModel {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_FAVORITE = 3;

    public TableModelPollsItemsTableModel() {
        super(DCSet.getInstance().getItemPollMap(), new String[]{"Key", "Name", "Creator", "Favorite"},
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

                return poll.viewName();

            case COLUMN_ADDRESS:

                return poll.getOwner().getPersonAsString();

            case COLUMN_FAVORITE:

                return poll.isFavorite();

        }

        return null;
    }

}
