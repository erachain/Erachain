package org.erachain.gui.library;

import java.io.ByteArrayOutputStream;
import java.util.zip.Inflater;

class MyByteArrayDecompress {

    public byte[] decompressByteArray(byte[] bytes) {

        ByteArrayOutputStream baos = null;
        Inflater iflr = new Inflater();
        iflr.setInput(bytes);
        baos = new ByteArrayOutputStream();
        byte[] tmp = new byte[4 * 1024];
        try {
            while (!iflr.finished()) {
                int size = iflr.inflate(tmp);
                baos.write(tmp, 0, size);
            }
        } catch (Exception ex) {

        } finally {
            try {
                if (baos != null) baos.close();
            } catch (Exception ex) {
            }
        }

        return baos.toByteArray();
    }

}
