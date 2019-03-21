package org.erachain.database.wallet;

import org.erachain.core.item.templates.TemplateCls;
import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

import java.util.List;
import java.util.Observer;

public class FavoriteItemTemplate extends FavoriteItem {

    // favorites init SET
    public FavoriteItemTemplate(DWSet dWSet, DB database) {
        super(dWSet, database, ObserverMessage.LIST_TEMPLATE_FAVORITES_TYPE, "template", TemplateCls.INITIAL_FAVORITES);
    }
}
