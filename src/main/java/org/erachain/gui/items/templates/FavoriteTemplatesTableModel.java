package org.erachain.gui.items.templates;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.gui.models.TableModelCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;

import javax.validation.constraints.Null;
import java.util.*;

@SuppressWarnings("serial")
public class FavoriteTemplatesTableModel extends FavoriteItemModelTable<Long, TemplateCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    public FavoriteTemplatesTableModel() {
        super(ItemCls.TEMPLATE_TYPE, new String[]{"Key", "Name", "Publisher", "Confirmed", "Favorite"},
                new Boolean[]{false, true, true, false, false});
        super.COLUMN_FAVORITE = COLUMN_FAVORITE;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        TemplateCls template = (TemplateCls) this.list.get(row);
        if (template == null)
            return null;


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

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.LIST_TEMPLATE_FAVORITES_TYPE && list == null) {
            list = new ArrayList<ItemCls>();
            fill((Set<Long>) message.getValue());
            fireTableDataChanged();
        }
        if (message.getType() == ObserverMessage.ADD_TEMPLATE_TYPE_FAVORITES_TYPE) {
            list.add(Controller.getInstance().getTemplate((long) message.getValue()));
            fireTableDataChanged();
        }
        if (message.getType() == ObserverMessage.DELETE_TEMPLATE_FAVORITES_TYPE) {
            list.remove(Controller.getInstance().getTemplate((long) message.getValue()));
            fireTableDataChanged();
        }
    }

    public void addObserversThis() {
        if (Controller.getInstance().doesWalletDatabaseExists())
            Controller.getInstance().wallet.database.getTemplateFavoritesSet().addObserver(this);
    }

    public void removeObserversThis() {
        if (Controller.getInstance().doesWalletDatabaseExists())
        Controller.getInstance().wallet.database.getTemplateFavoritesSet().deleteObserver(this);
    }

}
