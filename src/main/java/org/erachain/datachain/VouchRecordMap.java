package org.erachain.datachain;
//04/01 +- 

//import java.lang.reflect.Array;

import org.erachain.dbs.DBMap;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun.Tuple2;
import org.erachain.utils.ObserverMessage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * Заверение другой транзакции<br><br>
 *     vouched record (BlockNo, RecNo) -> ERM balabce + List of vouchers records
 *
 * Ключ: ссылка на запись которую заверяем.<br>
 * Значение: Сумма ERA на момент заверения на счету заверителя + ссылка на запись заверения:<br>
 vouched record (BlockNo, RecNo) -> ERM balabce + List of vouchers records
 * @return dcMap
 */

public class VouchRecordMap extends DCMap<Long, Tuple2<BigDecimal, List<Long>>> {

    public VouchRecordMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_VOUCH_TYPE);
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_VOUCH_TYPE);
            this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_VOUCH_TYPE);
            this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_VOUCH_TYPE);
        }
    }

    public VouchRecordMap(VouchRecordMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    protected void createIndexes() {
    }

    //@SuppressWarnings("unchecked")
    private Map<Long, Tuple2<BigDecimal, List<Long>>> openMap(DB database) {

        BTreeMap<Long, Tuple2<BigDecimal, List<Long>>> map =
                database.createTreeMap("vouch_records")
                        //.keySerializer(BTreeKeySerializer.TUPLE2)
                        //.valueSerializer(new TransactionSerializer())
                        .makeOrGet();
        return map;

    }


    @Override
    protected void getMap() {
        //OPEN MAP
        map = openMap(database);
    }

    @Override
    protected void getMemoryMap() {
        DB database = DBMaker.newMemoryDB().make();

        //OPEN MAP
        this.getMap();
    }

    @Override
    protected Tuple2<BigDecimal, List<Long>> getDefaultValue() {
        return null;
    }

}
