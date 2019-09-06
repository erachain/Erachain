package org.erachain.database.keys;


import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.mapdb.Fun;

import java.util.Comparator;

public class Key12 implements Comparator<byte[]> {

    private static final long serialVersionUID = -816277286657643283L;

    final byte[] k = new byte[12];

    public Key12(byte[] bytes) {
        System.arraycopy(bytes, 0, this.k, 0, 12);
    }

    @Override
    public int compare(byte[] o1, byte[] o2) {
        if(o1==o2) return 0;
        final int len = Math.min(o1.length,o2.length);
        for(int i=0;i<len;i++){
            if(o1[i]==o2[i])
                continue;
            if(o1[i]>o2[i])
                return 1;
            return -1;
        }
        return (o1.length < o2.length) ? -1 : ((o1.length == o2.length) ? 0 : 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return compare(k, (byte[])o) == 0;

    }

    @Override
    public int hashCode() {
        int result = k != null ? Ints.fromByteArray(k) : 0;
        return result;
    }

    @Override
    public String toString() {
        return "k12[" + hashCode() + "]";
    }
}
