package org.erachain.dbs.rocksDB.transformation.lists;

import lombok.Setter;
import org.erachain.dbs.rocksDB.transformation.Byteable;

import java.util.Arrays;
import java.util.Stack;
@Setter
public abstract class ByteableStack<T> implements Byteable<Stack<T>> {

    public abstract int sizeElement();

    private Byteable<T> byteableElement;

    @Override
    public Stack<T> receiveObjectFromBytes(byte[] bytes) {
        Stack<T> result = new Stack<>();
        int sizeElement = sizeElement();
        int size = bytes.length / sizeElement;
        byte[] temp;
        for (int i = 0; i < size; i++) {
            temp = Arrays.copyOfRange(bytes, i * sizeElement, (i + 1) * sizeElement);
            result.push(byteableElement.receiveObjectFromBytes(temp));
        }
        return result;
    }

    @Override
    public byte[] toBytesObject(Stack<T> value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        byte[] result = new byte[0];
        for (T t : value) {
            result = org.bouncycastle.util.Arrays.concatenate(result, byteableElement.toBytesObject(t));
        }
        return result;
    }
}
