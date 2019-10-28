package org.erachain.datachain;

import org.erachain.database.DBASet;
import org.mapdb.DB;

/**
 * суперкласс для таблиц цепочки блоков с функционалом Форканья (см. fork()
 * - универсальный в котром есть Мапка для Форка через getForkedMap
 * @param <T>
 * @param <U>
<br><br>
ВНИМАНИЕ !!! Вторичные ключи не хранят дубли - тоесть запись во втричном ключе не будет учтена иперезапишется если такой же ключ прийдет
Поэтому нужно добавлять униальность

 */
public abstract class DCUMap<T, U> extends org.erachain.dbs.DCUMapImpl<T, U> {

    public DCUMap(DBASet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public DCUMap(org.erachain.dbs.DCUMapImpl<T, U> parent, DBASet dcSet) {
        super(parent, dcSet);
    }

}