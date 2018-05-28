package datachain;

import org.mapdb.DB;
import utils.ObserverMessage;

public class KKPollUnionMap extends KK_Map {
    public KKPollUnionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, "status_union",
                ObserverMessage.RESET_POLL_UNION_TYPE,
                ObserverMessage.ADD_POLL_UNION_TYPE,
                ObserverMessage.REMOVE_POLL_UNION_TYPE,
                ObserverMessage.LIST_POLL_UNION_TYPE
        );
    }

    public KKPollUnionMap(KKPollUnionMap parent) {
        super(parent);
    }

}
