package org.erachain.gui.items.templates;

import org.erachain.controller.Controller;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.utils.ObserverMessage;

@SuppressWarnings("serial")
public class FavoriteTemplatesTableModel extends FavoriteItemModelTable {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_FAVORITE = 3;

    public FavoriteTemplatesTableModel() {
        super(DCSet.getInstance().getItemTemplateMap(),
                Controller.getInstance().getWallet().dwSet.getTemplateFavoritesSet(),
                new String[]{"Key", "Name", "Publisher", "Favorite"},
                new Boolean[]{false, true, true, false},
                ObserverMessage.RESET_TEMPLATE_FAVORITES_TYPE,
                ObserverMessage.ADD_TEMPLATE_FAVORITES_TYPE,
                ObserverMessage.REMOVE_TEMPLATE_FAVORITES_TYPE,
                ObserverMessage.LIST_TEMPLATE_FAVORITES_TYPE,
                COLUMN_FAVORITE);
    }

    @Override
    protected void updateMap() {
        favoriteMap = Controller.getInstance().getWallet().dwSet.getTemplateFavoritesSet();
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
            case COLUMN_CONFIRMATIONS:
                return template.getConfirmations(dcSet);

            case COLUMN_KEY:
                return template.getKey();

            case COLUMN_NAME:
                return template;

            case COLUMN_ADDRESS:
                return template.getMaker().getPersonAsString();

            case COLUMN_FAVORITE:
                return template.isFavorite();

        }

        return null;
    }

}
