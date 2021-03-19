package org.erachain.dbs.rocksDB.transformation;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.item.assets.Trade;
import org.erachain.dbs.rocksDB.exceptions.WrongParseException;

@Slf4j
public class ByteableTrade implements Byteable<Trade>{

    @Override
    public Trade receiveObjectFromBytes(byte[] bytes) {
        try {
            return Trade.parse(bytes);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new WrongParseException(e);
        }
    }

    @Override
    public byte[] toBytesObject(Trade value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        return value.toBytes();
    }







}
