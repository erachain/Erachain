package org.erachain.dbs.rocksDB.transformation.tuples;

import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableBigInteger;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;

public class ByteableTuple3LongIntegerBigInteger extends ByteableTuple3<String, Long, String> {

    public ByteableTuple3LongIntegerBigInteger() {
        setByteables(new Byteable[]{new ByteableLong(),new ByteableInteger(),new ByteableBigInteger()});
    }

    @Override
    public int[] sizeElements() {
        // TODO - wrong ? Long.BYTES !
        return new int[]{Long.BYTES,Integer.BYTES};
    }


}
