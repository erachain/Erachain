package org.erachain.gui.models;
////////

import org.erachain.controller.Controller;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.SortableList;
import org.erachain.lang.Lang;
import org.mapdb.Fun.Tuple2;
import org.erachain.utils.ObserverMessage;

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

    private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Owner", "Confirmed", "Favorite"});

    public WalletItemTemplatesTableModel() {
        Controller.getInstance().addWalletListener(this);
    }

    @Override
    public SortableList<Tuple2<String, String>, TemplateCls> getSortableList() {
        return this.templates;
    }

    public TemplateCls getItem(int row) {
        return this.templates.get(row).getB();
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
}
