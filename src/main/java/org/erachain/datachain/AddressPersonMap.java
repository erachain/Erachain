package org.erachain.datachain;

import org.erachain.dbs.DBTab;
import org.mapdb.Fun;

import java.util.Stack;

public interface AddressPersonMap extends DBTab<byte[], Stack<Fun.Tuple4<
        Long, // person key
        Integer, // end_date day
        Integer, // block height
        Integer>>> {

    void addItem(String address, Fun.Tuple4<Long, Integer, Integer, Integer> item);

    void addItem(byte[] address, Fun.Tuple4<Long, Integer, Integer, Integer> item);

    Fun.Tuple4<Long, Integer, Integer, Integer> getItem(String address);

    Fun.Tuple4<Long, Integer, Integer, Integer> getItem(byte[] address);

    void removeItem(String address);

    void removeItem(byte[] address);
}
