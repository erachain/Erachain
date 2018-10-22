package org.erachain.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * uses java build in deflate and inflate methods to compress objects serialized
 * to bytes
 */

public class Zip_Bytes {

    public static byte[] compress(byte[] data) throws IOException {

        if (data == null) {
            throw new IllegalArgumentException("data was null");
        }

        Deflater deflater = new Deflater();
        deflater.setInput(data);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            deflater.finish();
            byte[] buffer = new byte[1024];
            // limit while loop to max 10000 runs - to avoid infinite loops
            int maxRun = 0;
            while (!deflater.finished() && maxRun < 10000) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
                maxRun++;
                if (maxRun >= 9998) {
                    System.out.println("max run reached - stopping to avoid infinite looping");
                    break;
                }
            }

            byte[] output = outputStream.toByteArray();
            deflater.end();

            System.out.println("Original: " + data.length + " bytes, -->  Compressed: " + output.length + " bytes");
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
            byte[] buffer = new byte[1024];
            // limit while loop to max 10000 runs - to avoid infinite loops
            int maxRun = 0;
            while (!inflater.finished() && maxRun < Integer.MAX_VALUE) {
                int count = inflater.inflate(buffer);

                outputStream.write(buffer, 0, count);
                maxRun++;
                if (maxRun >= Integer.MAX_VALUE - 1) {
                    System.out.println("max run reached - stopping to avoid infinite looping");
                    break;
                }
            }

            byte[] output = outputStream.toByteArray();
            inflater.end();
            System.out.println("Original: " + data.length + " bytes --> " + "Uncompressed: " + output.length + " bytes");
            return output;
        }
    }

}
