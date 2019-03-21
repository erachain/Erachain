package org.erachain.datachain;

//04/01 +- 

import com.google.common.primitives.Longs;
import org.erachain.database.DBMap;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.erachain.utils.ObserverMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Поиск по подписи ссылки на транзакцию
 * signature (as UUID bytes16) -> <BlockHeoght, Record No> (as Long)
 */
public class TransactionFinalMapSigns extends DCMap<UUID, Long> {

    public TransactionFinalMapSigns(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public TransactionFinalMapSigns(TransactionFinalMapSigns parent) {
        super(parent, null);
    }

    protected void createIndexes(DB database) {
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, Long> openMap(DB database) {

        // HASH map is so QUICK
        return database.createHashMap("signature_final_tx")
                .keySerializer(SerializerBase.UUID)
                .valueSerializer(SerializerBase.LONG)
                .makeOrGet();

    }

    @Override
    protected Map<UUID, Long> getMap(DB database) {
        //OPEN MAP
        return openMap(database);
    }

    @Override
    protected Map<UUID, Long> getMemoryMap() {
        DB database = DBMaker.newMemoryDB().make();

        //OPEN MAP
        return this.getMap(database);
    }

    @Override
    protected Long getDefaultValue() {
        return null;
    }

    public boolean contains(byte[] signature) {

        // make 12 bytes KEY
        long key1 = Longs.fromBytes(signature[0], signature[1], signature[2], signature[3],
                signature[4], signature[5], signature[6], signature[7]);
        long key2 = Longs.fromBytes(signature[8], signature[9], signature[10], signature[11],
                (byte)0, (byte)0, (byte)0, (byte)0);

        UUID key = new UUID(key1, key2);
        return this.contains(key);
    }

    public Long get(byte[] signature) {
        long key1 = Longs.fromBytes(signature[0], signature[1], signature[2], signature[3],
                signature[4], signature[5], signature[6], signature[7]);
        long key2 = Longs.fromBytes(signature[8], signature[9], signature[10], signature[11],
                (byte)0, (byte)0, (byte)0, (byte)0);

        UUID key = new UUID(key1, key2);
        return this.get(key);
    }

    public void delete(byte[] signature) {
        long key1 = Longs.fromBytes(signature[0], signature[1], signature[2], signature[3],
                signature[4], signature[5], signature[6], signature[7]);
        long key2 = Longs.fromBytes(signature[8], signature[9], signature[10], signature[11],
                (byte)0, (byte)0, (byte)0, (byte)0);

        UUID key = new UUID(key1, key2);
        this.delete(key);
    }

    public boolean set(byte[] signature, Long refernce) {
        long key1 = Longs.fromBytes(signature[0], signature[1], signature[2], signature[3],
                signature[4], signature[5], signature[6], signature[7]);
        long key2 = Longs.fromBytes(signature[8], signature[9], signature[10], signature[11],
                (byte)0, (byte)0, (byte)0, (byte)0);

        UUID key = new UUID(key1, key2);
        return this.set(key, refernce);

    }

}