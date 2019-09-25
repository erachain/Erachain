package org.erachain.datachain;

//04/01 +- 

import org.mapdb.DB;
import org.mapdb.Hasher;
import org.mapdb.SerializerBase;

/**
 * Поиск по подписи ссылки на транзакцию
 * signature (as UUID bytes16) -> <BlockHeoght, Record No> (as Long)
 */
public class TransactionFinalMapSigns extends DCUMap<byte[], Long> {

    static int KEY_LEN = 12;

    public TransactionFinalMapSigns(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public TransactionFinalMapSigns(TransactionFinalMapSigns parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    protected void getMap() {
        //OPEN MAP
        // HASH map is so QUICK
        map = database.createHashMap("signature_final_tx")
                .keySerializer(SerializerBase.BYTE_ARRAY)
                .hasher(Hasher.BYTE_ARRAY)
                .valueSerializer(SerializerBase.LONG)
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        //OPEN MAP
        getMap();
    }

    @Override
    protected Long getDefaultValue() {
        return null;
    }

    public boolean contains(byte[] signature) {

        byte[] key = new byte[KEY_LEN];
        System.arraycopy(signature, 0, key, 0, KEY_LEN);
        return super.contains(key);
    }

    public Long get(byte[] signature) {
        byte[] key = new byte[KEY_LEN];
        System.arraycopy(signature, 0, key, 0, KEY_LEN);
        return super.get(key);
    }

    public void delete(byte[] signature) {
        byte[] key = new byte[KEY_LEN];
        System.arraycopy(signature, 0, key, 0, KEY_LEN);
        super.remove(key);
    }

    public Long remove(byte[] signature) {
        byte[] key = new byte[KEY_LEN];
        System.arraycopy(signature, 0, key, 0, KEY_LEN);
        return super.remove(key);
    }

    public boolean set(byte[] signature, Long refernce) {
        byte[] key = new byte[KEY_LEN];
        System.arraycopy(signature, 0, key, 0, KEY_LEN);
        return super.set(key, refernce);

    }

}