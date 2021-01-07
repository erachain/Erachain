package org.erachain.lang;

import org.erachain.controller.Controller;
import org.json.simple.JSONObject;

public class LangFile {

    private String name;
    private String filename;
    private String iso;
    private long timestamp;
    private JSONObject langJson;

    public LangFile() {
        this.name = "English";
        this.filename = "en.json";
        this.timestamp = Controller.buildTimestamp;
    }

    public LangFile(JSONObject langJson) {
        this.name = (String) langJson.get("_lang_name_");
        this.filename = (String) langJson.get("_file_");
        this.iso = filename.replaceAll(".json", "");
        this.timestamp = Long.parseLong(langJson.get("_timestamp_of_translation_").toString());
        this.langJson = langJson;
    }

    public String getName() {
        return this.name;
    }

    public String getISO() {
        return this.iso;
    }

    public String getFileName() {
        return this.filename;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public JSONObject getLangJson() {
        return this.langJson;
    }

    public String toString() {
        return "[" + filename.substring(0, filename.lastIndexOf('.')) + "] " + this.name;
    }
}
