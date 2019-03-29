package org.erachain.database.wallet;

import org.erachain.core.item.templates.TemplateCls;
import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

public class FavoriteItemMapImprint extends FavoriteItemMap {

    // favorites init SET
    public FavoriteItemMapImprint(DWSet dWSet, DB database) {
        super(dWSet, database, ObserverMessage.LIST_IMPRINT_FAVORITES_TYPE, "imprint", TemplateCls.INITIAL_FAVORITES);
    }
}
