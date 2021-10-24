package org.erachain.datachain;

import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.mapdb.SerializerBase;

import java.util.HashMap;

/**
 * Use states for save values that self-linked (change itself by previous state)
 * <b>Ключ:</b> smartContract.id + state.No<br>
 *
 * <b>Значение:</b> State values
 */

public class SmartContractState extends DCUMap<Tuple2<Integer, Integer>, Object[]> {

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
        map = new HashMap<Tuple2<Integer, Integer>, Object[]>();
    }

    @Override
    public void put(Tuple2<Integer, Integer> key, Object[] state) {
        Tuple2<Integer, Integer> firstKey = new Tuple2<>(key.a, 0);
        Object[] first = get(firstKey);
        int stateID;
        if (first == null) {
            super.put(firstKey, new Object[]{1});
            stateID = 1;
        } else {
            stateID = (int) first[0] + 1;
        }
        super.put(new Tuple2<>(key.a, stateID), state);
    }

    @Override
    public void delete(Tuple2<Integer, Integer> key) {
        Tuple2<Integer, Integer> firstKey = new Tuple2<>(key.a, 0);
        Object[] first = get(firstKey);
        int stateID = (int) first[0];
        super.delete(new Tuple2<>(key.a, stateID));
        super.put(firstKey, new Object[]{stateID - 1});
    }

}
