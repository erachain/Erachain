package org.erachain.datachain;

import org.erachain.dbs.DBTab;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple3;

import java.util.Stack;
import java.util.TreeMap;

/**
 * Супер Класс
 *
 * Ключ: НомерСущности
 * Значение: крта по строке - не используется - лучше байты
 *
 // key to Name_Stack End_Date Map
 // in days
 */
public class KNMap extends DCUMap<
        Long, // item1 Key
        TreeMap<String, // item2 Key
                Stack<Tuple3<
                        Long, // end_date
                        Integer, // block.getHeight
                        byte[] // transaction.getReference
                        >>>> {

    private String name;

    public KNMap(DCSet databaseSet, DB database,
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

    public KNMap(KNMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    public void openMap() {
        //OPEN MAP
        map = database.createTreeMap(name)
                .keySerializer(BTreeKeySerializer.BASIC)
                .makeOrGet();

    }

    @Override
    protected void getMemoryMap() {
        // HashMap ?
        map = new TreeMap<Long, TreeMap<String, Stack<Tuple3<Long, Integer, byte[]>>>>();
    }

    @Override
    public TreeMap<String, Stack<Tuple3<Long, Integer, byte[]>>> getDefaultValue(Long key) {
        return new TreeMap<String, Stack<Tuple3<Long, Integer, byte[]>>>();
    }

    @SuppressWarnings("unchecked")
    public void addItem(Long key, String nameKey, Tuple3<Long, Integer, byte[]> item) {

        TreeMap<String, Stack<Tuple3<Long, Integer, byte[]>>> value = this.get(key);

        TreeMap<String, Stack<Tuple3<Long, Integer, byte[]>>> value_new;
        if (false // походу если КЭШ используется там будет такая же ошибка и поэтому надо всегда делать новый объект
                // иначе новое ззначение может передать свои значения в другую обработку после форка базы
                && this.parent == null)
            value_new = value;
        else {
            // !!!! NEEED .clone() !!!
            // need for updates only in fork - not in parent DB
            value_new = (TreeMap<String, Stack<Tuple3<Long, Integer, byte[]>>>) value.clone();
        }

        Stack<Tuple3<Long, Integer, byte[]>> stack = value_new.get(nameKey);
        if (stack == null) {
            stack = new Stack<Tuple3<Long, Integer, byte[]>>();
            stack.push(item);
            value_new.put(nameKey, stack);
        } else {
            if (false // походу если КЭШ используется там будет такая же ошибка и поэтому надо всегда делать новый объект
                    // иначе новое ззначение может передать свои значения в другую обработку после форка базы
                    && this.parent == null) {
                stack.push(item);
                value_new.put(nameKey, stack);
            } else {
                Stack<Tuple3<Long, Integer, byte[]>> stack_new;
                stack_new = (Stack<Tuple3<Long, Integer, byte[]>>) stack.clone();
                stack_new.push(item);
                value_new.put(nameKey, stack_new);
            }

        }

        this.put(key, value_new);
    }

    public Tuple3<Long, Integer, byte[]> getItem(Long key, String nameKey) {
        TreeMap<String, Stack<Tuple3<Long, Integer, byte[]>>> value = this.get(key);
        Stack<Tuple3<Long, Integer, byte[]>> stack = value.get(nameKey);
        return stack == null || stack.isEmpty() ? null : stack.peek();
    }

    // remove only last item from stack for this key of nameKey
    @SuppressWarnings("unchecked")
    public void removeItem(Long key, String nameKey) {
        TreeMap<String, Stack<Tuple3<Long, Integer, byte[]>>> value = this.get(key);

        TreeMap<String, Stack<Tuple3<Long, Integer, byte[]>>> value_new;
        if (false // походу если КЭШ используется там будет такая же ошибка и поэтому надо всегда делать новый объект
                // иначе новое ззначение может передать свои значения в другую обработку после форка базы
                && this.parent == null)
            value_new = value;
        else {
            // !!!! NEEED .clone() !!!
            // need for updates only in fork - not in parent DB
            value_new = (TreeMap<String, Stack<Tuple3<Long, Integer, byte[]>>>) value.clone();
        }

        Stack<Tuple3<Long, Integer, byte[]>> stack = value_new.get(nameKey);
        if (stack == null) return;

        if (false // походу если КЭШ используется там будет такая же ошибка и поэтому надо всегда делать новый объект
                // иначе новое ззначение может передать свои значения в другую обработку после форка базы
                && this.parent == null) {
            stack.pop();
            value_new.put(nameKey, stack);
        } else {
            Stack<Tuple3<Long, Integer, byte[]>> stack_new;
            stack_new = (Stack<Tuple3<Long, Integer, byte[]>>) stack.clone();
            stack_new.pop();
            value_new.put(nameKey, stack_new);
        }

        this.put(key, value_new);

    }

}
