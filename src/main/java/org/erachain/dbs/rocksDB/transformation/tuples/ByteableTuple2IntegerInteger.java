package org.erachain.dbs.rocksDB.transformation.tuples;

import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;

public class ByteableTuple2IntegerInteger extends ByteableTuple2<Integer, Integer>{
    public ByteableTuple2IntegerInteger() {
        setByteables(new Byteable[]{new ByteableInteger(), new ByteableInteger()});
    }

    @Override
    public int sizeElements() {
        return Integer.BYTES;
    }






}
