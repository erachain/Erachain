package org.erachain.dbs.mapDB;

import org.erachain.core.item.assets.Order;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.OrderSerializer;
import org.erachain.datachain.CompletedOrderMap;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DBTabImpl;
import org.erachain.utils.ObserverMessage;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;

import java.util.Map;

/**
 * Хранит исполненные ордера, или отмененные - все что уже не активно<br>
 * <br>
 * Ключ: ссылка на запись создания заказа<br>
 * Значение: заказ<br>
 */

@Slf4j
public class CompletedOrdersSuitMapDB extends DBMapSuit<Long, Order> {

    public CompletedOrdersSuitMapDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger);
    }

    @Override
    protected void getMap() {
        //OPEN MAP
        map = database.createTreeMap("completed_orders")
                .valueSerializer(new OrderSerializer())
                .comparator(Fun.COMPARATOR)
                .makeOrGet();
    }

}
