package org.erachain.database.wallet;

import org.erachain.core.item.polls.PollCls;
import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

import java.util.List;
import java.util.Observer;

public class FavoriteItemPoll extends FavoriteItem {

    // favorites init SET
    public FavoriteItemPoll(DWSet dWSet, DB database) {
        super(dWSet, database, ObserverMessage.WALLET_LIST_POLL_FAVORITES_TYPE, "poll", PollCls.INITIAL_FAVORITES);
    }
}
