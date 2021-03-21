package org.erachain.dbs.rocksDB.transformation.lists;

import lombok.Setter;
import org.erachain.dbs.rocksDB.transformation.Byteable;

import java.util.ArrayList;
import java.util.List;

@Setter
public abstract class ByteableList<T> implements Byteable<List<T>> {

    public abstract int sizeElement();

    private Byteable<T> byteableElement;

    @Override
    public List<T> receiveObjectFromBytes(byte[] bytes) {
        List<T> result = new ArrayList<>();
        int sizeElement = sizeElement();
        int size = bytes.length / sizeElement;
        byte[] temp;
        for (int i = 0; i < size; i++) {
            temp = java.util.Arrays.copyOfRange(bytes, i * sizeElement, (i + 1) * sizeElement);
            result.add(byteableElement.receiveObjectFromBytes(temp));
        }
        return result;
    }

    @Override
    public byte[] toBytesObject(List<T> value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        byte[] result = new byte[0];
        for (T t : value) {
            result = org.bouncycastle.util.Arrays.concatenate(result, byteableElement.toBytesObject(t));
        }
        return result;
    }
}
