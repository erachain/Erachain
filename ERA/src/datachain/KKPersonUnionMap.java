package datachain;

import org.mapdb.DB;
import utils.ObserverMessage;

public class KKPersonUnionMap extends KK_Map {

    public KKPersonUnionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, "person_union",
                ObserverMessage.RESET_PERSON_UNION_TYPE,
                ObserverMessage.ADD_PERSON_UNION_TYPE,
                ObserverMessage.REMOVE_PERSON_UNION_TYPE,
                ObserverMessage.LIST_PERSON_UNION_TYPE
        );

    }

    public KKPersonUnionMap(KKPersonUnionMap parent) {
        super(parent);
    }
}
