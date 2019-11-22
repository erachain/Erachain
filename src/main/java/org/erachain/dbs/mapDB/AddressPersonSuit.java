package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.dbs.DBTab;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple4;
import org.mapdb.Hasher;
import org.mapdb.SerializerBase;

import java.util.Stack;

/**
 * Хранит Удостоверения персон для заданного публичного ключа.
 * address -> Stack person + end_date + block.height + transaction.reference.
 * Тут block.getHeight + transaction index  - это ссылка на транзакцию создавшую данную заметку<br>
 *
 * <b>Ключ:</b> (String)publickKey<br>
 *
 * <b>Значение:</b><br>
 * Stack((Long)person key,
 * (Integer)end_date - дата окончания действия удостоврения,<br>
 * (Integer)block.getHeight - номер блока,<br>
 * (Integer)transaction index - номер транзакции в блоке<br>
 * ))
 */
// TODO укротить до 20 байт адрес и ссылку на Long

@Slf4j
public class AddressPersonSuit extends DBMapSuit<byte[], Stack<Tuple4<
        Long, // person key
        Integer, // end_date day
        Integer, // block height
        Integer>>> // transaction index
{
    public AddressPersonSuit(DBASet databaseSet, DB database, DBTab cover) {
        super(databaseSet, database, logger, false, cover);
    }

    @Override
    public void openMap() {

        //OPEN MAP
        map = database.createHashMap("address_person")
                //.keySerializer(BTreeKeySerializer.STRING)
                .keySerializer(SerializerBase.BYTE_ARRAY)
                .hasher(Hasher.BYTE_ARRAY)
                .makeOrGet();
    }

}
