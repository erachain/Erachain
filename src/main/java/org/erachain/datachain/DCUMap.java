package org.erachain.datachain;

import org.erachain.database.DBASet;
import org.mapdb.DB;
import org.mapdb.Serializer;

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

    public DCUMap(DBASet databaseSet, DB database, String tabName, Serializer tabSerializer) {
        super(databaseSet, database, tabName, tabSerializer, false);
    }

    public DCUMap(DBASet databaseSet, DB database) {
        super(databaseSet, database, false);
    }
    public DCUMap(DBASet databaseSet, DB database, boolean sizeEnable) {
        super(databaseSet, database, sizeEnable);
    }

    public DCUMap(org.erachain.dbs.DCUMapImpl<T, U> parent, DBASet dcSet) {
        super(parent, dcSet, false);
    }

    public DCUMap(org.erachain.dbs.DCUMapImpl<T, U> parent, DBASet dcSet, boolean sizeEnable) {
        super(parent, dcSet, sizeEnable);
    }

}