package org.erachain.dbs;

/**
 * Общий Описатель Таблиц (Tab) и Обернутых карт - (Suit)
 *
 * @param <T>
 * @param <U>
 */
public interface UMap<T, U> {

    /**
     * not check old value
     *
     * @param key
     * @param value
     */
    void put(T key, U value);

    /**
     * not check old value
     *
     * @param key
     * @return
     */
    void delete(T key);

}
