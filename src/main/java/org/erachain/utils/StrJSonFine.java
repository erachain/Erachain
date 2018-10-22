package org.erachain.utils;

import com.cedarsoftware.util.io.JsonWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.Map;

public class StrJSonFine {

    public static String convert(JSONObject json) {

        return convert(json.toJSONString());
    }

    public static String convert(JSONArray json) {
        return convert(json.toJSONString());
    }

    @SuppressWarnings("rawtypes")
    public static String convert(Map map) {
        return convert(JSONValue.toJSONString(map));
    }

    public static String convert(String str) {
        return JsonWriter.formatJson(str);

    }

}
