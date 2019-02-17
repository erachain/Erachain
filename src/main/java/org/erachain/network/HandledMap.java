package org.erachain.network;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class HandledMap<K, V> extends ConcurrentSkipListMap {

    /**
     * Тут значение по ссылке - как только создали Список - его не меняем как объект, а меняем внутри его список
     * @param key
     * @param item
     * @return true if LIST was empty
     */
    public boolean addHandledItem(Object key, Object item) {

        CopyOnWriteArrayList itemsList;

        if (!this.containsKey(key)) {

            // Если еще нет данных
            itemsList = new CopyOnWriteArrayList();
            itemsList.add(item);

            // добавит если пусто или выдаст список который уже есть
            itemsList = (CopyOnWriteArrayList)this.putIfAbsent(key, itemsList);

            if (itemsList == null)
                return true;

        } else {
            itemsList = (CopyOnWriteArrayList)this.get(key);
        }

        itemsList.add(item);

        return false;

    }
}
