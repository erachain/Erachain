package datachain;


import org.mapdb.DB;
import utils.ObserverMessage;

/*
public class KK_Map extends DCMap<
Long, // item1 Key <-- PERSON
TreeMap<Long, // item2 Key <-- STATUS
	Stack<Tuple5<
		Long, // beg_date
		Long, // end_date

		byte[], // any additional data
		
		Integer, // block.getHeight() -> db.getBlockMap(db.getHeightMap().getBlockByHeight(index))
		Integer // block.getTransaction(transaction.getSignature()) -> block.getTransaction(index)
	>>>>
{
*/

public class KKPersonStatusMap extends KK_Map {

    public KKPersonStatusMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, "person_status",
                ObserverMessage.RESET_PERSON_STATUS_TYPE,
                ObserverMessage.ADD_PERSON_STATUS_TYPE,
                ObserverMessage.REMOVE_PERSON_STATUS_TYPE,
                ObserverMessage.LIST_PERSON_STATUS_TYPE
        );
    }

    public KKPersonStatusMap(KKPersonStatusMap parent) {
        super(parent);
    }

}
