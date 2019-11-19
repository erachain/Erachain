package org.erachain.dbs.rocksDB.transformation.lists;

import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.tuples.ByteableTuple4;
import org.mapdb.Fun.Tuple4;

public class ByteableStackTuple4 extends ByteableStack<Tuple4<Long, Integer, Integer, Integer>> {

    ByteableLong byteableLong = new ByteableLong();
    ByteableInteger byteableInteger = new ByteableInteger();

    public ByteableStackTuple4() {
        ByteableTuple4<Long, Integer, Integer, Integer> byteableElement = new ByteableTuple4<Long, Integer, Integer, Integer>() {
            @Override
            public int[] sizeElements() {
                return new int[]{Long.BYTES, Integer.BYTES, Integer.BYTES, Integer.BYTES};
            }
        };
        byteableElement.setByteables(new Byteable[]{byteableLong, byteableInteger, byteableInteger, byteableInteger});
        setByteableElement(byteableElement);
    }


    @Override
    public int sizeElement() {
        return Long.BYTES + Long.BYTES + Integer.BYTES + Integer.BYTES;
    }


}
