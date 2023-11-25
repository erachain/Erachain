package org.erachain.network.message;

import com.google.common.primitives.Bytes;
import org.erachain.core.block.Block;

import java.util.Arrays;


public class GetSignaturesMessage extends Message {

    private static final int GET_HEADERS_LENGTH = Block.SIGNATURE_LENGTH;
    private byte[] parent;

    public GetSignaturesMessage(byte[] parent) {
        super(GET_SIGNATURES_TYPE);

        this.parent = parent;
    }

    public static Message parse(byte[] data) throws Exception {
        //CHECK IF DATA MATCHES LENGTH
        if (data.length != GET_HEADERS_LENGTH) {
            throw new Exception("Data does not match length");
        }

        return new GetSignaturesMessage(data);
    }

    public byte[] getParent() {
        return this.parent;
    }

    public boolean isRequest() {
        return true;
    }

    @Override
    public byte[] toBytes() {
        byte[] data = new byte[0];

        //WRITE PARENT
        data = Bytes.concat(data, this.parent);

        //ADD CHECKSUM
        data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);

        return data;
    }

    protected byte[] generateChecksum(byte[] data) {
        this.loadBytes = data;
        byte[] checksum = parent;
        checksum = Arrays.copyOfRange(checksum, 0, CHECKSUM_LENGTH);
        return checksum;
    }

    @Override
    public int getDataLength() {
        return GET_HEADERS_LENGTH;
    }

}
