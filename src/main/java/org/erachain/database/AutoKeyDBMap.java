package org.erachain.database;

import org.erachain.dbs.DCUMapImpl;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import java.util.Collection;

public abstract class AutoKeyDBMap<T, U> extends DCUMapImpl<T, U> {

    protected BTreeMap AUTOKEY_INDEX;

    public AutoKeyDBMap(DBASet databaseSet) {
        super(databaseSet);
    }

    public AutoKeyDBMap(DBASet databaseSet, DB database) {
        super(databaseSet, database);

    }

    protected void makeAutoKey(DB database, Bind.MapWithModificationListener map, String name) {

        this.AUTOKEY_INDEX = database.createTreeMap(name + "_AUTOKEY_INDEX")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //BIND
        Bind.secondaryKey(map, this.AUTOKEY_INDEX, new Fun.Function2<Long, T, Tuple2>() {
            @Override
            public Long run(T key, Tuple2 value) {
                return (Long) value.a;
            }
        });
    }

    public Collection<T> getFromToKeys(long fromKey, long limit) {
        // РАБОТАЕТ намного БЫСТРЕЕ
        return AUTOKEY_INDEX.subMap(fromKey, fromKey + limit).values();

    }

    /**
     * добавляем только в конец по AUTOKEY_INDEX, иначе обновляем
     * @param key
     * @param value
     * @return
     */
    @Override
    public boolean set(T key, U value) {

        if (this.contains(key)) {
            // если запись тут уже есть то при перезаписи не меняем AUTO_KEY
            Tuple2 item = (Tuple2) super.get(key);
            return super.set(key, (U) new Tuple2(item.a, ((Tuple2)value).b));

        } else {
            // новый элемент - добавим в конец карты
            return super.set(key, (U) new Tuple2(-((long)size() + 1), ((Tuple2)value).b));
        }
    }

    /**
     * удаляет только если это верхний элемент. Инача ничего не делаем, так как иначе размер собъется
     *
     * @param key
     * @return
     */
    public U remove(T key) {

        U item = super.get(key);
        if (item == null) {
            return item;
        }

        // отрицательны и со сдигом -1
        if (((Long)((Tuple2)item).a).equals(-size() - 1))
            return super.remove(key);

        return item;
    }


}
