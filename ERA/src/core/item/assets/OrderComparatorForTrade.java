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
public class OrderComparatorForTrade implements Comparator<Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> {

    @Override
    public int compare(Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order1,
            Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order2) {
        
        int compare = order1.a.e.compareTo(order2.a.e);
        if (compare > 0)
            return 1;
        if (compare < 0)
            return -1;

        compare = order1.a.c.compareTo(order2.a.c);
        if (compare > 0)
            return 1;
        if (compare < 0)
            return -1;

        return 0;
    }

}
