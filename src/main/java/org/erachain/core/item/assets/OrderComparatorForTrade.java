package org.erachain.core.item.assets;

import java.util.Comparator;

/**
 * Sorts Orders by price and TIMESTAMP for resolve exchange
 *
 * @author icreator
 */
public class OrderComparatorForTrade implements Comparator<Order> {

    @Override
    public int compare(Order order1, Order order2) {

        int compare = order1.calcLeftPrice().compareTo(order2.calcLeftPrice());
        if (compare != 0)
            return compare;

        return order1.getId().compareTo(order2.getId());

    }

}
