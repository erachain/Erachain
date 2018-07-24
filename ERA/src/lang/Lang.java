package lang;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import settings.Settings;

public class Lang {

    public static final String translationsUrl = "https://raw.githubusercontent.com/erachain/erachain-public/master/languages/";

    private static final Logger LOGGER = Logger.getLogger(Lang.class);
    private static Lang instance;
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

            List<String> lines = null;
            try {
                lines = Files.readLines(file, Charsets.UTF_8);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                lines = new ArrayList<String>();
            }

            String jsonString = "";
            for (String line : lines) {
                jsonString += line.replace("\t", "");
            }

            langJsonObject = (JSONObject) JSONValue.parse(jsonString);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            langJsonObject = new JSONObject();
        }

        if (langJsonObject == null) {
            langJsonObject = new JSONObject();
        }

        if (langJsonObject.isEmpty()) {
            LOGGER.error("ERROR reading language file " + filename + ".");
        }

        return langJsonObject;
    }

    public void loadLang() {
        LOGGER.debug("try lang file: " + Settings.getInstance().getLangFileName());
        langObj = openLangFile(Settings.getInstance().getLangFileName());
        noTranslateMap = new LinkedHashMap<String, String>();
    }

    public String[] translate(String[] Messages) {
        String[] translateMessages = Messages.clone();
        for (int i = 0; i < translateMessages.length; i++) {
            translateMessages[i] = this.translate(translateMessages[i]);
        }
        return translateMessages;
    }

    public String translate(String message) {
        //COMMENT AFTER # FOR TRANSLATE THAT WOULD BE THE SAME TEXT IN DIFFERENT WAYS TO TRANSLATE
        String messageWithoutComment = message.replaceFirst("(?<!\\\\)#.*$", "");
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
                    LOGGER.debug("try lang file: " + fileList.get(i));
                    JSONObject langFile = openLangFile(fileList.get(i));
                    if (langFile.isEmpty())
                        continue;

                    String lang_name = (String) langFile.get("_lang_name_");
                    long time_of_translation = Long.parseLong(langFile.get("_timestamp_of_translation_").toString());
                    //long time_of_translation = (long)langFile.get("_timestamp_of_translation_");
                    lngList.add(new LangFile(lang_name, fileList.get(i), time_of_translation));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        return lngList;
    }


    public List<Tuple2<String, String>> getLangListToWeb() {


        List<Tuple2<String, String>> lngList = new ArrayList<>();


        List<String> fileList = getFileListAvailable();

        lngList.add(new Tuple2<String, String>("en", new LangFile().getName()));

        for (int i = 0; i < fileList.size(); i++) {
            if (!fileList.get(i).equals("en.json") && !fileList.get(i).equals("available.json")) {
                try {
                    LOGGER.debug("try lang file: " + fileList.get(i));
                    JSONObject langFile = openLangFile(fileList.get(i));
                    if (langFile.isEmpty())
                        continue;

                    lngList.add(new Tuple2<String, String>(fileList.get(i).replaceAll(".json", ""), (String) langFile.get("_lang_name_")));

                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        return lngList;
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

    public String translate_from_langObj(String message, JSONObject langObj1) {
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
                return messageWithoutComment;
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