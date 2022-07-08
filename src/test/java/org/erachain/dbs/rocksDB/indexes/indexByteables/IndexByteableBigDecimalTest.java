package org.erachain.dbs.rocksDB.indexes.indexByteables;

import org.erachain.utils.ByteArrayUtils;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class IndexByteableBigDecimalTest {

    IndexByteableBigDecimal ibBigDecimal = new IndexByteableBigDecimal();

    @Test
    public void toBytes() {

        byte[] v1 = ibBigDecimal.toBytes(new BigDecimal("102.54450000"));
        byte[] v2 = ibBigDecimal.toBytes(new BigDecimal("102.64270000"));

        assertEquals(-1, ByteArrayUtils.compareUnsignedAsMask(v1, v2));

    }

}