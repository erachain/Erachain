package datachain;


import org.mapdb.DB;
import utils.ObserverMessage;

// Person has Status of Union - person Ermolaev getBySignature Director status in Polza union
public class KK_KPersonStatusUnionMap extends KK_K_Map {

    public KK_KPersonStatusUnionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, "person_status_union",
                ObserverMessage.RESET_PERSON_STATUS_UNION_TYPE,
                ObserverMessage.ADD_PERSON_STATUS_UNION_TYPE,
                ObserverMessage.REMOVE_PERSON_STATUS_UNION_TYPE,
                ObserverMessage.LIST_PERSON_STATUS_UNION_TYPE
        );
    }

    public KK_KPersonStatusUnionMap(KK_KPersonStatusUnionMap parent) {
        super(parent);
    }

}
