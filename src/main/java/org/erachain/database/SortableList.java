package org.erachain.database;

import org.erachain.dbs.DBTab;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.erachain.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Тормознутый класс который нельзя использовать
 *
 * @param <T>
 * @param <U>
 * @deprecated
 */

public class SortableList<T, U> extends AbstractList<Pair<T, U>> implements Closeable {

    static Logger LOGGER = LoggerFactory.getLogger(SortableList.class.getName());
    private DBTab<T, U> db;
    private int index;
    private boolean descending;
    private int position;
    private IteratorCloseable<T> iterator;
    private Pattern pattern;
    private int size;
    private Pair<T, U> lastValue;
    private Collection<T> keys;
    private List<String> additionalFilterFields;


    public SortableList(DBTabImpl<T, U> db) {
        this.db = db;

        //LOAD DEFAULT ITERATOR
        this.index = db.DEFAULT_INDEX;
        this.size = db.size();
        this.descending = false;
        this.iterator = IteratorCloseableImpl.make(this.filter(db.getIndexIterator(db.DEFAULT_INDEX, this.descending)));
        this.position = 0;
        additionalFilterFields = new ArrayList<String>();
    }

    public SortableList(DBTabImpl<T, U> db, Collection<T> keys) {
        this.db = db;
        this.keys = keys;

        //LOAD DEFAULT ITERATOR
        this.index = db.DEFAULT_INDEX;
        this.size = keys.size();
        this.descending = false;
        this.iterator = IteratorCloseableImpl.make(keys.iterator());
        this.position = 0;
        additionalFilterFields = new ArrayList<String>();
    }

    public static SortableList makeSortableList(DBTabImpl map, boolean descending, int limit) {

        List keys = new ArrayList<Object>();

        // обрезаем полный список в базе до 1000
        try (IteratorCloseable iterator = map.getIndexIterator(map.DEFAULT_INDEX, descending)) {

            int i = 0;
            while (iterator.hasNext() && i++ < limit) {
                keys.add(iterator.next());
            }

        } catch (IOException e) {
        }

        return new SortableList(map, keys);

    }

    /**
     * Присоединяет к этому списку таблицу от котрой ловит события Добавить и Удалить.
     * По котрым запускает свой sort - что может очень тормозить все. <br>
     * Поэтому использовать с сотрожностью. Хотя я вставил в Update 2 секундную защелку
     * на обработку событий - если прошло меньше времени то ничего не делать
     *
     */
    //public void registerObserver() {
    //    this.db.addObserver(this);
    //}

    //public void removeObserver() {
    //   this.db.deleteObserver(this);
    //}
    @Override
    public Pair<T, U> get(int i) {

        //CHECK IF LAST VALUE
        if (this.position - 1 == i && this.lastValue != null) {
            return this.lastValue;
        }

        if (i < this.position) {
            //RESET ITERATOR
            if (this.keys != null) {
                // старый итератор закроем чтобы освободить память в РоксДБ
                try (IteratorCloseable oldIterator = this.iterator) {
                    this.iterator = IteratorCloseableImpl.make(this.keys.iterator());
                } catch (IOException e) {
                }
            } else {
                try (IteratorCloseable oldIterator = this.iterator; IteratorCloseable newIterator = this.db.getIndexIterator(index, descending)) {
                    this.iterator = IteratorCloseableImpl.make(this.filter(newIterator));
                } catch (IOException e) {
                }
            }

            this.position = 0;
        }

        //ITERATE UNTIL WE ARE AT THE POSITION
        while (this.position < i && this.iterator.hasNext()) {
            this.iterator.next();
            this.position++;
        }

        if (!this.iterator.hasNext()) {
            return null;
        }

        //RETURN
        T key = this.iterator.next();
        U value = this.db.get(key);
        this.position++;
        this.lastValue = new Pair<T, U>(key, value);
        return this.lastValue;

    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean contains(Object key) {
        if (this.db.contains((T)key))
            return true;

        return false;
    }

    public void sort() {
        if (this.keys != null) {
            this.size = this.keys.size();
            // старый итератор закроем чтобы освободить память в РоксДБ
            try (IteratorCloseable oldIterator = this.iterator) {
                this.iterator = IteratorCloseableImpl.make(this.keys.iterator());
            } catch (IOException e) {
            }
        } else {
            this.size = db.size();
            // старый итератор закроем чтобы освободить память в РоксДБ
            try (IteratorCloseable oldIterator = this.iterator; IteratorCloseable newIterator = this.db.getIndexIterator(index, descending)) {
                this.iterator = IteratorCloseableImpl.make(this.filter(newIterator));
            } catch (IOException e) {
            }
        }

        this.position = 0;
        this.lastValue = null;
    }

    public void sort(int index, boolean descending) {
        this.index = index;
        this.descending = descending;

        this.sort();

    }

    public void sort(int index) {
        this.index = index;
        this.descending = false;

        this.sort();
    }

	/*
	public void rescan() {
		
		this.size = db.size();
		this.iterator = this.filter(this.db.getIterator(index, descending));

		this.position = 0;
		this.lastValue = null;
		
	}
	*/


    private long timePoint;

    /*
     * нужно только для сортировки при измнении таблицы
     * @param o
     * @param object
    @Override
    public void update(Observable o, Object object) {

        if (this.db == null || this.db.observableData == null)
            return;

        // ограничим частоту сортировки
        if (System.currentTimeMillis() - timePoint < 2000)
            return;

        timePoint = System.currentTimeMillis();

        ObserverMessage message = (ObserverMessage) object;

        if (this.db.observableData.containsKey(DBMap.NOTIFY_ADD)
                && message.getType() == this.db.observableData.get(DBMap.NOTIFY_ADD)
            || this.db.observableData.containsKey(DBMap.NOTIFY_REMOVE)
                && message.getType() == this.db.observableData.get(DBMap.NOTIFY_REMOVE)) {
            //RESORT DATA
            this.sort();
        }

    }
    */

    private Iterator<T> filter(Iterator<T> iterator) {
        if (this.pattern != null) {
            List<T> keys = new ArrayList<T>();

            Main:
            while (iterator.hasNext()) {
                T key = iterator.next();
                String keyString = key.toString();

                Matcher matcher = this.pattern.matcher(keyString);
                if (matcher.find()) {
                    keys.add(key);
                    continue Main;
                }

                U value = this.db.get(key);

                for (String fieldToSearch : additionalFilterFields) {

                    try {
                        Field field = value.getClass().getDeclaredField(fieldToSearch);
                        field.setAccessible(true);
                        String searchVal = (String) field.get(value);

                        matcher = this.pattern.matcher(searchVal);
                        if (matcher.find()) {
                            keys.add(key);
                            continue Main;
                        }

                    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {

                        LOGGER.error(e.getMessage(), e);
                    }
                }

            }

            this.size = keys.size();
            return keys.iterator();
        }

        return iterator;
    }

    public void setFilter(String filter) {
        this.pattern = Pattern.compile(".*" + filter + ".*");
        this.sort();
    }

    /**
     * Add a field to the filter list
     *
     * @param fieldname this should be a field of type String in the Class of generic type U
     */
    public void addFilterField(String fieldname) {
        if (!additionalFilterFields.contains(fieldname)) {
            additionalFilterFields.add(fieldname);
        }
    }

    @Override
    public void close() {
        try {
            iterator.close();
        } catch (IOException e) {

        }
    }

    @Override
    public void finalize() {
        close();
    }
}
