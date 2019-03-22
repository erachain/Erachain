package org.erachain.database.wallet;

import org.erachain.core.item.templates.TemplateCls;
import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

public class FavoriteItemMapTemplate extends FavoriteItemMap {

    // favorites init SET
    public FavoriteItemMapTemplate(DWSet dWSet, DB database) {
        super(dWSet, database, ObserverMessage.LIST_TEMPLATE_FAVORITES_TYPE, "template", TemplateCls.INITIAL_FAVORITES);
    }
}
