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
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.exdata.exLink.ExLinkAuthor;
import org.erachain.core.exdata.exLink.ExLinkSource;
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

public class ExPays {

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

    private static final byte BALANCE_FLAG_MASK = 64;
    private static final byte TXTYPE_FLAG_MASK = 32;
    private static final byte AUTHORS_FLAG_MASK = 16;
    private static final byte SOURCES_FLAG_MASK = 8;
    private static final byte TAGS_FLAG_MASK = 4;

    public static final byte LINK_SIMPLE_TYPE = 0; // для выбора типа в ГУИ
    public static final byte LINK_APPENDIX_TYPE = 1; // дополнение / приложение к другому документу или Сущности
    public static final byte LINK_REPLY_COMMENT_TYPE = 2; // ответ всем на предыдущий документ - Ссылка для отслеживания
    public static final byte LINK_COMMENT_TYPE_FOR_VIEW = 3; // замечание без получетелей - используется только для ГУИ
    public static final byte LINK_SURELY_TYPE = 5; // гарантия / поручительство на долю

    public static final byte LINK_SOURCE_TYPE = 6; // как Источник
    public static final byte LINK_AUTHOR_TYPE = 7; // как Автор

    private static final Logger LOGGER = LoggerFactory.getLogger(ExPays.class);

    /**
     * 0 - version; 1 - flag 1;
     */
    private int flags;

    private final Long assetKey;
    private final int balancePos;
    private final boolean backward;
    private final BigDecimal amountMin;
    private final BigDecimal amountMax;

    private final int payMethod; // 0 - by Total, 1 - by Percent
    private final BigDecimal payMethodValue;

    private Long filterAssetKey;
    private int filterBalancePos;
    private int filterBalanceSide;
    private BigDecimal filterBalanceLessThen;
    private BigDecimal filterBalanceMoreThen;

    private Integer filterTXType;
    private Long filterTXStart;
    private Long filterTXEnd;

    private final Integer filterByPerson; // = gender or all


    public ExPays(int flags, Long assetKey, int balancePos, boolean backward, BigDecimal amountMin, BigDecimal amountMax,
                  int payMethod, BigDecimal payMethodValue, Long filterAssetKey, int filterBalancePos, int filterBalanceSide,
                  BigDecimal filterBalanceLessThen, BigDecimal filterBalanceMoreThen,
                  Integer filterTXType, Long filterTXStart, Long filterTXEnd, Integer filterByPerson) {
        this.flags = flags;

        this.assetKey = assetKey;

        this.balancePos = balancePos;
        this.backward = backward;

        this.amountMin = amountMin;
        this.amountMax = amountMax;
        this.payMethod = payMethod;
        this.payMethodValue = payMethodValue;

        if (filterAssetKey != null && filterAssetKey != 0) {
            this.flags |= BALANCE_FLAG_MASK;
            this.filterAssetKey = filterAssetKey;
            this.filterBalancePos = filterBalancePos;
            this.filterBalanceSide = filterBalanceSide;
            this.filterBalanceLessThen = filterBalanceLessThen;
            this.filterBalanceMoreThen = filterBalanceMoreThen;
        }

        if (filterTXType != null && filterTXType != 0) {
            this.flags |= TXTYPE_FLAG_MASK;
            this.filterTXType = filterTXType;
            this.filterTXStart = filterTXStart;
            this.filterTXEnd = filterTXEnd;
        }

        this.filterByPerson = filterByPerson;
    }

    public boolean hasAssetFilter() {
        return (this.flags & BALANCE_FLAG_MASK) != 0;
    }

    public boolean hasTXTypeFilter() {
        return (this.flags & TXTYPE_FLAG_MASK) != 0;
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


    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ExPays parse(
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
                return new ExPays(0, null, items[0], dataJson, null);

            case 1:
                text = new String(data, StandardCharsets.UTF_8);
                dataJson = (JSONObject) JSONValue.parseWithException(text);
                String title = dataJson.get("Title").toString();
                return new ExPays(1, null, title, dataJson, null);

            default:

                byte[] flags;
                int titleSize;
                ExLink exLink;
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

                    titleSize = Arrays.copyOfRange(data, position, position + 1)[0];
                    position++;

                }

                byte[] titleByte = Arrays.copyOfRange(data, position, position + titleSize);
                position += titleSize;
                title = new String(titleByte, StandardCharsets.UTF_8);

                if (onlyTitle) {
                    return new ExPays(version, null, title, null, null);
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
                        return new ExPays(flags, exLink, title, recipientsFlags, recipients, authorsFlags, authors, sourcesFlags, sources, tags, null, null);
                    } else {
                        // version 2.0 - 2.1
                        return new ExPays(version, exLink, title, null, null);
                    }
                } else {


                    if (isEncrypted) {
                        // version 3 - with SECRETS
                        return new ExPays(flags, exLink, title, recipientsFlags, recipients, authorsFlags, authors, sourcesFlags, sources, tags, secretsFlags, secrets,
                                Arrays.copyOfRange(data, position, data.length));
                    } else {

                        Fun.Tuple2<JSONObject, HashMap> jsonAndFiles = parseJsonAndFiles(Arrays.copyOfRange(data, position, data.length), andFiles);
                        return new ExPays(flags, exLink, title, recipientsFlags, recipients, authorsFlags, authors, sourcesFlags, sources, tags, jsonAndFiles.a,
                                jsonAndFiles.b);
                    }
                }

        }
    }

    public static byte[] make(ExLink exLink, PrivateKeyAccount creator, String title, boolean signCanOnlyRecipients, Account[] recipients,
                              ExLinkAuthor[] authors, ExLinkSource[] sources, String tagsStr, boolean isEncrypted,
                              TemplateCls template, HashMap<String, String> params_Template, boolean uniqueTemplate,
                              String message, boolean uniqueMessage,
                              HashMap<String, String> hashes_Map, boolean uniqueHashes,
                              Set<Tuple3<String, Boolean, byte[]>> files_Set, boolean uniqueFiles)
            throws Exception {

        JSONObject out_Map = new JSONObject();
        JSONObject params_Map = new JSONObject();
        JSONObject hashes_JSON = new JSONObject();

        // add template AND params
        if (template != null) {
            out_Map.put("TM", template.getKey());

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
            // случайный парольmake и его для всех шифруем
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

            return new ExPays(flags, exLink, title, recipientsFlags, recipients, authorsFlags, authors, sourcesFlags, sources, tags, (byte) 0, secrets, encryptedData).toByte();
        }

        return new ExPays(flags, exLink, title, recipientsFlags, recipients, authorsFlags, authors, sourcesFlags, sources, tags, new JSONObject(out_Map), filesMap).toByte();

    }

    /**
     * Version 2 maker for BlockExplorer
     */
    public void makeJSONforHTML(Map output,
                                int blockNo, int seqNo, JSONObject langObj) {

        if (title != null && !title.isEmpty()) {
            output.put("Label_title", Lang.getInstance().translateFromLangObj("Title", langObj));
            output.put("title", title);
        }

        if (exLink != null) {
            output.put("Label_LinkType", Lang.getInstance().translateFromLangObj("Link Type", langObj));
            output.put("exLink_Name", Lang.getInstance().translateFromLangObj(exLink.viewTypeName(hasRecipients()), langObj));
            output.put("exLink", exLink.makeJSONforHTML(hasRecipients(), langObj));
            output.put("Label_Parent", Lang.getInstance().translateFromLangObj("for # для", langObj));

        }

        if (isCanSignOnlyRecipients()) {
            output.put("Label_CanSignOnlyRecipients", Lang.getInstance().translateFromLangObj("To sign can only Recipients", langObj));
        }

        if (recipients != null && recipients.length > 0) {
            output.put("Label_recipients", Lang.getInstance().translateFromLangObj("Recipients", langObj));
            List<List<String>> recipientsOut = new ArrayList<>();
            for (Account recipient : recipients) {
                recipientsOut.add(Arrays.asList(recipient.getAddress(), recipient.getPersonAsString()));
            }
            output.put("recipients", recipientsOut);
        }

        if (authors != null && authors.length > 0) {
            output.put("Label_Authors", Lang.getInstance().translateFromLangObj("Authors", langObj));
            JSONArray authorsOut = new JSONArray();
            for (ExLinkAuthor author : authors) {
                authorsOut.add(author.makeJSONforHTML(langObj));
            }
            output.put("authors", authorsOut);
        }

        if (sources != null && sources.length > 0) {
            output.put("Label_Sources", Lang.getInstance().translateFromLangObj("Sources", langObj));
            JSONArray sourcesOut = new JSONArray();
            for (ExLinkSource source : sources) {
                sourcesOut.add(source.makeJSONforHTML(langObj));
            }
            output.put("sources", sourcesOut);
        }

        if (tags != null && tags.length > 0) {
            output.put("Label_Tags", Lang.getInstance().translateFromLangObj("Tags", langObj));
            output.put("tags", new String(tags, StandardCharsets.UTF_8));
        }

        if (isEncrypted()) {
            output.put("encrypted", Lang.getInstance().translateFromLangObj("Encrypted", langObj));
            return;

        } else {

            output.put("Label_template_hash", Lang.getInstance().translateFromLangObj("Template hash", langObj));
            output.put("Label_mess_hash", Lang.getInstance().translateFromLangObj("Text hash", langObj));
            output.put("Label_hashes", Lang.getInstance().translateFromLangObj("Hashes", langObj));
            output.put("Label_files", Lang.getInstance().translateFromLangObj("Files", langObj));

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
                            + Lang.getInstance().translateFromLangObj("Download", langObj) + "</b></a><br>";

                    filesCount++;

                }

                output.put("files", filesStr);
            }
        }
    }

    public JSONObject toJson() {

        JSONObject toJson = new JSONObject();

        toJson.put("title", title);

        if (exLink != null) {
            toJson.put("exLink", exLink.toJson(hasRecipients()));
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

    public int isValid(DCSet dcSet, RSignNote rNote) {

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

        return Transaction.VALIDATE_OK;
    }

    public void process(Transaction transaction) {
        if (exLink != null)
            exLink.process(transaction);

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

    }

    public void orphan(Transaction transaction) {
        if (exLink != null)
            exLink.orphan(transaction);

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
    }

}
