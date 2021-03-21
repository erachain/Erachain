package org.erachain.dbs.rocksDB.transformation;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.item.assets.Order;
import org.erachain.dbs.rocksDB.exceptions.WrongParseException;
import org.mapdb.Fun.Tuple2;

import java.util.Arrays;

@Slf4j
public class ByteableLongAndOrder implements Byteable<Tuple2<Long, Order>> {

    private ByteableLong byteableLong = new ByteableLong();

    private ByteableOrder byteableOrder = new ByteableOrder();

    @Override
    public Tuple2<Long, Order> receiveObjectFromBytes(byte[] bytes) {
        byte[] longBytes = Arrays.copyOf(bytes, Long.BYTES);
        Long number = byteableLong.receiveObjectFromBytes(longBytes);
        byte[] bytesOrder = Arrays.copyOfRange(bytes, Long.BYTES, bytes.length);
        try {
            return new Tuple2<>(number,
                    byteableOrder.receiveObjectFromBytes(bytesOrder));
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new WrongParseException(e);
        }
    }


    @Override
    public byte[] toBytesObject(Tuple2<Long, Order> value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        Long aLong = value.a;
        Order order = value.b;
        byte[] bytesLong = byteableLong.toBytesObject(aLong);
        byte[] bytesOrder = order.toBytes();
        return org.bouncycastle.util.Arrays.concatenate(bytesLong, bytesOrder);
    }
}
