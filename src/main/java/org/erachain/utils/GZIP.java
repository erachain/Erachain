package org.erachain.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class GZIP {

    static Logger LOGGER = LoggerFactory.getLogger(GZIP.class.getName());

    private static byte[] GZIPcompress(String str) throws Exception {
        try (ByteArrayOutputStream obj = new ByteArrayOutputStream(); GZIPOutputStream gzip = new GZIPOutputStream(obj);) {
            gzip.write(str.getBytes(StandardCharsets.UTF_8));
            gzip.close();
            return obj.toByteArray();
        }

    }


    public static byte[] GZIPcompress(byte[] bytes) throws Exception {
        try (ByteArrayOutputStream obj = new ByteArrayOutputStream(); GZIPOutputStream gzip = new GZIPOutputStream(obj);) {
            gzip.write(bytes);
            gzip.close();
            return obj.toByteArray();
        }

    }


    private static String GZIPdecompress(byte[] bytes) throws Exception {
        String outStr = "";
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));


             BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));) {
            String line;
            while ((line = bf.readLine()) != null) {
                outStr += line + "\r\n";
            }
        }
        return outStr;
    }

    @SuppressWarnings("unchecked")
    public static byte[] GZIPdecompress_b(byte[] bytes) throws Exception {

        byte[] ss = new byte[0];
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));


        byte[] buf = new byte[1];
        while ((gis.read(buf)) != -1) {

            ss = ArrayUtils.add(ss, buf[0]);


        }
        return ss;
    }

    public static String webDecompress(String value) {
        if (value.startsWith("?gz!")) {
            value = value.substring(4, value.length());

            byte[] compressed = Base64.getDecoder().decode(value);

            try {
                value = GZIPdecompress(compressed);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return value;
    }

    public static String autoDecompress(String text) {
        if (text.startsWith("?gz!")) {
            text = text.substring(4, text.length());

            byte[] compressed = Base64.getDecoder().decode(text);
            try {
                text = GZIPdecompress(compressed);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            return text;
        } else {
            return compress(text);
        }
    }

    public static String compress(String text) {
        byte[] compressed = null;
        try {
            compressed = GZIPcompress(text);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return "?gz!" + Base64.getEncoder().encodeToString(compressed);
    }

    public static String compressOnDemand(String text) {
        byte[] compressed = null;
        try {
            compressed = GZIPcompress(text);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        String compressedVariant = "?gz!" + Base64.getEncoder().encode(compressed);

        if (compressedVariant.length() >= text.length()) {
            compressedVariant = text;
        }

        return compressedVariant;
    }

}
