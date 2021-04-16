package org.erachain.datachain;

import org.erachain.core.account.Account;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.mapDB.AddressPersonSuit;
import org.erachain.dbs.nativeMemMap.NativeMapTreeMapFork;
import org.erachain.dbs.rocksDB.AddressPersonRocksSuit;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple4;

import java.util.Stack;

import static org.erachain.database.IDB.DBS_MAP_DB;
import static org.erachain.database.IDB.DBS_ROCK_DB;

/**
 * Хранит Удостоверения персон для заданного счета.
 * addressShort -> Stack person + end_date + block.height + transaction.reference.
 * Тут block.getHeight + transaction index  - это ссылка на транзакцию создавшую данную заметку<br>
 *
 * <b>Ключ:</b> (byte[])short Address<br>
 *
 * <b>Значение:</b><br>
 * Stack((Long)person key,
 * (Integer)period_Days - через сколько дней окончание действия удостоверения,<br>
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
                    map = new AddressPersonRocksSuit(databaseSet, database, this);
                    break;
                default:
                    map = new AddressPersonSuit(databaseSet, database, this);
            }
        } else {
            switch (dbsUsed) {
                case DBS_MAP_DB:
                case DBS_ROCK_DB:
                default:
                    map = new NativeMapTreeMapFork<>(parent, databaseSet, Fun.BYTE_ARRAY_COMPARATOR, this);
            }
        }
    }

    @Override
    public Stack<Tuple4<Long, Integer, Integer, Integer>> getDefaultValue() {
        return new Stack<Tuple4<Long, Integer, Integer, Integer>>();
    }

    @Override
    public void addItem(byte[] address, Tuple4<Long, Integer, Integer, Integer> item) {
        Stack<Tuple4<Long, Integer, Integer, Integer>> value = this.get(address);

        Stack<Tuple4<Long, Integer, Integer, Integer>> value_new;

        if (false // походу если КЭШ используется там будет такая же ошибка и поэтому надо всегда делать новый объект
                // иначе новое ззначение может передать свои значения в другую обработку после форка базы
                && this.parent == null)
            value_new = (Stack<Tuple4<Long, Integer, Integer, Integer>>) value; //.clone(); // тут DEFAULT_VALUE даже меняет ((
        else {
            // !!!! NEEED .clone() !!!
            // need for updates only in fork - not in parent DB
            value_new = (Stack<Tuple4<Long, Integer, Integer, Integer>>) value.clone();
        }

        value_new.push(item);

        this.put(address, value_new);

    }

    @Override
    public Tuple4<Long, Integer, Integer, Integer> getItem(byte[] address) {
        Stack<Tuple4<Long, Integer, Integer, Integer>> value = this.get(address);
        return value == null || value.isEmpty() ? null : value.peek();
    }

    @Override
    public Tuple4<Long, Integer, Integer, Integer> getItem(String address) {
        Account account = new Account(address);
        return getItem(account.getShortAddressBytes());
    }

    @Override
    public void removeItem(byte[] address) {
        Stack<Tuple4<Long, Integer, Integer, Integer>> value = this.get(address);
        if (value == null || value.isEmpty()) return;

        Stack<Tuple4<Long, Integer, Integer, Integer>> value_new;
        if (false // походу если КЭШ используется там будет такая же ошибка и поэтому надо всегда делать новый объект
                // иначе новое ззначение может передать свои значения в другую обработку после форка базы
                && this.parent == null)
            value_new = (Stack<Tuple4<Long, Integer, Integer, Integer>>) value; //.clone(); // тут DEFAULT_VALUE даже меняет ((
        else {
            // !!!! NEEED .clone() !!!
            // need for updates only in fork - not in parent DB
            value_new = (Stack<Tuple4<Long, Integer, Integer, Integer>>) value.clone();
        }

        value_new.pop();

        this.put(address, value_new);

    }

}