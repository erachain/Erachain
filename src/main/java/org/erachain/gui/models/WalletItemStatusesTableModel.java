package org.erachain.gui.models;
////////

import org.erachain.controller.Controller;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;

import javax.validation.constraints.Null;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletItemStatusesTableModel extends TableModelCls<Tuple2<String, String>, StatusCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_UNIQUE = 3;
    public static final int COLUMN_CONFIRMED = 4;
    public static final int COLUMN_FAVORITE = 5;

    private SortableList<Tuple2<String, String>, StatusCls> statuses;

    private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Creator", "Unique", "Confirmed", "Favorite"});
    private Boolean[] column_AutuHeight = new Boolean[]{false, true, true, false, false};

    public WalletItemStatusesTableModel() {
        Controller.getInstance().addWalletListener(this);
    }

    @Override
    public SortableList<Tuple2<String, String>, StatusCls> getSortableList() {
        return this.statuses;
    }

    public StatusCls getItem(int row) {
        return this.statuses.get(row).getB();
    }

    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    // читаем колонки которые изменяем высоту
    public Boolean[] get_Column_AutoHeight() {

        return this.column_AutuHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void set_get_Column_AutoHeight(Boolean[] arg0) {
        this.column_AutuHeight = arg0;
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


        return (this.statuses == null) ? 0 : this.statuses.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.statuses == null || row > this.statuses.size() - 1) {
            return null;
        }

        Pair<Tuple2<String, String>, StatusCls> res = this.statuses.get(row);
        if (res == null)
            return null;

        StatusCls status = res.getB();

        switch (column) {
            case COLUMN_KEY:

                return status.getKey(DCSet.getInstance());

            case COLUMN_NAME:

                return status.viewName();

            case COLUMN_ADDRESS:

                return status.getOwner().getPersonAsString();

            case COLUMN_CONFIRMED:

                return status.isConfirmed();

            case COLUMN_FAVORITE:

                return status.isFavorite();

            case COLUMN_UNIQUE:

                return status.isUnique();

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
        if (message.getType() == ObserverMessage.LIST_STATUS_TYPE) {
            if (this.statuses == null) {
                this.statuses = (SortableList<Tuple2<String, String>, StatusCls>) message.getValue();
                this.statuses.registerObserver();
                //this.statuses.sort(PollMap.NAME_INDEX);
            }

            this.fireTableDataChanged();
        }

        //CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.ADD_STATUS_TYPE || message.getType() == ObserverMessage.REMOVE_STATUS_TYPE) {
            this.fireTableDataChanged();
        }
    }

    public void removeObservers() {
        if (this.statuses != null) this.statuses.removeObserver();
        Controller.getInstance().deleteObserver(this);
    }
}
