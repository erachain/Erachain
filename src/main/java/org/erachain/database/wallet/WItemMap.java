package org.erachain.database.wallet;

import org.erachain.core.item.ItemCls;
import org.erachain.database.serializer.ItemSerializer;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DCUMapImpl;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

// TODO reference as TIMESTAMP of transaction

/**
 * key: Address + refDB</br>
 * Value: autoIncrement + Object
 */
public class WItemMap extends DCUMapImpl<Long, ItemCls> {

    public static final int NAME_INDEX = 1;
    public static final int CREATOR_INDEX = 2;
    static Logger LOGGER = LoggerFactory.getLogger(WItemMap.class.getName());
    protected int type;
    protected String name;

    public WItemMap(DWSet dWSet, DB database, int type, String name,
                    int observeReset,
                    int observeAdd,
                    int observeRemove,
                    int observeList
    ) {

        // не создаем КАрту и Индексы так как не известно пока ИМЯ и ТИП
        super(dWSet);

        this.type = type;
        this.name = name;

        // Теперь задаем БАЗУ
        this.database = database;

        // ИМЯ и ТИП заданы, создаем карту и ИНдексы
        openMap();

        ///makeAutoKey(database, (Bind.MapWithModificationListener)map, name + "_wak");

        this.createIndexes();

        if (databaseSet.isWithObserver()) {
            observableData = new HashMap<Integer, Integer>(8, 1);
            this.observableData.put(DBTab.NOTIFY_RESET, observeReset);
            this.observableData.put(DBTab.NOTIFY_LIST, observeList);
            this.observableData.put(DBTab.NOTIFY_ADD, observeAdd);
            this.observableData.put(DBTab.NOTIFY_REMOVE, observeRemove);
        }
    }

    @Override
    public void openMap() {

        HI = Long.MAX_VALUE;
        LO = 0L;

        //OPEN MAP
        if (this.name == null)
            return;

        map = database.createTreeMap(this.name)
                .keySerializer(BTreeKeySerializer.ZERO_OR_POSITIVE_LONG)
                .valueSerializer(new ItemSerializer(this.type))
                .counterEnable()
                .makeOrGet();
        //map = ((BTreeMap) map).descendingMap();
    }

    @Override
    protected void getMemoryMap() {
    }

    public ItemCls get(Long key) {
        ItemCls item = super.get(key);
        item.setKey(key);
        return item;
    }
}
