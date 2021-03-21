package org.erachain.dbs.rocksDB.transformation;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.block.Block;
import org.erachain.core.block.BlockFactory;
import org.erachain.dbs.rocksDB.exceptions.WrongParseException;

@Slf4j
public class ByteableBlock implements Byteable<Block>{
    @Override
    public Block receiveObjectFromBytes(byte[] bytes) {
        try {
            return BlockFactory.getInstance().parse(bytes, 0);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new WrongParseException(e);
        }
    }

    @Override
    public byte[] toBytesObject(Block value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        return value.toBytes(true, true);
    }
}
