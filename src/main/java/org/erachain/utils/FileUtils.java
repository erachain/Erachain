package org.erachain.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.util.List;

public class FileUtils {

    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        byte[] bytes = new byte[(int) length];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        is.close();
        return bytes;
    }

    public static String readCommentedText(String path) throws IOException {

        File file = new File(path);

        if (!file.exists())
            return null;

        List<String> lines;
        try {
            lines = Files.readLines(file, Charsets.UTF_8);
        } catch (IOException e) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        String jsonString = "";
        for (String line : lines) {
            if (line.trim().startsWith("//")) {
                continue;
            }
            jsonString += line;
        }

        return jsonString;
    }

    public static JSONObject readCommentedJSONObject(String path) throws IOException {
        String text = readCommentedText(path);
        if (text == null)
            return new JSONObject();

        //CREATE JSON OBJECT
        return (JSONObject) JSONValue.parse(text);
    }

    public static JSONArray readCommentedJSONArray(String path) throws IOException {
        String text = readCommentedText(path);
        if (text == null)
            return new JSONArray();

        //CREATE JSON ARRAY
        return (JSONArray) JSONValue.parse(text);
    }

    public static String readFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer();
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024 << 4];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }
}
