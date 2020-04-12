package org.erachain.core.exdata;

import com.google.common.primitives.Ints;
import org.erachain.core.blockexplorer.BlockExplorer;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
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

    /**
     * Title - не может быть равен нулю
     *
     * @param version
     * @param data
     * @param onlyTitle
     * @param andFiles
     * @return
     * @throws Exception
     */
    // parse data with File info
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Tuple4<String, String, JSONObject, HashMap<String, Tuple3<byte[], Boolean, byte[]>>> parse(
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
                byte[] jsonData = Arrays.copyOfRange(data, position, position + JSONSize);
                position += JSONSize;
                try {
                    JSONObject json = (JSONObject) JSONValue.parseWithException(new String(jsonData, StandardCharsets.UTF_8));

                    if (andFiles) {
                        JSONObject files;
                        Set files_key_Set;

                        if (json.containsKey("F")) {
                            // v 2.1

                            files = (JSONObject) json.get("F");
                            HashMap<String, Tuple3<byte[], Boolean, byte[]>> filesMap = new HashMap<String, Tuple3<byte[], Boolean, byte[]>>();

                            files_key_Set = files.keySet();
                            for (int i = 0; i < files_key_Set.size(); i++) {
                                JSONObject file = (JSONObject) files.get(i + "");

                                String name = (String) file.get("FN"); // File_Name
                                Boolean zip = new Boolean((String) file.get("ZP")); // ZIP
                                int size = new Integer((String) file.get("SZ"));
                                byte[] bb = Arrays.copyOfRange(data, position, position + size);
                                position = position + size;
                                filesMap.put(name, new Tuple3(Crypto.getInstance().digest(bb), zip, bb));

                            }

                            return new Tuple4(versiondata, title, json, filesMap);

                        } else if (json.containsKey("&*&*%$$%_files_#$@%%%")) {
                            //v2.0

                            files = (JSONObject) json.get("&*&*%$$%_files_#$@%%%");
                            HashMap<String, Tuple3<byte[], Boolean, byte[]>> filesMap = new HashMap<String, Tuple3<byte[], Boolean, byte[]>>();

                            files_key_Set = files.keySet();
                            for (int i = 0; i < files_key_Set.size(); i++) {
                                JSONObject file = (JSONObject) files.get(i + "");


                                String name = (String) file.get("File_Name"); // File_Name
                                Boolean zip = new Boolean((String) file.get("ZIP")); // ZIP
                                int size = new Integer((String) file.get("Size"));
                                byte[] bb = Arrays.copyOfRange(data, position, position + size);
                                position = position + size;
                                filesMap.put(name, new Tuple3(Crypto.getInstance().digest(bb), zip, bb));

                            }
                            return new Tuple4(versiondata, title, json, filesMap);
                        }
                    }

                    return new Tuple4(versiondata, title, json, null);
                } catch (Exception e) {
                    return new Tuple4(versiondata, title, null, null);
                }

        }
    }

    public static JSONObject getHashes(Tuple4<String, String, JSONObject, HashMap<String, Tuple3<byte[], Boolean, byte[]>>> parsedData) {

        if (parsedData == null)
            return null;

        if (parsedData.c.containsKey("HS")) {
            return (JSONObject) parsedData.c.get("HS");
        }
        if (parsedData.c.containsKey("Hashes")) {
            return (JSONObject) parsedData.c.get("Hashes");
        }
        return null;
    }

    public static byte[][] getAllHashesAsBytes(Tuple4<String, String, JSONObject, HashMap<String, Tuple3<byte[], Boolean, byte[]>>> parsedData) {

        JSONObject hashesJson = getHashes(parsedData);

        int count = 0;
        if (hashesJson != null) {
            count = hashesJson.size();
        }

        if (parsedData.d != null && parsedData.d.isEmpty()) {
            count += parsedData.d.size();
        }

        if (count == 0)
            return null;

        byte[][] hashes = new byte[count][];

        count = 0;
        // ADD native hashes
        if (hashesJson != null) {
            for (Object hash : hashesJson.keySet()) {
                hashes[count++] = Base58.decode(hash.toString());
            }
        }

        // ADD hashes of files
        if (parsedData.d != null && parsedData.d.isEmpty()) {
            for (Tuple3<byte[], Boolean, byte[]> fileItem : parsedData.d.values()) {
                hashes[count++] = fileItem.a;
            }
        }

        return hashes;
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

    public static String templateWithValues(String description, JSONObject params) {
        Set<String> kS = params.keySet();
        for (String s : kS) {
            description = description.replace("{{" + s + "}}", (CharSequence) params.get(s));
        }

        return description;
    }

    /**
     * Version 2 maker for BlockExplorer
     */
    public static void makeJSONforHTML(DCSet dcSet, Map output, Tuple4<String, String, JSONObject, HashMap<String, Tuple3<byte[], Boolean, byte[]>>> noteData,
                                       int blockNo, int seqNo, JSONObject langObj) {

        if (noteData.b != null) {
            output.put("title", noteData.b);
        }

        JSONObject dataJson = noteData.c;
        // parse JSON
        if (dataJson != null) {

            if (dataJson.containsKey("MS")) {
                // v 2.1
                output.put("message", dataJson.get("MS"));

            } else if (dataJson.containsKey("Message")) {
                // Message v2.0
                output.put("message", dataJson.get("Message"));

            }

            Long templateKey = null;
            String paramsStr = null;

            if (dataJson.containsKey("TM")) {
                // V2.1 Template
                templateKey = new Long(dataJson.get("TM").toString());

                // Template Params
                if (dataJson.containsKey("PR")) {
                    paramsStr = dataJson.get("PR").toString();
                }
            } else if (dataJson.containsKey("Template")) {
                // V2.0 Template
                templateKey = new Long(dataJson.get("Template").toString());

                // Template Params
                if (dataJson.containsKey("Statement_Params")) {
                    paramsStr = dataJson.get("Statement_Params").toString();
                }
            }

            if (templateKey != null) {
                TemplateCls template = (TemplateCls) ItemCls.getItem(dcSet, ItemCls.TEMPLATE_TYPE, templateKey);
                if (template != null) {
                    String description = template.viewDescription();

                    // Template Params
                    if (paramsStr != null) {
                        JSONObject params;
                        try {
                            params = (JSONObject) JSONValue.parseWithException(paramsStr);
                            description = templateWithValues(description, params);
                        } catch (ParseException e) {
                        }

                    }

                    output.put("body", description);

                }
            }

            ///////// NATIVE HASHES
            JSONObject hashes = getHashes(noteData);
            if (hashes == null) {
                hashes = new JSONObject();
            }

            /////////// FILES
            HashMap<String, Tuple3<byte[], Boolean, byte[]>> filesMap = noteData.d;

            if (filesMap != null && !filesMap.isEmpty()) {
                String files = "";
                Set<String> fileNames = filesMap.keySet();

                int filesCount = 1;
                for (String fileName : fileNames) {

                    Tuple3<byte[], Boolean, byte[]> fileValue = filesMap.get(fileName);
                    String hash = Base58.encode(fileValue.a);
                    hashes.put(hash, fileName);

                    files += filesCount + " " + fileName
                            + " <a href=?q=" + hash + BlockExplorer.get_Lang(langObj) + "&search=transactions>[" + hash + "]</a> ";
                    files += "<a href ='../apidocuments/getFile?download=true&block="
                            + blockNo + "&seqNo=" + seqNo + "&name=" + fileName + "'> "
                            + Lang.getInstance().translateFromLangObj("Download", langObj) + "</a><br>";

                    filesCount++;

                }

                output.put("files", files);
            }

            ///////// HASHES
            if (!hashes.isEmpty()) {
                String hashesHTML = "";
                int hashesCount = 1;

                for (Object hash : hashes.keySet()) {
                    hashesHTML += hashesCount
                            + " <a href=?q=" + hash + BlockExplorer.get_Lang(langObj) + "&search=transactions>" + hash + "</a> "
                            + hashes.get(hash) + "<br>";
                    hashesCount++;
                }

                output.put("hashes", hashesHTML);
            }

        }

    }

    /**
     * Version 1 maker for BlockExplorer
     */
    public static void makeJSONforHTML_1(DCSet dcSet, Map output, JSONObject jsonData, Long templateKey) {

        Set<String> kS;
        String description;
        String paramsStr;
        JSONObject params;
        TemplateCls template = (TemplateCls) ItemCls.getItem(dcSet, ItemCls.TEMPLATE_TYPE, templateKey);
        if (template != null) {
            description = template.viewDescription();

            output.put("title", jsonData.get("Title"));

            output.put("message", jsonData.get("Message"));

            paramsStr = jsonData.get("Statement_Params").toString();

            try {
                params = (JSONObject) JSONValue.parseWithException(paramsStr);
                description = templateWithValues(description, params);
            } catch (Exception e) {
            }

            output.put("body", description);

        }

        try {
            String hashes = "";
            paramsStr = jsonData.get("Hashes").toString();
            params = (JSONObject) JSONValue.parseWithException(paramsStr);
            kS = params.keySet();

            int i = 1;
            for (String s : kS) {
                hashes += i + " " + s + " " + params.get(s) + "<br>";
            }
            output.put("hashes", hashes);
        } catch (Exception e) {
        }

    }
}
