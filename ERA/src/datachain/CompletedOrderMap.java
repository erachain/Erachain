package datachain;

import core.item.assets.Order;
import database.DBMap;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;
import utils.ObserverMessage;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class CompletedOrderMap extends DCMap<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
        Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> {
    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    public CompletedOrderMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        /*
        duplicated in OrderMap
        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_ORDER_TYPE);
            if (databaseSet.isDynamicGUI()) {
                this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_ORDER_TYPE);
                this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_ORDER_TYPE);
            }
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_ORDER_TYPE);
        }
        */
    }

    public CompletedOrderMap(CompletedOrderMap parent) {
        super(parent, null);
    }

    @Override
    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getMap(DB database) {
        //OPEN MAP
        return this.openMap(database);
    }

    @Override
    protected Map<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getMemoryMap() {
        DB database = DBMaker.newMemoryDB().make();

        //OPEN MAP
        return this.openMap(database);
    }

    private Map<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> openMap(DB database) {
        //OPEN MAP
        BTreeMap<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> map = database.createTreeMap("completedorders")
                //.valueSerializer(new OrderSerializer())
                .makeOrGet();

        //RETURN
        return map;
    }

    @Override
    protected Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    public void add(Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order) {
        // this order is NOT executable
        ////order = datachain.OrderMap.setExecutable(order, false);

        this.set(order.a.a, order);
    }

    /*
    @Override
    public Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> get(BigInteger key) {
        Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = super.get(key);
        ///return datachain.OrderMap.setExecutable(order, false);
        return order;
    }
    */

    public void delete(Order order) {
        this.delete(order.getId());
    }
}
