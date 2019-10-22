package org.erachain.datachain;

import org.erachain.core.crypto.Base58;
import org.erachain.dbs.DBTab;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import java.util.TreeMap;

/**
 * Учет времени последней транзакции данного вида, посланной со счета - на подобие как сделан
 * учет последней трнзакции сл счета без учета типа трнзакции - ReferenceMap,
 * - для быстрого поиска записей данного вида для данного счета.
 * Пока не используется ни где.
 * Если ключ создан без времени, то хранит ссылку на последнюю транзакцию с этого счета
 * Ключ - счет (20 байт) + время (Long)
 * Значение  - массив байтов
 * Используется как супер класс для AddressStatementRefs (которая сейчас не используется?)
 */
public abstract class AddressItemRefs extends DCUMap<Tuple2<byte[], Long>, byte[]> {
    protected String name;

    public AddressItemRefs(DCSet databaseSet, DB database, String name,
                           int observeReset, int observeAdd, int observeRemove, int observeList
    ) {
        super(databaseSet, database);
        this.name = name;

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, observeReset);
            this.observableData.put(DBTab.NOTIFY_LIST, observeList);
            this.observableData.put(DBTab.NOTIFY_ADD, observeAdd);
            this.observableData.put(DBTab.NOTIFY_REMOVE, observeRemove);
        }

    }


    public AddressItemRefs(AddressItemRefs parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    protected void createIndexes() {
    }

    @Override
    protected void openMap() {
        //OPEN MAP
        map = database.createTreeMap("address_" + this.name + "_refs")
                //.keySerializer(BTreeKeySerializer.TUPLE2)
                //.comparator(UnsignedBytes.lexicographicalComparator())
                .comparator(new Fun.Tuple2Comparator(Fun.BYTE_ARRAY_COMPARATOR, Fun.COMPARATOR)) // - for Tuple2<byte[]m byte[]>
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        //return new TreeMap<Tuple2<byte[], Long>, byte[]>(UnsignedBytes.lexicographicalComparator());
        map = new TreeMap<Tuple2<byte[], Long>, byte[]>();
    }

    @Override
    protected byte[] getDefaultValue() {
        return null;
    }

    public byte[] get(String address, Long key) {
        return this.get(new Tuple2<byte[], Long>(Base58.decode(address), key));
    }

    public void set(String address, Long key, byte[] ref) {
        this.set(new Tuple2<byte[], Long>(Base58.decode(address), key), ref);
    }

    public void delete(String address, Long key) {
        this.remove(new Tuple2<byte[], Long>(Base58.decode(address), key));
    }
}
