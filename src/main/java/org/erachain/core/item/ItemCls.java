package org.erachain.core.item;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.Jsonable;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.blockexplorer.ExplorerJsonLine;
import org.erachain.core.blockexplorer.WebTransactionsHTML;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.RSetStatusToItem;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.IssueItemMap;
import org.erachain.datachain.ItemMap;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.Iconable;
import org.erachain.gui.library.Library;
import org.erachain.lang.Lang;
import org.erachain.utils.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple6;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    protected static final int TYPE_LENGTH = 2;
    protected static final int MAKER_LENGTH = PublicKeyAccount.PUBLIC_KEY_LENGTH;
    protected static final int NAME_SIZE_LENGTH = 1;
    //public static final int MIN_NAME_LENGTH = 10;
    public static final int MAX_NAME_LENGTH = Transaction.MAX_TITLE_BYTES_LENGTH;
    protected static final int ICON_SIZE_LENGTH = 2;
    protected static final int IMAGE_SIZE_LENGTH = 4;
    protected static final int FLAGS_LENGTH = Long.BYTES;
    protected static final int DESCRIPTION_SIZE_LENGTH = 4;
    protected static final int REFERENCE_LENGTH = Transaction.SIGNATURE_LENGTH;
    protected static final int DBREF_LENGTH = Transaction.DBREF_LENGTH;
    protected static final int BASE_LENGTH = TYPE_LENGTH + MAKER_LENGTH
            + NAME_SIZE_LENGTH + ICON_SIZE_LENGTH + IMAGE_SIZE_LENGTH + DESCRIPTION_SIZE_LENGTH;

    public static final int MAX_ICON_LENGTH = (int) Math.pow(256, ICON_SIZE_LENGTH) - 1;
    public static final int MAX_IMAGE_LENGTH = 1500000; //(int) Math.pow(256, IMAGE_SIZE_LENGTH) - 1;

    protected static final int TIMESTAMP_LENGTH = Transaction.TIMESTAMP_LENGTH;

    protected byte[] typeBytes;
    protected PublicKeyAccount maker;

    /**
     * добавочные флаги по знаку из Размер Картинки
     */
    protected static final int FLAGS_MASK = 1 << 31;
    // настройки для данного актива - могут быть использованы в будущем - 64 байта
    // то есть можно установить 64 разных флагов
    protected long[] flags;

    protected String name;
    protected String description;
    protected long key = 0;
    /**
     * this is signature of issued record
     */
    protected byte[] reference = null;
    protected long dbRef;
    protected byte[] icon;
    protected byte[] image;

    public Transaction referenceTx = null;

    public ItemCls(byte[] typeBytes, long[] flags, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description) {
        this.typeBytes = typeBytes;
        this.flags = flags;
        this.maker = maker;
        this.name = name.trim();
        this.description = description;
        this.icon = icon == null ? new byte[0] : icon;
        this.image = image == null ? new byte[0] : image;

    }

    public ItemCls(int type, long[] flags, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description) {
        this(new byte[TYPE_LENGTH], flags, maker, name, icon, image, description);
        this.typeBytes[0] = (byte) type;
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

    public abstract ItemMap getDBMap(DCSet db);

    public abstract IssueItemMap getDBIssueMap(DCSet db);

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
        String[] words = this.name.split(" ");
        String name = words[0].trim();
        if (name.length() > 6) {
            name = name.substring(0, 6);
        }
        return name;

    }

    /**
     * доп метки для поиска данной сущности или её типа
     *
     * @return
     */
    public String[] getTags() {
        return null;
    }

    public byte[] getIcon() {
        return this.icon;
    }

    public byte[] getImage() {
        return this.image;
    }

    public boolean hasIconURL() {
        if (this.icon != null && this.icon.length > 0 && this.icon.length < 400) {
            // внешняя ссылка - обработаем ее
            return true;
        }
        return false;
    }

    public boolean hasImageURL() {
        if (this.image != null && this.image.length > 0 && this.image.length < 400) {
            // внешняя ссылка - обработаем ее
            return true;
        }
        return false;
    }

    public String getIconURL() {
        if (hasIconURL()) {
            // внешняя ссылка - обработаем ее
            return new String(icon, StandardCharsets.UTF_8);
        }
        return null;
    }

    public String getImageURL() {
        if (hasImageURL()) {
            // внешняя ссылка - обработаем ее
            return new String(image, StandardCharsets.UTF_8);
        }
        return null;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public long getKey() {
        return this.key;
    }

    public long getKey(DCSet db) {
        // resolve key in that DB
        ////resolveKey(db);
        return this.key;
    }

    public long resolveKey(DCSet db) {

        if (this.reference == null || BlockChain.isWiped(this.reference))
            return 0L;

        if (false && this.key == 0 // & this.reference != null
        ) {
            if (this.getDBIssueMap(db).contains(this.reference)) {
                this.key = this.getDBIssueMap(db).get(this.reference);
            } else if (BlockChain.CHECK_BUGS > 0
                    && !BlockChain.CLONE_MODE && !BlockChain.TEST_MODE
                    && Base58.encode(this.reference).equals("2Mm3MY2F19CgqebkpZycyT68WtovJbgBb9p5SJDhPDGFpLQq5QjAXsbUZcRFDpr8D4KT65qMV7qpYg4GStmRp4za")

            ) {
                LOGGER.error("Item [" + this.name + "] not found for REFERENCE: " + Base58.encode(this.reference));
                if (BlockChain.CHECK_BUGS > 3) {
                    Long error = null;
                    error++;
                }
            }
        }

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
        if (true) {
            return key != 0;
        } else {
            return this.getDBIssueMap(db).contains(this.reference);
        }
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
        return Ints.fromByteArray(reference);
    }

    @Override
    public boolean equals(Object item) {
        if (item instanceof ItemCls)
            return Arrays.equals(this.reference, ((ItemCls) item).reference);
        return false;
    }

    // forMakerSign - use only DATA needed for making signature
    public byte[] toBytes(boolean includeReference, boolean forMakerSign) {

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

        boolean hasFlags = flags != null && flags.length != 0;

        if (useAll) {
            //WRITE ICON SIZE - 2 bytes = 64kB max
            int iconLength = this.icon.length;
            byte[] iconLengthBytes = Ints.toByteArray(iconLength);
            data = Bytes.concat(data, new byte[]{iconLengthBytes[2], iconLengthBytes[3]});

            //WRITE ICON
            data = Bytes.concat(data, this.icon);
        }

        if (useAll) {
            //WRITE IMAGE SIZE
            int imageLength = this.image.length;

            // если флаги подняты - отразим это
            if (hasFlags)
                imageLength *= -1;

            byte[] imageLengthBytes = Ints.toByteArray(imageLength);
            data = Bytes.concat(data, imageLengthBytes);
        }

        //WRITE IMAGE
        data = Bytes.concat(data, this.image);

        /// FLAGS
        if (hasFlags) {
            // WRITE FLAGS LENGTH
            data = Bytes.concat(data, new byte[]{(byte) this.flags.length});

            for (int i = 0; i < flags.length; i++) {
                data = Bytes.concat(data, Longs.toByteArray(this.flags[i]));
            }

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
                + (flags == null || flags.length == 0 ? 0 : FLAGS_LENGTH * flags.length)
                + this.name.getBytes(StandardCharsets.UTF_8).length
                + this.icon.length
                + this.image.length
                + this.description.getBytes(StandardCharsets.UTF_8).length
                + (includeReference ? REFERENCE_LENGTH + DBREF_LENGTH : 0);
    }

    //OTHER

    public String toString(DCSet db) {
        long key = this.getKey(db);
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

        // ADD DATA
        itemJSON.put("key", this.getKey());
        itemJSON.put("name", this.name);


        if (hasIconURL()) {
            itemJSON.put("iconURL", getIconURL());
        } else {
            if (withIcon && this.getIcon() != null && this.getIcon().length > 0)
                itemJSON.put("icon", java.util.Base64.getEncoder().encodeToString(this.getIcon()));
            else
                itemJSON.put("icon", "");
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
        itemJSON.put("item_type", this.getItemTypeName());
        //itemJSON.put("itemType", this.getItemTypeName());
        itemJSON.put("item_type_sub", this.getItemSubType());
        //itemJSON.put("itemTypeSub", this.getItemSubType());
        itemJSON.put("type0", Byte.toUnsignedInt(this.typeBytes[0]));
        itemJSON.put("type1", Byte.toUnsignedInt(this.typeBytes[1]));
        itemJSON.put("description", this.description);
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

        if (hasIconURL()) {
            itemJSON.put("iconURL", getIconURL());
        }
        if (hasImageURL()) {
            itemJSON.put("imageURL", getImageURL());
        }

        return itemJSON;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJsonData() {

        JSONObject itemJSON = new JSONObject();

        // ADD DATA
        if (hasIconURL()) {
            itemJSON.put("iconURL", getIconURL());
            itemJSON.put("icon", "");
        } else {
            if (getIcon() != null && getIcon().length > 0)
                itemJSON.put("icon", java.util.Base64.getEncoder().encodeToString(this.getIcon()));
            else
                itemJSON.put("icon", "");
        }

        if (hasImageURL()) {
            itemJSON.put("imageURL", getImageURL());
            itemJSON.put("image", "");
        } else {
            if (getImage() != null && getImage().length > 0)
                itemJSON.put("image", java.util.Base64.getEncoder().encodeToString(this.getImage()));
            else
                itemJSON.put("image", "");
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

        JSONObject json = new JSONObject();
        json.put("key", this.getKey());
        json.put("name", this.viewName());

        if (description != null && !description.isEmpty()) {
            if (viewDescription().length() > 100) {
                json.put("description", viewDescription().substring(0, 100));
            } else {
                json.put("description", viewDescription());
            }
        } else {
            json.put("description", "");
        }

        json.put("maker", this.getMaker().getAddress());
        Fun.Tuple2<Integer, PersonCls> person = this.getMaker().getPerson();
        if (person != null) {
            json.put("person", person.b.getName());
            json.put("person_key", person.b.getKey());
        }

        if (hasIconURL()) {
            json.put("iconURL", getIconURL());
            json.put("icon", "");
        } else {
            if (getIcon() != null && getIcon().length > 0)
                json.put("icon", java.util.Base64.getEncoder().encodeToString(this.getIcon()));
            else
                json.put("icon", "");
        }

        return json;
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
        try (IteratorCloseable<Long> iterator = map.getIteratorFrom(start, descending)) {
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

        itemJson.put("maker", this.getMaker().getAddress());
        Fun.Tuple2<Integer, PersonCls> person = this.getMaker().getPerson();
        if (person != null) {
            itemJson.put("maker_person", person.b.getName());
            itemJson.put("maker_person_key", person.b.getKey());
        }

        if (hasIconURL()) {
            itemJson.put("iconURL", getIconURL());
        } else {
            if (getIcon() != null && getIcon().length > 0)
                itemJson.put("icon", java.util.Base64.getEncoder().encodeToString(getIcon()));
        }

        if (hasImageURL()) {
            itemJson.put("imageURL", getImageURL());
        } else {
            if (getImage() != null && getImage().length > 0)
                itemJson.put("image", java.util.Base64.getEncoder().encodeToString(getImage()));
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
        return "";
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
     * @param creator
     * @param dcSet
     * @return key если еще не добавлен, -key если добавлен и 0 - если это не НОВА
     */
    public long isNovaAsset(Account creator, DCSet dcSet) {
        Object item = getNovaItems().get(this.name);
        if (item != null && creator.equals(getNovaItemCreator(item))) {
            ItemMap dbMap = this.getDBMap(dcSet);
            Long key = (Long) getNovaItemKey(item);
            if (dbMap.contains(key)) {
                return -key;
            } else {
                return key;
            }
        }

        return 0L;
    }

    //
    public Long insertToMap(DCSet db, long startKey) {
        //INSERT INTO DATABASE
        ItemMap dbMap = this.getDBMap(db);

        long newKey;
        long novaKey = this.isNovaAsset(this.maker, db);
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

        if (false) {
            // теперь ключ прямо в записи храним и не нужно его отдельно хранить
            //SET ORPHAN DATA
            this.getDBIssueMap(db).put(this.reference, newKey);
        }

        return key;
    }

    public long deleteFromMap(DCSet db, long startKey) {
        //DELETE FROM DATABASE

        long thisKey = this.getKey(db);

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
                thisKey = this.getKey(db);
            }
            map.delete(thisKey);
        }

        if (false) {
            // теперь ключ прямо в записи храним и не нужно его отдельно хранить
            //DELETE ORPHAN DATA
            this.getDBIssueMap(db).delete(this.reference);
        }

        return thisKey;

    }

}
