package org.erachain.network.message;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.mapdb.Fun.Tuple2;

import java.util.Arrays;

public class HWeightMessage extends Message {

    private static final int HEIGHT_LENGTH = 4;
    private static final int WEIGHT_LENGTH = 8;
    private static final int BASE_LENGTH = HEIGHT_LENGTH + WEIGHT_LENGTH;

    private int height;
    private long weight;

    public HWeightMessage(int height, long weight) {
        super(HWEIGHT_TYPE);

        this.height = height;
        this.weight = weight;
    }

    public HWeightMessage(Tuple2<Integer, Long> value) {
        this(value.a, value.b);
    }

    public static Message parse(byte[] data) throws Exception {

        //CHECK IF DATA MATCHES LENGTH
        if (data.length != BASE_LENGTH) {
            throw new Exception("Data does not match length");
        }

        //READ HEIGHT
        int height = Ints.fromByteArray(data);
        byte[] endData = Arrays.copyOfRange(data, HEIGHT_LENGTH, data.length);
        long weight = Longs.fromByteArray(endData);

        return new HWeightMessage(height, weight);
    }

    public Tuple2<Integer, Long> getHWeight() {
        return new Tuple2<Integer, Long>(this.height, this.weight);
    }

    public boolean isRequest() {
        return false;
    }

    @Override
    public boolean quickSend() {
        return this.id > 0;
    }

    @Override
    public byte[] toBytes() {
        byte[] data = new byte[0];

        //WRITE HEIGHT
        byte[] heightBytes = Ints.toByteArray(this.height);
        heightBytes = Bytes.ensureCapacity(heightBytes, HEIGHT_LENGTH, 0);
        data = Bytes.concat(data, heightBytes);

        //WRITE WEIGHT
        byte[] weightBytes = Longs.toByteArray(this.weight);
        weightBytes = Bytes.ensureCapacity(weightBytes, WEIGHT_LENGTH, 0);
        data = Bytes.concat(data, weightBytes);

        //ADD CHECKSUM
        data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);

        return data;
    }

    @Override
    public int getDataLength() {
        return BASE_LENGTH;
    }

}
