package org.erachain.rocksDB.transformation;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.item.assets.Order;
import org.erachain.rocksDB.exceptions.WrongParseException;

@Slf4j
public class ByteableOrder implements Byteable<Order>{
    @Override
    public Order receiveObjectFromBytes(byte[] bytes) {
        try {
            return Order.parse(bytes);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new WrongParseException(e);
        }
    }

    @Override
    public byte[] toBytesObject(Order value) {
        return value.toBytes();
    }
}
