package org.erachain.datachain;

import org.erachain.core.item.assets.Order;
import org.erachain.database.DBMap;
import org.erachain.database.serializer.OrderSerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.erachain.utils.ObserverMessage;

import java.util.Map;

/**
 * Хранит исполненные ордера, или отмененные - все что уже не активно<br>
 * <br>
 * Ключ: ссылка на запись создания заказа<br>
 * Значение: заказ<br>
 */
public class CompletedOrderMap extends DCMap<Long, Order> {

    public CompletedOrderMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_COMPL_ORDER_TYPE);
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_COMPL_ORDER_TYPE);
            this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_COMPL_ORDER_TYPE);
            this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_COMPL_ORDER_TYPE);
        }
    }

    public CompletedOrderMap(CompletedOrderMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<Long, Order> getMap(DB database) {
        //OPEN MAP
        return this.openMap(database);
    }

    @Override
    protected Map<Long, Order> getMemoryMap() {
        DB database = DBMaker.newMemoryDB().make();

        //OPEN MAP
        return this.openMap(database);
    }

    private Map<Long, Order> openMap(DB database) {
        //OPEN MAP
        BTreeMap<Long, Order> map = database.createTreeMap("completedorders")
                .valueSerializer(new OrderSerializer())
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //RETURN
        return map;
    }

    @Override
    protected Order getDefaultValue() {
        return null;
    }

    public void add(Order order) {
        // this order is NOT executable
        ////order = datachain.OrderMap.setExecutable(order, false);

        this.set(order.getId(), order);
    }

    /*
    @Override
    public Tuple3<Tuple5<Long, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> get(Long key) {
        Tuple3<Tuple5<Long, String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = super.get(key);
        ///return datachain.OrderMap.setExecutable(order, false);
        return order;
    }
    */

    public void delete(Order order) {

        this.delete(order.getId());
    }
}
