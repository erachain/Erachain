package org.erachain.core.item;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.Jsonable;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.blockexplorer.ExplorerJsonLine;
import org.erachain.core.blockexplorer.WebTransactionsHTML;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.RSetStatusToItem;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemMap;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.Iconable;
import org.erachain.gui.library.Library;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.Pair;
import org.erachain.webserver.PreviewMaker;
import org.erachain.webserver.WebResource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple6;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//import java.math.BigDecimal;
//import com.google.common.primitives.Longs;

public abstract class ItemCls implements Iconable, ExplorerJsonLine, Jsonable {

    static Logger LOGGER = LoggerFactory.getLogger(ItemCls.class.getName());

    public final static long START_KEY_OLD = 1L << 14;
    public static final long MIN_START_KEY_OLD = 1000L;

    public static final int ASSET_TYPE = 1;
    public static final int IMPRINT_TYPE = 2;
    public static final int TEMPLATE_TYPE = 3;
    public static final int PERSON_TYPE = 4;
    public static final int STATUS_TYPE = 5;
    public static final int UNION_TYPE = 6;
    public static final int STATEMENT_TYPE = 7;
    public static final int POLL_TYPE = 8;
    public static final int AUTHOR_TYPE = 41;

    public static final int MEDIA_TYPE_IMG = 0;
    public static final int MEDIA_TYPE_VIDEO = 1;
    public static final int MEDIA_TYPE_AUDIO = 2;
    public static final int MEDIA_TYPE_FRAME = 10; // POST

    protected static final int TYPE_LENGTH = 2;
    protected static final int MAKER_LENGTH = PublicKeyAccount.PUBLIC_KEY_LENGTH;
    protected static final int NAME_SIZE_LENGTH = 1;
    //public static final int MIN_NAME_LENGTH = 10;
    public static final int MAX_NAME_LENGTH = Transaction.MAX_TITLE_BYTES_LENGTH;
    protected static final int ICON_SIZE_LENGTH = 2;
    protected static final int IMAGE_SIZE_LENGTH = 4;
    protected static final int APP_DATA_LENGTH = Integer.BYTES;
    protected static final int DESCRIPTION_SIZE_LENGTH = 4;
    protected static final int REFERENCE_LENGTH = Transaction.SIGNATURE_LENGTH;
    protected static final int DBREF_LENGTH = Transaction.DBREF_LENGTH;
    protected static final int BASE_LENGTH = TYPE_LENGTH + MAKER_LENGTH
            + NAME_SIZE_LENGTH + ICON_SIZE_LENGTH + IMAGE_SIZE_LENGTH + DESCRIPTION_SIZE_LENGTH;

    public static final int MAX_ICON_LENGTH = (int) Math.pow(256, ICON_SIZE_LENGTH) - 1;
    public static final int MAX_IMAGE_LENGTH = BlockChain.MAIN_MODE ? 1500000 : Transaction.MAX_DATA_BYTES_LENGTH >> 1;

    protected static final int TIMESTAMP_LENGTH = Transaction.TIMESTAMP_LENGTH;

    protected byte[] typeBytes;
    protected PublicKeyAccount maker;

    /**
     * добавочные флаги по знаку из Размер Картинки
     */
    protected static final int APP_DATA_MASK = 1 << 31;
    /**
     * Флаг который показывает что есть еще данные вычисленные для бзы данных
     */
    protected static final int DB_DATA_MASK = 1 << 30;

    // appDATA [10]
    protected static final byte APP_DATA_ITEM_START_MASK = (byte) 64;
    protected static final byte APP_DATA_ITEM_STOP_MASK = (byte) 32;

    // ITEM_FLAGS[0]
    protected static final byte ITEM_HAS_URL_MASK = (byte) -128;
    protected static final long ITEM_FLAGS_HAS_TAGS = 1L << (Long.SIZE - 1);

    /**
     * 0-1 - байты переключателей для включения обработчиков супер-класса и суб-классов
     * Если супер класс включился - он берет 8 байт на свои флаги и 2 байта на свойства Иконки И картинки
     */
    protected byte[] appData;
    protected long flags;

    protected String name;
    protected String description;
    protected long key = 0;
    /**
     * this is signature of issued record
     */
    protected byte[] reference = null;
    protected long dbRef;
    protected byte[] icon;
    protected boolean iconAsURL;
    protected int iconType;
    protected byte[] image;
    protected boolean imageAsURL;
    protected int imageType;

    protected Long startDate;
    protected Long stopDate;

    protected String tags;

    public Transaction referenceTx = null;

    protected int parsedPos;

    // нужно для AssetUniqueSeries
    public ItemCls(byte[] typeBytes) {
        this.typeBytes = typeBytes;
    }

    public ItemCls(byte[] data, boolean includeReference, int forDeal) throws Exception {
        parseHead(data, includeReference, forDeal);
    }

    public ItemCls(byte[] typeBytes, byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description) {
        this.typeBytes = typeBytes;
        this.appData = appData;
        this.maker = maker;
        this.name = name.trim();
        this.description = description;
        this.icon = icon == null ? new byte[0] : icon;
        this.image = image == null ? new byte[0] : image;

        parseAppData();

    }

    public ItemCls(int type, byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description) {
        this(new byte[TYPE_LENGTH], appData, maker, name, icon, image, description);
        this.typeBytes[0] = (byte) type;
    }

    /**
     * load fields after get or delete in DB
     * У некоторых типов сущностей (актив.уникальный.серия AssetUniqueSeries) нужно подгружать данные из базы.
     * Так же при создании вторичных ключей надо вызывать это.
     */
    public void loadExtData(ItemMap itemMap) {
    }

    public void parseHead(byte[] data, boolean includeReference, int forDeal) throws Exception {

        // READ TYPE
        typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        parsedPos = TYPE_LENGTH;

        //READ CREATOR
        byte[] makerBytes = Arrays.copyOfRange(data, parsedPos, parsedPos + MAKER_LENGTH);
        maker = new PublicKeyAccount(makerBytes);
        parsedPos += MAKER_LENGTH;

        //READ FULL NAME
        int fullNameLength = Byte.toUnsignedInt(data[parsedPos]);
        parsedPos++;

        // !!! Проверяем по максимуму протокола - по супер классу ItemCls. Локальные ограничения в isValid тут
        if (fullNameLength < 1 || fullNameLength > ItemCls.MAX_NAME_LENGTH) {
            throw new Exception("Invalid full name length");
        }

        byte[] fullNameBytes = Arrays.copyOfRange(data, parsedPos, parsedPos + fullNameLength);
        name = new String(fullNameBytes, StandardCharsets.UTF_8);
        parsedPos += fullNameLength;

        //READ ICON
        byte[] iconLengthBytes = Arrays.copyOfRange(data, parsedPos, parsedPos + ICON_SIZE_LENGTH);
        int iconLength = Ints.fromBytes((byte) 0, (byte) 0, iconLengthBytes[0], iconLengthBytes[1]);
        parsedPos += ICON_SIZE_LENGTH;

        // !!! Проверяем по максимуму протокола - по супер классу ItemCls. Локальные ограничения в isValid тут
        if (iconLength < 0 || iconLength > ItemCls.MAX_ICON_LENGTH) {
            throw new Exception("Invalid icon length - " + iconLength);
        }

        icon = Arrays.copyOfRange(data, parsedPos, parsedPos + iconLength);
        parsedPos += iconLength;

        //READ IMAGE
        byte[] imageLengthBytes = Arrays.copyOfRange(data, parsedPos, parsedPos + IMAGE_SIZE_LENGTH);
        int imageLength = Ints.fromByteArray(imageLengthBytes);
        parsedPos += IMAGE_SIZE_LENGTH;

        // TEST APP DATA
        boolean hasAppData = (imageLength & APP_DATA_MASK) != 0;
        if (hasAppData)
            // RESET LEN
            imageLength &= ~APP_DATA_MASK;

        // !!! Проверяем по максимуму протокола - по супер классу ItemCls. Локальные ограничения в isValid тут
        if (imageLength < 0 || imageLength > ItemCls.MAX_IMAGE_LENGTH) {
            throw new Exception("Invalid image length " + imageLength);
        }

        image = Arrays.copyOfRange(data, parsedPos, parsedPos + imageLength);
        parsedPos += imageLength;

        if (hasAppData) {
            // READ APP DATA
            int appDataLen = Ints.fromByteArray(Arrays.copyOfRange(data, parsedPos, parsedPos + APP_DATA_LENGTH));
            parsedPos += APP_DATA_LENGTH;

            appData = Arrays.copyOfRange(data, parsedPos, parsedPos + appDataLen);
            parsedPos += appDataLen;

        } else {
            appData = null;
        }

        //READ DESCRIPTION
        byte[] descriptionLengthBytes = Arrays.copyOfRange(data, parsedPos, parsedPos + DESCRIPTION_SIZE_LENGTH);
        int descriptionLength = Ints.fromByteArray(descriptionLengthBytes);
        parsedPos += DESCRIPTION_SIZE_LENGTH;

        if (descriptionLength < 0 || descriptionLength > BlockChain.MAX_REC_DATA_BYTES) {
            throw new Exception("Invalid description length");
        }

        byte[] descriptionBytes = Arrays.copyOfRange(data, parsedPos, parsedPos + descriptionLength);
        description = new String(descriptionBytes, StandardCharsets.UTF_8);
        parsedPos += descriptionLength;

        if (includeReference) {
            //READ REFERENCE
            reference = Arrays.copyOfRange(data, parsedPos, parsedPos + REFERENCE_LENGTH);
            parsedPos += REFERENCE_LENGTH;

            //READ SEQNO
            byte[] dbRefBytes = Arrays.copyOfRange(data, parsedPos, parsedPos + DBREF_LENGTH);
            dbRef = Longs.fromByteArray(dbRefBytes);
            parsedPos += DBREF_LENGTH;
        }

    }

    /**
     * Должно в конце каждого класса вызываться для распарсивания ДопДанных
     *
     * @return
     */
    protected int parseAppData() {

        if (appData == null)
            return 0;

        // пропустим сразу 2 первых байта - там включатели обработчиков
        int pos = 2;

        // parse ITEM APP DATA
        flags = Longs.fromByteArray(Arrays.copyOfRange(appData, pos, pos + Long.BYTES));
        pos += Long.BYTES;

        byte iconTypeByte = appData[pos++];
        if (iconTypeByte < 0) {
            iconAsURL = true;
            iconTypeByte &= ~ITEM_HAS_URL_MASK;
        }
        iconType = iconTypeByte;

        byte imageTypeByte = appData[pos++];
        if (imageTypeByte < 0) {
            imageAsURL = true;
            imageTypeByte &= ~ITEM_HAS_URL_MASK;
        }
        imageType = imageTypeByte;

        if ((appData[0] & APP_DATA_ITEM_START_MASK) != 0) {
            startDate = Longs.fromByteArray(Arrays.copyOfRange(appData, pos, pos + Long.BYTES));
            pos += Long.BYTES;
        }
        if ((appData[0] & APP_DATA_ITEM_STOP_MASK) != 0) {
            stopDate = Longs.fromByteArray(Arrays.copyOfRange(appData, pos, pos + Long.BYTES));
            pos += Long.BYTES;
        }
        if ((flags & ITEM_FLAGS_HAS_TAGS) != 0) {
            int len = Byte.toUnsignedInt(appData[pos++]);
            tags = new String(Arrays.copyOfRange(appData, pos, pos + len), StandardCharsets.UTF_8);
            pos += len;
        }

        return pos;
    }

    public static byte[] makeAppData(long flags, boolean iconAsURL, int iconType, boolean imageAsURL, int imageType, Long startDate, Long stopDate, String tags) {
        if (flags != 0 || iconAsURL || imageAsURL || iconType != 0 || imageType != 0
                || startDate != null || stopDate != null
                || tags != null && !tags.isEmpty()) {
            byte[] appData = new byte[12];

            // 2 байта пропустим, потом флаги
            System.arraycopy(Longs.toByteArray(flags), 0, appData, 2, Long.BYTES);

            // байт по Иконке
            appData[10] = (byte) iconType;
            if (iconAsURL)
                appData[10] |= ITEM_HAS_URL_MASK;
            // байт по Картинке
            appData[11] = (byte) imageType;
            if (imageAsURL)
                appData[11] |= ITEM_HAS_URL_MASK;

            if (startDate != null) {
                appData[0] |= APP_DATA_ITEM_START_MASK;
                appData = Bytes.concat(appData, Longs.toByteArray(startDate));
                if (stopDate != null && stopDate < startDate) {
                    stopDate = null;
                }
            }
            if (stopDate != null) {
                appData[0] |= APP_DATA_ITEM_STOP_MASK;
                appData = Bytes.concat(appData, Longs.toByteArray(stopDate));
            }

            if (tags != null && !tags.isEmpty()) {
                flags |= ITEM_FLAGS_HAS_TAGS;
                byte[] tagsBytes = tags.getBytes(StandardCharsets.UTF_8);
                appData = Bytes.concat(appData, new byte[]{(byte) tagsBytes.length});
                appData = Bytes.concat(appData, tagsBytes);
            }

            // Теперь Флаги собранные - (2 байта пропустим)
            System.arraycopy(Longs.toByteArray(flags), 0, appData, 2, Long.BYTES);

            return appData;

        } else {
            return null;
        }
    }

    public static byte[] makeEmptyAppData() {
        return new byte[12];
    }

    public static Pair<Integer, Long> resolveDateFromStr(String str, Long defaultVol) {
        if (str.length() == 0) return new Pair<Integer, Long>(0, defaultVol);
        else if (str.length() == 1) {
            if (str == "+")
                return new Pair<Integer, Long>(0, Long.MAX_VALUE);
            else if (str == "-")
                return new Pair<Integer, Long>(0, Long.MIN_VALUE);
            else
                return new Pair<Integer, Long>(0, defaultVol);
        } else {
            try {
                Long date = Long.parseLong(str);
                return new Pair<Integer, Long>(0, date);
            } catch (Exception e) {
                return new Pair<Integer, Long>(-1, 0L);
            }
        }
    }

    public static Pair<Integer, Integer> resolveEndDayFromStr(String str, Integer defaultVol) {
        if (str.length() == 0) return new Pair<Integer, Integer>(0, defaultVol);
        else if (str.length() == 1) {
            if (str == "+")
                return new Pair<Integer, Integer>(0, Integer.MAX_VALUE);
            else if (str == "-")
                return new Pair<Integer, Integer>(0, Integer.MIN_VALUE);
            else
                return new Pair<Integer, Integer>(0, defaultVol);
        } else {
            try {
                Integer date = Integer.parseInt(str);
                return new Pair<Integer, Integer>(0, date);
            } catch (Exception e) {
                return new Pair<Integer, Integer>(-1, 0);
            }
        }
    }

    public static ItemCls getItem(DCSet db, int type, long key) {
        //return Controller.getInstance().getItem(db, type, key);
        return db.getItem_Map(type).get(key);
    }

    public int getIconMAXLength() {
        return this.MAX_ICON_LENGTH;
    }

    public int getImageMAXLength() {
        return this.MAX_IMAGE_LENGTH;
    }

    public String errorValue;

    public int isValid() {

        // TEST ALL BYTES for database FIELD
        if (name.getBytes(StandardCharsets.UTF_8).length > ItemCls.MAX_NAME_LENGTH) {
            errorValue = "" + name.getBytes(StandardCharsets.UTF_8).length + " > " + ItemCls.MAX_NAME_LENGTH;
            return Transaction.INVALID_NAME_LENGTH_MAX;
        }

        if (icon != null && icon.length > getIconMAXLength()) {
            errorValue = "" + icon.length + " > " + getIconMAXLength();
            return Transaction.INVALID_ICON_LENGTH_MAX;
        } else if (image != null && image.length > getImageMAXLength()) {
            errorValue = "" + image.length + " > " + getImageMAXLength();
            return Transaction.INVALID_IMAGE_LENGTH_MAX;
        }

        if (tags != null && tags.getBytes(StandardCharsets.UTF_8).length > 255) {
            return Transaction.INVALID_TAGS_LENGTH_MAX;
        }

        //CHECK DESCRIPTION LENGTH
        int descriptionLength = description == null ? 0 : description.getBytes(StandardCharsets.UTF_8).length;
        if (descriptionLength > Transaction.MAX_DATA_BYTES_LENGTH) {
            errorValue = "" + descriptionLength + " > " + Transaction.MAX_DATA_BYTES_LENGTH;
            return Transaction.INVALID_DESCRIPTION_LENGTH_MAX;
        }

        if (iconAsURL && (icon == null || icon.length > 512)) {
            errorValue = "icon";
            return Transaction.INVALID_URL_LENGTH;
        }

        if (imageAsURL && (image == null || image.length > 512)) {
            errorValue = "image";
            return Transaction.INVALID_URL_LENGTH;
        }

        if (iconType == MEDIA_TYPE_AUDIO) {
            errorValue = "!=audio";
            return Transaction.INVALID_ICON_TYPE;
        }

        return Transaction.VALIDATE_OK;
    }

    public abstract int getItemType();

    public abstract long START_KEY();

    public abstract long MIN_START_KEY();

    public static long getStartKey(int itemType, long startKey, long minStartKey) {
        if (!BlockChain.CLONE_MODE)
            return minStartKey;

        long startKeyUser = BlockChain.startKeys[itemType];

        if (startKeyUser == 0) {
            return startKey;
        } else if (startKeyUser < minStartKey) {
            return (BlockChain.startKeys[itemType] = minStartKey);
        }
        return startKeyUser;
    }

    public long getStartKey() {
        return getStartKey(getItemType(), START_KEY(), MIN_START_KEY());
    }

    public abstract int getMinNameLen();

    public abstract String getItemTypeName();
    //public abstract FavoriteItemMap getDBFavoriteMap();

    public abstract String getItemSubType();

    public byte[] getAppData() {
        return appData;
    }

    public abstract ItemMap getDBMap(DCSet db);

    public byte[] getTypeBytes() {
        return this.typeBytes;
    }

    public byte getProps() {
        return this.typeBytes[1];
    }

    public void setProps(byte props) {
        this.typeBytes[1] = props;
    }

    public PublicKeyAccount getMaker() {
        return this.maker;
    }

    public String getName() {
        return this.name;
    }

    public String getShortName() {
        String[] words = this.viewName().split(" ");
        String result = "";
        for (String word : words) {
            if (word.length() > 6) {
                result += word.substring(0, 5) + ".";
            } else {
                result += word + " ";
            }
            if (result.length() > 25)
                break;
        }

        return result.trim();

    }

    public String getTickerName() {
        String[] words = getName().split(" ");
        String name = words[0].trim();
        if (name.length() > 6) {
            name = name.substring(0, 6);
        }
        return name;

    }

    public String getTagsSelf() {
        return tags;
    }

    /**
     * доп метки для поиска данной сущности или её типа
     *
     * @return
     */
    public String[] getTags() {
        if (tags != null && !tags.isEmpty()) {
            String[] array = tags.toLowerCase().split(",");
            for (int i = 0; i < array.length; i++) {
                array[i] = array[i].trim();
            }
            return array;
        }
        return null;
    }

    public String getTagsStr() {
        return tags;
    }

    public long getFlags() {
        return flags;
    }

    public byte[] getIcon() {
        return this.icon;
    }

    public int getIconType() {
        return iconType;
    }

    public static String viewMediaType(int iconType) {
        switch (iconType) {
            case MEDIA_TYPE_IMG:
                return "img";
            case MEDIA_TYPE_VIDEO:
                return "video";
            case MEDIA_TYPE_AUDIO:
                return "audio";
            case MEDIA_TYPE_FRAME:
                return "frame";
            default:
                return "unknown";
        }
    }

    public static MediaType getMediaType(int mediaType, byte[] media) {
        if (mediaType == ItemCls.MEDIA_TYPE_IMG) {
            if (media.length > 20) {
                byte[] header = new byte[20];
                System.arraycopy(media, 0, header, 0, 20);
                String typeName = new String(header).trim();
                if (typeName.contains("PNG")) {
                    typeName = "png";
                } else if (typeName.contains("GIF")) {
                    typeName = "gif";
                } else {
                    typeName = "jpeg";
                }
                return new MediaType("image", typeName);
            } else {
                return WebResource.TYPE_JPEG;
            }

        } else if (mediaType == ItemCls.MEDIA_TYPE_VIDEO) {
            return WebResource.TYPE_VIDEO;
        } else if (mediaType == ItemCls.MEDIA_TYPE_AUDIO) {
            return WebResource.TYPE_AUDIO;
        }
        return null;
    }

    public String getImageTypeExt() {
        if (imageType == ItemCls.MEDIA_TYPE_VIDEO) {
            return "mp4";
        } else if (imageType == ItemCls.MEDIA_TYPE_AUDIO) {
            return "mp3";
        }
        if (image.length > 20) {
            byte[] header = new byte[20];
            System.arraycopy(image, 0, header, 0, 20);
            String typeName = new String(header).trim();
            if (typeName.contains("PNG")) {
                return "png";
            } else if (typeName.contains("GIF")) {
                return "gif";
            }
        }
        return "jpg";
    }

    public MediaType getImageMediaType() {
        return getMediaType(imageType, image);
    }

    public MediaType getIconMediaType() {
        return getMediaType(iconType, icon);
    }

    public boolean hasIconURL() {
        return iconAsURL;
    }

    public String getIconURL() {
        if (iconAsURL) {
            // внешняя ссылка - обработаем ее
            return new String(getIcon(), StandardCharsets.UTF_8);
        } else if (getIcon() != null && getIcon().length > 0) {
            return "/api" + getItemTypeName() + "/icon/" + key;
        }

        return null;
    }

    public byte[] getImage() {
        return this.image;
    }

    public int getImageType() {
        return imageType;
    }

    public String getImageTypeName() {
        return viewMediaType(imageType);
    }

    public boolean hasImageURL() {
        return imageAsURL;
    }

    public boolean hasStartDate() {
        return startDate != null;
    }

    public boolean hasStopDate() {
        return stopDate != null;
    }

    public long getStartDate() {
        return startDate;
    }

    public long getStopDate() {
        return stopDate;
    }

    public String viewStartDate() {
        return DateTimeFormat.timestamptoString(this.startDate, Settings.getInstance().getBirthTimeFormat(), "UTC");
    }

    public String viewStopDate() {
        return DateTimeFormat.timestamptoString(this.stopDate, Settings.getInstance().getBirthTimeFormat(), "UTC");
    }

    public boolean isActive(long timestamp) {
        if (startDate != null && timestamp < startDate) {
            errorValue = "< " + viewStartDate();
            return false;
        }
        if (stopDate != null && timestamp >= stopDate) {
            errorValue = ">= " + viewStopDate();
            return false;
        }
        return true;
    }

    public String getImageURL() {
        if (imageAsURL) {
            // внешняя ссылка - обработаем ее
            return new String(getImage(), StandardCharsets.UTF_8);
        } else if (getImage() != null && getImage().length > 0) {
            return "/api" + getItemTypeName() + "/image/" + key;
        }

        return null;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public long getKey() {
        return this.key;
    }

    public long resolveKey(DCSet db) {

        if (this.reference == null || BlockChain.isWiped(this.reference))
            return 0L;

        return this.key;
    }

    /**
     * При поиске будет в нижний регистр перевернуто. Поэтому тут нельзя использовать маленькие буквы/
     * TT - тип трнзакции - используется в Transaction.tags
     *
     * @param itemType
     * @return
     */
    public static String getItemTypeAndKey(int itemType) {
        switch (itemType) {
            case ItemCls.ASSET_TYPE:
                return "A";
            case ItemCls.IMPRINT_TYPE:
                return "I";
            case ItemCls.PERSON_TYPE:
                return "P";
            case ItemCls.POLL_TYPE:
                return "V"; // Vote
            case ItemCls.UNION_TYPE:
                return "U";
            case ItemCls.STATEMENT_TYPE:
                return "N"; // NOTE
            case ItemCls.STATUS_TYPE:
                return "S";
            case ItemCls.TEMPLATE_TYPE:
                return "T"; // TEMPLATE
            case ItemCls.AUTHOR_TYPE:
                return "PA"; // for quick search
            default:
                return "x";

        }
    }

    public static String getItemTypeAndKey(int itemType, Object itemKey) {
        return "@" + getItemTypeAndKey(itemType) + itemKey.toString();
    }

    public static String getItemTypeAndTag(int itemType, Object tag) {
        return "@" + getItemTypeAndKey(itemType) + tag.toString();
    }

    public String getItemTypeAndKey() {
        return getItemTypeAndKey(getItemType(), key);
    }

    public static String getItemTypeName(int itemType) {
        switch (itemType) {
            case ItemCls.ASSET_TYPE:
                return "ASSET";
            case ItemCls.IMPRINT_TYPE:
                return "IMPRINT";
            case ItemCls.PERSON_TYPE:
                return "PERSON";
            case ItemCls.POLL_TYPE:
                return "POLL"; // Opinion
            case ItemCls.UNION_TYPE:
                return "UNION";
            case ItemCls.STATEMENT_TYPE:
                return "STATEMENT"; // TeXT
            case ItemCls.STATUS_TYPE:
                return "STATUS";
            case ItemCls.TEMPLATE_TYPE:
                return "TEMPLATE"; // TeMPLATE
            default:
                return null;

        }
    }

    public static int getItemTypeByName(String itemTypeName) {
        String type = itemTypeName.toLowerCase();

        if (type.startsWith("asset")) {
            return ItemCls.ASSET_TYPE;
        } else if (type.startsWith("imprint")) {
            return ItemCls.IMPRINT_TYPE;
        } else if (type.startsWith("person")) {
            return ItemCls.PERSON_TYPE;
        } else if (type.startsWith("poll")) {
            return ItemCls.POLL_TYPE;
        } else if (type.startsWith("statement")) {
            return ItemCls.STATEMENT_TYPE;
        } else if (type.startsWith("status")) {
            return ItemCls.STATUS_TYPE;
        } else if (type.startsWith("template")) {
            return ItemCls.TEMPLATE_TYPE;
        } else if (type.startsWith("union")) {
            return ItemCls.UNION_TYPE;
        }

        return -1;

    }

    public long getHeight(DCSet db) {
        //INSERT INTO DATABASE
        ItemMap dbMap = this.getDBMap(db);
        long key = dbMap.getLastKey();
        return key;
    }

    public void resetKey() {
        this.key = 0;
    }

    public String viewName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String viewDescription() {
        return this.description;
    }

    public byte[] getReference() {
        return this.reference;
    }

    /**
     * Тут может быть переопределена повторно - если транзакция валялась в неподтвержденных и была уже проверена
     * ранее. Это не страшно
     *
     * @param signature
     * @param dbRef
     */
    public void setReference(byte[] signature, long dbRef) {
        this.reference = signature;
        this.dbRef = dbRef;
    }

    public Transaction getIssueTransaction(DCSet dcSet) {
        return dcSet.getTransactionFinalMap().get(this.reference);
    }

    public boolean isConfirmed() {
        return isConfirmed(DCSet.getInstance());
    }

    public boolean isConfirmed(DCSet db) {
        return key != 0;
    }

    public int getConfirmations(DCSet db) {

        // CHECK IF IN UNCONFIRMED TRANSACTION

        if (!isConfirmed())
            return 0;

        Long dbRef = db.getTransactionFinalMapSigns().get(this.reference);
        if (dbRef == null)
            return 0;

        int height = Transaction.parseHeightDBRef(dbRef);

        return 1 + db.getBlockMap().size() - height;

    }

    public boolean isFavorite() {
        return Controller.getInstance().isItemFavorite(this);
    }

    @Override
    public int hashCode() {
        return (reference == null ? 0 : Ints.fromByteArray(reference));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemCls) {
            ItemCls item = (ItemCls) obj;
            if (this.reference != null && item.reference != null)
                return Arrays.equals(this.reference, item.reference);
        }
        return false;
    }

    // forMakerSign - use only DATA needed for making signature
    public byte[] toBytes(int forDeal, boolean includeReference, boolean forMakerSign) {

        byte[] data = new byte[0];
        boolean useAll = !forMakerSign;

        if (useAll) {
            //WRITE TYPE
            data = Bytes.concat(data, this.typeBytes);
        }

        if (useAll) {
            //WRITE MAKER
            try {
                data = Bytes.concat(data, this.maker.getPublicKey());
            } catch (Exception e) {
                //DECODE EXCEPTION
            }
        }

        byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
        if (useAll) {
            //WRITE NAME SIZE
            data = Bytes.concat(data, new byte[]{(byte) nameBytes.length});
        }

        //WRITE NAME
        data = Bytes.concat(data, nameBytes);

        boolean hasAppData = appData != null && appData.length > 0;

        if (useAll) {
            //WRITE ICON SIZE - 2 bytes = 64kB max
            byte[] iconLengthBytes = Shorts.toByteArray((short) this.icon.length);
            data = Bytes.concat(data, iconLengthBytes);

            //WRITE ICON
            data = Bytes.concat(data, this.icon);
        }

        if (useAll) {
            //WRITE IMAGE SIZE
            // !!! this.image не может быть равно NULL !!! - чтобы флаги собирать всегда на до Данные
            int imageLength = this.image.length;

            // если Appdata exist - поднимем флаг
            if (hasAppData)
                imageLength |= APP_DATA_MASK;

            if (false && forDeal == Transaction.FOR_DB_RECORD) {
                imageLength |= DB_DATA_MASK;
            }

            byte[] imageLengthBytes = Ints.toByteArray(imageLength);
            data = Bytes.concat(data, imageLengthBytes);
        }

        //WRITE IMAGE
        data = Bytes.concat(data, this.image);

        /// APP DATA
        if (useAll && hasAppData) {
            // WRITE APP DATA LENGTH
            data = Bytes.concat(data, Ints.toByteArray(this.appData.length));
            data = Bytes.concat(data, this.appData);

        }

        byte[] descriptionBytes = this.description.getBytes(StandardCharsets.UTF_8);
        if (useAll) {
            //WRITE DESCRIPTION SIZE
            int descriptionLength = descriptionBytes.length;
            byte[] descriptionLengthBytes = Ints.toByteArray(descriptionLength);
            data = Bytes.concat(data, descriptionLengthBytes);
        }

        //WRITE DESCRIPTION
        data = Bytes.concat(data, descriptionBytes);

        if (useAll && includeReference) {
            //WRITE REFERENCE
            data = Bytes.concat(data, this.reference);
            data = Bytes.concat(data, Longs.toByteArray(this.dbRef));
        }

        return data;
    }

    public int getDataLength(boolean includeReference) {
        return BASE_LENGTH
                + (appData == null || appData.length == 0 ? 0 : APP_DATA_LENGTH + appData.length)
                + this.name.getBytes(StandardCharsets.UTF_8).length
                + this.icon.length
                + this.image.length
                + this.description.getBytes(StandardCharsets.UTF_8).length
                + (includeReference ? REFERENCE_LENGTH + DBREF_LENGTH : 0);
    }

    //OTHER

    public String toString(DCSet db) {
        long key = this.getKey();
        return (key < getStartKey() ? "" : "[" + key + "] ") + this.viewName();
    }

    public String toString(DCSet db, byte[] data) {
        String str = this.toString(db);

        Tuple6<Long, Long, byte[], byte[], Long, byte[]> tuple = RSetStatusToItem.unpackData(data);

        if (str.contains("%1") && tuple.a != null)
            str = str.replace("%1", tuple.a.toString());
        else
            str = str.replace("%1", "");

        if (str.contains("%2") && tuple.b != null)
            str = str.replace("%2", tuple.b.toString());
        else
            str = str.replace("%2", "");

        if (str.contains("%3") && tuple.c != null)
            str = str.replace("%3", new String(tuple.c, StandardCharsets.UTF_8));
        else
            str = str.replace("%3", "");

        if (str.contains("%4") && tuple.d != null)
            str = str.replace("%4", new String(tuple.d, StandardCharsets.UTF_8));
        else
            str = str.replace("%4", "");

        if (str.contains("%D") && tuple.f != null)
            str = str.replace("%D", new String(new String(tuple.f, StandardCharsets.UTF_8)));
        else
            str = str.replace("%D", "");

        return str;
    }

    public String toStringNoKey(byte[] data) {
        String str = name;

        Tuple6<Long, Long, byte[], byte[], Long, byte[]> tuple = RSetStatusToItem.unpackData(data);

        if (str.contains("%1") && tuple.a != null)
            str = str.replace("%1", tuple.a.toString());
        if (str.contains("%2") && tuple.b != null)
            str = str.replace("%2", tuple.b.toString());
        if (str.contains("%3") && tuple.c != null)
            str = str.replace("%3", new String(tuple.c, StandardCharsets.UTF_8));
        if (str.contains("%4") && tuple.d != null)
            str = str.replace("%4", new String(tuple.d, StandardCharsets.UTF_8));
        if (str.contains("%D") && tuple.f != null)
            str = str.replace("%D", new String(new String(tuple.f, StandardCharsets.UTF_8)));

        return str;
    }

    @Override
    public String toString() {
        return toString(DCSet.getInstance());
    }

    public String getShort(DCSet db) {
        return this.viewName().substring(0, Math.min(this.viewName().length(), 30));
    }

    public String getShort() {
        return getShort(DCSet.getInstance());
    }

    public JSONObject toJsonLite(boolean withIcon, boolean showPerson) {

        JSONObject itemJSON = new JSONObject();

        itemJSON.put("item_type", this.getItemTypeName());
        itemJSON.put("itemType", this.getItemTypeName());
        itemJSON.put("startKey", this.getStartKey());

        itemJSON.put("key", this.getKey());
        itemJSON.put("name", this.name);
        if (tags != null && !tags.isEmpty())
            itemJSON.put("tags", this.tags);

        itemJSON.put("iconType", getIconType());
        itemJSON.put("iconTypeName", viewMediaType(iconType));

        String iconURL = getIconURL();
        if (iconURL != null) {
            itemJSON.put("iconURL", getIconURL());
            itemJSON.put("iconMediaType", getIconMediaType().toString());
        }

        itemJSON.put("maker", this.maker.getAddress());
        if (showPerson) {
            Fun.Tuple2<Integer, PersonCls> person = this.maker.getPerson();
            if (person != null) {
                itemJSON.put("makerPersonKey", person.b.getKey());
                itemJSON.put("makerPersonName", person.b.getName());
            }
        }

        return itemJSON;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJson() {

        JSONObject itemJSON = toJsonLite(false, false);

        itemJSON.put("charKey", getItemTypeAndKey());

        // ADD DATA
        itemJSON.put("itemCharKey", getItemTypeAndKey());
        //itemJSON.put("itemType", this.getItemTypeName());
        itemJSON.put("item_type_sub", this.getItemSubType());
        //itemJSON.put("itemTypeSub", this.getItemSubType());
        itemJSON.put("type0", Byte.toUnsignedInt(this.typeBytes[0]));
        itemJSON.put("type1", Byte.toUnsignedInt(this.typeBytes[1]));
        itemJSON.put("description", viewDescription());
        itemJSON.put("maker", this.maker.getAddress());
        itemJSON.put("creator", this.maker.getAddress()); // @Deprecated
        itemJSON.put("maker_public_key", this.maker.getBase58());
        itemJSON.put("maker_publickey", this.maker.getBase58());
        //itemJSON.put("makerPubkey", this.maker.getBase58());
        itemJSON.put("isConfirmed", this.isConfirmed());
        itemJSON.put("is_confirmed", this.isConfirmed());
        itemJSON.put("reference", Base58.encode(this.reference));
        itemJSON.put("tx_signature", Base58.encode(this.reference));

        Long txSeqNo = DCSet.getInstance().getTransactionFinalMapSigns().get(getReference());
        if (txSeqNo != null) {
            // если транзакция еще не подтверждена - чтобы ошибок не было при отображении в блокэксплорере
            itemJSON.put("tx_seqNo", Transaction.viewDBRef(txSeqNo));
            if (referenceTx == null)
                referenceTx = DCSet.getInstance().getTransactionFinalMap().get(txSeqNo);

            if (referenceTx != null) {
                PublicKeyAccount creator = referenceTx.getCreator();
                if (creator == null) {
                    itemJSON.put("tx_creator", "GENESIS");
                    itemJSON.put("tx_creator_pubkey", "GENESIS");
                } else {
                    itemJSON.put("tx_creator", creator.getAddress());
                    itemJSON.put("tx_creator_pubkey", creator.getBase58());
                }
                itemJSON.put("tx_timestamp", referenceTx.getTimestamp());
                itemJSON.put("block_timestamp", Controller.getInstance().blockChain.getTimestamp(referenceTx.getBlockHeight()));
            }
        }

        itemJSON.put("iconType", getIconType());
        itemJSON.put("iconTypeName", viewMediaType(iconType));

        String iconURL = getIconURL();
        if (iconURL != null) {
            itemJSON.put("iconURL", getIconURL());
            itemJSON.put("iconMediaType", getIconMediaType().toString());
        }

        itemJSON.put("imageType", getImageType());
        itemJSON.put("imageTypeName", viewMediaType(imageType));

        String imageURL = getImageURL();
        if (imageURL != null) {
            itemJSON.put("imageURL", imageURL);
            itemJSON.put("imageMediaType", getImageMediaType().toString());
            itemJSON.put("imagePreviewMediaType", PreviewMaker.getPreviewType(this).toString());
        }

        if (startDate != null)
            itemJSON.put("startDate", startDate);
        if (stopDate != null)
            itemJSON.put("stopDate", stopDate);

        return itemJSON;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJsonData() {

        JSONObject itemJSON = new JSONObject();

        itemJSON.put("iconType", getIconType());
        itemJSON.put("iconTypeName", viewMediaType(iconType));

        // ADD DATA
        if (hasIconURL()) {
            itemJSON.put("iconURL", getIconURL());
        } else {
            if (getIcon() != null && getIcon().length > 0)
                itemJSON.put("icon", java.util.Base64.getEncoder().encodeToString(this.getIcon()));
        }

        itemJSON.put("imageType", getImageType());
        itemJSON.put("imageTypeName", viewMediaType(imageType));

        if (hasImageURL()) {
            itemJSON.put("imageURL", getImageURL());
        } else {
            if (getImage() != null && getImage().length > 0)
                itemJSON.put("image", java.util.Base64.getEncoder().encodeToString(this.getImage()));
        }

        return itemJSON;
    }

    /**
     * JSON for BlockExplorer lists
     * @param langObj
     * @param args
     * @return
     */
    public JSONObject jsonForExplorerPage(JSONObject langObj, Object[] args) {
        //DCSet dcSet = DCSet.getInstance();

        JSONObject itemJSON = new JSONObject();
        itemJSON.put("key", this.getKey());
        itemJSON.put("nameOrig", getName());
        itemJSON.put("name", this.viewName());
        String[] tagsArray = getTags();
        if (tagsArray != null && tagsArray.length > 0) {
            JSONArray tagsJson = new JSONArray();
            for (String tag : tagsArray) {
                tagsJson.add(tag);
            }
            itemJSON.put("tags", tagsJson);
        }
        itemJSON.put("item_type", this.getItemTypeName());

        if (description != null && !description.isEmpty()) {
            if (viewDescription().length() > 100) {
                itemJSON.put("description", viewDescription().substring(0, 100));
            } else {
                itemJSON.put("description", viewDescription());
            }
        } else {
            itemJSON.put("description", "");
        }

        itemJSON.put("maker", this.getMaker().getAddress());
        Fun.Tuple2<Integer, PersonCls> person = this.getMaker().getPerson();
        if (person != null) {
            itemJSON.put("person", person.b.getName());
            itemJSON.put("person_key", person.b.getKey());
        }


        itemJSON.put("iconType", getIconType());
        itemJSON.put("iconTypeName", viewMediaType(iconType));

        String iconURL = getIconURL();
        if (iconURL != null)
            itemJSON.put("iconURL", iconURL);

        return itemJSON;
    }

    public static void makeJsonLitePage(DCSet dcSet, int itemType, long start, int pageSize,
                                        Map output, boolean showPerson, boolean descending) {

        ItemMap map = dcSet.getItem_Map(itemType);
        ItemCls element;
        long size = map.getLastKey();

        if (start < 1 || start > size && size > 0) {
            start = size;
        }
        output.put("start", start);
        output.put("pageSize", pageSize);
        output.put("listSize", size);

        JSONArray array = new JSONArray();

        long key = 0;
        try (IteratorCloseable<Long> iterator = map.getIterator(start, descending)) {
            while (iterator.hasNext() && pageSize-- > 0) {
                key = iterator.next();
                element = map.get(key);
                if (element != null) {
                    array.add(element.toJsonLite(true, showPerson));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        output.put("pageItems", array);
        output.put("lastKey", key);

    }

    public JSONObject jsonForExplorerInfo(DCSet dcSet, JSONObject langObj, boolean forPrint) {

        JSONObject itemJson = toJson();

        if (getKey() > 0 && getKey() < getStartKey()) {
            itemJson.put("description", Lang.T(viewDescription(), langObj));
        }

        itemJson.put("Label_Maker", Lang.T("Maker", langObj));
        itemJson.put("Label_Pubkey", Lang.T("Public Key", langObj));
        itemJson.put("Label_TXCreator", Lang.T("Creator", langObj));
        itemJson.put("Label_Number", Lang.T("Number", langObj));
        itemJson.put("Label_TXIssue", Lang.T("Transaction of Issue", langObj));
        itemJson.put("Label_DateIssue", Lang.T("Issued Date", langObj));
        itemJson.put("Label_Signature", Lang.T("Signature", langObj));
        itemJson.put("Label_Actions", Lang.T("Actions", langObj));
        itemJson.put("Label_RAW", Lang.T("Bytecode", langObj));
        itemJson.put("Label_Print", Lang.T("Print", langObj));
        itemJson.put("Label_Description", Lang.T("Description", langObj));
        itemJson.put("Label_seqNo", Lang.T("Номер", langObj));
        itemJson.put("Label_SourceText", Lang.T("Source Text # исходный текст", langObj));
        itemJson.put("Label_ValidityPeriod", Lang.T("Validity period", langObj));

        itemJson.put("maker", this.getMaker().getAddress());
        Fun.Tuple2<Integer, PersonCls> person = this.getMaker().getPerson();
        if (person != null) {
            itemJson.put("maker_person", person.b.getName());
            itemJson.put("maker_person_key", person.b.getKey());
            itemJson.put("maker_person_image_url", person.b.getImageURL());
            itemJson.put("maker_person_image_type", person.b.getImageTypeName());
            itemJson.put("maker_person_image_media_type", person.b.getImageMediaType().toString());

        }

        if (referenceTx != null) {
            if (referenceTx.getCreator() != null) {
                itemJson.put("tx_creator_person", referenceTx.viewCreator());
            }

            WebTransactionsHTML.getAppLink(itemJson, referenceTx, langObj);
            WebTransactionsHTML.getVouches(itemJson, referenceTx, langObj);
            WebTransactionsHTML.getLinks(itemJson, referenceTx, langObj);

        }

        return itemJson;
    }

    public String makeHTMLView() {

        String text = makeHTMLHeadView();
        text += makeHTMLFootView(true);

        return text;
    }

    public String makeHTMLHeadView() {

        String text = "[" + getKey() + "]" + Lang.T("Name") + ":&nbsp;" + viewName() + "<br>";
        return text;

    }

    public String makeHTMLFootView(boolean andLabel) {

        String text = andLabel ? Lang.T("Description") + ":<br>" : "";
        if (getKey() > 0 && getKey() < START_KEY()) {
            text += Library.to_HTML(Lang.T(viewDescription())) + "<br>";
        } else {
            text += Library.to_HTML(viewDescription()) + "<br>";
        }

        return text;

    }

    public HashMap getNovaItems() {
        return new HashMap<String, Fun.Tuple3<Long, Long, byte[]>>();
    }

    public byte[] getNovaItemCreator(Object item) {
        return ((Fun.Tuple3<Integer, Long, byte[]>) item).c;
    }

    public Long getNovaItemKey(Object item) {
        return ((Fun.Tuple3<Long, Long, byte[]>) item).a;
    }

    /**
     * @param dcSet
     * @return key если еще не добавлен, -key если добавлен и 0 - если это не НОВА
     */
    public long isNovaItem(DCSet dcSet) {
        Object item = getNovaItems().get(this.name);
        if (item != null && maker.equals(getNovaItemCreator(item))) {
            ItemMap dbMap = this.getDBMap(dcSet);
            Long key = getNovaItemKey(item);
            if (dbMap.contains(key)) {
                return -key;
            } else {
                return key;
            }
        }

        return 0L;
    }

    //
    public long insertToMap(DCSet db, long startKey) {
        //INSERT INTO DATABASE
        ItemMap dbMap = this.getDBMap(db);

        long newKey;
        long novaKey = this.isNovaItem(db);
        if (novaKey > 0) {

            // INSERT WITH NOVA KEY
            newKey = novaKey;
            dbMap.put(newKey, this);

            // если в Генесиз вносим NOVA ASSET - пересчитаем и Размер
            if (dbMap.getLastKey() < newKey) {
                dbMap.setLastKey(newKey);
            }

        } else {

            // INSERT WITH NEW KEY
            newKey = dbMap.getLastKey();
            if (newKey < startKey) {
                // IF this not GENESIS issue - start from startKey
                dbMap.setLastKey(startKey);
            }
            newKey = dbMap.incrementPut(this);

        }

        this.key = newKey;

        return key;
    }

    public long deleteFromMap(DCSet db, long startKey) {
        //DELETE FROM DATABASE

        long thisKey = this.getKey();

        ItemMap map = this.getDBMap(db);
        if (thisKey > startKey) {
            map.decrementDelete(thisKey);

            if (BlockChain.CHECK_BUGS > 1
                    && map.getLastKey() != thisKey - 1 && !BlockChain.isNovaAsset(thisKey)) {
                LOGGER.error("After delete KEY: " + key + " != map.value.key - 1: " + map.getLastKey());
                Long error = null;
                error++;
            }

        } else {
            if (false && BlockChain.CHECK_BUGS > 3 && thisKey == 0) {
                thisKey = this.getKey();
            }
            map.delete(thisKey);
        }

        return thisKey;

    }

}
