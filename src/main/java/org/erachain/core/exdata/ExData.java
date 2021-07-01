package org.erachain.core.exdata;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.erachain.api.ApiErrorFactory;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.blockexplorer.BlockExplorer;
import org.erachain.core.crypto.AEScrypto;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exActions.ExAction;
import org.erachain.core.exdata.exActions.ExAirDrop;
import org.erachain.core.exdata.exActions.ExPays;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.exdata.exLink.ExLinkAuthor;
import org.erachain.core.exdata.exLink.ExLinkSource;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.ZipBytes;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
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
    private static final int AUTHORS_SIZE_LENGTH = 3; // size version part
    private static final int SOURCES_SIZE_LENGTH = 3; // size version part
    private static final int SECRET_LENGTH = Crypto.HASH_LENGTH; // size version part

    /**
     * flags[1] masks
     */
    private static final byte HAS_PARENT_MASK = -128;
    private static final byte RECIPIENTS_FLAG_MASK = 64;
    private static final byte ENCRYPT_FLAG_MASK = 32;
    private static final byte AUTHORS_FLAG_MASK = 16;
    private static final byte SOURCES_FLAG_MASK = 8;
    private static final byte TAGS_FLAG_MASK = 4;
    private static final byte ACTION_FLAG_MASK = 2;
    /**
     * flags[2] masks
     */
    private static final byte AIRDROP_FLAG_MASK = 2;

    public static final byte LINK_SIMPLE_TYPE = 0; // для выбора типа в ГУИ
    public static final byte LINK_APPENDIX_TYPE = 1; // дополнение / приложение к другому документу или Сущности
    public static final byte LINK_REPLY_COMMENT_TYPE = 2; // ответ всем на предыдущий документ - Ссылка для отслеживания
    public static final byte LINK_COMMENT_TYPE_FOR_VIEW = 3; // замечание без получателей - используется только для ГУИ
    public static final byte LINK_SURELY_TYPE = 5; // гарантия / поручительство на долю

    public static final byte LINK_SOURCE_TYPE = 6; // как Источник
    public static final byte LINK_AUTHOR_TYPE = 7; // как Автор

    private static final Logger LOGGER = LoggerFactory.getLogger(ExData.class);

    /**
     * 0 - version; 1-3 - flags;
     */
    private final byte[] flags;

    private final ExLink exLink;

    private final ExAction exAction;

    private final String title;
    private JSONObject json;

    private String message;
    private JSONObject hashes;

    /**
     * Name: hash, is ZIP?, file data
     */
    private HashMap<String, Tuple3<byte[], Boolean, byte[]>> files;

    private byte recipientsFlags;
    private static final byte RECIPIENTS_FLAG_SING_ONLY_MASK = -128;
    private Account[] recipients;

    private byte authorsFlags;
    private ExLinkAuthor[] authors;

    private byte sourcesFlags;
    private ExLinkSource[] sources;

    private byte[] tags;
    private long templateKey;
    private TemplateCls template;
    private JSONObject params;
    private String valuedText;

    private byte secretsFlags;
    private byte[][] secrets;
    private byte[] encryptedData;


    public DCSet dcSet;
    public String errorValue;

    /**
     * OLD version 1-2
     *
     * @param version
     * @param exLink
     * @param title
     * @param json
     * @param files
     */
    public ExData(int version, ExLink exLink, String title,
                  JSONObject json, HashMap<String, Tuple3<byte[], Boolean, byte[]>> files) {
        this.flags = new byte[]{(byte) version, 0, 0, 0};

        this.exLink = exLink;
        if (exLink != null) {
            //this.flags[1] = (byte) (this.flags[1] | HAS_PARENT_MASK);
            this.flags[1] |= HAS_PARENT_MASK;
        }

        exAction = null;

        this.title = title;
        this.json = json;
        this.files = files;

    }

    /**
     * Version 3
     *
     * @param flags
     * @param exLink
     * @param exAction
     * @param title
     * @param recipients
     * @param authorsFlags
     * @param authors
     * @param sourcesFlags
     * @param sources
     * @param tags
     * @param json
     * @param files
     */
    public ExData(byte[] flags, ExLink exLink, ExAction exAction, String title,
                  byte recipientsFlags, Account[] recipients,
                  byte authorsFlags, ExLinkAuthor[] authors, byte sourcesFlags, ExLinkSource[] sources,
                  byte[] tags, JSONObject json, HashMap<String, Tuple3<byte[], Boolean, byte[]>> files) {
        this.flags = flags;

        this.exLink = exLink;
        if (exLink != null) {
            this.flags[1] |= HAS_PARENT_MASK;
        }

        this.exAction = exAction;
        if (exAction != null) {
            this.flags[1] |= ACTION_FLAG_MASK;
        }

        this.title = title;
        this.recipientsFlags = recipientsFlags;
        this.recipients = recipients;

        this.authorsFlags = authorsFlags;
        this.authors = authors;

        this.sourcesFlags = sourcesFlags;
        this.sources = sources;

        this.tags = tags;

        this.json = json;
        this.files = files;

    }

    /**
     * version 3 encrypted
     *
     * @param flags
     * @param exLink
     * @param exAction
     * @param exAirDrop
     * @param title
     * @param recipients
     * @param authorsFlags
     * @param authors
     * @param sourcesFlags
     * @param sources
     * @param tags
     * @param encryptedData
     */
    public ExData(byte[] flags, ExLink exLink, ExAction exAction, ExAirDrop exAirDrop, String title,
                  byte recipientsFlags, Account[] recipients,
                  byte authorsFlags, ExLinkAuthor[] authors, byte sourcesFlags, ExLinkSource[] sources,
                  byte[] tags, byte secretsFlags, byte[][] secrets,
                  byte[] encryptedData) {
        this.flags = flags;

        this.exLink = exLink;
        if (exLink != null) {
            this.flags[1] |= HAS_PARENT_MASK;
        }

        this.exAction = exAction;
        if (this.exAction != null) {
            this.flags[1] |= ACTION_FLAG_MASK;
        }

        this.title = title;

        this.recipientsFlags = recipientsFlags;
        this.recipients = recipients;

        this.authorsFlags = authorsFlags;
        this.authors = authors;

        this.sourcesFlags = sourcesFlags;
        this.sources = sources;

        this.tags = tags;

        this.secretsFlags = secretsFlags;
        this.secrets = secrets;
        this.encryptedData = encryptedData;

    }

    public void setDC(DCSet dcSet) {

        if (this.dcSet != null && this.dcSet.equals(dcSet)) {
            // SAME DCSet
            return;
        }

        this.dcSet = dcSet;
        if (exAction != null) {
            exAction.setDC(dcSet);
        }
        resolveValues();

    }

    /**
     * for set up all values from JSON etc.
     */
    private void resolveValues() {

        String str = "";
        Set<String> kS;
        if (json == null || json.isEmpty())
            return;

        try {
            // v 2.1
            if (json.containsKey("TM") && json.get("TM") != null) {
                try {
                    templateKey = new Long(json.get("TM").toString());
                } catch (Exception e) {
                }

                if (templateKey != 0) {
                    template = (TemplateCls) ItemCls.getItem(DCSet.getInstance(), ItemCls.TEMPLATE_TYPE, templateKey);
                }

                if (template == null) {
                    valuedText = "ERROR: template [" + templateKey + "] not found!";
                } else {
                    valuedText = template.viewDescription();

                    if (json.containsKey("PR")) {
                        params = (JSONObject) json.get("PR");

                        kS = params.keySet();
                        for (String s : kS) {
                            valuedText = valuedText.replace("{{" + s + "}}", (CharSequence) params.get(s));
                        }
                    }
                }
            } else
                // v2.0
                if (json.containsKey("Template") && json.get("Template") != null) {
                    try {
                        templateKey = new Long(json.get("Template").toString());
                    } catch (Exception e) {
                    }

                    if (templateKey != 0) {
                        template = (TemplateCls) ItemCls.getItem(dcSet, ItemCls.TEMPLATE_TYPE, templateKey);
                    }

                    if (template == null) {
                        valuedText = "ERROR: template [" + templateKey + "] not found!";
                    } else {
                        valuedText = template.viewDescription();

                        if (json.containsKey("Statement_Params")) {
                            /// str = json.get("Statement_Params").toString();
                            /// params = (JSONObject) JSONValue.parseWithException(str);
                            params = (JSONObject) json.get("Statement_Params");

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

    public void parseDBData(byte[] dbData) {

        int position = 0;
        if (exAction != null) {
            position = exAction.parseDBData(dbData, position);
        }

    }

    public byte[] makeDBData() {

        byte[] dbData = new byte[0];
        if (exAction != null) {
            dbData = Bytes.concat(dbData, exAction.getDBdata());
        }

        return dbData;
    }

    public boolean isParsedWithFiles() {
        return files != null;
    }

    public ExLink getExLink() {
        return exLink;
    }

    public ExAction getExAction() {
        return exAction;
    }

    public byte getParentRefFlags() {
        if (exLink == null)
            return 0;
        return exLink.getFlags();
    }

    public byte getParentType() {
        if (exLink == null)
            return 0;
        return exLink.getType();
    }

    public long getParentRef() {
        if (exLink == null)
            return 0;
        return exLink.getRef();
    }

    public byte getLinkType() {
        if (exLink == null)
            return 0;
        return exLink.getType();
    }

    public byte getLinkValue1() {
        if (exLink == null)
            return 0;
        return exLink.getValue1();
    }

    public byte getLinkValue2() {
        if (exLink == null)
            return 0;
        return exLink.getValue2();
    }

    public String viewLinkTypeName() {
        if (exLink == null) {
            return ExLink.viewTypeName(LINK_SIMPLE_TYPE, false);
        }
        return exLink.viewTypeName(hasRecipients());
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

    public ExLinkAuthor[] getAuthors() {
        return authors == null ? new ExLinkAuthor[0] : authors;
    }

    public ExLinkSource[] getSources() {
        return sources == null ? new ExLinkSource[0] : sources;
    }

    public byte[] getTags() {
        return tags == null ? new byte[0] : tags;
    }

    public byte[][] getSecrets() {
        return secrets == null ? new byte[0][] : secrets;
    }

    public JSONObject getJsonObject() {
        return json;
    }

    public JSONObject getTemplateValues() {
        return json == null ? null : (JSONObject) json.get("PR");
    }

    public HashMap<String, Tuple3<byte[], Boolean, byte[]>> getFiles() {
        return files;
    }

    public boolean hasRecipients() {
        return recipients != null && recipients.length > 0;
    }

    public boolean hasExPays() {
        return exAction != null;
    }

    public boolean hasAuthors() {
        return authors != null && authors.length > 0;
    }

    public boolean hasSources() {
        return sources != null && sources.length > 0;
    }

    public boolean isCanSignOnlyRecipients() {
        return (recipientsFlags & RECIPIENTS_FLAG_SING_ONLY_MASK) != 0;
    }

    public boolean hasFiles() {
        return files != null && !files.isEmpty();
    }

    public boolean hasPublicText() {
        if (Transaction.hasPublicText(title, null, true, false, message)
                || hasAuthors() // авторов только удостоверенный счет может назначить
                || getTemplateValues() != null // в параметрах могут написать что угодно
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

    public static byte[] setEncryptedFlag(byte[] flags, boolean value) {
        byte[] newFlags = new byte[flags.length];
        System.arraycopy(flags, 0, newFlags, 0, flags.length);

        if (value) {
            newFlags[1] |= ENCRYPT_FLAG_MASK;
        } else {
            newFlags[1] &= ~ENCRYPT_FLAG_MASK;
        }
        return newFlags;
    }

    public boolean isTemplateUnique() {
        return !isEncrypted() && json.containsKey("TMU");
    }

    public boolean isMessageUnique() {
        return !isEncrypted() && json.containsKey("MSU");
    }

    public boolean isHashesUnique() {
        return !isEncrypted() && json.containsKey("HSU");
    }

    public boolean isFilesUnique() {
        return !isEncrypted() && json.containsKey("FU");
    }

    static long templateRoyaltyFee = 200 * 10;

    public long getRoyaltyFee() {
        if (templateKey > 0)
            return templateRoyaltyFee;

        return 0L;
    }

    public BigDecimal getRoyaltyFeeBG() {
        long royaltyFee = getRoyaltyFee();
        if (royaltyFee == 0L)
            return BigDecimal.ZERO;

        return new BigDecimal(royaltyFee).multiply(BlockChain.FEE_RATE).setScale(BlockChain.FEE_SCALE, BigDecimal.ROUND_UP);
    }

    // info to byte[]
    @SuppressWarnings("unchecked")

    static boolean newStyle = true;

    public static byte[] toByteJsonAndFiles(ByteArrayOutputStream outStream, JSONObject json,
                                            HashMap<String, Tuple3<byte[], Boolean, byte[]>> files) throws Exception {

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
        Iterator<Entry<String, Tuple3<byte[], Boolean, byte[]>>> iterator = files.entrySet().iterator();
        JSONArray filesJsonArray = new JSONArray();

        int i = 0;
        byte[][] outFiles = new byte[files.size()][];
        if (newStyle) {
            while (iterator.hasNext()) {
                Entry<String, Tuple3<byte[], Boolean, byte[]>> file = iterator.next();
                JSONObject fileJson = new JSONObject();
                fileJson.put("FN", file.getKey()); // File_Name
                fileJson.put("ZP", file.getValue().b); // ZIP
                fileJson.put("SZ", file.getValue().c.length); // Size
                filesJsonArray.add(fileJson);
                outFiles[i++] = file.getValue().c;
            }
            json.put("F", filesJsonArray);
        } else {
            //ArrayList<byte[]> out_files = new ArrayList<byte[]>();
            while (iterator.hasNext()) {
                Entry<String, Tuple3<byte[], Boolean, byte[]>> file = iterator.next();
                JSONObject fileJson = new JSONObject();
                fileJson.put("FN", file.getKey()); // File_Name
                fileJson.put("ZP", file.getValue().b); // ZIP
                fileJson.put("SZ", file.getValue().c.length); // Size
                filesJsonArray.add(fileJson);
                outFiles[i++] = file.getValue().c;
                i++;
            }
            json.put("F", filesJsonArray);

        }
        JSON_Bytes = json.toString().getBytes(StandardCharsets.UTF_8);
        // convert int to byte
        size_Json = ByteBuffer.allocate(DATA_JSON_PART_LENGTH).putInt(JSON_Bytes.length).array();
        outStream.write(size_Json);
        outStream.write(JSON_Bytes);
        for (i = 0; i < outFiles.length; i++) {
            outStream.write(outFiles[i]);
        }
        return outStream.toByteArray();

    }

    public int getLengthDBData() {
        int len = 0;
        if (exAction != null)
            len += exAction.getLengthDBData();

        if (airdrop != null)
            len += airdrop.length();

        return len;
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

        if (exLink != null) {
            outStream.write(exLink.toBytes());
        }

        if (exAction != null) {
            outStream.write(exAction.toBytes());
        }

        if (airdrop != null) {
            outStream.write(airdrop.toBytes());
        }

        if ((flags[1] & RECIPIENTS_FLAG_MASK) > 0) {
            byte[] recipientsSize = Ints.toByteArray(recipients.length);
            recipientsSize[0] = recipientsFlags;
            outStream.write(recipientsSize);

            for (int i = 0; i < recipients.length; i++) {
                outStream.write(recipients[i].getShortAddressBytes());
            }
        }

        if ((flags[1] & AUTHORS_FLAG_MASK) > 0) {
            byte[] authorsSize = Ints.toByteArray(authors.length);
            authorsSize[0] = authorsFlags;
            outStream.write(authorsSize);

            for (int i = 0; i < authors.length; i++) {
                outStream.write(authors[i].toBytes());
            }
        }

        if ((flags[1] & SOURCES_FLAG_MASK) > 0) {
            byte[] sourcesSize = Ints.toByteArray(sources.length);
            sourcesSize[0] = sourcesFlags;
            outStream.write(sourcesSize);

            for (int i = 0; i < sources.length; i++) {
                outStream.write(sources[i].toBytes());
            }
        }

        if ((flags[1] & TAGS_FLAG_MASK) > 0) {
            outStream.write((byte) tags.length);
            outStream.write(tags);
        }


        // IF JSON and FILES ENCRYPTED?
        if ((flags[1] & ENCRYPT_FLAG_MASK) > 0) {
            // SECRETS
            outStream.write(secretsFlags);
            for (int i = 0; i < secrets.length; i++) {
                outStream.write((byte) secrets[i].length);
                outStream.write(secrets[i]);
            }

            // тут длину для JSON не нужно записывать
            outStream.write(encryptedData);

            return outStream.toByteArray();

        } else {

            return toByteJsonAndFiles(outStream, json, files);
        }

    }

    private static Fun.Tuple2<JSONObject, HashMap> parseJsonAndFiles(byte[] data, boolean andFiles) {

        int position = 0;

        //READ Length JSON PART
        byte[] jsonSizeBytes = Arrays.copyOfRange(data, position, position + Transaction.DATA_JSON_PART_LENGTH);
        int JSONSize = Ints.fromByteArray(jsonSizeBytes);
        position += Transaction.DATA_JSON_PART_LENGTH;

        if (JSONSize == 0) {
            return new Fun.Tuple2<>(null, null);
        }

        //READ JSON
        byte[] jsonData = Arrays.copyOfRange(data, position, position + JSONSize);

        try {
            JSONObject json = (JSONObject) JSONValue.parseWithException(new String(jsonData, StandardCharsets.UTF_8));

            HashMap<String, Tuple3<byte[], Boolean, byte[]>> filesMap;

            if (andFiles) {
                position += JSONSize;

                JSONObject files;
                Set files_key_Set;

                // запомним даже если пустой список - делаем НЕ НУЛЬ тут - это флаг что Файлы Парсили
                filesMap = new HashMap<String, Tuple3<byte[], Boolean, byte[]>>();

                if (json.containsKey("F")) {
                    // v 2.1

                    Object filesObj = json.get("F");
                    if (filesObj instanceof JSONArray) {
                        // new STYLE
                        JSONArray filesArray = (JSONArray) json.get("F");

                        for (int i = 0; i < filesArray.size(); i++) {
                            JSONObject file = (JSONObject) filesArray.get(i);

                            String name = (String) file.get("FN"); // File_Name
                            Boolean zip = (Boolean) file.get("ZP"); // ZIP
                            int size = (int) (long) file.get("SZ");
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
                        // OLD style
                        files = (JSONObject) json.get("F");

                        files_key_Set = files.keySet();
                        for (int i = 0; i < files_key_Set.size(); i++) {
                            JSONObject file = (JSONObject) files.get(i + "");

                            String name = (String) file.get("FN"); // File_Name
                            Boolean zip = new Boolean(file.get("ZP").toString()); // ZIP
                            int size = (int) (long) new Long(file.get("SZ").toString());
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

            return new Fun.Tuple2<>(json, filesMap);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new Fun.Tuple2<>(null, null);
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ExData parse(
            int version, byte[] data, boolean onlyTitle, boolean andFiles) throws Exception {

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
                return new ExData(0, null, items[0], dataJson, null);

            case 1:
                text = new String(data, StandardCharsets.UTF_8);
                dataJson = (JSONObject) JSONValue.parseWithException(text);
                String title = dataJson.get("Title").toString();
                return new ExData(1, null, title, dataJson, null);

            default:

                byte[] flags;
                int titleSize;
                ExLink exLink;
                ExPays exPays;
                ExAirDrop exAirdrop;
                byte recipientsFlags;
                Account[] recipients;
                byte authorsFlags;
                ExLinkAuthor[] authors;
                byte sourcesFlags;
                ExLinkSource[] sources;
                byte[] tags;
                boolean isEncrypted;
                byte secretsFlags;
                byte[][] secrets;

                // версия тут нафиг не нужна в строковом виде
                if (version == 2) {

                    position += Transaction.DATA_VERSION_PART_LENGTH;
                    flags = null; // здесь чтобы ошибки синтаксиса не было задаем - ниже переопределим

                    // read title size
                    byte[] titleSizeBytes = Arrays.copyOfRange(data, position, position + Transaction.DATA_TITLE_PART_LENGTH);
                    titleSize = Ints.fromByteArray(titleSizeBytes);
                    position += Transaction.DATA_TITLE_PART_LENGTH;

                } else {

                    flags = Arrays.copyOfRange(data, position, Integer.BYTES);
                    position += Integer.BYTES;

                    titleSize = Byte.toUnsignedInt(data[position]);
                    position++;

                }

                byte[] titleByte = Arrays.copyOfRange(data, position, position + titleSize);
                position += titleSize;
                title = new String(titleByte, StandardCharsets.UTF_8);

                if (onlyTitle) {
                    return new ExData(version, null, title, null, null);
                }

                if (version > 2) {

                    ///////////// PARS by FLAGS
                    if (flags[1] < 0) {
                        // ExLink READ
                        exLink = ExLink.parse(data, position);
                        position += exLink.length();
                    } else {
                        exLink = null;
                    }

                    if ((flags[1] & ACTION_FLAG_MASK) > 0) {
                        // ExLink READ
                        exPays = ExPays.parse(data, position);
                        position += exPays.length();
                    } else {
                        exPays = null;
                    }

                    if ((flags[2] & AIRDROP_FLAG_MASK) > 0) {
                        // ExLink READ
                        exAirdrop = ExAirDrop.parse(data, position);
                        position += exAirdrop.length();
                    } else {
                        exAirdrop = null;
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

                    if ((flags[1] & AUTHORS_FLAG_MASK) > 0) {
                        //////// AUTHORS
                        byte[] sizeBytes = Arrays.copyOfRange(data, position, position + AUTHORS_SIZE_LENGTH + 1);
                        authorsFlags = sizeBytes[0];
                        sizeBytes[0] = 0;
                        int authorsSize = Ints.fromByteArray(sizeBytes);
                        position += AUTHORS_SIZE_LENGTH + 1;

                        authors = new ExLinkAuthor[authorsSize];
                        for (int i = 0; i < authorsSize; i++) {
                            authors[i] = new ExLinkAuthor(data, position);
                            position += authors[i].length();
                            //ExLink exLink1 = ExLink.parse(authors[i].toBytes());
                        }

                    } else {
                        authorsFlags = 0;
                        authors = new ExLinkAuthor[0];
                    }

                    if ((flags[1] & SOURCES_FLAG_MASK) > 0) {
                        //////// SOURCES
                        byte[] sizeBytes = Arrays.copyOfRange(data, position, position + SOURCES_SIZE_LENGTH + 1);
                        sourcesFlags = sizeBytes[0];
                        sizeBytes[0] = 0;
                        int sourcesSize = Ints.fromByteArray(sizeBytes);
                        position += SOURCES_SIZE_LENGTH + 1;

                        sources = new ExLinkSource[sourcesSize];
                        for (int i = 0; i < sourcesSize; i++) {
                            sources[i] = new ExLinkSource(data, position);
                            position += sources[i].length();
                        }

                    } else {
                        sourcesFlags = 0;
                        sources = new ExLinkSource[0];
                    }

                    if ((flags[1] & TAGS_FLAG_MASK) > 0) {
                        int tagsSize = data[position];
                        position++;
                        tags = Arrays.copyOfRange(data, position, position + tagsSize);
                        position += tagsSize;
                    } else {
                        tags = null;
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
                    exLink = null;
                    exPays = null;
                    exAirdrop = null;

                    isEncrypted = false;
                    flags = new byte[]{(byte) version, 0, 0, 0};

                    recipientsFlags = 0;
                    recipients = null;

                    authorsFlags = 0;
                    authors = null;

                    sourcesFlags = 0;
                    sources = null;

                    tags = null;

                    secretsFlags = 0;
                    secrets = null;
                }

                if (data.length == position) {
                    if (version > 2) {
                        return new ExData(flags, exLink, exPays, title, recipientsFlags, recipients, authorsFlags, authors, sourcesFlags, sources, tags, null, null);
                    } else {
                        // version 2.0 - 2.1
                        return new ExData(version, exLink, title, null, null);
                    }
                } else {


                    if (isEncrypted) {
                        // version 3 - with SECRETS
                        return new ExData(flags, exLink, exPays, exAirdrop, title, recipientsFlags, recipients, authorsFlags, authors, sourcesFlags, sources, tags, secretsFlags, secrets,
                                Arrays.copyOfRange(data, position, data.length));
                    } else {

                        Fun.Tuple2<JSONObject, HashMap> jsonAndFiles = parseJsonAndFiles(Arrays.copyOfRange(data, position, data.length), andFiles);
                        return new ExData(flags, exLink, exPays, title, recipientsFlags, recipients, authorsFlags, authors, sourcesFlags, sources, tags, jsonAndFiles.a,
                                jsonAndFiles.b);
                    }
                }

        }
    }

    public byte[] getTemplateHash() {
        return Crypto.getInstance().digest(("" + templateKey
                + (params == null ? "" : params.toJSONString()))
                .getBytes(StandardCharsets.UTF_8));
    }

    public byte[] getMessageHash() {
        return Crypto.getInstance().digest(message.getBytes(StandardCharsets.UTF_8));
    }

    public byte[][] getAllHashesAsBytes(boolean onlyUnique) {

        int count = 0;
        if (hashes != null && (!onlyUnique || isHashesUnique())) {
            count = hashes.size();
        }

        if (templateKey > 0 && (!onlyUnique || isTemplateUnique())) {
            count++;
        }

        if (message != null && !message.isEmpty() && (!onlyUnique || isMessageUnique())) {
            count++;
        }

        if (files != null && !files.isEmpty() && (!onlyUnique || isFilesUnique())) {
            count += files.size();
        }

        if (count == 0)
            return null;

        byte[][] allHashes = new byte[count][];

        count = 0;

        // ADD template first
        if (templateKey != 0 && (!onlyUnique || isTemplateUnique())) {
            allHashes[count++] = getTemplateHash();

        }

        // ADD message
        if (message != null && !message.isEmpty() && (!onlyUnique || isMessageUnique())) {
            allHashes[count++] = getMessageHash();
        }

        // ADD native hashes
        if (hashes != null && !hashes.isEmpty() && (!onlyUnique || isHashesUnique())) {
            for (Object hash : hashes.keySet()) {
                allHashes[count++] = Base58.decode(hash.toString());
            }
        }

        // ADD hashes of files
        if (files != null && !files.isEmpty() && (!onlyUnique || isFilesUnique())) {
            for (Tuple3<byte[], Boolean, byte[]> fileItem : files.values()) {
                allHashes[count++] = fileItem.a;
            }
        }

        return allHashes;
    }

    public static byte[] make(ExLink exLink, ExPays exPays, ExAirDrop exAirDrop, PrivateKeyAccount creator, String title, boolean signCanOnlyRecipients, Account[] recipients,
                              ExLinkAuthor[] authors, ExLinkSource[] sources, String tagsStr, boolean isEncrypted,
                              Long templateKey, HashMap<String, String> params_Template, boolean uniqueTemplate,
                              String message, boolean uniqueMessage,
                              HashMap<String, String> hashes_Map, boolean uniqueHashes,
                              Set<Tuple3<String, Boolean, byte[]>> files_Set, boolean uniqueFiles)
            throws Exception {

        JSONObject out_Map = new JSONObject();
        JSONObject params_Map = new JSONObject();
        JSONObject hashes_JSON = new JSONObject();

        // add template AND params
        if (templateKey != null) {
            out_Map.put("TM", templateKey);

            Iterator<Entry<String, String>> it_templ = params_Template.entrySet().iterator();
            while (it_templ.hasNext()) {
                Entry<String, String> key1 = it_templ.next();
                params_Map.put(key1.getKey(), key1.getValue());
            }
            if (!params_Map.isEmpty())
                out_Map.put("PR", params_Map);

            if (!isEncrypted && uniqueTemplate) {
                out_Map.put("TMU", true);
            }
        }

        // add Message
        if (message != null && !message.isEmpty()) {
            out_Map.put("MS", message);
            if (!isEncrypted && uniqueMessage) {
                out_Map.put("MSU", true);
            }
        }

        if (hashes_Map != null && !hashes_Map.isEmpty()) {
            // add hashes
            Iterator<Entry<String, String>> it_Hash = hashes_Map.entrySet().iterator();
            while (it_Hash.hasNext()) {
                Entry<String, String> hash = it_Hash.next();
                hashes_JSON.put(hash.getKey(), hash.getValue());
            }

            out_Map.put("HS", hashes_JSON);

            if (!isEncrypted && uniqueHashes) {
                out_Map.put("HSU", true);
            }
        }

        // add files
        HashMap<String, Tuple3<byte[], Boolean, byte[]>> filesMap = new HashMap<>();
        Iterator<Tuple3<String, Boolean, byte[]>> it_Filles = files_Set.iterator();
        while (it_Filles.hasNext()) {
            Tuple3<String, Boolean, byte[]> file = it_Filles.next();

            Boolean zip = file.b;
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

        if (!isEncrypted && uniqueFiles) {
            out_Map.put("FU", true);
        }

        byte[] flags = new byte[]{3, 0, 0, 0};

        byte recipientsFlags = 0;
        if (signCanOnlyRecipients) {
            recipientsFlags |= RECIPIENTS_FLAG_SING_ONLY_MASK;
        }

        if (recipients != null && recipients.length > 0) {
            flags[1] = (byte) (flags[1] | RECIPIENTS_FLAG_MASK);
        }

        byte authorsFlags = 0;
        if (authors != null && authors.length > 0) {
            flags[1] = (byte) (flags[1] | AUTHORS_FLAG_MASK);
        }

        byte sourcesFlags = 0;
        if (sources != null && sources.length > 0) {
            flags[1] = (byte) (flags[1] | SOURCES_FLAG_MASK);
        }

        byte[] tags;
        if (tagsStr != null && !tagsStr.isEmpty()) {
            flags[1] = (byte) (flags[1] | TAGS_FLAG_MASK);
            tags = tagsStr.getBytes(StandardCharsets.UTF_8);
        } else {
            tags = null;
        }

        if (isEncrypted) {
            // случайный пароль make и его для всех шифруем
            flags[1] = (byte) (flags[1] | ENCRYPT_FLAG_MASK);

            int recipientsLen = recipients == null ? 0 : recipients.length;
            byte[][] secrets = new byte[recipientsLen + 1][];

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

            if (recipients != null) {
                for (int i = 0; i < recipientsLen; i++) {

                    //recipient
                    Account recipient = recipients[i];
                    byte[] publicKey;
                    if (recipient instanceof PublicKeyAccount) {
                        publicKey = ((PublicKeyAccount) recipient).getPublicKey();
                    } else {
                        publicKey = Controller.getInstance().getPublicKeyByAddress(recipient.getAddress());
                    }

                    if (publicKey == null) {
                        JOptionPane.showMessageDialog(new JFrame(), Lang.T(recipient.toString() + " : " +
                                        ApiErrorFactory.getInstance().messageError(ApiErrorFactory.ERROR_NO_PUBLIC_KEY)),
                                Lang.T("Error"), JOptionPane.ERROR_MESSAGE);

                        return null;
                    }

                    secrets[i] = AEScrypto.dataEncrypt(password, privateKey, publicKey);

                }
            }

            secrets[recipientsLen] = AEScrypto.dataEncrypt(password, privateKey, creator.getPublicKey());

            return new ExData(flags, exLink, exPays, exAirDrop, title, recipientsFlags, recipients, authorsFlags, authors,
                    sourcesFlags, sources, tags, (byte) 0, secrets, encryptedData).toByte();
        }

        return new ExData(flags, exLink, exPays, title, recipientsFlags, recipients, authorsFlags, authors,
                sourcesFlags, sources, tags, new JSONObject(out_Map), filesMap).toByte();

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
    public void makeJSONforHTML(Map output,
                                int blockNo, int seqNo, JSONObject langObj) {

        if (title != null && !title.isEmpty()) {
            output.put("Label_title", Lang.T("Title", langObj));
            output.put("title", title);
        }

        if (exLink != null) {
            output.put("Label_LinkType", Lang.T("Link Type", langObj));
            output.put("exLink_Name", Lang.T(exLink.viewTypeName(hasRecipients()), langObj));
            output.put("exLink", exLink.makeJSONforHTML(hasRecipients(), langObj));
            output.put("Label_Parent", Lang.T("for # для", langObj));

        }

        if (exAction != null) {
            output.put("Label_Accruals", Lang.T("Accruals", langObj));
            output.put("exPays", exAction.makeJSONforHTML(langObj));

        }

        if (isCanSignOnlyRecipients()) {
            output.put("Label_CanSignOnlyRecipients", Lang.T("To sign can only Recipients", langObj));
        }

        if (recipients != null && recipients.length > 0) {
            output.put("Label_recipients", Lang.T("Recipients", langObj));
            List<List<String>> recipientsOut = new ArrayList<>();
            for (Account recipient : recipients) {
                recipientsOut.add(Arrays.asList(recipient.getAddress(), recipient.getPersonAsString()));
            }
            output.put("recipients", recipientsOut);
        }

        if (authors != null && authors.length > 0) {
            output.put("Label_Authors", Lang.T("Authors", langObj));
            JSONArray authorsOut = new JSONArray();
            for (ExLinkAuthor author : authors) {
                authorsOut.add(author.makeJSONforHTML(langObj));
            }
            output.put("authors", authorsOut);
        }

        if (sources != null && sources.length > 0) {
            output.put("Label_Sources", Lang.T("Sources", langObj));
            JSONArray sourcesOut = new JSONArray();
            for (ExLinkSource source : sources) {
                sourcesOut.add(source.makeJSONforHTML(langObj));
            }
            output.put("sources", sourcesOut);
        }

        if (tags != null && tags.length > 0) {
            output.put("Label_Tags", Lang.T("Tags", langObj));
            output.put("tags", new String(tags, StandardCharsets.UTF_8));
        }

        if (isEncrypted()) {
            output.put("encrypted", Lang.T("Encrypted", langObj));
            return;

        } else {
            output.put("Label_Used_Template", Lang.T("Used template", langObj));
            output.put("Label_template_hash", Lang.T("Template text hash", langObj));
            output.put("Label_mess_hash", Lang.T("Text hash", langObj));
            output.put("Label_hashes", Lang.T("Hashes", langObj));
            output.put("Label_files", Lang.T("Files", langObj));

        }

        if (templateKey > 0) {
            output.put("templateKey", templateKey);
            output.put("templateName", template == null ? templateKey : template.viewName());
            if (valuedText != null) {
                output.put("body", valuedText);
            }
            output.put("templateHash", Base58.encode(getTemplateHash()));
            if (isTemplateUnique()) {
                output.put("templateUnique", 1);
            }
        }

        // parse JSON
        if (json != null) {

            if (message != null && !message.isEmpty()) {
                output.put("message", message);
                if (isMessageUnique()) {
                    output.put("messageUnique", 1);
                }
                output.put("messageHash", Base58.encode(getMessageHash()));
                output.put("Label_Source_Mess", Lang.T("Source Message # исходное сообщение", langObj));
            }

            ///////// NATIVE HASHES
            if (hashes == null) {
                hashes = new JSONObject();
            }

            if (!hashes.isEmpty()) {
                String hashesHTML = "";
                int hashesCount = 1;
                boolean isUnique = isHashesUnique();

                for (Object hash : hashes.keySet()) {
                    hashesHTML += hashesCount;
                    if (isUnique) {
                        hashesHTML += " <a href=?q=" + hash + BlockExplorer.get_Lang(langObj) + "&search=transactions>" + hash + "</a>";
                    } else {
                        hashesHTML += " " + hash;
                    }
                    hashesHTML += " - " + hashes.get(hash) + "<br>";
                    hashesCount++;
                }

                output.put("hashes", hashesHTML);
            }

            /////////// FILES

            if (files != null && !files.isEmpty()) {
                String filesStr = "";
                Set<String> fileNames = files.keySet();

                boolean isUnique = isFilesUnique();
                int filesCount = 1;
                for (String fileName : fileNames) {

                    Tuple3<byte[], Boolean, byte[]> fileValue = files.get(fileName);
                    String hash = Base58.encode(fileValue.a);

                    filesStr += filesCount + " " + fileName;

                    if (isUnique) {
                        filesStr += " <a href=?q=" + hash + BlockExplorer.get_Lang(langObj) + "&search=transactions>[" + hash + "]</a>";
                    } else {
                        filesStr += " [" + hash + "]";

                    }
                    filesStr += " - <a href ='../apidocuments/getFile?download=true&block="
                            + blockNo + "&seqNo=" + seqNo + "&name=" + fileName + "'><b>"
                            + Lang.T("Download", langObj) + "</b></a><br>";

                    filesCount++;

                }

                output.put("files", filesStr);
            }
        }
    }

    public Fun.Tuple3<Integer, String, ExData> decrypt(PublicKeyAccount account, Account recipient) {

        byte[] password;
        int pos = -1;
        if (account.equals((recipient))) {
            pos = recipients.length; // последний в Секретах
        } else {
            for (int i = 0; i < recipients.length; i++) {
                if (recipients[i].equals(recipient)) {
                    pos = i;
                    break;
                }
            }
        }

        if (pos < 0) {
            return new Fun.Tuple3<>(pos, "Address not found", null);
        }

        try {
            password = Controller.getInstance().decrypt(account, recipient, secrets[pos]);
            byte[] decryptedData = AEScrypto.aesDecrypt(encryptedData, password);
            Fun.Tuple2<JSONObject, HashMap> jsonAndFiles = parseJsonAndFiles(decryptedData, true);

            // это уже не зашифрованный - сбросим
            byte[] decryptedFlags = setEncryptedFlag(flags, false);
            return new Tuple3<>(pos, null, new ExData(decryptedFlags, exLink, exAction, title, recipientsFlags, recipients,
                    authorsFlags, authors, sourcesFlags, sources, tags, jsonAndFiles.a,
                    jsonAndFiles.b));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new Fun.Tuple3<>(pos, e.getMessage(), null);
        }

    }

    public JSONObject toJson() {

        JSONObject toJson = new JSONObject();

        toJson.put("title", title);

        if (exLink != null) {
            toJson.put("exLink", exLink.toJson(hasRecipients()));
        }

        if (exAction != null) {
            toJson.put("exPays", exAction.toJson());
        }

        if (hasRecipients()) {
            JSONArray recipients = new JSONArray();
            for (Account recipient : getRecipients()) {
                recipients.add(recipient.getAddress());
            }
            toJson.put("recipientsFlags", recipientsFlags);
            toJson.put("recipients", recipients);
        }

        if (hasAuthors()) {
            JSONArray authors = new JSONArray();
            for (ExLinkAuthor author : getAuthors()) {
                authors.add(author.toJson());
            }
            toJson.put("authorsFlags", authorsFlags);
            toJson.put("authors", authors);
        }

        if (hasSources()) {
            JSONArray sources = new JSONArray();
            for (ExLinkSource source : getSources()) {
                sources.add(source.toJson());
            }
            toJson.put("sourcesFlags", sourcesFlags);
            toJson.put("sources", sources);
        }

        if (tags != null && tags.length > 0) {
            toJson.put("tags", new String(tags, StandardCharsets.UTF_8));
        }

        if (isEncrypted()) {
            JSONArray secretsArray = new JSONArray();
            for (byte[] secret : getSecrets()) {
                secretsArray.add(Base58.encode(secret));
            }
            toJson.put("secretsFlags", secretsFlags);
            toJson.put("secrets", secretsArray);
            toJson.put("encryptedData64", Base64.getEncoder().encodeToString(encryptedData));

        } else if (json != null) {
            toJson.put("json", json);
        }

        return toJson;
    }

    public int isValid(RSignNote rNote) {

        int result;

        if (hashes != null) {
            for (Object hashObject : hashes.keySet()) {
                if (Base58.isExtraSymbols(hashObject.toString())) {
                    rNote.errorValue = hashObject.toString();
                    return Transaction.INVALID_DATA_FORMAT;
                }
            }
        }

        if (hasAuthors()) {
            if (!rNote.isCreatorPersonalized()) return Transaction.CREATOR_NOT_PERSONALIZED;

            for (ExLinkAuthor author : getAuthors()) {
                result = author.isValid(dcSet);
                if (result != Transaction.VALIDATE_OK) {
                    rNote.errorValue = "" + author.getRef();
                    return result;
                }
            }
        }

        if (hasSources()) {
            for (ExLinkSource source : getSources()) {
                result = source.isValid(dcSet);
                if (result != Transaction.VALIDATE_OK) {
                    rNote.errorValue = Transaction.viewDBRef(source.getRef());
                    return result;
                }
            }
        }

        if (tags != null && tags.length > 255) {
            return Transaction.INVALID_TAGS_LENGTH;
        }

        if (exLink != null) {
            Transaction parentTx = dcSet.getTransactionFinalMap().get(exLink.getRef());
            if (parentTx == null) {
                return Transaction.INVALID_BLOCK_TRANS_SEQ_ERROR;
            }

            // проверим запрет на создание Приложений если там ограничено подписание только списком получателей
            if (parentTx instanceof RSignNote && exLink instanceof ExLinkAppendix) {
                RSignNote parentRNote = (RSignNote) parentTx;
                parentRNote.parseDataV2WithoutFiles();
                if (parentRNote.isCanSignOnlyRecipients()
                        && !parentRNote.isInvolved(rNote.getCreator())) {
                    return Transaction.ACCOUNT_ACCSES_DENIED;
                }
            }
        }

        if (exAction != null) {
            result = exAction.isValid(rNote);
            if (result != Transaction.VALIDATE_OK)
                rNote.errorValue = exAction.errorValue;
            return result;
        }

        return Transaction.VALIDATE_OK;
    }

    public void process(Transaction transaction, Block block) {
        if (exLink != null)
            exLink.process(transaction);

        if (exAction != null)
            exAction.process(transaction, block);

        if (authors != null) {
            for (ExLinkAuthor author : authors) {
                author.process(transaction);
            }
        }

        if (sources != null) {
            for (ExLinkSource source : sources) {
                source.process(transaction);
            }
        }

        if (template != null) {
            DCSet dcSet = transaction.getDCSet();
            Account templateOwner = template.getMaker();

            BigDecimal royaltyFee = getRoyaltyFeeBG();
            if (royaltyFee != null && royaltyFee.signum() != 0) {
                templateOwner.changeBalance(dcSet, false, false, Transaction.FEE_KEY, royaltyFee, false, false, false);
                // учтем что получили бонусы
                templateOwner.changeCOMPUStatsBalances(dcSet, false, royaltyFee, Account.FEE_BALANCE_SIDE_TOTAL_EARNED);

                transaction.addCalculated(block, templateOwner, Transaction.FEE_KEY, royaltyFee,
                        "template royalty for %1".replace("%1", "" + templateKey));
            }

        }

    }

    public void orphan(Transaction transaction) {
        if (exLink != null)
            exLink.orphan(transaction);

        if (exAction != null)
            exAction.orphan(transaction);

        if (authors != null) {
            for (ExLinkAuthor author : authors) {
                author.orphan(transaction);
            }
        }

        if (sources != null) {
            for (ExLinkSource source : sources) {
                source.orphan(transaction);
            }
        }

        if (template != null) {
            DCSet dcSet = transaction.getDCSet();
            Account templateOwner = template.getMaker();

            BigDecimal royaltyFee = getRoyaltyFeeBG();
            if (royaltyFee != null && royaltyFee.signum() != 0) {
                templateOwner.changeBalance(dcSet, true, false, Transaction.FEE_KEY, royaltyFee, false, false, false);
                // учтем что получили бонусы
                templateOwner.changeCOMPUStatsBalances(dcSet, true, royaltyFee, Account.FEE_BALANCE_SIDE_TOTAL_EARNED);
            }

        }
    }

}
