package org.erachain.utils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class ByteArrayUtils {

    public static boolean contains(List<byte[]> arrays, byte[] other) {
        for (byte[] b : arrays)
            if (Arrays.equals(b, other))
                return true;
        return false;
    }

    public static void remove(List<byte[]> list, byte[] toremove) {
        byte[] result = null;
        for (byte[] bs : list) {
            if (Arrays.equals(bs, toremove)) {
                result = bs;
                break;
            }
        }

        list.remove(result);
    }

    public static int indexOf(List<byte[]> list, byte[] bytearray) {
        int i = -1;
        for (int j = 0; j < list.size(); j++) {
            if (Arrays.equals(list.get(j), bytearray)) {
                i = j;
                break;
            }
        }

        return i;
    }

    public static byte[] long2ByteArray(long value) {
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    public static byte[] float2ByteArray(float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public static float ByteArray2float(byte[] array) {
        return ByteBuffer.wrap(array).getFloat();
    }

    /**
     * If Mask equal to first bytes of Array
     *
     * @param a
     * @param mask
     * @return
     */
    public static boolean areEqualMask(
            byte[] a,
            byte[] mask) {
        if (a == mask) {
            return true;
        }

        if (a == null || mask == null) {
            return false;
        }

        if (a.length < mask.length) {
            return false;
        }

        for (int i = 0; i != mask.length; i++) {
            if (a[i] != mask[i]) {
                return false;
            }
        }

        return true;
    }

}
