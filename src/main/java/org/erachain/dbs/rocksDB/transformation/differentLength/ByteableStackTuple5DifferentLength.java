package org.erachain.dbs.rocksDB.transformation.differentLength;

import org.apache.flink.api.java.tuple.Tuple5;
import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class ByteableStackTuple5DifferentLength implements Byteable<Stack<Tuple5<Long, Long, Integer, Integer, byte[]>>> {
    private final ByteableLong byteableLong = new ByteableLong();
    private final ByteableInteger byteableInteger = new ByteableInteger();
    private int i;

    @Override
    public Stack<Tuple5<Long, Long, Integer, Integer, byte[]>> receiveObjectFromBytes(byte[] bytes) {
        Stack<Tuple5<Long, Long, Integer, Integer, byte[]>> result = new Stack<>();
        i = 0;
        while (bytes.length > i) {
            Long f0 = receiveLong(bytes);
            Long f1 = receiveLong(bytes);
            Integer f2 = receiveInteger(bytes);
            Integer f3 = receiveInteger(bytes);
            byte[] data = receiveByteDifferentLength(bytes);
            result.push(Tuple5.of(f0, f1, f2, f3, data));
        }
        return result;
    }

    private byte[] receiveByteDifferentLength(byte[] bytes) {
        Integer lengthByteArray = receiveInteger(bytes);
        byte[] temp = Arrays.copyOfRange(bytes, i, i + lengthByteArray);
        i += lengthByteArray;
        return temp;
    }

    private Long receiveLong(byte[] bytes) {
        byte[] temp = Arrays.copyOfRange(bytes, i, i + Long.BYTES);
        i += Long.BYTES;
        return byteableLong.receiveObjectFromBytes(temp);
    }

    private Integer receiveInteger(byte[] bytes) {
        byte[] temp = Arrays.copyOfRange(bytes, i, i + Integer.BYTES);
        i += Integer.BYTES;
        return byteableInteger.receiveObjectFromBytes(temp);
    }


    @Override
    public byte[] toBytesObject(Stack<Tuple5<Long, Long, Integer, Integer, byte[]>> value) {
        List<byte[]> tempResult = new ArrayList<>();
        for (Tuple5<Long, Long, Integer, Integer, byte[]> tuple5 : value) {
            tempResult.add(byteableLong.toBytesObject(tuple5.f0));
            tempResult.add(byteableLong.toBytesObject(tuple5.f1));
            tempResult.add(byteableInteger.toBytesObject(tuple5.f2));
            tempResult.add(byteableInteger.toBytesObject(tuple5.f3));
            byte[] data = tuple5.f4;
            tempResult.add(byteableInteger.toBytesObject(data.length));
            tempResult.add(data);
        }
        return tempResult.stream().reduce(org.bouncycastle.util.Arrays::concatenate).get();
    }
}
