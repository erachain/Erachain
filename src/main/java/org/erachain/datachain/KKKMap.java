package org.erachain.datachain;

import org.erachain.dbs.DBTab;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import java.util.Stack;
import java.util.TreeMap;

/**
 *  Супер Класс для хранения по ключу НомерСущности + НомерСущности
// key+key to key_Stack for End_Date Map
// in days
 *
 * ключ: НомерСузности + НомерСущности
 * Значение: карта с ключем НомерСущности - обычно Актив, Значение: СТЭК (Дата Окончания, Блок, подпись транзакции ее создавшей
 *
 * TODO: передлать подпись на Long
 */
public class KKKMap extends DCUMap<
        Tuple2<Long, Long>, // item1 Key + item2 Key
        TreeMap<Long, // item3 Key
                Stack<Tuple3<
                        Long, // end_date
                        Integer, // block.getHeight
                        byte[] // transaction.getReference
                        >>>> {

    private String name;

    public KKKMap(DCSet databaseSet, DB database,
                  String name,
                  int observerMessage_reset, int observerMessage_add, int observerMessage_remove, int observerMessage_list
    ) {
        super(databaseSet, database);

        this.name = name;

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, observerMessage_reset);
            this.observableData.put(DBTab.NOTIFY_LIST, observerMessage_list);
            this.observableData.put(DBTab.NOTIFY_ADD, observerMessage_add);
            this.observableData.put(DBTab.NOTIFY_REMOVE, observerMessage_remove);
        }

    }

    public KKKMap(KKKMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    public void openMap() {
        //OPEN MAP
        map = database.createTreeMap(name)
                .keySerializer(BTreeKeySerializer.TUPLE2)
                .makeOrGet();

    }

    @Override
    protected void getMemoryMap() {
        // HashMap ?
        map = new TreeMap<Tuple2<Long, Long>, TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>>>();
    }

    @Override
    public TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>> getDefaultValue(Tuple2<Long, Long> key) {
        return new TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>>();
    }

    @SuppressWarnings("unchecked")
    public void addItem(Long key1, Long key2, Long itemKey, Tuple3<Long, Integer, byte[]> item) {

        Tuple2<Long, Long> key = new Tuple2<Long, Long>(key1, key2);

        TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>> value = this.get(key);

        TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>> value_new;
        if (false // походу если КЭШ используется там будет такая же ошибка и поэтому надо всегда делать новый объект
                // иначе новое ззначение может передать свои значения в другую обработку после форка базы
                && this.parent == null)
            value_new = value;
        else {
            // !!!! NEEED .clone() !!!
            // need for updates only in fork - not in parent DB
            value_new = (TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>>) value.clone();
        }

        Stack<Tuple3<Long, Integer, byte[]>> stack = value_new.get(itemKey);
        if (stack == null) {
            stack = new Stack<Tuple3<Long, Integer, byte[]>>();
            stack.push(item);
            value_new.put(itemKey, stack);
        } else {
            if (false // походу если КЭШ используется там будет такая же ошибка и поэтому надо всегда делать новый объект
                    // иначе новое ззначение может передать свои значения в другую обработку после форка базы
                    && this.parent == null) {
                stack.push(item);
                value_new.put(itemKey, stack);
            } else {
                Stack<Tuple3<Long, Integer, byte[]>> stack_new;
                stack_new = (Stack<Tuple3<Long, Integer, byte[]>>) stack.clone();
                stack_new.push(item);
                value_new.put(itemKey, stack_new);
            }
        }

        this.put(key, value_new);

    }

    public Tuple3<Long, Integer, byte[]> getItem(Long key1, Long key2, Long itemKey) {
        Tuple2<Long, Long> key = new Tuple2<Long, Long>(key1, key2);

        TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>> value = this.get(key);
        Stack<Tuple3<Long, Integer, byte[]>> stack = value.get(itemKey);

        //stack.elementAt()
        return stack == null || stack.isEmpty() ? null : stack.peek();
    }

    // remove only last item from stack for this key of itemKey
    @SuppressWarnings("unchecked")
    public void removeItem(Long key1, Long key2, Long itemKey) {
        Tuple2<Long, Long> key = new Tuple2<Long, Long>(key1, key2);

        TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>> value = this.get(key);

        TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>> value_new;
        if (false // походу если КЭШ используется там будет такая же ошибка и поэтому надо всегда делать новый объект
                // иначе новое ззначение может передать свои значения в другую обработку после форка базы
                && this.parent == null)
            value_new = value;
        else {
            // !!!! NEEED .clone() !!!
            // need for updates only in fork - not in parent DB
            value_new = (TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>>) value.clone();
        }

        Stack<Tuple3<Long, Integer, byte[]>> stack = value_new.get(itemKey);
        if (stack == null || stack.isEmpty()) return;

        if (false // походу если КЭШ используется там будет такая же ошибка и поэтому надо всегда делать новый объект
                // иначе новое ззначение может передать свои значения в другую обработку после форка базы
                && this.parent == null) {
            stack.pop();
            value_new.put(itemKey, stack);
        } else {
            Stack<Tuple3<Long, Integer, byte[]>> stack_new;
            stack_new = (Stack<Tuple3<Long, Integer, byte[]>>) stack.clone();
            stack_new.pop();
            value_new.put(itemKey, stack_new);
        }

        this.put(key, value_new);

    }

}