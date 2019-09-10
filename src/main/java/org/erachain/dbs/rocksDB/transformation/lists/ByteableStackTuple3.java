package org.erachain.dbs.rocksDB.transformation.lists;

import org.apache.flink.api.java.tuple.Tuple3;
import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.erachain.dbs.rocksDB.transformation.tuples.ByteableTuple3;

public class ByteableStackTuple3 extends ByteableStack<Tuple3<Integer, Integer, Integer>> {

    public ByteableStackTuple3() {
        ByteableTuple3<Integer, Integer, Integer> byteableElement = new ByteableTuple3<Integer, Integer, Integer>() {
            @Override
            public int[] sizeElements() {
                return new int[]{Integer.BYTES,Integer.BYTES};
            }
        };
        ByteableInteger byteableInteger = new ByteableInteger();
        byteableElement.setByteables(new Byteable[]{byteableInteger,byteableInteger,byteableInteger});
        setByteableElement(byteableElement);
    }

    @Override
    public int sizeElement() {
        return 3*Integer.BYTES;
    }



}
