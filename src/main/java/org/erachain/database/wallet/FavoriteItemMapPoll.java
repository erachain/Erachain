package org.erachain.database.wallet;

import org.erachain.core.item.polls.PollCls;
import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

public class FavoriteItemMapPoll extends FavoriteItemMap {

    // favorites init SET
    public FavoriteItemMapPoll(DWSet dWSet, DB database) {
        super(dWSet, database, ObserverMessage.WALLET_LIST_POLL_FAVORITES_TYPE, "poll", PollCls.INITIAL_FAVORITES);
    }
}
