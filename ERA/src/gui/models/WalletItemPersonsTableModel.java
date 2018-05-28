package gui.models;
////////

import controller.Controller;
import core.item.persons.PersonCls;
import datachain.DCSet;
import datachain.SortableList;
import lang.Lang;
import org.mapdb.Fun.Tuple2;
import utils.ObserverMessage;
import utils.Pair;

import javax.validation.constraints.Null;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletItemPersonsTableModel extends TableModelCls<Tuple2<String, String>, PersonCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    private SortableList<Tuple2<String, String>, PersonCls> persons;

    private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Publisher", "Confirmed", "Favorite"});
    private Boolean[] column_AutuHeight = new Boolean[]{false, true, true, false, false};

    public WalletItemPersonsTableModel() {
        //Controller.getInstance().addWalletListener(this);
        addObservers();
    }

    @Override
    public SortableList<Tuple2<String, String>, PersonCls> getSortableList() {
        return this.persons;
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

    public PersonCls getItem(int row) {
        Pair<Tuple2<String, String>, PersonCls> personRes = this.persons.get(row);
        if (personRes == null)
            return null;

        return personRes.getB();
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

        return this.persons == null ? 0 : this.persons.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.persons == null || row > this.persons.size() - 1) {
            return null;
        }

        Pair<Tuple2<String, String>, PersonCls> personRes = this.persons.get(row);
        if (personRes == null)
            return null;

        PersonCls person = personRes.getB();

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
        if (message.getType() == ObserverMessage.LIST_PERSON_TYPE || message.getType() == ObserverMessage.WALLET_LIST_PERSON_TYPE) {
            if (this.persons == null) {
                this.persons = (SortableList<Tuple2<String, String>, PersonCls>) message.getValue();
                this.persons.registerObserver();
            }

            this.fireTableDataChanged();
        }

        //CHECK IF LIST UPDATED

        if (message.getType() == ObserverMessage.ADD_PERSON_TYPE || message.getType() == ObserverMessage.REMOVE_PERSON_TYPE
                || message.getType() == ObserverMessage.WALLET_ADD_PERSON_TYPE || message.getType() == ObserverMessage.WALLET_REMOVE_PERSON_TYPE) {
            //		this.persons = (SortableList<Tuple2<String, String>, PersonCls>) message.getValue();
            this.fireTableDataChanged();
        }
    }

    public void addObservers() {
        Controller.getInstance().wallet.database.getPersonMap().addObserver(this);


    }


    public void removeObservers() {
        //this.persons.removeObserver();
        //Controller.getInstance().deleteWalletObserver(this);
        Controller.getInstance().wallet.database.getPersonMap().deleteObserver(this);
        persons.removeObserver();
    }
}
