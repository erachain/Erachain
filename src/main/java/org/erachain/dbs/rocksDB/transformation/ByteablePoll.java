package org.erachain.rocksDB.transformation;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.voting.Poll;
import org.erachain.rocksDB.exceptions.WrongParseException;

@Slf4j
public class ByteablePoll implements Byteable<Poll>{

    @Override
    public Poll receiveObjectFromBytes(byte[] bytes) {
        try {
            return Poll.parse(bytes);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new WrongParseException(e);
        }
    }

    @Override
    public byte[] toBytesObject(Poll value) {
        return value.toBytes();
    }
}
