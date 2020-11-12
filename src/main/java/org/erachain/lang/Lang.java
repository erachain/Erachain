package org.erachain.lang;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.erachain.settings.Settings;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Lang {

    public static final String translationsUrl = "https://raw.githubusercontent.com/erachain/erachain-public/master/languages/";

    private static final Logger logger = LoggerFactory.getLogger(Lang.class);
    private volatile static Lang instance;
    private Map<String, String> noTranslateMap;

    private JSONObject langObj;

    private Lang() {
        loadLang();
    }

    public static Lang getInstance() {
        if (instance == null) {
            instance = new Lang();
        }
        return instance;
    }

    public static JSONObject openLangFile(String filename) {
        JSONObject langJsonObject;

        try {
            File file = new File(Settings.getInstance().getLangDir(), filename);
            if (!file.isFile()) {
                return (JSONObject) JSONValue.parse("");
            }

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

    public void loadLang() {
        logger.debug("try lang file: " + Settings.getInstance().getLangFileName());
        langObj = openLangFile(Settings.getInstance().getLangFileName());
        noTranslateMap = new LinkedHashMap<String, String>();
    }

    public String[] translate(String[] Messages) {
        String[] translateMessages = Messages.clone();
        for (int i = 0; i < translateMessages.length; i++) {
            translateMessages[i] = translate(translateMessages[i]);
        }
        return translateMessages;
    }

    public String translate(String message) {
        //COMMENT AFTER # FOR TRANSLATE THAT WOULD BE THE SAME TEXT IN DIFFERENT WAYS TO TRANSLATE
        String messageWithoutComment = message.replaceFirst("(?<!\\\\)#.*$", "").trim();
        messageWithoutComment = messageWithoutComment.replace("\\#", "#");

        if (langObj == null) {
            noTranslate(message);
            return messageWithoutComment;
        }

        if (!langObj.containsKey(message)) {
            noTranslate(message);
            //IF NO SUITABLE TRANSLATION WITH THE COMMENT THEN RETURN WITHOUT COMMENT
            if (!langObj.containsKey(messageWithoutComment)) {
                return messageWithoutComment;
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

    private void noTranslate(String message) {
        if (!noTranslateMap.containsKey(message)) {
            noTranslateMap.put(message, message);
        }
    }

    public Map<String, String> getNoTranslate() {
        return noTranslateMap;
    }

    public List<LangFile> getLangListAvailable() {
        List<LangFile> lngList = new ArrayList<>();

        List<String> fileList = getFileListAvailable();

        lngList.add(new LangFile());

        for (int i = 0; i < fileList.size(); i++) {
            if (!fileList.get(i).equals("en.json") && !fileList.get(i).equals("available.json")) {
                try {
                    logger.debug("try lang file: " + fileList.get(i));
                    JSONObject langFile = openLangFile(fileList.get(i));
                    if (langFile.isEmpty())
                        continue;

                    String lang_name = (String) langFile.get("_lang_name_");
                    long time_of_translation = Long.parseLong(langFile.get("_timestamp_of_translation_").toString());
                    lngList.add(new LangFile(lang_name, fileList.get(i), time_of_translation));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        return lngList;
    }


    public List<Tuple2<String, String>> getLangListToWeb() {
        List<Tuple2<String, String>> result = new ArrayList<>();
        List<String> fileList = getFileListAvailable();
        result.add(new Tuple2<>("en", new LangFile().getName()));
        for (String fileName : fileList) {
            if (!fileName.equals("en.json") && !fileName.equals("available.json")) {
                try {
                    logger.debug("try lang file: " + fileName);
                    JSONObject langFile = openLangFile(fileName);
                    if (langFile.isEmpty()) {
                        continue;
                    }
                    result.add(new Tuple2<>(fileName.replaceAll(".json", ""),
                            (String) langFile.get("_lang_name_")));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return result;
    }


    public List<String> getFileListAvailable() {
        List<String> lngFileList = new ArrayList<>();

        File[] fileList;
        File dir = new File(Settings.getInstance().getLangDir());

        if (!dir.exists()) {
            dir.mkdir();
        }

        fileList = dir.listFiles();

        lngFileList.add("en.json");

        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isFile() && fileList[i].getName().endsWith(".json")) {
                lngFileList.add(fileList[i].getName());
            }
        }

        return lngFileList;
    }

    public String translateFromLangObj(String message, JSONObject langObj1) {
        //COMMENT AFTER # FOR TRANSLATE THAT WOULD BE THE SAME TEXT IN DIFFERENT WAYS TO TRANSLATE
        String messageWithoutComment = message.replaceFirst("(?<!\\\\)#.*$", "");
        messageWithoutComment = messageWithoutComment.replace("\\#", "#");

        if (langObj1 == null) {
            //noTranslate(message);
            return messageWithoutComment;
        }

        if (!langObj1.containsKey(message)) {
            //	noTranslate(message);
            //IF NO SUITABLE TRANSLATION WITH THE COMMENT THEN RETURN WITHOUT COMMENT
            if (!langObj1.containsKey(messageWithoutComment)) {
                return messageWithoutComment.trim();
            } else {
                return langObj1.get(messageWithoutComment).toString();
            }
        }
        // if "message" = isNull  - return message
        // if "massage" = "any string"  - return "any string"
        String res = langObj1.get(message).toString();
        if (res.isEmpty()) return (message);
        return res;
    }

}