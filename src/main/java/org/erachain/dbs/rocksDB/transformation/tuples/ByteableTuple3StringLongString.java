package org.erachain.rocksDB.transformation.tuples;

import org.erachain.rocksDB.transformation.Byteable;
import org.erachain.rocksDB.transformation.ByteableLong;
import org.erachain.rocksDB.transformation.ByteableString;
import org.erachain.rocksDB.utils.ConstantsRocksDB;

public class ByteableTuple3StringLongString extends ByteableTuple3<String, Long, String> {

    public ByteableTuple3StringLongString() {
        setByteables(new Byteable[]{new ByteableString(), new ByteableLong(), new ByteableString()});
    }

    @Override
    public int[] sizeElements() {
        return new int[]{ConstantsRocksDB.SIZE_ADDRESS, Long.BYTES};
    }
}
