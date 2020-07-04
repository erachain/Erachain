package org.erachain.core.exdata;

import com.google.common.primitives.Ints;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.blockexplorer.BlockExplorer;
import org.erachain.core.crypto.AEScrypto;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.ZipBytes;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.DataFormatException;

/**
 * StandardCharsets.UTF_8 JSON "TM" - template key "PR" - template params
 * "HS" - Hashes "MS" - message
 * <p>
 * PARAMS template:TemplateCls param_keys: [id:text] hashes_Set: [name:hash]
 * mess: message title: Title file_Set: [file Name, ZIP? , file byte[]]
 */

public class ExData {

    private static final int DATA_TITLE_PART_LENGTH = Transaction.DATA_TITLE_PART_LENGTH; // size title message
    private static final int DATA_JSON_PART_LENGTH = Transaction.DATA_JSON_PART_LENGTH; // size JSON part
    private static final int DATA_VERSION_PART_LENGTH = Transaction.DATA_VERSION_PART_LENGTH; // size version part

    private static final int RECIPIENTS_SIZE_LENGTH = 3; // size version part
    private static final int SECRET_LENGTH = Crypto.HASH_LENGTH; // size version part

    private static final byte RECIPIENTS_FLAG_MASK = 64;
    private static final byte ENCRYPT_FLAG_MASK = 32;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExData.class);

    /**
     * 0 - version; 1 - flag 1;
     */
    private byte[] flags;
    private String title;
    private JSONObject json;

    private String message;
    private JSONObject hashes;

    /**
     * Name: hash, is ZIP?, file data
     */
    private HashMap<String, Tuple3<byte[], Boolean, byte[]>> files;


    private byte recipientsFlags;
    private Account[] recipients;

    private long templateKey;
    private TemplateCls template;
    private String valuedText;

    private byte secretsFlags;
    private byte[][] secrets;
    private byte[] encryptedData;
    private byte[] decryptedData;

    /**
     * OLD version 1-2
     *
     * @param version
     * @param title
     * @param json
     * @param files
     */
    public ExData(int version, String title,
                  JSONObject json, HashMap<String, Tuple3<byte[], Boolean, byte[]>> files) {
        this.flags = new byte[]{(byte) version, 0, 0, 0};
        this.title = title;
        this.json = json;
        this.files = files;

    }

    /**
     * Version 3
     *
     * @param flags
     * @param title
     * @param recipients
     * @param json
     * @param files
     */
    public ExData(byte[] flags, String title,
                  byte recipientsFlags, Account[] recipients,
                  JSONObject json, HashMap<String, Tuple3<byte[], Boolean, byte[]>> files
    ) {
        this.flags = flags;
        this.title = title;
        this.recipientsFlags = recipientsFlags;
        this.recipients = recipients;
        this.json = json;
        this.files = files;

    }

    /**
     * version 3 encrypted
     *
     * @param flags
     * @param title
     * @param recipients
     * @param encryptedData
     */
    public ExData(byte[] flags, String title,
                  byte recipientsFlags, Account[] recipients,
                  byte secretsFlags, byte[][] secrets,
                  byte[] encryptedData) {
        this.flags = flags;
        this.title = title;

        this.recipientsFlags = recipientsFlags;
        this.recipients = recipients;

        this.secretsFlags = secretsFlags;
        this.secrets = secrets;
        this.encryptedData = encryptedData;

    }

    /**
     * for set up all values from JSON etc.
     */
    public void resolveValues(DCSet dcSet) {

        String str = "";
        JSONObject params;
        Set<String> kS;
        if (json == null || json.isEmpty())
            return;

        try {
            // v 2.1
            if (json.containsKey("TM")) {

                templateKey = new Long((String) json.get("TM"));
                if (dcSet != null) {
                    template = (TemplateCls) ItemCls.getItem(DCSet.getInstance(), ItemCls.TEMPLATE_TYPE, templateKey);
                }
                if (template != null) {
                    valuedText = template.viewDescription();

                    if (json.containsKey("PR")) {
                        str = json.get("PR").toString();

                        params = (JSONObject) JSONValue.parseWithException(str);

                        kS = params.keySet();
                        for (String s : kS) {
                            valuedText = valuedText.replace("{{" + s + "}}", (CharSequence) params.get(s));
                        }
                    }
                }
            } else
                // v2.0
                if (json.containsKey("Template")) {

                    templateKey = new Long((String) json.get("Template"));
                    if (dcSet != null) {
                        template = (TemplateCls) ItemCls.getItem(dcSet, ItemCls.TEMPLATE_TYPE, templateKey);
                    }
                    if (template != null) {
                        valuedText = template.viewDescription();

                        if (json.containsKey("Statement_Params")) {
                            str = json.get("Statement_Params").toString();

                            params = (JSONObject) JSONValue.parseWithException(str);

                            kS = params.keySet();
                            for (String s : kS) {
                                valuedText = valuedText.replace("{{" + s + "}}", (CharSequence) params.get(s));
                            }
                        }
                    }
                }

            // hashes

            // 2.1
            if (json.containsKey("HS")) {
                hashes = (JSONObject) json.get("HS");
            } else
                // v2.0
                if (json.containsKey("Hashes")) {
                    hashes = (JSONObject) json.get("Hashes");
                }

            // v 2.1
            if (json.containsKey("MS"))
                message = (String) json.get("MS");
            else
                // v 2.0
                if (json.containsKey("Message"))
                    message = (String) json.get("Message");

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public boolean isParsedWithFiles() {
        return files != null;
    }

    public String getTitle() {
        return title;
    }

    public TemplateCls getTemplate() {
        return template;
    }

    public long getTemplateKey() {
        return templateKey;
    }

    public String getValuedText() {
        return valuedText;
    }

    public Account[] getRecipients() {
        return recipients == null ? new Account[0] : recipients;
    }

    public byte[][] getSecrets() {
        return secrets == null ? new byte[0][] : secrets;
    }

    public JSONObject getJsonObject() {
        return json;
    }

    public HashMap<String, Tuple3<byte[], Boolean, byte[]>> getFiles() {
        return files;
    }

    public boolean hasRecipients() {
        return recipients != null && recipients.length > 0;
    }

    public boolean hasFiles() {
        return files != null && !files.isEmpty();
    }

    public boolean hasPublicText() {
        if (Transaction.hasPublicText(title, null, false, false)
                || message != null && !message.isEmpty()
                || templateKey > 0
                || files != null && !files.isEmpty())
            return true;

        return false;
    }

    public boolean hasHashes() {
        return hashes != null && !hashes.isEmpty();
    }

    public JSONObject getHashes() {
        return hashes;
    }

    public String getMessage() {
        return message;
    }

    public boolean isEncrypted() {
        return (flags[1] & ENCRYPT_FLAG_MASK) > 0;
    }

    // info to byte[]
    @SuppressWarnings("unchecked")

    public static byte[] toByteJsonAndFiles(ByteArrayOutputStream outStream, JSONObject json,
                                            HashMap<String, Tuple3<byte[], Boolean, byte[]>> files) throws Exception {

        if (json == null || json.isEmpty())
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
        Iterator<Entry<String, Tuple3<byte[], Boolean, byte[]>>> it = files.entrySet().iterator();
        JSONObject files_Json = new JSONObject();
        int i = 0;
        ArrayList<byte[]> out_files = new ArrayList<byte[]>();
        while (it.hasNext()) {
            Entry<String, Tuple3<byte[], Boolean, byte[]>> file = it.next();
            JSONObject file_Json = new JSONObject();
            file_Json.put("FN", file.getKey()); // File_Name
            file_Json.put("ZP", file.getValue().b.toString()); // ZIP
            file_Json.put("SZ", file.getValue().c.length + ""); // Size
            files_Json.put(i + "", file_Json);
            out_files.add(i, file.getValue().c);
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

    public byte[] toByte() throws Exception {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        if (false) {
            outStream.write("v 2.00".getBytes(StandardCharsets.UTF_8)); // only 6
        }

        if (flags[0] > 2) {
            outStream.write(flags);
        }

        if (title != null && !title.isEmpty()) {
            byte[] title_Bytes = title.getBytes(StandardCharsets.UTF_8);
            if (flags[0] > 2) {
                outStream.write((byte) title_Bytes.length);
            } else {
                byte[] size_Title = ByteBuffer.allocate(DATA_TITLE_PART_LENGTH).putInt(title_Bytes.length).array();
                outStream.write(size_Title);
            }
            outStream.write(title_Bytes);
        } else {
            if (flags[0] > 2) {
                outStream.write((byte) 0);
            } else {
                outStream.write(new byte[DATA_TITLE_PART_LENGTH]);
            }
        }

        if ((flags[1] & RECIPIENTS_FLAG_MASK) > 0) {
            byte[] recipientsSize = Ints.toByteArray(recipients.length);
            recipientsSize[0] = recipientsFlags;
            outStream.write(recipientsSize);

            for (int i = 0; i < recipients.length; i++) {
                outStream.write(recipients[i].getShortAddressBytes());
            }
        }

        // IF JSON and FILES ENCRYPTED?
        if ((flags[1] & ENCRYPT_FLAG_MASK) > 0) {
            // SECRETS
            outStream.write(secretsFlags);
            for (int i = 0; i < secrets.length; i++) {
                outStream.write((byte) secrets[i].length);
                outStream.write(secrets[i]);
            }

            byte[] encryptedDataSize = ByteBuffer.allocate(DATA_JSON_PART_LENGTH).putInt(encryptedData.length).array();
            outStream.write(encryptedDataSize);
            outStream.write(encryptedData);

            return outStream.toByteArray();

        } else {

            return toByteJsonAndFiles(outStream, json, files);
        }

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
    public static ExData parse(
            int version, byte[] data, boolean onlyTitle, boolean andFiles) throws Exception {
        // Version, Title, JSON, Files

        //CHECK IF WE MATCH BLOCK LENGTH
        if (data.length < Transaction.DATA_JSON_PART_LENGTH) {
            throw new Exception("Data does not match block length " + data.length);
        }
        int position = 0;

        switch (version) {
            case 0:
                String text = new String(data, StandardCharsets.UTF_8);
                String[] items = text.split("\n");
                JSONObject dataJson = new JSONObject();
                dataJson.put("Message", text.substring(items[0].length()));
                return new ExData(0, items[0], dataJson, null);

            /// return new ExData(0, new String(data, StandardCharsets.UTF_8), null, null);
            case 1:
                text = new String(data, StandardCharsets.UTF_8);
                dataJson = (JSONObject) JSONValue.parseWithException(text);
                String title = dataJson.get("Title").toString();
                return new ExData(1, title, dataJson, null);

            //String[] items = new String(data, StandardCharsets.UTF_8).split("\n");
            //return new ExData(1, items[0], null, null);
            default:

                byte[] flags;
                String versiondata;
                int titleSize;
                byte recipientsFlags;
                Account[] recipients;
                boolean isEncrypted;
                byte secretsFlags;
                byte[][] secrets;
                int jsonDataPos;
                int filesDataPos;

                // версия тут нафиг не нужна в строковом виде
                if (version == 2) {

                    flags = null;
                    recipientsFlags = 0;
                    recipients = null;
                    secretsFlags = 0;
                    secrets = null;

                    // read version
                    if (false) {
                        /////// SKIP
                        byte[] versionByte;
                        versionByte = Arrays.copyOfRange(data, position, Transaction.DATA_VERSION_PART_LENGTH);
                        versiondata = new String(versionByte, StandardCharsets.UTF_8);
                    }
                    position += Transaction.DATA_VERSION_PART_LENGTH;

                    // read title size
                    byte[] titleSizeBytes = Arrays.copyOfRange(data, position, position + Transaction.DATA_TITLE_PART_LENGTH);
                    titleSize = Ints.fromByteArray(titleSizeBytes);
                    position += Transaction.DATA_TITLE_PART_LENGTH;

                } else {

                    flags = Arrays.copyOfRange(data, position, Integer.BYTES);
                    position += Integer.BYTES;

                    titleSize = Arrays.copyOfRange(data, position, position + 1)[0];
                    position++;

                }

                byte[] titleByte = Arrays.copyOfRange(data, position, position + titleSize);
                position += titleSize;
                title = new String(titleByte, StandardCharsets.UTF_8);

                if (onlyTitle) {
                    return new ExData(version, title, null, null);
                }

                if (version > 2) {

                    ///////////// PARS by FLAGS

                    if ((flags[1] & 128) > 0) {
                        /// RESERVED
                    }

                    int recipientsSize;
                    if ((flags[1] & RECIPIENTS_FLAG_MASK) > 0) {
                        //////// RECIPIENTS
                        byte[] sizeBytes = Arrays.copyOfRange(data, position, position + RECIPIENTS_SIZE_LENGTH + 1);
                        recipientsFlags = sizeBytes[0];
                        sizeBytes[0] = 0;
                        recipientsSize = Ints.fromByteArray(sizeBytes);
                        position += RECIPIENTS_SIZE_LENGTH + 1;

                        recipients = new Account[recipientsSize];
                        for (int i = 0; i < recipientsSize; i++) {
                            recipients[i] = new Account(Arrays.copyOfRange(data, position, position + Account.ADDRESS_SHORT_LENGTH));
                            position += Account.ADDRESS_SHORT_LENGTH;
                        }

                    } else {
                        recipientsSize = 0;
                        recipientsFlags = 0;
                        recipients = new Account[0];
                    }

                    isEncrypted = (flags[1] & ENCRYPT_FLAG_MASK) > 0;
                    if (isEncrypted) {
                        secretsFlags = Arrays.copyOfRange(data, position, position + 1)[0];
                        position++;
                        int secretsSize = recipientsSize + 1;
                        secrets = new byte[secretsSize][];
                        int passwordLen;
                        for (int i = 0; i < secretsSize; i++) {
                            passwordLen = Arrays.copyOfRange(data, position, position + 1)[0];
                            position++;
                            secrets[i] = Arrays.copyOfRange(data, position, position + passwordLen);
                            position += passwordLen;
                        }
                    } else {
                        secretsFlags = 0;
                        secrets = null;
                    }
                } else {
                    isEncrypted = false;
                    flags = null;
                    recipientsFlags = 0;
                    recipients = null;
                    secretsFlags = 0;
                    secrets = null;
                }

                if (data.length == position) {
                    if (version > 2) {
                        return new ExData(flags, title, recipientsFlags, recipients, null, null);
                    } else {
                        // version 2.0 - 2.1
                        return new ExData(version, title, null, null);
                    }
                } else {
                    //READ Length JSON PART
                    byte[] dataSizeBytes = Arrays.copyOfRange(data, position, position + Transaction.DATA_JSON_PART_LENGTH);
                    int JSONSize = Ints.fromByteArray(dataSizeBytes);
                    position += Transaction.DATA_JSON_PART_LENGTH;

                    //READ JSON
                    byte[] jsonData = Arrays.copyOfRange(data, position, position + JSONSize);
                    position += JSONSize;

                    if (isEncrypted) {
                        // version 3 - with SECRETS
                        return new ExData(flags, title, recipientsFlags, recipients, secretsFlags, secrets, jsonData);
                    } else {
                        try {

                            JSONObject json = (JSONObject) JSONValue.parseWithException(new String(jsonData, StandardCharsets.UTF_8));

                            HashMap<String, Tuple3<byte[], Boolean, byte[]>> filesMap;

                            if (andFiles) {
                                JSONObject files;
                                Set files_key_Set;

                                // запомним даже если пустой список - делаем НЕ НУЛЬ тут - это флаг что Файлы Парсили
                                filesMap = new HashMap<String, Tuple3<byte[], Boolean, byte[]>>();

                                if (json.containsKey("F")) {
                                    // v 2.1

                                    files = (JSONObject) json.get("F");

                                    files_key_Set = files.keySet();
                                    for (int i = 0; i < files_key_Set.size(); i++) {
                                        JSONObject file = (JSONObject) files.get(i + "");

                                        String name = (String) file.get("FN"); // File_Name
                                        Boolean zip = new Boolean((String) file.get("ZP")); // ZIP
                                        int size = new Integer((String) file.get("SZ"));
                                        byte[] fileBytes = Arrays.copyOfRange(data, position, position + size);
                                        position = position + size;
                                        byte[] fileBytesOrig = null;
                                        if (zip) {
                                            try {
                                                fileBytesOrig = ZipBytes.decompress(fileBytes);
                                            } catch (DataFormatException e1) {
                                                LOGGER.error(e1.getMessage(), e1);
                                            }
                                        } else {
                                            fileBytesOrig = fileBytes;
                                        }
                                        filesMap.put(name, new Tuple3(Crypto.getInstance().digest(fileBytesOrig), zip, fileBytes));

                                    }

                                } else if (json.containsKey("&*&*%$$%_files_#$@%%%")) {
                                    //v2.0

                                    files = (JSONObject) json.get("&*&*%$$%_files_#$@%%%");

                                    files_key_Set = files.keySet();
                                    for (int i = 0; i < files_key_Set.size(); i++) {
                                        JSONObject file = (JSONObject) files.get(i + "");

                                        String name = (String) file.get("File_Name"); // File_Name
                                        Boolean zip = new Boolean((String) file.get("ZIP")); // ZIP
                                        int size = new Integer((String) file.get("Size"));
                                        byte[] fileBytes = Arrays.copyOfRange(data, position, position + size);
                                        position = position + size;

                                        byte[] fileBytesOrig = null;
                                        if (zip) {
                                            try {
                                                fileBytesOrig = ZipBytes.decompress(fileBytes);
                                            } catch (DataFormatException e1) {
                                                LOGGER.error(e1.getMessage(), e1);
                                            }
                                        } else {
                                            fileBytesOrig = fileBytes;
                                        }
                                        filesMap.put(name, new Tuple3(Crypto.getInstance().digest(fileBytesOrig), zip, fileBytes));

                                    }
                                } else {
                                    filesMap = null;
                                }
                            } else {
                                filesMap = null;
                            }

                            if (version > 2) {
                                return new ExData(flags, title, recipientsFlags, recipients, json, filesMap);
                            } else {
                                // version 2.0 - 2.1
                                return new ExData(version, title, json, filesMap);
                            }

                        } catch (Exception e) {
                            return new ExData(version, title, null, null);
                        }
                    }
                }

        }
    }

    public byte[][] getAllHashesAsBytes() {

        JSONObject hashesJson = getHashes();

        int count = 0;
        if (hashesJson != null) {
            count = hashesJson.size();
        }

        String message = getMessage();
        if (message != null && !message.isEmpty()) {
            count++;
        }

        if (files != null && !files.isEmpty()) {
            count += files.size();
        }

        if (count == 0)
            return null;

        byte[][] hashes = new byte[count][];

        count = 0;

        // ADD message first
        if (message != null && !message.isEmpty()) {
            hashes[count++] = Crypto.getInstance().digest(message.getBytes(StandardCharsets.UTF_8));
        }

        // ADD native hashes
        if (hashesJson != null && !hashesJson.isEmpty()) {
            for (Object hash : hashesJson.keySet()) {
                hashes[count++] = Base58.decode(hash.toString());
            }
        }

        // ADD hashes of files
        if (files != null && !files.isEmpty()) {
            for (Tuple3<byte[], Boolean, byte[]> fileItem : files.values()) {
                hashes[count++] = fileItem.a;
            }
        }

        return hashes;
    }

    public static byte[] make(PrivateKeyAccount creator, String title, Account[] recipients, boolean isEncrypted,
                              TemplateCls template, HashMap<String, String> params_Template,
                              HashMap<String, String> hashes_Map, String message, Set<Tuple3<String, Boolean, byte[]>> files_Set)
            throws Exception {

        JSONObject out_Map = new JSONObject();
        JSONObject params_Map = new JSONObject();
        JSONObject hashes_JSON = new JSONObject();

        // add template AND params
        if (template != null) {
            out_Map.put("TM", template.getKey() + "");

            Iterator<Entry<String, String>> it_templ = params_Template.entrySet().iterator();
            while (it_templ.hasNext()) {
                Entry<String, String> key1 = it_templ.next();
                params_Map.put(key1.getKey(), key1.getValue());
            }
            if (!params_Map.isEmpty())
                out_Map.put("PR", params_Map);

        }

        // add hashes
        Iterator<Entry<String, String>> it_Hash = hashes_Map.entrySet().iterator();
        while (it_Hash.hasNext()) {
            Entry<String, String> hash = it_Hash.next();
            hashes_JSON.put(hash.getKey(), hash.getValue());
        }
        if (!hashes_JSON.isEmpty())
            out_Map.put("HS", hashes_JSON);

        // add Message
        if (message != null && !message.isEmpty())
            out_Map.put("MS", message);

        // add files
        HashMap<String, Tuple3<byte[], Boolean, byte[]>> filesMap = new HashMap<>();
        Iterator<Tuple3<String, Boolean, byte[]>> it_Filles = files_Set.iterator();
        while (it_Filles.hasNext()) {
            Tuple3<String, Boolean, byte[]> file = it_Filles.next();

            boolean zip = file.b;
            byte[] fileBytes = file.c;
            byte[] fileBytesOrig = null;
            if (zip) {
                try {
                    fileBytesOrig = ZipBytes.decompress(fileBytes);
                } catch (DataFormatException e1) {
                    LOGGER.error(e1.getMessage(), e1);
                }
            } else {
                fileBytesOrig = fileBytes;
            }
            filesMap.put(file.a, new Tuple3(Crypto.getInstance().digest(fileBytesOrig), zip, fileBytes));
        }

        byte[] flags = new byte[]{3, 0, 0, 0};

        if (recipients != null && recipients.length > 0) {
            flags[1] = (byte) (flags[1] | RECIPIENTS_FLAG_MASK);
        }

        if (isEncrypted) {
            // случайный пароль и его для всех шифруем
            flags[1] = (byte) (flags[1] | ENCRYPT_FLAG_MASK);

            byte[][] secrets = new byte[recipients.length + 1][];

            byte[] password = Crypto.getInstance().createSeed(Crypto.HASH_LENGTH);

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] encryptedData = toByteJsonAndFiles(outStream, new JSONObject(out_Map), filesMap);

            try {
                encryptedData = AEScrypto.aesEncrypt(encryptedData, password);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return null;
            }

            byte[] privateKey = creator.getPrivateKey();
            for (int i = 0; i < recipients.length; i++) {

                //recipient
                Account recipient = recipients[i];
                byte[] publicKey;
                if (recipient instanceof PublicKeyAccount) {
                    publicKey = ((PublicKeyAccount) recipient).getPublicKey();
                } else {
                    publicKey = Controller.getInstance().getPublicKeyByAddress(recipient.getAddress());
                }

                if (publicKey == null) {
                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(recipient.toString() + " : " +
                                    "The recipient has not yet performed any action in the blockchain.\nYou can't send an encrypted message to him."),
                            Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

                    return null;
                }

                secrets[i] = AEScrypto.dataEncrypt(password, privateKey, publicKey);

            }

            secrets[recipients.length] = AEScrypto.dataEncrypt(password, privateKey, creator.getPublicKey());

            return new ExData(flags, title, (byte) 0, recipients, (byte) 0, secrets, encryptedData).toByte();
        }

        return new ExData(flags, title, (byte) 0, recipients, new JSONObject(out_Map), filesMap).toByte();

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
    public void makeJSONforHTML(DCSet dcSet, Map output,
                                int blockNo, int seqNo, JSONObject langObj) {

        if (title != null) {
            output.put("title", title);
        }

        if (recipients != null && recipients.length > 0) {
            List<List<String>> recipientsOut = new ArrayList<>();
            for (Account recipient : recipients) {
                recipientsOut.add(Arrays.asList(recipient.getAddress(), recipient.getPersonAsString()));
            }
            output.put("recipients", recipientsOut);
        }

        // parse JSON
        if (json != null) {

            if (message != null && !message.isEmpty()) {
                output.put("message", message);
                output.put("messageHash", Base58.encode(Crypto.getInstance().digest(message.getBytes(StandardCharsets.UTF_8))));
            }

            output.put("body", valuedText);

            /////////// FILES
            HashMap<String, Tuple3<byte[], Boolean, byte[]>> filesMap = files;

            if (filesMap != null && !filesMap.isEmpty()) {
                String files = "";
                Set<String> fileNames = filesMap.keySet();

                int filesCount = 1;
                for (String fileName : fileNames) {

                    Tuple3<byte[], Boolean, byte[]> fileValue = filesMap.get(fileName);
                    String hash = Base58.encode(fileValue.a);

                    files += filesCount + " " + fileName
                            + " <a href=?q=" + hash + BlockExplorer.get_Lang(langObj) + "&search=transactions>[" + hash + "]</a>";
                    files += " - <a href ='../apidocuments/getFile?download=true&block="
                            + blockNo + "&seqNo=" + seqNo + "&name=" + fileName + "'><b>"
                            + Lang.getInstance().translateFromLangObj("Download", langObj) + "</b></a><br>";

                    filesCount++;

                }

                output.put("files", files);
            }

            ///////// NATIVE HASHES
            if (hashes == null) {
                hashes = new JSONObject();
            }

            if (!hashes.isEmpty()) {
                String hashesHTML = "";
                int hashesCount = 1;

                for (Object hash : hashes.keySet()) {
                    hashesHTML += hashesCount
                            + " <a href=?q=" + hash + BlockExplorer.get_Lang(langObj) + "&search=transactions>" + hash + "</a> - "
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
    public void makeJSONforHTML_1(DCSet dcSet, Map output, Long templateKey) {

        output.put("title", title);

        if (message != null && !message.isEmpty()) {
            output.put("message", message);
            output.put("messageHash", Base58.encode(Crypto.getInstance().digest(message.getBytes(StandardCharsets.UTF_8))));
        }

        Set<String> kS;
        String description;
        String paramsStr;
        JSONObject params;
        TemplateCls template = (TemplateCls) ItemCls.getItem(dcSet, ItemCls.TEMPLATE_TYPE, templateKey);
        if (template != null) {
            description = template.viewDescription();
            paramsStr = json.get("Statement_Params").toString();

            try {
                params = (JSONObject) JSONValue.parseWithException(paramsStr);
                description = templateWithValues(description, params);
            } catch (Exception e) {
            }

            output.put("body", description);

        }

        try {
            String hashes = "";
            paramsStr = json.get("Hashes").toString();
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

    public boolean decrypt(PublicKeyAccount account, Account recipient) {

        byte[] password;
        int pos = -1;
        if (secrets.equals((recipient))) {
            pos = secrets.length - 1;
        } else {
            for (int i = 0; i < recipients.length - 1; i++) {
                if (recipients[i].equals(recipient)) {
                    pos = i;
                    break;
                }
            }
        }

        if (pos < 0) {
            return false;
        }

        try {
            password = Controller.getInstance().decrypt(account, recipient, secrets[pos]);
            decryptedData = AEScrypto.aesDecrypt(encryptedData, password);
            parse(flags[0], decryptedData, false, true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }

        return true;
    }
}
