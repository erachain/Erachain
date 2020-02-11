package org.erachain.datachain;

import org.erachain.dbs.IteratorCloseable;

public interface CompletedOrderSuit {

    IteratorCloseable<Long> getAddressIterator(String address, Long fromOrder);
}
