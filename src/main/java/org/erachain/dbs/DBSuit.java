package org.erachain.dbs;

/**
 * Оболочка для Карты от конкретной СУБД чтобы эту оболочку вставлять в Таблицу, которая запускает события для ГУИ.
 * Для каждой СУБД свой порядок обработки команд
 *
 * @param <T>
 * @param <U>
 */
public interface DBSuit<T, U> extends IMap<T, U> {

    Object getSource();
}
