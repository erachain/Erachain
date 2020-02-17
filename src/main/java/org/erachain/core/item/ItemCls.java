package org.erachain.core.item;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.apache.commons.net.util.Base64;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.blockexplorer.ExplorerJsonLine;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.RSetStatusToItem;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.IssueItemMap;
import org.erachain.datachain.ItemMap;
import org.erachain.utils.Pair;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple6;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

//import java.math.BigDecimal;
//import com.google.common.primitives.Longs;

public abstract class ItemCls implements ExplorerJsonLine {

    public static final int ASSET_TYPE = 1;
    public static final int IMPRINT_TYPE = 2;
    public static final int TEMPLATE_TYPE = 3;
    public static final int PERSON_TYPE = 4;
    public static final int STATUS_TYPE = 5;
    public static final int UNION_TYPE = 6;
    public static final int STATEMENT_TYPE = 7;
    public static final int POLL_TYPE = 8;
    public static final int MAX_ICON_LENGTH = 11000; //(int) Math.pow(256, ICON_SIZE_LENGTH) - 1;
    public static final int MAX_IMAGE_LENGTH = 1100000; //(int) Math.pow(256, IMAGE_SIZE_LENGTH) - 1;
    protected static final int TYPE_LENGTH = 2;
    protected static final int OWNER_LENGTH = PublicKeyAccount.PUBLIC_KEY_LENGTH;
    protected static final int NAME_SIZE_LENGTH = 1;
    //public static final int MIN_NAME_LENGTH = 10;
    public static final int MAX_NAME_LENGTH = (int) Math.pow(256, NAME_SIZE_LENGTH) - 1;
    protected static final int ICON_SIZE_LENGTH = 2;
    protected static final int IMAGE_SIZE_LENGTH = 4;
    protected static final int DESCRIPTION_SIZE_LENGTH = 4;
    protected static final int REFERENCE_LENGTH = Transaction.SIGNATURE_LENGTH;
    protected static final int BASE_LENGTH = TYPE_LENGTH + OWNER_LENGTH + NAME_SIZE_LENGTH + ICON_SIZE_LENGTH + IMAGE_SIZE_LENGTH + DESCRIPTION_SIZE_LENGTH;

    protected static final int TIMESTAMP_LENGTH = Transaction.TIMESTAMP_LENGTH;

    //protected DCMap dbMap;
    //protected DCMap dbIssueMap;
    static Logger LOGGER = LoggerFactory.getLogger(ItemCls.class.getName());
    protected byte[] typeBytes;
    protected PublicKeyAccount owner;
    protected String name;
    protected String description;
    protected long key = 0;
    // TODO: поменять ссылку на Long
    protected byte[] reference = null; // this is signature of issued record
    protected byte[] icon;
    protected byte[] image;

    public ItemCls(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description) {
        this.typeBytes = typeBytes;
        this.owner = owner;
        this.name = name.trim();
        this.description = description;
        this.icon = icon == null ? new byte[0] : icon;
        this.image = image == null ? new byte[0] : image;

    }

    public ItemCls(int type, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description) {
        this(new byte[TYPE_LENGTH], owner, name, icon, image, description);
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
                return new Pair<Integer, Long>(-1, 0l);
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

    public abstract int getMinNameLen();

    public abstract int getItemType();

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

    public PublicKeyAccount getOwner() {
        return this.owner;
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
        if (name.length() >6) {
            name = name.substring(0, 6);
        }
        return name;

    }

    public byte[] getIcon() {
        return this.icon;
    }

    public byte[] getImage() {
        return this.image;
    }

    public long getKey() {
        return getKey(DCSet.getInstance());
    }

    public void setKey(long key) {
        this.key = key;
    }

    public long getKey(DCSet db) {
        // resolve key in that DB
        resolveKey(db);
        return this.key;
    }

    public static String getItemTypeChar(int itemType) {
        switch (itemType) {
            case ItemCls.ASSET_TYPE:
                return "A";
            case ItemCls.IMPRINT_TYPE:
                return "I";
            case ItemCls.PERSON_TYPE:
                return "P";
            case ItemCls.POLL_TYPE:
                return "O"; // Opinion
            case ItemCls.UNION_TYPE:
                return "U";
            case ItemCls.STATEMENT_TYPE:
                return "T"; // TeXT
            case ItemCls.STATUS_TYPE:
                return "S";
            case ItemCls.TEMPLATE_TYPE:
                return "E"; // exDATA
            default:
                return "x";

        }
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

    public static String getItemTypeChar2(int itemType) {
        return "@" + getItemTypeChar(itemType);
    }


    public long getHeight(DCSet db) {
        //INSERT INTO DATABASE
        ItemMap dbMap = this.getDBMap(db);
        long key = dbMap.getLastKey();
        return key;

    }

    public long resolveKey(DCSet db) {

        if (this.reference == null || BlockChain.isWiped(this.reference))
            return 0L;

        if (this.key == 0 // & this.reference != null
                ) {
            if (this.getDBIssueMap(db).contains(this.reference)) {
                this.key = this.getDBIssueMap(db).get(this.reference);
            } else if (BlockChain.CHECK_BUGS > 0
                    && !BlockChain.TEST_MODE
                    && Base58.encode(this.reference).equals("2Mm3MY2F19CgqebkpZycyT68WtovJbgBb9p5SJDhPDGFpLQq5QjAXsbUZcRFDpr8D4KT65qMV7qpYg4GStmRp4za")
                ///|| Base58.encode(this.reference).equals("4VLYXuFEx9hYVwg82921Nh1N1y2ozCyxpvoTs2kXnQk89HLGshF15FJossTBU6dZhXRDAXKUwysvLUD4TFNJfXhW")) // see issue/1149

            ) {
                // zDLLXWRmL8qhrU9DaxTTG4xrLHgb7xLx5fVrC2NXjRaw2vhzB1PArtgqNe2kxp655saohUcWcsSZ8Bo218ByUzH
                LOGGER.error("Item [" + this.name + "] not found for REFERENCE: " + Base58.encode(this.reference));
                if (BlockChain.CHECK_BUGS > 3) {
                    Long error = null;
                    error++;
                }
            }
        }

        return this.key;
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

    public void setReference(byte[] reference) {
        // TODO - if few itens issued in one recor - need reference to include nonce here
        this.reference = reference;

    }

    public Transaction getIssueTransaction(DCSet dcSet) {
        return dcSet.getTransactionFinalMap().get(this.reference);

    }

    public boolean isConfirmed() {
        return isConfirmed(DCSet.getInstance());
    }

    public boolean isConfirmed(DCSet db) {
        return this.getDBIssueMap(db).contains(this.reference);
    }

    public boolean isFavorite() {
        return Controller.getInstance().isItemFavorite(this);
    }

    // forOwnerSign - use only DATA needed for making signature
    public byte[] toBytes(boolean includeReference, boolean forOwnerSign) {

        byte[] data = new byte[0];
        boolean useAll = !forOwnerSign;

        if (useAll) {
            //WRITE TYPE
            data = Bytes.concat(data, this.typeBytes);
        }

        if (useAll) {
            //WRITE OWNER
            try {
                data = Bytes.concat(data, this.owner.getPublicKey());
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
            byte[] imageLengthBytes = Ints.toByteArray(imageLength);
            data = Bytes.concat(data, imageLengthBytes);
        }

        //WRITE IMAGE
        data = Bytes.concat(data, this.image);

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
        }

        return data;
    }

	/*
	@SuppressWarnings("unchecked")
	protected JSONObject getJsonBase()
	{
		JSONObject out=new JSONObject();

		out.put("key", this.getKey());
		out.put("name", this.getName());
		out.put("description", this.getDescription());
		out.put("owner", this.getOwner().getAddress());

		return out;
	}

	public abstract JSONObject toJson();
	 */

    public int getDataLength(boolean includeReference) {
        return BASE_LENGTH
                + this.name.getBytes(StandardCharsets.UTF_8).length
                + this.icon.length
                + this.image.length
                + this.description.getBytes(StandardCharsets.UTF_8).length
                + (includeReference ? REFERENCE_LENGTH : 0);
    }

    //OTHER

    public String toString(DCSet db) {
        long key = this.getKey(db);
        //String creator = GenesisBlock.CREATOR.equals(this.owner)? "GENESIS": this.owner.getPersonAsString_01(false);
        return "[" + (key == 0 ? "?:" : key)
                + "] " + this.viewName();
        //+ (creator.length()==0?"": " (" +creator + ")");
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
        long key = this.getKey(db);
        //String creator = GenesisBlock.CREATOR.equals(this.owner)? "GENESIS": this.owner.getPersonAsString_01(true);
        return (key < 1 ? "? " : key + ": ") + this.viewName().substring(0, Math.min(this.viewName().length(), 30));
        //+ (creator.length()==0?"": " (" +creator + ")");
    }

    public String getShort() {
        return getShort(DCSet.getInstance());
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJson() {

        JSONObject itemJSON = new JSONObject();

        // ADD DATA
        itemJSON.put("item_type", this.getItemTypeName());
        itemJSON.put("item_type_sub", this.getItemSubType());
        itemJSON.put("type0", Byte.toUnsignedInt(this.typeBytes[0]));
        itemJSON.put("type1", Byte.toUnsignedInt(this.typeBytes[1]));
        itemJSON.put("key", this.getKey());
        itemJSON.put("name", this.name);
        itemJSON.put("description", this.description);
        itemJSON.put("creator", this.owner.getAddress()); // @Deprecated
        itemJSON.put("owner", this.owner.getAddress());
        itemJSON.put("owner_publick_key", this.owner.getBase58());
        itemJSON.put("owner_publickey", this.owner.getBase58());
        itemJSON.put("isConfirmed", this.isConfirmed());
        itemJSON.put("reference", Base58.encode(this.reference));

        Transaction txReference = Controller.getInstance().getTransaction(this.reference);
        if (txReference != null) {
            itemJSON.put("timestamp", txReference.getTimestamp());
        }

        return itemJSON;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJsonData() {

        JSONObject itemJSON = new JSONObject();

        // ADD DATA
        itemJSON.put("icon", Base58.encode(this.getIcon()));
        itemJSON.put("image", Base58.encode(this.getImage()));

        return itemJSON;
    }

    /**
     * JSON for BlockExplorer lists
     * @param langObj
     * @return
     */
    public JSONObject jsonForExplorerPage(JSONObject langObj) {
        //DCSet dcSet = DCSet.getInstance();

        JSONObject json = new JSONObject();
        json.put("key", this.getKey());
        json.put("name", this.getName());

        if (description != null && !description.isEmpty()) {
            if (description.length() > 100) {
                json.put("description", description.substring(0, 100));
            } else {
                json.put("description", description);
            }
        } else {
            json.put("description", "");
        }

        json.put("owner", this.getOwner().getAddress());
        Fun.Tuple2<Integer, PersonCls> person = this.getOwner().getPerson();
        if (person != null) {
            json.put("person", person.b.getName());
            json.put("person_key", person.b.getKey());
        }

        if (icon != null)
            json.put("icon", Base64.encodeBase64String(getIcon()));

        return json;
    }

    /**
     *
     * @param creator
     * @param dcSet
     * @return key если еще не добавлен, -key если добавлен и 0 - если это не НОВА
     */
    public long isNovaAsset(Account creator, DCSet dcSet) {
        Pair<Integer, byte[]> pair = BlockChain.NOVA_ASSETS.get(this.name);
        if (pair != null && creator.equals(pair.getB())) {
            ItemMap dbMap = this.getDBMap(dcSet);
            long key = (long)pair.getA();
            if (dbMap.contains(key)) {
                return -key;
            } else {
                return key;
            }
        }

        return 0l;
    }

    //
    public Long insertToMap(DCSet db, long startKey) {
        //INSERT INTO DATABASE
        ItemMap dbMap = this.getDBMap(db);

        long newKey;
        long novaKey = this.isNovaAsset(this.owner, db);
        if (novaKey > 0) {

            // INSERT WITH NOVA KEY
            newKey = novaKey;
            dbMap.put(newKey, this);

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
        //SET ORPHAN DATA
        this.getDBIssueMap(db).put(this.reference, newKey);

        return key;
    }

    public long deleteFromMap(DCSet db, long startKey) {
        //DELETE FROM DATABASE

        long thisKey = this.getKey(db);

        ItemMap map = this.getDBMap(db);
        if (thisKey > startKey) {
            map.decrementDelete(thisKey);

            if (BlockChain.CHECK_BUGS > 1
                    && map.getLastKey() != thisKey - 1) {
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

        //DELETE ORPHAN DATA
        this.getDBIssueMap(db).delete(this.reference);

        return thisKey;

    }

}
