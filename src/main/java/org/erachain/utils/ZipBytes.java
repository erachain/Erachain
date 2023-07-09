package org.erachain.utils;

import org.erachain.core.BlockChain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * uses java build in deflate and inflate methods to compress objects serialized
 * to bytes
 */

public class ZipBytes {

    static int BUFF_LEN_POW = 10;
    static int TIMES_RUN_MAX = 1 << BUFF_LEN_POW;
    static int BUFF_LEN = BlockChain.MAX_BLOCK_SIZE_BYTES >> BUFF_LEN_POW;

    public static byte[] compress(byte[] data) throws IOException {

        if (data == null) {
            throw new IllegalArgumentException("data was null");
        }

        Deflater deflater = new Deflater();
        deflater.setInput(data);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            deflater.finish();
            byte[] buffer = new byte[BUFF_LEN];
            // limit while loop to max runs - to avoid infinite loops
            int maxRun = 0;
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
                maxRun++;
                if (maxRun >= TIMES_RUN_MAX) {
                    throw new RuntimeException("max run reached - stopping to avoid infinite looping");
                }
            }

            byte[] output = outputStream.toByteArray();
            deflater.end();

            //System.out.println("Original: " + data.length + " bytes, -->  Compressed: " + output.length + " bytes");
            return output;
        }
    }

    public static byte[] decompress(byte[] data) throws DataFormatException, IOException {
        if (data == null) {
            throw new IllegalArgumentException("data was null");
        }

        Inflater inflater = new Inflater();
        inflater.setInput(data);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            byte[] buffer = new byte[BUFF_LEN];
            // limit while loop to max 10000 runs - to avoid infinite loops
            int maxRun = 0;
            while (!inflater.finished() && maxRun < Integer.MAX_VALUE) {
                int count = inflater.inflate(buffer);

                outputStream.write(buffer, 0, count);
                maxRun++;
                if (maxRun >= Short.MAX_VALUE - 1) {
                    throw new RuntimeException("max run reached - stopping to avoid infinite looping");
                }
            }

            byte[] output = outputStream.toByteArray();
            inflater.end();
            //System.out.println("Original: " + data.length + " bytes --> " + "Uncompressed: " + output.length + " bytes");
            return output;
        }
    }

}
