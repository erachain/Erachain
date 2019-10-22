package org.erachain.dbs.rocksDB.transformation.lists;

import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.tuples.ByteableTuple4;
import org.mapdb.Fun.Tuple4;

public class ByteableStackTuple4 extends ByteableStack<Tuple4<Long, Long, Integer, Integer>> {


    public ByteableStackTuple4() {
        ByteableTuple4<Long, Long, Integer, Integer> byteableElement = new ByteableTuple4<Long, Long, Integer, Integer>() {
            @Override
            public int[] sizeElements() {
                return new int[]{Long.BYTES, Long.BYTES, Integer.BYTES};
            }
        };
        ByteableLong byteableLong = new ByteableLong();
        ByteableInteger byteableInteger = new ByteableInteger();
        byteableElement.setByteables(new Byteable[]{byteableLong, byteableLong, byteableInteger, byteableInteger});
        setByteableElement(byteableElement);
    }


    @Override
    public int sizeElement() {
        return Long.BYTES + Long.BYTES + Integer.BYTES + Integer.BYTES;
    }


}
