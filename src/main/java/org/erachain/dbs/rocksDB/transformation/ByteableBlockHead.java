package org.erachain.dbs.rocksDB.transformation;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.block.Block;
import org.erachain.dbs.rocksDB.exceptions.WrongParseException;

@Slf4j
public class ByteableBlockHead implements Byteable<Block.BlockHead> {
    @Override
    public Block.BlockHead receiveObjectFromBytes(byte[] bytes) {
        try {
            return Block.BlockHead.parse(bytes);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new WrongParseException(e);
        }
    }

    @Override
    public byte[] toBytesObject(Block.BlockHead value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        return value.toBytes();
    }
}
