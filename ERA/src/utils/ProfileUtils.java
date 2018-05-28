package utils;

import core.web.NameStorageMap;
import datachain.DCSet;
import org.json.simple.JSONObject;

public class ProfileUtils {


    public static JSONObject getBlogBlackWhiteList(String blogname) {
        JSONObject json = new JSONObject();
        NameStorageMap nameStorageMap = DCSet.getInstance().getNameStorageMap();

        if (nameStorageMap.get(blogname) != null) {
            addToJson(blogname, json, nameStorageMap, Corekeys.BLOGWHITELIST);
            addToJson(blogname, json, nameStorageMap, Corekeys.BLOGBLACKLIST);
        }


        return json;

    }

    public static JSONObject getProfile(String profilename) {
        JSONObject json = new JSONObject();
        NameStorageMap nameStorageMap = DCSet.getInstance().getNameStorageMap();

        if (nameStorageMap.get(profilename) != null) {
            addToJson(profilename, json, nameStorageMap, Corekeys.BLOGTITLE);
            addToJson(profilename, json, nameStorageMap, Corekeys.BLOGDESCRIPTION);
            addToJson(profilename, json, nameStorageMap, Corekeys.BLOGENABLE);
            addToJson(profilename, json, nameStorageMap, Corekeys.PROFILEENABLE);
            addToJson(profilename, json, nameStorageMap, Corekeys.PROFILEAVATAR);
            addToJson(profilename, json, nameStorageMap, Corekeys.PROFILEMAINGRAPHIC);
            addToJson(profilename, json, nameStorageMap, Corekeys.PROFILEFOLLOW);
            addToJson(profilename, json, nameStorageMap, Corekeys.PROFILELIKEPOSTS);
            addToJson(profilename, json, nameStorageMap, Corekeys.BLOGBLOCKCOMMENTS);
        }


        return json;

    }

    @SuppressWarnings("unchecked")
    public static void addToJson(String profilename, JSONObject json,
                                 NameStorageMap nameStorageMap, Corekeys key) {
        String value = nameStorageMap.getOpt(profilename, key.toString());
        if (value != null) {
            json.put(key.toString(), value);
        }
    }

}
