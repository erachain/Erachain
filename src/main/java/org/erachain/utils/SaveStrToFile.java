package org.erachain.utils;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SaveStrToFile {

    public static void save(File file, String str) throws IOException {
        FileUtils.writeStringToFile(file, str, StandardCharsets.UTF_8, false);
    }

    public static void save(String path, String str) throws IOException {
        save(new File(path), str);
    }

    public static void saveJsonFine(String path, JSONObject json) throws IOException {
        save(path, StrJSonFine.convert(json));
    }

    public static void saveJsonFine_not_Convert(String path, String json) throws IOException {
        save(path, json);
    }
}
