package org.erachain.dbs.rocksDB.transformation;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.tuple.Tuple2;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.dbs.rocksDB.exceptions.WrongParseException;

import java.util.Arrays;
@Slf4j
public class ByteableLongItem implements Byteable<Tuple2<Long, ItemCls>> {


    private ByteableLong byteableLong = new ByteableLong();

    private ByteableItem byteableItem;

    //ItemCls.ASSET_TYPE:
    //ItemCls.IMPRINT_TYPE:
    //ItemCls.TEMPLATE_TYPE:
    //ItemCls.PERSON_TYPE:
    //ItemCls.POLL_TYPE:
    //ItemCls.STATUS_TYPE:
    //ItemCls.UNION_TYPE:
    public ByteableLongItem(int type) {
        byteableItem = new ByteableItem(type);
    }

    @Override
    public Tuple2<Long, ItemCls> receiveObjectFromBytes(byte[] bytes) {
        byte[] longBytes = Arrays.copyOf(bytes, Long.BYTES);
        Long number = byteableLong.receiveObjectFromBytes(longBytes);
        byte[] byteItem = Arrays.copyOfRange(bytes, Long.BYTES, bytes.length);
        try {
            return new Tuple2<>(number,
                    byteableItem.receiveObjectFromBytes(byteItem));
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new WrongParseException(e);
        }
    }

    @Override
    public byte[] toBytesObject(Tuple2<Long, ItemCls> value) {
        Tuple2<Long, ItemCls> tuple = value;
        Long aLong = tuple.f0;
        ItemCls itemCls = tuple.f1;
        byte[] bytesLong = byteableLong.toBytesObject(aLong);
        byte[] bytesItem = byteableItem.toBytesObject(itemCls);
        return org.bouncycastle.util.Arrays.concatenate(bytesLong, bytesItem);
    }
}
