package org.erachain.datachain;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple4;

import java.util.Stack;
import java.util.TreeMap;

/**
 * Хранит Удостоверения персон для заданного публичного ключа.
 * address -> Stack person + end_date + block.height + transaction.reference.
 * Тут block.getHeight + transaction index  - это ссылка на транзакцию создавшую данную заметку<br>
 *
 * <b>Ключ:</b> (String)publickKey<br>

 * <b>Значение:</b><br>
 Stack((Long)person key,
 (Integer)end_date - дата окончания действия удостоврения,<br>
 (Integer)block.getHeight - номер блока,<br>
 (Integer)transaction index - номер транзакции в блоке<br>
 ))
 */
// TODO укротить до 20 байт адрес и ссылку на Long
public class AddressPersonMap extends DCUMap<String, Stack<Tuple4<
        Long, // person key
        Integer, // end_date day
        Integer, // block height
        Integer>>> // transaction index
{
    public AddressPersonMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public AddressPersonMap(AddressPersonMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    protected void createIndexes() {
    }

    @Override
    protected void openMap() {
        //OPEN MAP
        map = database.createTreeMap("address_person")
                .keySerializer(BTreeKeySerializer.STRING)
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        // HashMap ?
        map = new TreeMap<String, Stack<Tuple4<Long, Integer, Integer, Integer>>>();
    }

    @Override
    protected Stack<Tuple4<Long, Integer, Integer, Integer>> getDefaultValue() {
        return new Stack<Tuple4<Long, Integer, Integer, Integer>>();
    }

    ///////////////////////////////
    @SuppressWarnings("unchecked")
    public void addItem(String address, Tuple4<Long, Integer, Integer, Integer> item) {
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

        this.set(address, value_new);

    }

    public Tuple4<Long, Integer, Integer, Integer> getItem(String address) {
        Stack<Tuple4<Long, Integer, Integer, Integer>> value = this.get(address);
        return value == null || value.isEmpty() ? null : value.peek();
    }

    @SuppressWarnings("unchecked")
    public void removeItem(String address) {
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

        this.set(address, value_new);

    }

}
