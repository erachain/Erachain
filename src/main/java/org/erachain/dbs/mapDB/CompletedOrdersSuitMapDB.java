package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.item.assets.Order;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.OrderSerializer;
import org.erachain.datachain.CompletedOrderSuit;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;

/**
 * Хранит исполненные ордера, или отмененные - все что уже не активно<br>
 * <br>
 * Ключ: ссылка на запись создания заказа<br>
 * Значение: заказ<br>
 */

@Slf4j
public class CompletedOrdersSuitMapDB extends DBMapSuit<Long, Order> implements CompletedOrderSuit {

    private BTreeMap addressKeyMap;

    public CompletedOrdersSuitMapDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger, false);
    }

    @Override
    public void openMap() {

        HI = Long.MAX_VALUE;
        LO = 0L;

        //OPEN MAP
        map = database.createTreeMap("completed_orders")
                .valueSerializer(new OrderSerializer())
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

        ///////////////////// HERE NOT PROTOCOL INDEXES

        // ADDRESS HAVE/WANT KEY
        this.addressKeyMap = database.createTreeMap("completed_orders_key_address")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //BIND HAVE/WANT KEY
        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.addressKeyMap,
                new Fun.Function2<Fun.Tuple2<String, Long>, Long, Order>() {
                    @Override
                    public Fun.Tuple2<String, Long> run(
                            Long key, Order value) {
                        return new Fun.Tuple2<String, Long>
                                (value.getCreator().getAddress(), key);
                    }
                });

    }

    @Override
    public IteratorCloseable<Long> getAddressIterator(String address, Long fromOrder) {

        return new IteratorCloseableImpl(((BTreeMap<Fun.Tuple2, Long>) this.addressKeyMap).subMap(
                Fun.t2(address, fromOrder),
                Fun.t2(address, Long.MAX_VALUE)).values().iterator());
    }

}
