package org.erachain.dbs;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Оболочка для Карты от конкретной СУБД чтобы эту оболочку вставлять в Таблицу, которая запускает события для ГУИ.
 * Для каждой СУБД свой порядок обработки команд
 * @param <T>
 * @param <U>
 */
public interface DBMapSuit<T, U> extends DBTabSuitCommon<T, U> {

    //void getMap();
    //void createIndexes();

    U getDefaultValue();

}
