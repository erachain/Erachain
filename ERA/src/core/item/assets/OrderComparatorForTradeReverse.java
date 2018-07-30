package core.item.assets;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

/**
 * Sorts Orders by price and TIMESTAMP for resolve exchange
 *
 * @author icreator
 */
public class OrderComparatorForTradeReverse implements Comparator<Order> {

    @Override
    public int compare(Order order1, Order order2) {

        int compare = order1.getPrice().compareTo(order2.getPrice());
        if (compare != 0)
            return -compare;

        return -Long.signum(order1.getTimestamp() - order2.getTimestamp());

    }

}
