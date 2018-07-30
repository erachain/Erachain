package core.item.assets;

import datachain.DCSet;
import datachain.OrderMap;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * Sorts Orders by price and TIMESTAMP for resolve exchange
 *
 * @author icreator
 */
public class OrderKeysComparatorForTradeReverse implements Comparator<Long> {

    @Override
    public int compare(Long orderKey1, Long orderKey2) {

        OrderMap map = DCSet.getInstance().getOrderMap();

        Tuple3<Tuple5<Long, String, Long, Boolean, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order1 = map.get(orderKey1);
        Tuple3<Tuple5<Long, String, Long, Boolean, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order2 = map.get(orderKey2);

        int compare = order1.a.e.compareTo(order2.a.e);
        if (compare != 0)
            return -compare;

        return -order1.a.c.compareTo(order2.a.c);

    }

}
