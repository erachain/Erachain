package org.erachain.database.wallet;

import org.erachain.core.item.templates.TemplateCls;
import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

import java.util.List;
import java.util.Observer;

public class FavoriteItemImprint extends FavoriteItem {

    // favorites init SET
    public FavoriteItemImprint(DWSet dWSet, DB database) {
        super(dWSet, database, ObserverMessage.LIST_IMPRINT_FAVORITES_TYPE, "imprint", TemplateCls.INITIAL_FAVORITES);
    }
}
