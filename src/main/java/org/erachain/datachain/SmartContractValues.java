package org.erachain.datachain;

import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.mapdb.SerializerBase;

import java.util.HashMap;

/**
 * Use only for unlinked values
 *
 * <b>Ключ:</b> DAPP.id + var.name<br>
 *
 * <b>Значение:</b> Value
 */

public class SmartContractValues extends DCUMap<Tuple2<Integer, String>, Object> {

    public SmartContractValues(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public SmartContractValues(SmartContractValues parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    public void openMap() {
        //OPEN MAP
        map = database.createHashMap("smart_contract_values")
                .valueSerializer(SerializerBase.BASIC)
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<Tuple2<Integer, String>, Object>();
    }
}
