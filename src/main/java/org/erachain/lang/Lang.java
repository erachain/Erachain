package org.erachain.lang;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.erachain.settings.Settings;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Lang {

    public static final String translationsUrl = "https://raw.githubusercontent.com/erachain/erachain-public/master/languages/";

    private static final Logger logger = LoggerFactory.getLogger(Lang.class);
    private volatile static Lang instance;
    private Map<String, String> noTranslateMap;

    private JSONObject langObj;
    private HashMap<String, LangFile> langList;

    private Lang() {
        noTranslateMap = new LinkedHashMap<String, String>();
        getLangListAvailable();
        setLangForNode();
    }

    public static Lang getInstance() {
        if (instance == null) {
            instance = new Lang();
        }
        return instance;
    }

    public JSONObject getLangJson(String iso) {
        if (iso == null) {
            return langList.get("en").getLangJson();
        }

        iso = iso.toLowerCase();
        LangFile langISO = langList.get(iso);
        if (langISO == null) {
            return langList.get("en").getLangJson();
        }

        return langISO.getLangJson();

    }

    public LangFile getLangFile(String iso) {
        return langList.get(iso);
    }

    public static JSONObject openLangFile(String filename) {

        JSONObject langJsonObject;
        try {
            File file = new File(Settings.getInstance().getLangDir(), filename);
            if (!file.isFile()) {
                langJsonObject = new JSONObject();
            } else {

                List<String> lines;
                try {
                    lines = Files.readLines(file, Charsets.UTF_8);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    lines = new ArrayList<String>();
                }

                StringBuilder jsonString = new StringBuilder();
                for (String line : lines) {
                    jsonString.append(line.replace("\t", ""));
                }

                langJsonObject = (JSONObject) JSONValue.parse(jsonString.toString());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            langJsonObject = new JSONObject();
        }

        if (langJsonObject == null) {
            langJsonObject = new JSONObject();
        }

        if (langJsonObject.isEmpty()) {
            logger.error("ERROR reading language file " + filename + ".");
        }

        return langJsonObject;
    }

    private void noTranslate(String message) {
        if (!noTranslateMap.containsKey(message)) {
            noTranslateMap.put(message, message);
        }
    }

    public Map<String, String> getNoTranslate() {
        return noTranslateMap;
    }

    public HashMap<String, LangFile> getLangListAvailable() {
        if (langList != null)
            return langList;

        langList = new HashMap<>();

        List<String> fileList = getFileListAvailable();

        for (int i = 0; i < fileList.size(); i++) {
            try {
                logger.debug("try lang file: " + fileList.get(i));
                JSONObject langJson = openLangFile(fileList.get(i));
                if (langJson.isEmpty())
                    continue;

                LangFile langFile = new LangFile(langJson);
                langList.put(langFile.getISO(), langFile);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return langList;
    }

    public List<String> getFileListAvailable() {
        List<String> langFileList = new ArrayList<>();

        File[] fileList;
        File dir = new File(Settings.getInstance().getLangDir());

        if (!dir.exists()) {
            dir.mkdir();
        }

        fileList = dir.listFiles();

        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isFile() && fileList[i].getName().endsWith(".json")
                    && !fileList[i].getName().equals("available.json")) {
                langFileList.add(fileList[i].getName());
            }
        }

        return langFileList;
    }

    public JSONObject getLangForNode() {
        return langObj;
    }

    public void setLangForNode() {
        LangFile langFile = langList.get(Settings.getInstance().getLang());
        langObj = langFile == null ? new JSONObject()
                : langFile.getLangJson();
    }

    public static String T(String message, JSONObject langObj) {
        if (message == null)
            return "NULL";

        //COMMENT AFTER # FOR TRANSLATE THAT WOULD BE THE SAME TEXT IN DIFFERENT WAYS TO TRANSLATE
        String messageWithoutComment = message.replaceFirst("(?<!\\\\)#.*$", "").trim();
        messageWithoutComment = messageWithoutComment.replace("\\#", "#");

        if (langObj == null) {
            //noTranslate(message);
            return messageWithoutComment;
        }

        if (!langObj.containsKey(message)) {
            //	noTranslate(message);
            //IF NO SUITABLE TRANSLATION WITH THE COMMENT THEN RETURN WITHOUT COMMENT
            if (!langObj.containsKey(messageWithoutComment)) {
                return messageWithoutComment.trim();
            } else {
                return langObj.get(messageWithoutComment).toString();
            }
        }
        // if "message" = isNull  - return message
        // if "massage" = "any string"  - return "any string"
        String res = langObj.get(message).toString();
        if (res.isEmpty()) return (message);
        return res;
    }

    public static String T(String message) {
        return T(message, getInstance().langObj);
    }


    public static String[] T(String[] Messages) {
        String[] translateMessages = Messages.clone();
        for (int i = 0; i < translateMessages.length; i++) {
            translateMessages[i] = T(translateMessages[i]);
        }
        return translateMessages;
    }

}