package org.erachain.datachain;

import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.mapDB.AddressPersonSuit;
import org.erachain.dbs.nativeMemMap.NativeMapTreeMapFork;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple4;

import java.util.Stack;

import static org.erachain.database.IDB.DBS_MAP_DB;
import static org.erachain.database.IDB.DBS_ROCK_DB;

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
public class AddressPersonMapImpl extends DBTabImpl<byte[], Stack<Tuple4<
        Long, // person key
        Integer, // end_date day
        Integer, // block height
        Integer>>> implements AddressPersonMap // transaction index
{

    final static Stack<Tuple4<Long, Integer, Integer, Integer>> DEFAULT_VALUE = new Stack<Tuple4<Long, Integer, Integer, Integer>>();

    public AddressPersonMapImpl(int dbs, DCSet databaseSet, DB database) {
        super(dbs, databaseSet, database);
    }

    public AddressPersonMapImpl(int dbs, AddressPersonMap parent, DCSet dcSet) {
        super(dbs, parent, dcSet);
    }

    protected void createIndexes() {
    }

    @Override
    public void openMap() {

        //OPEN MAP
        if (parent == null) {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    //map = new AddressPersonSuit(databaseSet, database);
                    //break;
                default:
                    map = new AddressPersonSuit(databaseSet, database);
            }
        } else {
            switch (dbsUsed) {
                case DBS_MAP_DB:
                    //map = new BlocksSuitMapDBFork((TransactionMap) parent, databaseSet);
                    //break;
                case DBS_ROCK_DB:
                    //map = new BlocksSuitMapDBFotk((TransactionMap) parent, databaseSet);
                    //break;
                default:
                    map = new NativeMapTreeMapFork<>(parent, databaseSet, Fun.BYTE_ARRAY_COMPARATOR, DEFAULT_VALUE);
            }
        }
    }

    protected void getMemoryMap_old() {
        // HashMap ?
        //map = new TreeMap<String, Stack<Tuple4<Long, Integer, Integer, Integer>>>();
    }

    @Override
    public void addItem(byte[] address, Tuple4<Long, Integer, Integer, Integer> item) {
        Stack<Tuple4<Long, Integer, Integer, Integer>> value = this.get(address);

        Stack<Tuple4<Long, Integer, Integer, Integer>> value_new;

        if (this.parent == null)
            value_new = value;
        else {
            // !!!! NEEED .clone() !!!
            // need for updates only in fork - not in parent DB
            value_new = (Stack<Tuple4<Long, Integer, Integer, Integer>>) value.clone();
        }

        value_new.push(item);

        this.put(address, value_new);

    }

    @Override
    public void addItem(String address, Tuple4<Long, Integer, Integer, Integer> item) {
        ;

    }

    @Override
    public Tuple4<Long, Integer, Integer, Integer> getItem(byte[] address) {
        Stack<Tuple4<Long, Integer, Integer, Integer>> value = this.get(address);
        return value == null || value.isEmpty() ? null : value.peek();
    }

    @Override
    public Tuple4<Long, Integer, Integer, Integer> getItem(String address) {
        return null;
    }

    @Override
    public void removeItem(byte[] address) {
        Stack<Tuple4<Long, Integer, Integer, Integer>> value = this.get(address);
        if (value == null || value.isEmpty()) return;

        Stack<Tuple4<Long, Integer, Integer, Integer>> value_new;
        if (this.parent == null)
            value_new = value;
        else {
            // !!!! NEEED .clone() !!!
            // need for updates only in fork - not in parent DB
            value_new = (Stack<Tuple4<Long, Integer, Integer, Integer>>) value.clone();
        }

        value_new.pop();

        this.put(address, value_new);

    }

    public void removeItem(String address) {

    }
}