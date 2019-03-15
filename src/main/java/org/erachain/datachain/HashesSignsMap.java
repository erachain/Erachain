package org.erachain.datachain;

import com.google.common.primitives.UnsignedBytes;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple3;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

//import org.mapdb.Fun.Tuple2;
//import org.mapdb.Fun.Tuple3;

// hash[byte] -> Stack person + block.height + transaction.seqNo
// Example - database.AddressPersonMap
/** Набор хэшей - по хэшу поиск записи в котрой он участвует и
 * используется в транзакции org.erachain.core.transaction.R_Hashes
 hash[byte] -> Stack person + block.height + transaction.seqNo

 * Ключ: хэш<br>
 * Значение: список - номер персоны (Если это персона создала запись, ссылка на запись)<br>
 // TODO укротить до 20 байт адрес и ссылку на Long

 */

public class HashesSignsMap extends DCMap<byte[], Stack<Tuple3<
        Long, // person key
        Integer, // block height
        Integer>>> // transaction index
{

    public HashesSignsMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public HashesSignsMap(HashesSignsMap parent) {
        super(parent, null);
    }

    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<byte[], Stack<Tuple3<Long, Integer, Integer>>> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap("hashes_signs")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .makeOrGet();
    }

    @Override
    protected Map<byte[], Stack<Tuple3<Long, Integer, Integer>>> getMemoryMap() {
        return new TreeMap<byte[], Stack<Tuple3<Long, Integer, Integer>>>(UnsignedBytes.lexicographicalComparator());
    }

    @Override
    protected Stack<Tuple3<Long, Integer, Integer>> getDefaultValue() {
        return new Stack<Tuple3<Long, Integer, Integer>>();
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    ///////////////////////////////
    @SuppressWarnings("unchecked")
    public void addItem(byte[] hash, Tuple3<Long, Integer, Integer> item) {

        Stack<Tuple3<Long, Integer, Integer>> value = this.get(hash);

        Stack<Tuple3<Long, Integer, Integer>> value_new;

        if (this.parent == null)
            value_new = value;
        else {
            // !!!! NEEED .clone() !!!
            // need for updates only in fork - not in parent DB
            value_new = (Stack<Tuple3<Long, Integer, Integer>>) value.clone();
        }

        value_new.push(item);

        this.set(hash, value_new);

    }

    public Tuple3<Long, Integer, Integer> getItem(byte[] hash) {
        Stack<Tuple3<Long, Integer, Integer>> value = this.get(hash);
        return !value.isEmpty() ? value.peek() : null;
    }

    @SuppressWarnings("unchecked")
    public void removeItem(byte[] hash) {
        Stack<Tuple3<Long, Integer, Integer>> value = this.get(hash);
        if (value == null || value.isEmpty()) return;

        Stack<Tuple3<Long, Integer, Integer>> value_new;
        if (this.parent == null)
            value_new = value;
        else {
            // !!!! NEEED .clone() !!!
            // need for updates only in fork - not in parent DB
            value_new = (Stack<Tuple3<Long, Integer, Integer>>) value.clone();
        }

        value_new.pop();

        this.set(hash, value_new);

    }

}
