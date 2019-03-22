package org.erachain.gui.models;
////////

import org.erachain.controller.Controller;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;

import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletItemTemplatesTableModel extends TableModelCls<Tuple2<String, String>, TemplateCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    private SortableList<Tuple2<String, String>, TemplateCls> templates;

    public WalletItemTemplatesTableModel() {
        super("WalletItemTemplatesTableModel", 1000,
                new String[]{"Key", "Name", "Owner", "Confirmed", "Favorite"});
    }

    @Override
    public SortableList<Tuple2<String, String>, TemplateCls> getSortableList() {
        return this.templates;
    }

    public TemplateCls getItem(int row) {
        return this.templates.get(row).getB();
    }

    @Override
    public int getRowCount() {
        return this.templates.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.templates == null || row > this.templates.size() - 1) {
            return null;
        }

        TemplateCls template = this.templates.get(row).getB();

        switch (column) {
            case COLUMN_KEY:

                return template.getKey(DCSet.getInstance());

            case COLUMN_NAME:

                return template.viewName();

            case COLUMN_ADDRESS:

                return template.getOwner().getPersonAsString();

            case COLUMN_CONFIRMED:

                return template.isConfirmed();

            case COLUMN_FAVORITE:

                return template.isFavorite();

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
        if (message.getType() == ObserverMessage.LIST_TEMPLATE_TYPE) {
            if (this.templates == null) {
                this.templates = (SortableList<Tuple2<String, String>, TemplateCls>) message.getValue();
                this.templates.registerObserver();
                //this.templates.sort(PollMap.NAME_INDEX);
            }

            this.fireTableDataChanged();
        }

        //CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.ADD_TEMPLATE_TYPE || message.getType() == ObserverMessage.REMOVE_TEMPLATE_TYPE) {
            this.fireTableDataChanged();
        }

    }

    public void addObserversThis() {
        Controller.getInstance().addWalletObserver(this);
    }

    public void removeObserversThis() {
    }

}
