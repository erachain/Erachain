package org.erachain.network.message;

import com.google.common.primitives.Bytes;
import org.erachain.core.block.Block;

import java.util.Arrays;

public class GetBlockMessage extends Message {

    private static final int GET_BLOCK_LENGTH = Block.SIGNATURE_LENGTH;
    private byte[] signature;

    public GetBlockMessage(byte[] signature) {
        super(GET_BLOCK_TYPE);

        this.signature = signature;
    }

    public static GetBlockMessage parse(byte[] data) throws Exception {
        //CHECK IF DATA MATCHES LENGTH
        if (data.length != GET_BLOCK_LENGTH) {
            throw new Exception("Data does not match length");
        }

        return new GetBlockMessage(data);
    }

    public byte[] getSignature() {
        return this.signature;
    }

    public boolean isRequest() {
        return true;
    }

    public int getSOT() {
        return 60000;
    }


    @Override
    public byte[] toBytes() {
        byte[] data = new byte[0];

        //WRITE SIGNATURE
        data = Bytes.concat(data, this.signature);

        //ADD CHECKSUM
        data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);

        return data;
    }

    protected byte[] generateChecksum(byte[] data) {
        this.loadBytes = data;
        byte[] checksum = signature;
        checksum = Arrays.copyOfRange(checksum, 0, CHECKSUM_LENGTH);
        return checksum;
    }

    @Override
    public int getDataLength() {
        return GET_BLOCK_LENGTH;
    }

}
