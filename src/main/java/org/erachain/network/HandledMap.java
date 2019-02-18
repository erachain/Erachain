package org.erachain.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HandledMap<K, V> extends ConcurrentHashMap {

    private int max_size;
    private List<K> handledList;

    public HandledMap(int max_size) {
        this.max_size = max_size;
        this.handledList = new ArrayList<>();
    }

    /**
     * Тут значение по ссылке - как только создали Список - его не меняем как объект, а меняем внутри его список
     * @param key
     * @param sender
     * @param forThisPeer если установлен то проверяем для конкретного Пира было ли принято такое сообщение?
     * @return true if LIST was empty
     */
    public boolean addHandledItem(Object key, Peer sender, boolean forThisPeer) {

        //CopyOnWriteArrayList sendersSet;
        Set<Peer> sendersSet;

        if (!super.containsKey(key)) {

            // Если еще нет данных
            //sendersSet = new CopyOnWriteArrayList();
            //sendersSet = Collections.synchronizedSet(new HashSet<Peer>());
            sendersSet = new HashSet<Peer>();

            if (sender != null)
                sendersSet.add(sender);

            // добавит если пусто или выдаст список который уже есть
            sendersSet = (Set<Peer>)super.putIfAbsent(key, sendersSet);

            if (sendersSet == null) {
                handledList.add((K)key);
                if (handledList.size() > this.max_size) {
                    // REMOVE first KEY
                    key = this.handledList.remove(0);
                    // REMOVE this KEY in HANDLED HASHMAP
                    super.remove(key);

                }
                return true;
            }

        } else {
            sendersSet = (Set<Peer>)super.get(key);
        }

        if (sender != null) {
            boolean result = sendersSet.add(sender);
            if (forThisPeer)
                return result;
        }

        return false;

    }

    public boolean removeFirst() {

        if (handledList.isEmpty())
            return false;

        // REMOVE first KEY
        Object key = this.handledList.remove(0);
        // REMOVE this KEY in HANDLED HASHMAP
        super.remove(key);

        return true;
    }

    public void clear() {

        handledList.clear();
        super.clear();
    }
}
