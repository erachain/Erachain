package org.erachain.gui.items.imprints;

import org.erachain.controller.Controller;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.database.SortableList;
import org.erachain.gui.models.TableModelCls;
import org.erachain.utils.ObserverMessage;

import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class TableModelImprints extends TableModelCls<Long, ImprintCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    private SortableList<Long, ImprintCls> imprints;

    public TableModelImprints() {
        super(new String[]{"Key", "Name", "Owner"}, new Boolean[]{false, true, true}, false);
    }

    @Override
    public SortableList<Long, ImprintCls> getSortableList() {
        return this.imprints;
    }

    public ImprintCls getImprint(int row) {
        return this.imprints.get(row).getB();
    }

    @Override
    public int getRowCount() {
        if (this.imprints == null) return 0;
        return this.imprints.size();

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.imprints == null || row > this.imprints.size() - 1) {
            return null;
        }

        ImprintCls imprint = this.imprints.get(row).getB();

        switch (column) {
            case COLUMN_KEY:

                return imprint.getKey();

            case COLUMN_NAME:

                return imprint.viewName();

            case COLUMN_ADDRESS:

                return imprint.getOwner().getPersonAsString();

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
        if (message.getType() == ObserverMessage.LIST_IMPRINT_TYPE) {
            if (this.imprints == null) {
                this.imprints = (SortableList<Long, ImprintCls>) message.getValue();
                this.imprints.addFilterField("name");
                this.imprints.registerObserver();
            }

            this.fireTableDataChanged();
        }

        //CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.ADD_IMPRINT_TYPE || message.getType() == ObserverMessage.REMOVE_IMPRINT_TYPE) {
            this.fireTableDataChanged();
        }
    }

    public void addObservers() {
        Controller.getInstance().addObserver(this);
    }

    public void deleteObservers() {
        this.imprints.removeObserver();
        Controller.getInstance().deleteObserver(this);
    }

    @Override
    public ImprintCls getItem(int k) {
        // TODO Auto-generated method stub
        return this.imprints.get(k).getB();
    }
}
