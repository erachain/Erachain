package org.erachain.datachain;

import org.erachain.core.account.Account;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;

import java.util.HashMap;
import java.util.Map;

// seek reference to tx_Parent by address
// account.addres + tx1.timestamp -> <tx2.signature>
/**
 * По адресу и времени найти подпись транзакции
 * seek reference to tx_Parent by address
 * // account.addres + tx1.timestamp -> <tx2.signature>
 *     Ключ: адрес создателя + время создания или только адрес
 *
 *     Значение: подпись транзакции или подпись последней транзакции
 *
 *     Используется для поиска публичного ключа для данного создателя и для поиска записей в отчетах
 *
       TODO укротить до 20 байт адрес
 *     TODO: заменить подпись на ссылку
 *
 * @return
 */

public class AddressTimeSignatureMap extends DCMap<Tuple2<String, Long>, byte[]> {
    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    public AddressTimeSignatureMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public AddressTimeSignatureMap(AddressTimeSignatureMap parent) {
        super(parent, null);
    }

    protected void createIndexes(DB database) {
    }

    @Override

    protected Map<Tuple2<String, Long>, byte[]> getMap(DB database) {
        //OPEN MAP
        return database.getTreeMap("references_signs");
    }

    @Override
    protected Map<Tuple2<String, Long>, byte[]> getMemoryMap() {
        return new HashMap<Tuple2<String, Long>, byte[]>();
    }

    @Override
    protected byte[] getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    public byte[] get(Account account, Long timestamp) {
        return this.get(account.getAddress(), timestamp);
    }

    public byte[] get(String address, Long timestamp) {
        return this.get(new Tuple2<String, Long>(address, timestamp));
    }

    public byte[] get(String address) {
        return this.get(new Tuple2<String, Long>(address, null));
    }

    public boolean contains(String address) {
        return this.contains((new Tuple2<String, Long>(address, null)));
    }

    public void set(Account account, Long timestampRef, byte[] signtureRef) {
        this.set(new Tuple2<String, Long>(account.getAddress(), timestampRef), signtureRef);
    }

    public void set(String address, Long timestampRef, byte[] signtureRef) {
        this.set(new Tuple2<String, Long>(address, timestampRef), signtureRef);
    }

    public void set(Account account, byte[] signtureRef) {
        this.set(new Tuple2<String, Long>(account.getAddress(), null), signtureRef);
    }

    public void set(String address, byte[] signtureRef) {
        this.set(new Tuple2<String, Long>(address, null), signtureRef);
    }

    public void delete(Account account, Long timestampRef) {
        this.delete(new Tuple2<String, Long>(account.getAddress(), timestampRef));
    }

    public void delete(String address, Long timestampRef) {
        this.delete(new Tuple2<String, Long>(address, timestampRef));
    }
}
