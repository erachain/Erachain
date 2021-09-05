package org.erachain.dbs.rocksDB.store.settings;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.rocksDB.common.RocksDbDataSourceImpl;
import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableAtomicLong;
import org.erachain.dbs.rocksDB.transformation.ByteableString;
import org.mapdb.Fun.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Это важный класс, который реализует хранение переменных в БД.
 */
@Slf4j
public class SettingsDBRocksDBTable {
    //  интерфейс доступа к БД
    private RocksDbDataSourceImpl db;
    private List<Tuple2<String, Byteable>> namesByteables;
    private ByteableAtomicLong byteableAtomicLong = new ByteableAtomicLong();
    private ByteableString byteableString = new ByteableString();
    public SettingsDBRocksDBTable(List<Tuple2<String, Byteable>> namesByteables, String nameTable) {
        this.namesByteables = namesByteables;
    }

    public Object get(String name) {
        Tuple2<String, Byteable> nameByteable = receiveTuple2ByName(name);
        byte[] bytes = db.get(byteableString.toBytesObject(name));
        return nameByteable.b.receiveObjectFromBytes(bytes);
    }

    public AtomicLong getAtomicLong(String name) {
        byte[] bytes = db.get(byteableString.toBytesObject(name));
        if (bytes == null) {
            db.put(byteableString.toBytesObject(name), new byte[]{0,0,0,0,0,0,0,0});// 0L - нулевое байтовое представление long
            return new AtomicLong(0);
        }
        return byteableAtomicLong.receiveObjectFromBytes(bytes);
    }
    public AtomicBoolean getAtomicBoolean(String name) {
        byte[] bytes = db.get(byteableString.toBytesObject(name));
        if (bytes == null) {
            db.put(byteableString.toBytesObject(name), new byte[]{0});
            return new AtomicBoolean(false);
        }
        if (Arrays.equals(bytes, new byte[]{0})) {
            return new AtomicBoolean(false);
        }
        return new AtomicBoolean(true);
    }
    public void put(String name, Object value) {
        Tuple2<String, Byteable> nameByteable = receiveTuple2ByName(name);
        db.put(byteableString.toBytesObject(name), nameByteable.b.toBytesObject(value));
    }

    public void putAtomicLong(String name, AtomicLong value) {
        db.put(byteableString.toBytesObject(name), byteableAtomicLong.toBytesObject(value));
    }
    public void putAtomicBoolean(String name, AtomicBoolean value) {
        //если false
        if (!value.get()) {
            db.put(byteableString.toBytesObject(name), new byte[]{0});
        } else {
            db.put(byteableString.toBytesObject(name), new byte[]{1});
        }

    }

    private Tuple2<String, Byteable> receiveTuple2ByName(String name) {
        return namesByteables.stream().filter((tuple2) -> (tuple2.a.equals(name))).findFirst().get();
    }

    public void close(){
        db.close();
    }

    public void putByteArray(String name, byte[] value) {
        db.put(byteableString.toBytesObject(name),value);
    }

    public byte[] getByteArray(String name) {
        byte[] bytes = db.get(byteableString.toBytesObject(name));
        if (bytes == null) {
            byte[] value = {0};
            db.put(byteableString.toBytesObject(name), value);
            return value;
        }
        return bytes;
    }
}
