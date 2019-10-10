package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.item.assets.Order;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.OrderSerializer;
import org.mapdb.DB;
import org.mapdb.Fun;

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
    protected void openMap() {
        //OPEN MAP
        map = database.createTreeMap("completed_orders")
                .valueSerializer(new OrderSerializer())
                .comparator(Fun.COMPARATOR)
                .makeOrGet();
    }

}
