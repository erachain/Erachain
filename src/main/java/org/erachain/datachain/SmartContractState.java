package org.erachain.datachain;

import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.mapdb.SerializerBase;

import java.util.HashMap;

/**
 * Use states for save values that self-linked (change itself by previous state)
 * <b>Ключ:</b> DApp.id + state.No (as SeqNo)<br>
 *
 * <b>Значение:</b> State values
 */

public class SmartContractState extends DCUMap<Tuple2<Integer, Long>, Object[]> {

    public SmartContractState(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public SmartContractState(SmartContractState parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    public void openMap() {
        //OPEN MAP
        map = database.createHashMap("smart_contract_state")
                .valueSerializer(SerializerBase.BASIC)
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<Tuple2<Integer, Long>, Object[]>();
    }

}
