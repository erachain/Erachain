package org.erachain.network.message;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.erachain.core.BlockChain;
import org.erachain.core.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignaturesMessage extends Message {

    private static final int SIGNATURE_LENGTH = Block.SIGNATURE_LENGTH;
    private static final int DATA_LENGTH = 4;

    static Logger LOGGER = LoggerFactory.getLogger(SignaturesMessage.class.getName());
    private static boolean loggerOn = BlockChain.CHECK_BUGS > 7;

    private List<byte[]> signatures;

    public SignaturesMessage(List<byte[]> signatures) {
        super(SIGNATURES_TYPE);

        this.signatures = signatures;
    }

    public static SignaturesMessage parse(byte[] data) throws Exception {
        //READ LENGTH

        if (loggerOn) {
            LOGGER.debug("try parse LEN: " + data.length);
        }

        byte[] lengthBytes = Arrays.copyOfRange(data, 0, DATA_LENGTH);
        int length = Ints.fromByteArray(lengthBytes);

        //CHECK IF DATA MATCHES LENGTH
        if (data.length != DATA_LENGTH + (length * SIGNATURE_LENGTH)) {
            throw new Exception("Data does not match length");
        }

        //CREATE HEADERS LIST
        List<byte[]> headers = new ArrayList<byte[]>();

        for (int i = 0; i < length; i++) {
            //CALCULATE POSITION
            int position = DATA_LENGTH + (i * SIGNATURE_LENGTH);

            //READ HEADER
            byte[] header = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);

            //ADD TO LIST
            headers.add(header);
        }

        return new SignaturesMessage(headers);
    }

    public List<byte[]> getSignatures() {
        return this.signatures;
    }

    public boolean isRequest() {
        return false;
    }

    @Override
    public byte[] toBytes() {

        int listSize = this.signatures.size();
        if (loggerOn) {
            LOGGER.debug("try toBytes items: " + listSize);
        }
        byte[] data = new byte[DATA_LENGTH + SIGNATURE_LENGTH * listSize];

        //WRITE LENGTH
        byte[] lengthBytes = Ints.toByteArray(listSize);
        lengthBytes = Bytes.ensureCapacity(lengthBytes, DATA_LENGTH, 0);
        System.arraycopy(lengthBytes, 0, data, 0, DATA_LENGTH);

        int pos = DATA_LENGTH;
        //WRITE SIGNATURES
        for (byte[] header : this.signatures) {
            //WRITE SIGNATURE
            //data = Bytes.concat(data, header);
            System.arraycopy(header, 0, data, pos, SIGNATURE_LENGTH);
            pos += SIGNATURE_LENGTH;
        }

        //ADD CHECKSUM
        data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);

        return data;
    }

    @Override
    public int getDataLength() {
        return DATA_LENGTH + (this.signatures.size() * SIGNATURE_LENGTH);
    }

}
