package org.erachain.core.exdata;

import com.google.common.primitives.Ints;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.transaction.Transaction;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

public class ExData {

    private static final int DATA_TITLE_PART_LENGTH = Transaction.DATA_TITLE_PART_LENGTH; // size title message
    private static final int DATA_JSON_PART_LENGTH = Transaction.DATA_JSON_PART_LENGTH; // size JSON part
    private static final int DATA_VERSION_PART_LENGTH = Transaction.DATA_VERSION_PART_LENGTH; // size version part

    /*
     * StandardCharsets.UTF_8 JSON "TM" - template key "PR" - template params
     * "HS" - Hashes "MS" - message
     *
     * PARAMS template:TemplateCls param_keys: [id:text] hashes_Set: [name:hash]
     * mess: message title: Title file_Set: [file Name, ZIP? , file byte[]]
     *
     */
    // null option Object

    // info to byte[]
    @SuppressWarnings("unchecked")
    public static byte[] jsonFilestoByte(String title, JSONObject json,
                                         HashMap<String, Tuple2<Boolean, byte[]>> files) throws Exception {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        outStream.write("v 2.00".getBytes(StandardCharsets.UTF_8)); // only 6
        // simbols!!!
        byte[] title_Bytes = "".getBytes(StandardCharsets.UTF_8);
        if (title != null) {
            title_Bytes = title.getBytes(StandardCharsets.UTF_8);
        }

        byte[] size_Title = ByteBuffer.allocate(DATA_TITLE_PART_LENGTH).putInt(title_Bytes.length).array();

        outStream.write(size_Title);
        outStream.write(title_Bytes);

        if (json == null || json.equals(""))
            return outStream.toByteArray();

        byte[] JSON_Bytes;
        byte[] size_Json;

        if (files == null || files.isEmpty()) {
            JSON_Bytes = json.toString().getBytes(StandardCharsets.UTF_8);
            // convert int to byte
            size_Json = ByteBuffer.allocate(DATA_JSON_PART_LENGTH).putInt(JSON_Bytes.length).array();
            outStream.write(size_Json);
            outStream.write(JSON_Bytes);
            return outStream.toByteArray();
        }
        // if insert Files
        Iterator<Entry<String, Tuple2<Boolean, byte[]>>> it = files.entrySet().iterator();
        JSONObject files_Json = new JSONObject();
        int i = 0;
        ArrayList<byte[]> out_files = new ArrayList<byte[]>();
        while (it.hasNext()) {
            Entry<String, Tuple2<Boolean, byte[]>> file = it.next();
            JSONObject file_Json = new JSONObject();
            file_Json.put("FN", file.getKey()); // File_Name
            file_Json.put("ZP", file.getValue().a.toString()); // ZIP
            file_Json.put("SZ", file.getValue().b.length + ""); // Size
            files_Json.put(i + "", file_Json);
            out_files.add(i, file.getValue().b);
            i++;
        }
        json.put("F", files_Json);
        JSON_Bytes = json.toString().getBytes(StandardCharsets.UTF_8);
        // convert int to byte
        size_Json = ByteBuffer.allocate(DATA_JSON_PART_LENGTH).putInt(JSON_Bytes.length).array();
        outStream.write(size_Json);
        outStream.write(JSON_Bytes);
        for (i = 0; i < out_files.size(); i++) {
            outStream.write(out_files.get(i));
        }
        return outStream.toByteArray();

    }

    // parse data with File info
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Tuple4<String, String, JSONObject, HashMap<String, Tuple2<Boolean, byte[]>>> parse(
            int version, byte[] data, boolean onlyTitle, boolean andFiles) throws Exception {
        // Version, Title, JSON, Files

        //CHECK IF WE MATCH BLOCK LENGTH
        if (data.length < Transaction.DATA_JSON_PART_LENGTH) {
            throw new Exception("Data does not match block length " + data.length);
        }
        int position = 0;

        switch (version) {
            case 0:
                return new Tuple4("0", new String(data, StandardCharsets.UTF_8), null, null);
            case 1:
                String[] items = new String(data, StandardCharsets.UTF_8).split("\n");
                return new Tuple4(version, items[0], null, null);
            default:
                // read version
                byte[] version_Byte = Arrays.copyOfRange(data, position, Transaction.DATA_VERSION_PART_LENGTH);

                String versiondata = new String(version_Byte, StandardCharsets.UTF_8);

                position += Transaction.DATA_VERSION_PART_LENGTH;

                // read title
                byte[] titleSizeBytes = Arrays.copyOfRange(data, position, position + Transaction.DATA_TITLE_PART_LENGTH);
                int titleSize = Ints.fromByteArray(titleSizeBytes);
                position += Transaction.DATA_TITLE_PART_LENGTH;

                byte[] titleByte = Arrays.copyOfRange(data, position, position + titleSize);

                String title = new String(titleByte, StandardCharsets.UTF_8);

                if (onlyTitle) {
                    return new Tuple4(versiondata, title, null, null);
                }

                position += titleSize;
                //READ Length JSON PART
                byte[] dataSizeBytes = Arrays.copyOfRange(data, position, position + Transaction.DATA_JSON_PART_LENGTH);
                int JSONSize = Ints.fromByteArray(dataSizeBytes);

                position += Transaction.DATA_JSON_PART_LENGTH;
                //READ JSON
                byte[] arbitraryData = Arrays.copyOfRange(data, position, position + JSONSize);
                JSONObject json = (JSONObject) JSONValue.parseWithException(new String(arbitraryData, StandardCharsets.UTF_8));

                position += JSONSize;
                HashMap<String, Tuple2<Boolean, byte[]>> out_Map = new HashMap<String, Tuple2<Boolean, byte[]>>();
                JSONObject files;
                Set files_key_Set;
                //v2.0
                if (json.containsKey("&*&*%$$%_files_#$@%%%")) { //return new Tuple4(version,title,json, null);


                    files = (JSONObject) json.get("&*&*%$$%_files_#$@%%%");


                    files_key_Set = files.keySet();
                    for (int i = 0; i < files_key_Set.size(); i++) {
                        JSONObject file = (JSONObject) files.get(i + "");


                        String name = (String) file.get("File_Name"); // File_Name
                        Boolean zip = new Boolean((String) file.get("ZIP")); // ZIP
                        byte[] bb = Arrays.copyOfRange(data, position, position + new Integer((String) file.get("Size"))); //Size
                        position = position + new Integer((String) file.get("Size")); //Size
                        out_Map.put(name, new Tuple2(zip, bb));

                    }
                    return new Tuple4(versiondata, title, json, out_Map);
                }
                // v 2.1
                if (json.containsKey("F")) { // return new Tuple4(version,title,json, null);


                    files = (JSONObject) json.get("F");


                    files_key_Set = files.keySet();
                    for (int i = 0; i < files_key_Set.size(); i++) {
                        JSONObject file = (JSONObject) files.get(i + "");


                        String name = (String) file.get("FN"); // File_Name
                        Boolean zip = new Boolean((String) file.get("ZP")); // ZIP
                        byte[] bb = Arrays.copyOfRange(data, position, position + new Integer((String) file.get("SZ"))); //Size
                        position = position + new Integer((String) file.get("SZ")); //Size
                        out_Map.put(name, new Tuple2(zip, bb));

                    }


                    return new Tuple4(versiondata, title, json, out_Map);
                }
        }

        return new Tuple4("-1", null, null, null);
    }

    public static JSONObject getHashes(JSONObject json) {
        if (json.containsKey("HS")) {
            return (JSONObject) json.get("HS");
        }
        return null;
    }

    public static byte[] toByte(String title, TemplateCls template, HashMap<String, String> params_Template,
                                HashMap<String, String> hashes_Map, String message, Set<Tuple3<String, Boolean, byte[]>> files_Set)
            throws Exception {
        // messageBytes = StrJSonFine.convert(out_Map).getBytes(
        // StandardCharsets.UTF_8 );
        JSONObject out_Map = new JSONObject();
        JSONObject params_Map = new JSONObject();
        JSONObject hashes_JSON = new JSONObject();
        // add template params
        if (template != null) {
            out_Map.put("TM", template.getKey() + "");
            Iterator<Entry<String, String>> it_templ = params_Template.entrySet().iterator();
            while (it_templ.hasNext()) {
                Entry<String, String> key1 = it_templ.next();
                params_Map.put(key1.getKey(), key1.getValue());
            }
        }
        if (!params_Map.isEmpty())
            out_Map.put("PR", params_Map);
        // add hashes
        Iterator<Entry<String, String>> it_Hash = hashes_Map.entrySet().iterator();
        while (it_Hash.hasNext()) {
            Entry<String, String> hash = it_Hash.next();
            hashes_JSON.put(hash.getKey(), hash.getValue());
        }
        if (!hashes_JSON.isEmpty())
            out_Map.put("HS", hashes_JSON);

        // add Message
        if (message.length() > 0)
            out_Map.put("MS", message);

        // add files
        HashMap out_Files = new HashMap();
        HashMap<String, Tuple2<Boolean, byte[]>> filesMap = new HashMap<String, Tuple2<Boolean, byte[]>>();
        Iterator<Tuple3<String, Boolean, byte[]>> it_Filles = files_Set.iterator();
        int i = 0;
        while (it_Filles.hasNext()) {
            Tuple3<String, Boolean, byte[]> file = it_Filles.next();
            filesMap.put(file.a, new Tuple2(file.b, file.c));

        }

        return jsonFilestoByte(title, new JSONObject(out_Map), filesMap);

    }

}
