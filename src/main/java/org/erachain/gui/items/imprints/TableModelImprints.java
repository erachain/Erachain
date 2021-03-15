package org.erachain.gui.items.imprints;

import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.gui.models.TimerTableModelCls;

import java.util.Observer;

@SuppressWarnings("serial")
public class TableModelImprints extends TimerTableModelCls<ImprintCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;

    public TableModelImprints() {
        super(new String[]{"Key", "Name", "Maker"}, new Boolean[]{false, true, true}, false);
    }


    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        ImprintCls imprint = this.list.get(row);

        switch (column) {
            case COLUMN_KEY:

                return imprint.getKey();

            case COLUMN_NAME:

                return imprint.viewName();

            case COLUMN_ADDRESS:

                return imprint.getMaker().getPersonAsString();

        }

        return null;
    }


    /*
    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.LIST_IMPRINT_TYPE) {
            if (this.imprints == null) {
                this.imprints = (SortableList<Long, ImprintCls>) message.getValue();
                this.imprints.addFilterField("name");
                //this.imprints.registerObserver();
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
        //this.imprints.removeObserver();
        Controller.getInstance().deleteObserver(this);
    }

    @Override
    public ImprintCls getItem(int k) {
        // TODO Auto-generated method stub
        return this.imprints.get(k).getB();
    }
     */
}
