package org.erachain.datachain;

import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.mapdb.SerializerBase;

import java.util.HashMap;

/**
 * Общая сумма переданных средств в кредит на другой счет
 * Используется для проверки сумм которые отдаются или забираются у заемщика<br><br>
 *
 * <b>Ключ:</b> account.address Creditor + asset key + account.address Debtor<br>
 *
 * <b>Значение:</b> сумма средств
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
                .valueSerializer(SerializerBase.BYTE_ARRAY)
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<Tuple2<Integer, String>, Object>();
    }

}
