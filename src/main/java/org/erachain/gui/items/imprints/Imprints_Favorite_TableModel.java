package org.erachain.gui.items.imprints;

import org.erachain.controller.Controller;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.TableModelCls;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;

import javax.validation.constraints.Null;
import java.util.*;

@SuppressWarnings("serial")
public class Imprints_Favorite_TableModel extends TableModelCls<Tuple2<String, String>, PersonCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;
    private List<ImprintCls> persons;
    private Boolean[] column_AutuHeight = new Boolean[]{false, true, true, false, false};

    public Imprints_Favorite_TableModel() {
        super("Imprints_Favorite_TableModel", 1000,
                new String[]{"Key", "Name", "Publisher", "Confirmed", "Favorite"});
        super.COLUMN_FAVORITE = COLUMN_FAVORITE;
    }

    @Override
    public SortableList<Tuple2<String, String>, PersonCls> getSortableList() {
        return null;
    }

    // читаем колонки которые изменяем высоту
    public Boolean[] get_Column_AutoHeight() {

        return this.column_AutuHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void set_get_Column_AutoHeight(Boolean[] arg0) {
        this.column_AutuHeight = arg0;
    }

    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    public ImprintCls getItem(int row) {
        return this.persons.get(row);

    }

    @Override
    public int getRowCount() {
        if (persons == null) return 0;
        return this.persons.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.persons == null || row > this.persons.size() - 1) {
            return null;
        }

        ImprintCls person = this.persons.get(row);
        if (person == null)
            return null;

        switch (column) {
            case COLUMN_KEY:

                return person.getKey(DCSet.getInstance());

            case COLUMN_NAME:

                return person.viewName();

            case COLUMN_ADDRESS:

                return person.getOwner().getPersonAsString();

            case COLUMN_CONFIRMED:

                return person.isConfirmed();

            case COLUMN_FAVORITE:

                return person.isFavorite();

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
        if (message.getType() == ObserverMessage.LIST_IMPRINT_FAVORITES_TYPE && persons == null) {
            persons = new ArrayList<ImprintCls>();
            fill((Set<Long>) message.getValue());
            fireTableDataChanged();
        }
        if (message.getType() == ObserverMessage.ADD_IMPRINT_TYPE_FAVORITES_TYPE) {
            persons.add(Controller.getInstance().getImprint((long) message.getValue()));
            fireTableDataChanged();
        }
        if (message.getType() == ObserverMessage.DELETE_IMPRINT_FAVORITES_TYPE) {
            persons.remove(Controller.getInstance().getImprint((long) message.getValue()));
            fireTableDataChanged();
        }
    }

    public void fill(Set<Long> set) {
        for (Long s : set) {
            persons.add(Controller.getInstance().getImprint(s));
        }
    }

    public void addObserversThis() {
        Controller.getInstance().wallet.database.getImprintFavoritesSet().addObserver(this);
    }

    public void removeObserversThis() {
        Controller.getInstance().wallet.database.getImprintFavoritesSet().deleteObserver(this);
    }

}
