package org.erachain.core.item.persons;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.*;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.ByteArrayUtils;
import org.erachain.utils.DateTimeFormat;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

//import java.math.BigDecimal;
//import java.util.Arrays;
// import org.slf4j.LoggerFactory;
//import com.google.common.primitives.Ints;

//birthLatitude -90..90; birthLongitude -180..180
public abstract class PersonCls extends ItemCls {

    public static final int TYPE_KEY = ItemCls.PERSON_TYPE;

    public static final long MIN_START_KEY_OLD = 0L;
    //public static final long START_KEY_UP_ITEMS = 1L << 20;

    public static int MAX_IMAGE_LENGTH = 28000;
    public static int MIN_IMAGE_LENGTH = 10240;

    public static final int HUMAN = 1;
    public static final int DOG = 2;
    public static final int CAT = 3;
    public static final int GENDER_LENGTH = 1;
    public static final int HEIGHT_LENGTH = 1;
    protected static final int BIRTHDAY_LENGTH = ItemCls.TIMESTAMP_LENGTH;
    protected static final int DEATHDAY_LENGTH = ItemCls.TIMESTAMP_LENGTH;
    protected static final int RACE_SIZE_LENGTH = 1;
    public static final int MAX_RACE_LENGTH = 256 ^ RACE_SIZE_LENGTH - 1;
    protected static final int LATITUDE_LENGTH = 4;
    protected static final int SKIN_COLOR_SIZE_LENGTH = 1;
    public static final int MAX_SKIN_COLOR_LENGTH = 256 ^ SKIN_COLOR_SIZE_LENGTH - 1;
    protected static final int EYE_COLOR_SIZE_LENGTH = 1;
    public static final int MAX_EYE_COLOR_LENGTH = 256 ^ EYE_COLOR_SIZE_LENGTH - 1;
    protected static final int HAIR_COLOR_SIZE_LENGTH = 1;
    public static final int MAX_HAIR_COLOR_LENGTH = 256 ^ HAIR_COLOR_SIZE_LENGTH - 1;
    protected static final int BASE_LENGTH = BIRTHDAY_LENGTH + DEATHDAY_LENGTH + GENDER_LENGTH + RACE_SIZE_LENGTH + LATITUDE_LENGTH * 2
            + SKIN_COLOR_SIZE_LENGTH + EYE_COLOR_SIZE_LENGTH + HAIR_COLOR_SIZE_LENGTH
            + HEIGHT_LENGTH;

    public static String[] GENDERS_LIST = {"Male", "Female", "-"};

    // already exist in super - protected String name; // First Name|Middle Name|Last Name
    protected long birthday; // timestamp
    protected long deathday; // timestamp
    protected byte gender; //
    protected String race;
    protected float birthLatitude;
    protected float birthLongitude;
    protected String skinColor; // First Name|Middle Name|Last Name
    protected String eyeColor; // First Name|Middle Name|Last Name
    protected String hairСolor; // First Name|Middle Name|Last Name
    protected byte height;

    public PersonCls(byte[] typeBytes, PublicKeyAccount maker, String name, long birthday, long deathday,
                     byte gender, String race, float birthLatitude, float birthLongitude,
                     String skinColor, String eyeColor, String hairСolor, byte height, byte[] icon, byte[] image, String description) {
        super(typeBytes, maker, name, icon, image, description);
        this.birthday = birthday;
        this.deathday = deathday;
        this.gender = gender;
        this.race = race == null ? "" : race;
        this.birthLatitude = birthLatitude;
        this.birthLongitude = birthLongitude;
        this.skinColor = skinColor == null ? "" : skinColor;
        this.eyeColor = eyeColor == null ? "" : eyeColor;
        this.hairСolor = hairСolor == null ? "" : hairСolor;
        this.height = height;
    }

    public PersonCls(byte[] typeBytes, PublicKeyAccount maker, String name, String birthday, String deathday,
                     byte gender, String race, float birthLatitude, float birthLongitude,
                     String skinColor, String eyeColor, String hairСolor, byte height, byte[] icon, byte[] image, String description) {
        this(typeBytes, maker, name, 0, 0,
                gender, race, birthLatitude, birthLongitude,
                skinColor, eyeColor, hairСolor, (byte) height, icon, image, description);

        if (birthday.length() < 11) birthday += " 00:01:01";
        this.birthday = Timestamp.valueOf(birthday).getTime();

        if (deathday != null && deathday.length() < 11) deathday += " 00:01:01";
        this.deathday = deathday == null ? Long.MIN_VALUE : Timestamp.valueOf(deathday).getTime();
    }

    public PersonCls(int type, PublicKeyAccount maker, String name, long birthday, long deathday,
                     byte gender, String race, float birthLatitude, float birthLongitude,
                     String skinColor, String eyeColor, String hairСolor, byte height, byte[] icon, byte[] image, String description) {
        this(new byte[]{(byte) type}, maker, name, birthday, deathday,
                gender, race, birthLatitude, birthLongitude,
                skinColor, eyeColor, hairСolor, height, icon, image, description);
    }

    //GETTERS/SETTERS

    @Override
    public int getItemType() {
        return TYPE_KEY;
    }

    @Override
    public long START_KEY() {
        if (Transaction.parseHeightDBRef(dbRef) > BlockChain.START_KEY_UP)
            return BlockChain.START_KEY_UP_ITEMS;
        //return START_KEY_UP_ITEMS;

        return START_KEY_OLD;
    }

    @Override
    public long MIN_START_KEY() {
        if (Transaction.parseHeightDBRef(dbRef) > BlockChain.START_KEY_UP)
            return BlockChain.START_KEY_UP_ITEMS;
        //return START_KEY_UP_ITEMS;

        return MIN_START_KEY_OLD;
    }

    public String getItemTypeName() {
        return "person";
    }

    public long getBirthday() {
        return this.birthday;
    }

    public long getDeathday() {
        return this.deathday;
    }

    public String getBirthdayStr() {
        if (true)
            return DateTimeFormat.timestamptoString(this.birthday, Settings.getInstance().getBirthTimeFormat(), "UTC");
        else
            return DateTimeFormat.timestamptoString(birthday, Settings.getInstance().getBirthTimeFormat(), "UTC");
    }

    public String getDeathdayStr() {
        if (true)
            return DateTimeFormat.timestamptoString(this.deathday, Settings.getInstance().getBirthTimeFormat(), "UTC");
        else
            return DateTimeFormat.timestamptoString(deathday, Settings.getInstance().getBirthTimeFormat(), "UTC");
    }

    public byte getGender() {
        return this.gender;
    }

    public String getRace() {
        return this.race;
    }

    public float getBirthLatitude() {
        return this.birthLatitude;
    }

    public float getBirthLongitude() {
        return this.birthLongitude;
    }

    public String getSkinColor() {
        return this.skinColor;
    }

    public String getEyeColor() {
        return this.eyeColor;
    }

    public String getHairColor() {
        return this.hairСolor;
    }

    public int getHeight() {
        return Byte.toUnsignedInt(this.height);
    }

    public int getMAXimageLenght() {
        return this.MAX_IMAGE_LENGTH;
    }

    public int getMINimageLenght() {
        return this.MIN_IMAGE_LENGTH;
    }

    public boolean isAlive(long onThisTime) {

        if (this.deathday == Long.MIN_VALUE
                || this.deathday == Long.MAX_VALUE
                || this.deathday < this.birthday)
            return true;

        if (onThisTime > 0L
                && this.deathday > onThisTime)
            return true;

        return false;

    }

    @Override
    public HashMap getNovaItems() {
        return BlockChain.NOVA_PERSONS;
    }

    public Set<String> getPubKeys(DCSet dcSet) {
        return dcSet.getPersonAddressMap().getItems(this.getKey(dcSet)).keySet();
    }

    public static BigDecimal getBalance(long personKey, long assetKey, int pos, int side) {

        Set<String> addresses = DCSet.getInstance().getPersonAddressMap().getItems(personKey).keySet();

        ItemAssetBalanceMap map = DCSet.getInstance().getAssetBalanceMap();

        // тут переключение внутри цикла идет - так же слишком ресурсно
        BigDecimal sum = addresses.stream()
                .map((adr) -> Account.makeShortBytes(adr))
                .map((key) -> map.get(key, assetKey))
                .map((balances) -> {
                    switch (pos) {
                        case 1:
                            switch (side) {
                                case Account.BALANCE_SIDE_DEBIT:
                                    return balances.a.a;
                                case Account.BALANCE_SIDE_LEFT:
                                    return balances.a.b;
                                case Account.BALANCE_SIDE_CREDIT:
                                    return balances.a.a.subtract(balances.a.b);
                                default:
                                    return BigDecimal.ZERO;
                            }
                        case 2:
                            switch (side) {
                                case Account.BALANCE_SIDE_DEBIT:
                                    return balances.b.a;
                                case Account.BALANCE_SIDE_LEFT:
                                    return balances.b.b;
                                case Account.BALANCE_SIDE_CREDIT:
                                    return balances.b.a.subtract(balances.b.b);
                                default:
                                    return BigDecimal.ZERO;
                            }
                        case 3:
                            switch (side) {
                                case Account.BALANCE_SIDE_DEBIT:
                                    return balances.c.a;
                                case Account.BALANCE_SIDE_LEFT:
                                    return balances.c.b;
                                case Account.BALANCE_SIDE_CREDIT:
                                    return balances.c.a.subtract(balances.c.b);
                                default:
                                    return BigDecimal.ZERO;
                            }
                        case 4:
                            switch (side) {
                                case Account.BALANCE_SIDE_DEBIT:
                                    return balances.d.a;
                                case Account.BALANCE_SIDE_LEFT:
                                    return balances.d.b;
                                case Account.BALANCE_SIDE_CREDIT:
                                    return balances.d.a.subtract(balances.d.b);
                                default:
                                    return BigDecimal.ZERO;
                            }
                        case 5:
                            switch (side) {
                                case Account.BALANCE_SIDE_DEBIT:
                                    return balances.e.a;
                                case Account.BALANCE_SIDE_LEFT:
                                    return balances.e.b;
                                case Account.BALANCE_SIDE_CREDIT:
                                    return balances.e.a.subtract(balances.e.b);
                                default:
                                    return BigDecimal.ZERO;
                            }
                        default:
                            return BigDecimal.ZERO;
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum;

    }


    // DB
    public ItemMap getDBMap(DCSet db) {
        return db.getItemPersonMap();
    }

    public IssueItemMap getDBIssueMap(DCSet db) {
        return db.getIssuePersonMap();
    }

    // to BYTES
    public byte[] toBytes(boolean includeReference, boolean forMakerSign) {

        byte[] data = super.toBytes(includeReference, forMakerSign);

        // WRITE BIRTHDAY
        byte[] birthdayBytes = Longs.toByteArray(this.birthday);
        birthdayBytes = Bytes.ensureCapacity(birthdayBytes, BIRTHDAY_LENGTH, 0);
        data = Bytes.concat(data, birthdayBytes);

        // WRITE DEATHDAY
        byte[] deathdayBytes = Longs.toByteArray(this.deathday);
        deathdayBytes = Bytes.ensureCapacity(deathdayBytes, DEATHDAY_LENGTH, 0);
        data = Bytes.concat(data, deathdayBytes);

        // WRITE GENDER
        data = Bytes.concat(data, new byte[]{gender});

        // WRITE RACE
        byte[] raceBytes = this.race.getBytes(StandardCharsets.UTF_8);
        data = Bytes.concat(data, new byte[]{(byte) raceBytes.length});

        //WRITE RACE
        data = Bytes.concat(data, raceBytes);

        //WRITE BIRTH_LATITUDE
        byte[] birthLatitudeBytes = ByteArrayUtils.float2ByteArray(this.birthLatitude);
        //birthdayBytes = Bytes.ensureCapacity(birthdayBytes, LATITUDE_LENGTH, 0);
        data = Bytes.concat(data, birthLatitudeBytes);

        //WRITE BIRTH_LONGITUDE
        byte[] birthLongitudeBytes = ByteArrayUtils.float2ByteArray(this.birthLongitude);
        //birthdayBytes = Bytes.ensureCapacity(birthdayBytes, LATITUDE_LENGTH, 0);
        data = Bytes.concat(data, birthLongitudeBytes);

        //WRITE SKIN COLOR SIZE
        byte[] skinColorBytes = this.skinColor.getBytes(StandardCharsets.UTF_8);
        data = Bytes.concat(data, new byte[]{(byte) skinColorBytes.length});

        //WRITE SKIN COLOR
        data = Bytes.concat(data, skinColorBytes);

        //WRITE EYE COLOR SIZE
        byte[] eyeColorBytes = this.eyeColor.getBytes(StandardCharsets.UTF_8);
        data = Bytes.concat(data, new byte[]{(byte) eyeColorBytes.length});

        //WRITE EYE COLOR
        data = Bytes.concat(data, eyeColorBytes);

        //WRITE HAIR COLOR SIZE
        byte[] hairColorBytes = this.hairСolor.getBytes(StandardCharsets.UTF_8);
        data = Bytes.concat(data, new byte[]{(byte) hairColorBytes.length});

        //WRITE HAIR COLOR
        data = Bytes.concat(data, hairColorBytes);

        //WRITE HEIGHT
        data = Bytes.concat(data, new byte[]{this.height});

        return data;
    }

    //@Override
    public int getDataLength(boolean includeReference) {
        return super.getDataLength(includeReference)
                + BASE_LENGTH
                + this.race.getBytes(StandardCharsets.UTF_8).length
                + this.skinColor.getBytes(StandardCharsets.UTF_8).length
                + this.eyeColor.getBytes(StandardCharsets.UTF_8).length
                + this.hairСolor.getBytes(StandardCharsets.UTF_8).length;
    }

    //OTHER

    @Override
    public String toString(DCSet db) {
        long key = this.getKey(db);
        return "[" + (key < 1 ? "?" : key) + (this.typeBytes[0] == HUMAN ? "" : ("." + this.typeBytes[0])) + "]"
                + this.name // + "♥"
                ///+ DateTimeFormat.timestamptoString(birthday, "dd-MM-YY", "UTC")
                ;
    }

    public static BigDecimal getTotalBalance(DCSet dcSet, Long personKey, Long assetKey, int position) {
        TreeMap<String, Stack<Fun.Tuple3<Integer, Integer, Integer>>> addresses
                = dcSet.getPersonAddressMap().getItems(personKey);

        if (addresses.isEmpty())
            return null;

        TransactionFinalMap transactionsMap = dcSet.getTransactionFinalMap();
        BigDecimal balanceTotal = BigDecimal.ZERO;

        for (String address : addresses.keySet()) {
            Account account = new Account(address);
            Tuple2<BigDecimal, BigDecimal> balance = account.getBalanceForAction(assetKey, position);
            if (balance != null)
                balanceTotal = balanceTotal.add(balance.b);
        }

        return balanceTotal;
    }

    @Override
    public String getShort(DCSet db) {
        long key = this.getKey(db);
        return "[" + (key < 1 ? "?" : key) + (this.typeBytes[0] == HUMAN ? "" : ("." + this.typeBytes[0])) + "]"
                + this.name.substring(0, Math.min(this.name.length(), 20)) //"♥"
                //+ DateTimeFormat.timestamptoString(birthday, "dd-MM-YY", "UTC")
                ;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJson() {

        JSONObject personJSON = super.toJson();

        // ADD DATA
        personJSON.put("birthday", this.birthday);
        personJSON.put("deathday", this.deathday);
        personJSON.put("gender", this.gender);
        personJSON.put("race", this.race);
        personJSON.put("birthLatitude", this.birthLatitude);
        personJSON.put("birthLongitude", this.birthLongitude);
        personJSON.put("skinColor", this.skinColor);
        personJSON.put("eyeColor", this.eyeColor);
        personJSON.put("hairColor", this.hairСolor);
        personJSON.put("height", Byte.toUnsignedInt(this.height));

        return personJSON;
    }

    public JSONObject jsonForExplorerPage(JSONObject langObj, Object[] args) {
        //DCSet dcSet = DCSet.getInstance();

        JSONObject json = super.jsonForExplorerPage(langObj, args);
        json.put("birthday", birthday);

        return json;

    }

    public JSONObject jsonForExplorerInfo(DCSet dcSet, JSONObject langObj, boolean forPrint) {

        JSONObject itemJson = super.jsonForExplorerInfo(dcSet, langObj, forPrint);
        itemJson.put("Label_TXCreator", Lang.T("Registrar", langObj));
        itemJson.put("Label_Authorship", Lang.T("Authorship", langObj));
        itemJson.put("Label_Born", Lang.T("Birthday", langObj));
        itemJson.put("Label_Gender", Lang.T("Gender", langObj));

        itemJson.put("birthday", getBirthdayStr());
        if (!isAlive(0L)) {
            itemJson.put("deathday", getDeathdayStr());
            itemJson.put("Label_dead", Lang.T("Deathday", langObj));

        }

        String gender = Lang.T("Man", langObj);
        if (getGender() == 0) {
            gender = Lang.T("Man", langObj);
        } else if (getGender() == 1) {
            gender = Lang.T("Woman", langObj);
        } else {
            gender = Lang.T("-", langObj);
        }
        itemJson.put("gender", gender);


        if (!forPrint) {
        }

        return itemJson;
    }

}
