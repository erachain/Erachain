package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.datachain.AddressTime_SignatureMap;
import org.erachain.datachain.DCSet;
import org.erachain.settings.Settings;
import org.erachain.utils.DateTimeFormat;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

// import org.slf4j.LoggerFactory;

//import java.math.RoundingMode;
//import java.math.MathContext;
//import java.util.Comparator;
//import javax.swing.JFrame;
//import javax.swing.JOptionPane;
//import org.erachain.lang.Lang;
//import org.erachain.settings.Settings;

public abstract class Transaction {

    public static final byte[][] DISCREDIR_ADDRESSES = new byte[][]{
            Base58.decode("HPftF6gmSH3mn9dKSAwSEoaxW2Lb6SVoguhKyHXbyjr7"),
            Base58.decode("AoPMZ3Q8u5q2g9aK8JZSQRnb6iS53FjUjrtT8hCfHg9F") // 7DedW8f87pSDiRnDArq381DNn1FsTBa68Y")
    };
    public static final byte[][] VALID_SIGN = new byte[][]{
            Base58.decode("5DnTfBxw2y8fDshzkdqppB24y5P98vnc873z4cofQZ31JskfJbnpRPjU5uZMQwfSYJYkJZzMxMYq6EeNCys18sEq"),
            Base58.decode("4CqzJSD9j4GNGcYVtNvMic98Zq9aQALLdkFkuXMLGnGqUTgdHqHcoSU7wJ24wvaAAukg2g1Kw1SA6UFQo7h3VasN"),
            Base58.decode("E4pUUdCqQt6HWCJ1pUeEtCDngow7pEJjyRtLZTLEDWFEFwicvxVXAgJbUPyASueZVUobZ28xtX6ZgDLb5cxeXy2"),
            Base58.decode("4UPo6sAF63fkqhgkAXh94tcF43XYz8d7f6PqBGSX13eo3UWCENptrk72qkLxtXYEEsHs1wS2eH6VnZEVctnPdUkb"),
            Base58.decode("3aYMNRUVYxVaozihhGkxU8JTeAFT73Ua7JDqUfvrDUpDNPs7mS4pxHUaaDiZsGYi91fK5c2yVLWVQW9tqqDGCK2a"),
            Base58.decode("KhVG9kHf4nttWSEvM6Rd99wuTtRtFAQvSwwo9ae4WFJ2fWaidY4jF33WTRDvYEtaWWu2cmh6x4tEX7ded6QDSGt"),
            Base58.decode("2KW1mywfP99GcEo5hV8mdHxgPDkFJj5ABcVjNa7vQd1GPC13HRqBERUPKfLZ5HrQ3Dyp42u8PWrzBKUP3cUHG3N4"),
            Base58.decode("2SsSCv8EuMnZrGYq4jFhvJ3gRdbdEU92Unp6u4JNwrw4D7SHHaRpH2b9VuLtTA3zuUVx1EqTB5wJQWxeuJbwxYvs"),
            Base58.decode("4iM1HvHgSV3WTXJ3M4XVMZ4AcfrDaA3bdyFmZcX5BJJkacNTjVURWuhp2gLyhxCJok7eAHkd94nM4q3VcDAc2zCJ"),
            Base58.decode("3THvTzHcyEDPGisprZAN955RMEhye84ygnBMPxrFRT6bCocQ84xt1jaSaNyD9px9dxq3zCNbebXnmL251JZhfCHm"),
            Base58.decode("M7jNQ8w2fCjD3Mvo8cH2G5JYFTnGfLYSQv7xCCso7BsmMKJ7Ruc3pnr1rbpFwVrBkQG3auB5SGCmoWbCq9pw8hU"),
            Base58.decode("m1ryu4QMHLaoALYwx35ugNtQec1QAS1KZe8kkx8bQ8UKcesGGbCbqRYhJrtrPDy3gsxVp4hTQGr7qY3NsndBebr"),
            Base58.decode("3Lzamim6R4khdsVfpdsCzyuhqbguCM6yQTyJPJmvPC7agsaBk7UhYuRxZ8tduLpRhZEMpJwAVd5ucRAiXY8cX6ZE"),
            Base58.decode("44chQvtt3NKgRjphBwKTgRfz4rD7YvpHs4k17w1Xvt6drmjBwJWXsFXBfHV97LbMx4kMkzpHCXgN7mNjDUZeTL6M"),
            Base58.decode("xckfcdNWJN1uoGGTe5nXg5JmGUEyzoJQYkt3bUB6vGUGs8p9j8uhVKeYsY5g2sj67w4pz6CcxdhrVFPzGZnkba2"),
            Base58.decode("2x8QSztNRFDKmMjotzfTvbAkDo7s7Uqh9HpyFVQTiDqYpfweV4z1wzcMjn6GtVHszqBZp6ynuUr4JP9PAEBPLtiy"),
            Base58.decode("9UBPJ4XJzRkw7kQAdFvXbEZuroUszFPomH25UAmMkYyTFPfnbyo9qKKTMZffoSjoMHzMssszaTPiFVhxaxEwBrY"),
            Base58.decode("4Vo6hmojFGgAJhfjyiN8PNYktpgrdHGF8Bqe12Pk3PvcvcH8tuJTcTnnCqyGChriHTuZX1u5Qwho8BuBPT4FJ53W")
    };
    
    /*
     *  SEE in concrete TRANSACTIONS
     * public static final byte[][] VALID_RECORDS = new byte[][]{
     * };
     */

    // toBYTE & PARSE fields for different DEALs
    public static final int FOR_MYPACK = 1; // not use this.timestamp & this.feePow
    public static final int FOR_PACK = 2; // not use feePow
    public static final int FOR_NETWORK = 3; // use all (but not calcalated)
    public static final int FOR_DB_RECORD = 4; // use all + calcalated fields (FEE, BlockNo + SeqNo)

    // FLAGS for VALIDATING
    public static final long NOT_VALIDATE_FLAG_FEE = 1l;
    public static final long NOT_VALIDATE_FLAG_PERSONAL = 2l;
    public static final long NOT_VALIDATE_FLAG_PUBLIC_TEXT = 4l;
    public static final long NOT_VALIDATE_FLAG_BALANCE = 8l;
    public static final long NOT_VALIDATE_KEY_COLLISION = 16l;

    // VALIDATION CODE
    public static final int VALIDATE_OK = 1;
    public static final int FUTURE_ABILITY = 2;
    public static final int INVALID_WALLET_ADDRESS = 3;
    public static final int INVALID_MAKER_ADDRESS = 5;
    public static final int INVALID_REFERENCE = 6;
    public static final int INVALID_TIMESTAMP = 7;
    public static final int INVALID_ADDRESS = 8;
    public static final int INVALID_FEE_POWER = 9;
    public static final int NOT_ENOUGH_FEE = 10;
    public static final int NO_BALANCE = 11;
    public static final int INVALID_PUBLIC_KEY = 12;
    public static final int INVALID_RAW_DATA = 13;
    public static final int INVALID_DATE = 14;
    public static final int INVALID_CREATOR = 15; // for some reasons that
    // creator is invalid (same
    // as trade order)
    public static final int INVALID_SIGNATURE = 16;
    public static final int NO_DEBT_BALANCE = 17;
    public static final int NO_HOLD_BALANCE = 18;
    public static final int INVALID_TRANSFER_TYPE = 19;
    public static final int NOT_ENOUGH_RIGHTS = 20;
    public static final int OWNER_NOT_PERSONALIZED = 21;
    public static final int ACCOUNT_ALREADY_PERSONALIZED = 23;
    public static final int TRANSACTION_DOES_NOT_EXIST = 24;
    public static final int CREATOR_NOT_PERSONALIZED = 25;
    public static final int RECEIVER_NOT_PERSONALIZED = 26;
    public static final int INVALID_CLAIM_RECIPIENT = 27;
    public static final int INVALID_CLAIM_DEBT_RECIPIENT = 28;
    public static final int INVALID_RECEIVER = 29;

    // ASSETS
    public static final int INVALID_QUANTITY = 30;
    
    // public static final int ASSET_DOES_NOT_EXIST = 31;
    public static final int INVALID_AMOUNT_IS_NULL = 31;
    public static final int NEGATIVE_AMOUNT = 32;
    public static final int INVALID_AMOUNT = 33;
    public static final int INVALID_RETURN = 34;
    public static final int HAVE_EQUALS_WANT = 35;
    public static final int ORDER_DOES_NOT_EXIST = 36;
    public static final int INVALID_ORDER_CREATOR = 37;
    public static final int INVALID_PAYMENTS_LENGTH = 38;
    public static final int NEGATIVE_PRICE = 39;
    public static final int INVALID_PRICE = 40;
    public static final int INVALID_CREATION_BYTES = 41;
    public static final int INVALID_TAGS_LENGTH = 42;
    public static final int INVALID_TYPE_LENGTH = 43;
    public static final int NOT_MOVABLE_ASSET = 44;
    public static final int NOT_DEBT_ASSET = 45;
    public static final int INVALID_ACCOUNTING_PAIR = 46;
    public static final int INVALID_HOLD_DIRECTION = 47;
    public static final int INVALID_ECXHANGE_PAIR = 48;
    
    public static final int NO_INCLAIM_BALANCE = 49;

    public static final int INVALID_NAME_LENGTH = 50;
    public static final int INVALID_ICON_LENGTH = 51;
    public static final int INVALID_IMAGE_LENGTH = 52;
    public static final int INVALID_DESCRIPTION_LENGTH = 53;
    public static final int INVALID_VALUE_LENGTH = 55;
    public static final int INVALID_TITLE_LENGTH = 56;

    public static final int INVALID_BACKWARD_ACTION = 57;

    // NAMES
    public static final int NAME_DOES_NOT_EXIST = 60;
    public static final int NAME_ALREADY_REGISTRED = 61;
    public static final int NAME_ALREADY_ON_SALE = 62;
    public static final int NAME_NOT_FOR_SALE = 63;
    public static final int BUYER_ALREADY_OWNER = 64;
    public static final int NAME_NOT_LOWER_CASE = 65;
    public static final int NAME_WITH_SPACE = 66;
    public static final int CREATOR_NOT_OWNER = 67;
    public static final int NAME_KEY_ALREADY_EXISTS = 68;
    public static final int NAME_KEY_NOT_EXISTS = 69;
    public static final int LAST_KEY_IS_DEFAULT_KEY = 70;
    
    
    // POLL
    public static final int INVALID_OPTIONS_LENGTH = 80;
    public static final int INVALID_OPTION_LENGTH = 81;
    public static final int DUPLICATE_OPTION = 82;
    public static final int POLL_ALREADY_CREATED = 83;
    public static final int POLL_ALREADY_HAS_VOTES = 84;
    public static final int POLL_NOT_EXISTS = 85;
    public static final int POLL_OPTION_NOT_EXISTS = 86;
    public static final int ALREADY_VOTED_FOR_THAT_OPTION = 87;
    public static final int INVALID_DATA_LENGTH = 88;
    public static final int INVALID_DATA = 89;
    public static final int INVALID_PARAMS_LENGTH = 90;
    public static final int INVALID_URL_LENGTH = 91;
    public static final int INVALID_HEAD_LENGTH = 92;

    public static final int KEY_COLLISION = 94;

    public static final int INVALID_MESSAGE_FORMAT = 95;
    public static final int INVALID_MESSAGE_LENGTH = 96;
    public static final int UNKNOWN_PUBLIC_KEY_FOR_ENCRYPT = 97;


    // ITEMS
    public static final int INVALID_ITEM_KEY = 99;
    public static final int INVALID_ITEM_VALUE = 100;
    public static final int ITEM_DOES_NOT_EXIST = 101;
    public static final int ITEM_ASSET_NOT_EXIST = 102;
    public static final int ITEM_IMPRINT_DOES_NOT_EXIST = 103;
    public static final int ITEM_TEMPLATE_NOT_EXIST = 104;
    public static final int ITEM_PERSON_NOT_EXIST = 105;
    public static final int ITEM_STATUS_NOT_EXIST = 106;
    public static final int ITEM_UNION_NOT_EXIST = 107;
    public static final int ITEM_DOES_NOT_STATUSED = 108;
    public static final int ITEM_DOES_NOT_UNITED = 109;
    public static final int ITEM_DUPLICATE_KEY = 110;
    public static final int ITEM_DUPLICATE = 111;
    public static final int ITEM_PERSON_IS_DEAD = 115;
    public static final int AMOUNT_LENGHT_SO_LONG = 116;
    public static final int AMOUNT_SCALE_SO_BIG = 117;
    public static final int AMOUNT_SCALE_WRONG = 118;
    public static final int ITEM_PERSON_LATITUDE_ERROR = 120;
    public static final int ITEM_PERSON_LONGITUDE_ERROR = 121;
    public static final int ITEM_PERSON_RACE_ERROR = 122;
    public static final int ITEM_PERSON_GENDER_ERROR = 123;
    public static final int ITEM_PERSON_SKIN_COLOR_ERROR = 124;
    public static final int ITEM_PERSON_EYE_COLOR_ERROR = 125;
    public static final int ITEM_PERSON_HAIR_COLOR_ERROR = 126;
    public static final int ITEM_PERSON_HEIGHT_ERROR = 127;
    public static final int ITEM_PERSON_OWNER_SIGNATURE_INVALID = 128;
    public static final int INVALID_UPDATE_VALUE = 140;
    public static final int INVALID_TRANSACTION_TYPE = 150;
    public static final int INVALID_BLOCK_HEIGHT = 200;
    public static final int INVALID_BLOCK_TRANS_SEQ_ERROR = 201;
    public static final int TELEGRAM_DOES_NOT_EXIST = 240;
    public static final int NOT_YET_RELEASED = 299;
    public static final int AT_ERROR = 300;

    // 
    // TYPES *******
    // universal
    public static final int EXTENDED = 0;
    // genesis
    public static final int GENESIS_ISSUE_ASSET_TRANSACTION = 1;
    public static final int GENESIS_ISSUE_TEMPLATE_TRANSACTION = 2;
    public static final int GENESIS_ISSUE_PERSON_TRANSACTION = 3;
    public static final int GENESIS_ISSUE_STATUS_TRANSACTION = 4;
    public static final int GENESIS_ISSUE_UNION_TRANSACTION = 5; //
    public static final int GENESIS_SEND_ASSET_TRANSACTION = 6;
    public static final int GENESIS_SIGN_NOTE_TRANSACTION = 7; //
    public static final int GENESIS_CERTIFY_PERSON_TRANSACTION = 8; // нет в гуи
    public static final int GENESIS_ASSIGN_STATUS_TRANSACTION = 9;//
    public static final int GENESIS_ADOPT_UNION_TRANSACTION = 10;//
    // ISSUE ITEMS
    public static final int ISSUE_ASSET_TRANSACTION = 21;
    public static final int ISSUE_IMPRINT_TRANSACTION = 22;
    public static final int ISSUE_TEMPLATE_TRANSACTION = 23;
    public static final int ISSUE_PERSON_TRANSACTION = 24;
    public static final int ISSUE_STATUS_TRANSACTION = 25;
    public static final int ISSUE_UNION_TRANSACTION = 26;
    public static final int ISSUE_STATEMENT_TRANSACTION = 27; // not in gui
    public static final int ISSUE_POLL_TRANSACTION = 28;
    // SEND ASSET
    public static final int SEND_ASSET_TRANSACTION = 31;
    // RENT ASSET
    //public static final int RENT_ASSET_TRANSACTION = 32; //
    // HOLD ASSET
    //public static final int HOLD_ASSET_TRANSACTION = 33; // not in gui
    // OTHER
    public static final int SIGN_NOTE2_TRANSACTION = 34;
    public static final int SIGN_NOTE_TRANSACTION = 35;
    public static final int CERTIFY_PUB_KEYS_TRANSACTION = 36;
    public static final int SET_STATUS_TO_ITEM_TRANSACTION = 37;
    public static final int SET_UNION_TO_ITEM_TRANSACTION = 38;
    public static final int SET_UNION_STATUS_TO_ITEM_TRANSACTION = 39; // not in
    // confirms other transactions
    // NOT EDIT - fkr CONCORCIUM = 40 !!!
    public static final int VOUCH_TRANSACTION = 40;
    // gui
    public static final int HASHES_RECORD = 41;
    // exchange of assets
    public static final int CREATE_ORDER_TRANSACTION = 50;
    public static final int CANCEL_ORDER_TRANSACTION = 51;
    // voting
    public static final int CREATE_POLL_TRANSACTION = 61;
    public static final int VOTE_ON_POLL_TRANSACTION = 62;
    public static final int VOTE_ON_ITEM_POLL_TRANSACTION = 63;
    public static final int RELEASE_PACK = 70;

    public static final int CALCULATED_TRANSACTION = 100;

    // old
    public static final int REGISTER_NAME_TRANSACTION = 6 + 130;
    public static final int UPDATE_NAME_TRANSACTION = 7 + 130;
    public static final int SELL_NAME_TRANSACTION = 8 + 130;
    public static final int CANCEL_SELL_NAME_TRANSACTION = 9 + 130;
    public static final int BUY_NAME_TRANSACTION = 10 + 130;
    public static final int ARBITRARY_TRANSACTION = 12 + 130;
    public static final int MULTI_PAYMENT_TRANSACTION = 13 + 130;
    public static final int DEPLOY_AT_TRANSACTION = 14 + 130;
    // FEE PARAMETERS
    public static final long RIGHTS_KEY = AssetCls.ERA_KEY;

    // public static final int ACCOUNTING_TRANSACTION = 26;
    // public static final int JSON_TRANSACTION = 27;
    public static final long FEE_KEY = AssetCls.FEE_KEY;

    // FEE PARAMETERS public static final int FEE_PER_BYTE = 1;
    // protected static final int PROP_LENGTH = 2; // properties
    public static final int TIMESTAMP_LENGTH = 8;

    // RELEASES
    // private static final long ASSETS_RELEASE = 0l;
    // private static final long POWFIX_RELEASE = 0L; // Block Version 3 //
    // 2016-02-25T19:00:00+00:00
    // public static final int REFERENCE_LENGTH = Crypto.SIGNATURE_LENGTH;
    public static final int REFERENCE_LENGTH = TIMESTAMP_LENGTH;

    /*
     * / public static long getVOTING_RELEASE() {
     * if(Settings.getInstance().isTestnet()) { return
     * Settings.getInstance().getGenesisStamp(); } return VOTING_RELEASE; }
     *
     * public static long getARBITRARY_TRANSACTIONS_RELEASE() {
     * if(Settings.getInstance().isTestnet()) { return
     * Settings.getInstance().getGenesisStamp(); } return
     * ARBITRARY_TRANSACTIONS_RELEASE; }
     *
     * public static int getAT_BLOCK_HEIGHT_RELEASE() {
     * if(Settings.getInstance().isTestnet()) { return 1; } return
     * AT_BLOCK_HEIGHT_RELEASE; }
     *
     * public static int getMESSAGE_BLOCK_HEIGHT_RELEASE() {
     * if(Settings.getInstance().isTestnet()) { return 1; } return
     * MESSAGE_BLOCK_HEIGHT_RELEASE; }
     *
     * public static long getASSETS_RELEASE() {
     * if(Settings.getInstance().isTestnet()) { return
     * Settings.getInstance().getGenesisStamp(); } return ASSETS_RELEASE; }
     *
     * public static long getPOWFIX_RELEASE() {
     * if(Settings.getInstance().isTestnet()) { return
     * Settings.getInstance().getGenesisStamp(); } return POWFIX_RELEASE; }
     */
    public static final int KEY_LENGTH = 8;
    // not need now protected static final int FEE_LENGTH = 8;
    public static final int SIGNATURE_LENGTH = Crypto.SIGNATURE_LENGTH;
    protected static final int TODO_h1 = 69000;
    // PROPERTIES LENGTH
    protected static final int SIMPLE_TYPE_LENGTH = 1;
    protected static final int TYPE_LENGTH = 4;
    protected static final int HEIGHT_LENGTH = 4;
    protected static final int DATA_JSON_PART_LENGTH = 4;
    protected static final int DATA_VERSION_PART_LENGTH = 6;
    protected static final int DATA_TITLE_PART_LENGTH = 4;
    protected static final int DATA_NUM_FILE_LENGTH = 4;
    protected static final int SEQ_LENGTH = 4;
    public static final int DATA_SIZE_LENGTH = 4;
    public static final int ENCRYPTED_LENGTH = 1;
    public static final int IS_TEXT_LENGTH = 1;
    protected static final int FEE_POWER_LENGTH = 1;
    public static final int FEE_LENGTH = 8;
    // protected static final int HKEY_LENGTH = 20;
    public static final int CREATOR_LENGTH = PublicKeyAccount.PUBLIC_KEY_LENGTH;
    protected static final int BASE_LENGTH_AS_MYPACK = TYPE_LENGTH;
    protected static final int BASE_LENGTH_AS_PACK = BASE_LENGTH_AS_MYPACK + TIMESTAMP_LENGTH
            + CREATOR_LENGTH + SIGNATURE_LENGTH;
    protected static final int BASE_LENGTH = BASE_LENGTH_AS_PACK + FEE_POWER_LENGTH + REFERENCE_LENGTH;
    protected static final int BASE_LENGTH_AS_DBRECORD = BASE_LENGTH + FEE_LENGTH;

    // in pack toByte and Parse - reference not included
    static Logger LOGGER = LoggerFactory.getLogger(Transaction.class.getName());

    protected DCSet dcSet;
    protected String TYPE_NAME = "unknown";
    // protected int type;
    protected byte[] typeBytes;
    protected Block block; // parent block
    protected int height;
    protected int seqNo;
    protected Long dbRef; // height + SeqNo

    // TODO REMOVE REFERENCE - use TIMESTAMP as reference
    protected Long reference = 0l;
    protected BigDecimal fee = BigDecimal.ZERO; // - for genesis
    // transactions
    // protected BigDecimal fee = new BigDecimal.valueOf(999000);
    protected byte feePow = 0;
    protected byte[] signature;
    protected long timestamp;
    protected PublicKeyAccount creator;

    // need for genesis
    protected Transaction(byte type, String type_name) {
        this.typeBytes = new byte[]{type, 0, 0, 0}; // for GENESIS
        this.TYPE_NAME = type_name;
    }

    protected Transaction(byte[] typeBytes, String type_name, PublicKeyAccount creator, byte feePow, long timestamp,
                          Long reference) {
        this.typeBytes = typeBytes;
        this.TYPE_NAME = type_name;
        this.creator = creator;
        // this.props = props;
        this.timestamp = timestamp;
        this.reference = reference;
        if (feePow < 0)
            feePow = 0;
        else if (feePow > BlockChain.FEE_POW_MAX)
            feePow = BlockChain.FEE_POW_MAX;
        this.feePow = feePow;
    }

    protected Transaction(byte[] typeBytes, String type_name, PublicKeyAccount creator, byte feePow, long timestamp,
                          Long reference, byte[] signature) {
        this(typeBytes, type_name, creator, feePow, timestamp, reference);
        this.signature = signature;
    }

    public static int getVersion(byte[] typeBytes) {
        return Byte.toUnsignedInt(typeBytes[1]);
    }

    public static Transaction findByHeightSeqNo(DCSet db, int height, int seq) {
        return db.getTransactionFinalMap().get(height, seq);
    }


    // reference in Map - or as signatire or as BlockHeight + seqNo
    public static Transaction findByDBRef(DCSet db, byte[] dbRef) {

        if (dbRef == null)
            return null;

        Long key;
        if (dbRef.length > 20) {
            // soft or hard confirmations
            key = db.getTransactionFinalMapSigns().get(dbRef);
            if (key == null) {
                return db.getTransactionMap().get(dbRef);
            }
        } else {
            int heightBlock = Ints.fromByteArray(Arrays.copyOfRange(dbRef, 0, 4));
            int seqNo = Ints.fromByteArray(Arrays.copyOfRange(dbRef, 4, 8));
            //key = new Tuple2<Integer, Integer>(blockHeight, seqNo);
            key = Transaction.makeDBRef(heightBlock, seqNo);

        }

        return db.getTransactionFinalMap().get(key);

    }

    public static Map<String, Map<Long, BigDecimal>> subAssetAmount(Map<String, Map<Long, BigDecimal>> allAssetAmount,
                                                                    String address, Long assetKey, BigDecimal amount) {
        return addAssetAmount(allAssetAmount, address, assetKey, BigDecimal.ZERO.subtract(amount));
    }

    public static Map<String, Map<Long, BigDecimal>> addAssetAmount(Map<String, Map<Long, BigDecimal>> allAssetAmount,
                                                                    String address, Long assetKey, BigDecimal amount) {
        Map<String, Map<Long, BigDecimal>> newAllAssetAmount;
        if (allAssetAmount != null) {
            newAllAssetAmount = new LinkedHashMap<String, Map<Long, BigDecimal>>(allAssetAmount);
        } else {
            newAllAssetAmount = new LinkedHashMap<String, Map<Long, BigDecimal>>();
        }

        Map<Long, BigDecimal> newAssetAmountOfAddress;

        if (!newAllAssetAmount.containsKey(address)) {
            newAssetAmountOfAddress = new LinkedHashMap<Long, BigDecimal>();
            newAssetAmountOfAddress.put(assetKey, amount);

            newAllAssetAmount.put(address, newAssetAmountOfAddress);
        } else {
            if (!newAllAssetAmount.get(address).containsKey(assetKey)) {
                newAssetAmountOfAddress = new LinkedHashMap<Long, BigDecimal>(newAllAssetAmount.get(address));
                newAssetAmountOfAddress.put(assetKey, amount);

                newAllAssetAmount.put(address, newAssetAmountOfAddress);
            } else {
                newAssetAmountOfAddress = new LinkedHashMap<Long, BigDecimal>(newAllAssetAmount.get(address));
                BigDecimal newAmount = newAllAssetAmount.get(address).get(assetKey).add(amount);
                newAssetAmountOfAddress.put(assetKey, newAmount);

                newAllAssetAmount.put(address, newAssetAmountOfAddress);
            }
        }

        return newAllAssetAmount;
    }

    public static Map<String, Map<Long, Long>> addStatusTime(Map<String, Map<Long, Long>> allStatusTime, String address,
                                                             Long assetKey, Long time) {
        Map<String, Map<Long, Long>> newAllStatusTime;
        if (allStatusTime != null) {
            newAllStatusTime = new LinkedHashMap<String, Map<Long, Long>>(allStatusTime);
        } else {
            newAllStatusTime = new LinkedHashMap<String, Map<Long, Long>>();
        }

        Map<Long, Long> newStatusTimetOfAddress;

        if (!newAllStatusTime.containsKey(address)) {
            newStatusTimetOfAddress = new LinkedHashMap<Long, Long>();
            newStatusTimetOfAddress.put(assetKey, time);

            newAllStatusTime.put(address, newStatusTimetOfAddress);
        } else {
            if (!newAllStatusTime.get(address).containsKey(assetKey)) {
                newStatusTimetOfAddress = new LinkedHashMap<Long, Long>(newAllStatusTime.get(address));
                newStatusTimetOfAddress.put(assetKey, time);

                newAllStatusTime.put(address, newStatusTimetOfAddress);
            } else {
                newStatusTimetOfAddress = new LinkedHashMap<Long, Long>(newAllStatusTime.get(address));
                Long newTime = newAllStatusTime.get(address).get(assetKey) + time;
                newStatusTimetOfAddress.put(assetKey, newTime);

                newAllStatusTime.put(address, newStatusTimetOfAddress);
            }
        }

        return newAllStatusTime;
    }

    // GETTERS/SETTERS

    public void setHeightSeq(int height, int seqNo) {
        this.dbRef = makeDBRef(height, seqNo);
        this.height = height;
        this.seqNo = seqNo;
    }

    // NEED FOR DB SECONDATY KEYS
    // see org.mapdb.Bind.secondaryKeys
    public void setDC(DCSet dcSet) {
        this.dcSet = dcSet;
    }

    public void setDC_HeightSeq(DCSet dcSet) {
        this.dcSet = dcSet;

        if (this.typeBytes[0] == Transaction.CALCULATED_TRANSACTION) {

        }

        Long dbRef2 = dcSet.getTransactionFinalMapSigns().get(this.signature);
        if (dbRef2 == null)
            return;

        this.dbRef = dbRef2;
        Tuple2<Integer, Integer> pair = Transaction.parseDBRef(dbRef2);
        this.height = pair.a;
        this.seqNo = pair.b;
    }

    public void setDC(DCSet dcSet, int asDeal, int blockHeight, int seqNo) {
        this.dcSet = dcSet;
        this.height = blockHeight; //this.getBlockHeightByParentOrLast(dcSet);
        this.seqNo = seqNo;
        if (asDeal > Transaction.FOR_PACK && (this.fee == null || this.fee.signum() == 0) )
            this.calcFee();
    }

    public void setBlock(Block block, DCSet dcSet, int asDeal, int seqNo) {
        this.block = block;
        this.dcSet = dcSet;
        this.height = block.getHeight();
        this.seqNo = seqNo;
        this.dbRef = Transaction.makeDBRef(height, seqNo);

        if (asDeal > Transaction.FOR_PACK && (this.fee == null || this.fee.signum() == 0) )
            this.calcFee();
    }

    public int getType() {
        return Byte.toUnsignedInt(this.typeBytes[0]);
    }

    public int getVersion() {
        return Byte.toUnsignedInt(this.typeBytes[1]);
    }

    public byte[] getTypeBytes() {
        return this.typeBytes;
    }

    public PublicKeyAccount getCreator() {
        return this.creator;
    }

    public List<PublicKeyAccount> getPublicKeys() {
        return null;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public static Long getTimestampByDBRef(Long dbRef) {
        Tuple2<Integer, Integer> key = parseDBRef(dbRef);
        BlockChain blockChain = Controller.getInstance().getBlockChain();
        return blockChain.getTimestamp(key.a) + key.b;
    }

    // for test signature only!!!
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getDeadline() {
        // 24HOUR DEADLINE TO INCLUDE TRANSACTION IN BLOCK
        return this.timestamp + (1000 * 60 * 60 * 24);
    }

    /// tyutuy jhg jhg jg j
    /*
     * // TIME public Long viewTime() { return 0L; } public Long getTime() {
     * return this.viewTime(); } public Long viewTime(Account account) { return
     * 0L; } public Long getTime(Account account) { return
     * this.viewTime(account); }
     */
    public long getKey() {
        return 0l;
    }

    public long getAbsKey() {
        long key = this.getKey();
        if (key < 0)
            return -key;
        return key;
    }

    public BigDecimal getAmount() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getAmount(Account account) {
        return BigDecimal.ZERO;
    }

    public BigDecimal getAmount(String account) {
        return BigDecimal.ZERO;
    }

    public BigDecimal getFee(String address) {
        if (this.creator != null)
            if (this.creator.getAddress().equals(address))
                return this.fee;
        return BigDecimal.ZERO;
    }

    public BigDecimal getFee(Account account) {
        return this.getFee(account.getAddress());
    }

    public BigDecimal getFee() {
        return this.fee;
    }

    public long getFeeLong() {
        return this.fee.unscaledValue().longValue();
    }

    /*
     * public Long getReference() { return this.reference; }
     */

    public byte getFeePow() {
        return this.feePow;
    }

    public long getAssetKey() {
        return 0l;
    }

    public AssetCls getAsset() {
        return null;
    }

    public byte[] getSignature() {
        return this.signature;
    }

    public List<byte[]> getSignatures() {
        return null;
    }

    // for isValid - check reference value
    public boolean isReferenced() {
        return true;
    }

    public abstract boolean hasPublicText();

    public int getJobLevel() {
        return 100;
    }

    public int calcCommonFee() {
        int len = this.getDataLength(Transaction.FOR_NETWORK, true);

        // FEE_FOR_ANONIMOUSE !
        /// if (this.getBlockHeightByParent(db))
        // TODO FEE_FOR_ANONIMOUSE + is PERSON + DB
        int anonimous = 0;
        // TODO DBSet get from CHAIN
        /*
         * Controller cnt = Controller.getInstance(); BlockChain bchain =
         * cnt.getBlockChain(); for ( Account acc : this.getRecipientAccounts())
         * { //byte[] publicKey = cnt.getPublicKeyByAddress(acc.getAddress());
         * if (!acc.isPerson(bchain.getDB())) { anonimuus +=
         * BlockChain.FEE_FOR_ANONIMOUSE; } }
         *
         * if ( anonimuus == 0 && this.creator != null) { byte[] publicKey =
         * cnt.getPublicKeyByAddress(this.creator.getAddress()); if (publicKey
         * == null) { anonimuus += BlockChain.FEE_FOR_ANONIMOUSE; } }
         */

        if (anonimous > 0) {
            len *= anonimous;
        }

        int minLen = getJobLevel();
        if (this.height < BlockChain.VERS_4_11 && BlockChain.VERS_4_11_USE_OLD_FEE)
            return len * BlockChain.FEE_PER_BYTE_4_10;

        if (len < minLen)
            len = minLen;

        return len * BlockChain.FEE_PER_BYTE;

    }

    // get fee
    public long calcBaseFee() {
        return calcCommonFee();
    }

    // calc FEE by recommended and feePOW
    public void calcFee() {

        long fee_long = calcBaseFee();
        if(this.height < BlockChain.VERS_4_11 && BlockChain.VERS_4_11_USE_OLD_FEE) {
            // OLD version with x64
            fee_long = (fee_long << 5) / 100;
        }
        BigDecimal fee = new BigDecimal(fee_long).multiply(BlockChain.FEE_RATE).setScale(BlockChain.AMOUNT_DEDAULT_SCALE, BigDecimal.ROUND_UP);

        if (this.feePow > 0) {
            this.fee = fee.multiply(new BigDecimal(BlockChain.FEE_POW_BASE).pow(this.feePow)).setScale(BlockChain.AMOUNT_DEDAULT_SCALE, BigDecimal.ROUND_UP);
        } else {
            this.fee = fee;
        }
    }

    // GET forged FEE without invited FEE
    public long getForgedFee() {
        long fee = this.fee.unscaledValue().longValue();
        long fee_invited = this.getInvitedFee();
        return fee - fee_invited;
    }

    // GET only INVITED FEE
    public long getInvitedFee() {
        if (this.height > BlockChain.VERS_4_11 || !BlockChain.VERS_4_11_USE_OLD_FEE)
            return 0l;

        long fee = this.fee.unscaledValue().longValue();
        return fee >> BlockChain.FEE_INVITED_SHIFT;
    }

    public BigDecimal feeToBD(int fee) {
        return BigDecimal.valueOf(fee, BlockChain.AMOUNT_DEDAULT_SCALE);
    }

    public Block getBlock(DCSet db) {

        if (this.block != null)
            return block;

        if (this.height <= 0) {
            Long key = db.getTransactionFinalMapSigns().get(this.signature);
            if (key == null)
                return null;

            Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
            this.height = pair.a;
        }

        this.block = db.getBlockMap().get(this.height);

        return block;
    }

    public Tuple2<Integer, Integer> getHeightSeqNo() {
        int transactionIndex = -1;
        int blockIndex;
        if (block == null) {
            // blockIndex = db.getBlocksHeadMap().getLastBlock().getHeight(db);
            blockIndex = -1;
        } else {
            blockIndex = block.getHeight();
            transactionIndex = block.getTransactionSeq(signature);
        }

        return new Tuple2<Integer, Integer>(blockIndex, transactionIndex);

    }

    public int getSeqNo() {
    return this.seqNo;
    }

    public int getBlockHeight() {
        //if (this.isConfirmed(db)) {

        if (this.height > 0)
            return this.height;

        if (this.block != null) {
            this.height = this.block.getHeight();
            return this.height;
        }
        return -1;
    }

    /*
    // get current or -1
    public int getBlockHeightByParent(DCSet db) {

        if (block != null)
            return block.getHeightByParent(db);

        return getBlockHeight(db);
    }
    */

    // get current or last
    public int getBlockHeightByParentOrLast(DCSet dc) {

        if (block != null)
            return block.getHeight();


        return dc.getBlockMap().size() + 1;
    }

    public int getSeqNo(DCSet db) {

        if(this.seqNo > 0)
            return this.seqNo;

        Block block = this.getBlock(db);
        if (block == null)
            return -1;

        return block.getTransactionSeq(this.signature);

    }

    public long getDBRef() {
        return this.dbRef;
    }

        // reference in Map - or as signatire or as BlockHeight + seqNo
    public byte[] getDBRef(DCSet db) {
        if (this.getConfirmations(db) < BlockChain.MAX_ORPHAN) {
            // soft or hard confirmations
            return this.signature;
        }

        int bh = this.getBlockHeight();
        if (bh < 1)
            // not in chain
            return null;

        byte[] ref = Ints.toByteArray(bh);
        Bytes.concat(ref, Ints.toByteArray(this.getSeqNo(db)));
        return ref;

    }

    // reference in Map - or as signatire or as BlockHeight + seqNo
    public static Long makeDBRef(int height, int seqNo) {

        byte[] ref = Ints.toByteArray(height);
        return Longs.fromByteArray(Bytes.concat(ref, Ints.toByteArray(seqNo)));

    }

    public static Long makeDBRef(Tuple2<Integer, Integer> dbRef) {

        byte[] ref = Ints.toByteArray(dbRef.a);
        return Longs.fromByteArray(Bytes.concat(ref, Ints.toByteArray(dbRef.b)));

    }

    public static Tuple2<Integer, Integer> parseDBRef(Long dbRef) {

        byte[] bytes = Longs.toByteArray(dbRef);

        int blockHeight = Ints.fromByteArray(Arrays.copyOfRange(bytes, 0, 4));
        int seqNo = Ints.fromByteArray(Arrays.copyOfRange(bytes, 4, 8));

        return new Tuple2<Integer, Integer>(blockHeight, seqNo);

    }

    public boolean addCalculated(R_Calculated calculated) {
        if (this.block != null && this.block.txCalculated != null) {
            this.block.txCalculated.add(calculated);
            return true;
        }
        return false;
    }

    public boolean addCalculated(Account creator, long assetKey, BigDecimal amount,
                                 String message) {
        if (this.block != null && this.block.txCalculated != null) {
            this.block.txCalculated.add(new R_Calculated(creator, assetKey, amount,
                    message, this.dbRef));
            return true;
        }
        return false;
    }

    ////
    // VIEW
    public String viewType() {
        return Byte.toUnsignedInt(typeBytes[0]) + "." + Byte.toUnsignedInt(typeBytes[1]);
    }

    public String viewTypeName() {
        return TYPE_NAME;
    }

    public String viewProperies() {
        return Byte.toUnsignedInt(typeBytes[2]) + "." + Byte.toUnsignedInt(typeBytes[3]);
    }

    public String viewSubTypeName() {
        return "";
    }

    public String viewFullTypeName() {
        String sub = viewSubTypeName();
        return sub.length() > 0 ? viewTypeName() + ":" + sub : viewTypeName();
    }

    public static String viewDBRef(long dbRef) {

        byte[] bytes = Longs.toByteArray(dbRef);

        int blockHeight = Ints.fromByteArray(Arrays.copyOfRange(bytes, 0, 4));
        int seqNo = Ints.fromByteArray(Arrays.copyOfRange(bytes, 4, 8));

        return blockHeight + "-" + seqNo;

    }

    public String viewHeightSeq(DCSet db) {
        int seq = this.getSeqNo(db);
        if (seq < 1)
            return "???";

        return this.getBlockHeight() + "-" + seq;
    }

    public String viewAmount(Account account) {
        return account == null ? "" : viewAmount(account.getAddress());
    }

    public String viewAmount(String address) {
        return "";
    }

    public String viewCreator() {
        return creator == null ? "GENESIS" : creator.getPersonAsString();
    }

    public String viewRecipient() {
        return "";
    }

    /*
     * public String viewReference() { //return
     * reference==null?"null":Base58.encode(reference); return
     * reference==null?"null":"" + reference; }
     */
    public String viewSignature() {
        return signature == null ? "null" : Base58.encode(signature);
    }

    public String viewTimestamp() {
        return timestamp < 1000 ? "null" : DateTimeFormat.timestamptoString(timestamp);
    }

    public int viewSize(int forDeal) {
        return getDataLength(forDeal, true);
    }

    // PARSE/CONVERT

    public String viewFee1() {
        return feePow + ":" + this.fee.unscaledValue().longValue();
    }

    public String viewFee() {
        Fun.Tuple2<BigDecimal, String> compu_rate = Controller.COMPU_RATES.get(Settings.getInstance().getLang());
        if (compu_rate == null) {
            compu_rate = Controller.COMPU_RATES.get("en");
        }
        String text = fee.toString();
        if (compu_rate != null) {
            BigDecimal fee_fiat = fee.multiply(compu_rate.a).setScale(compu_rate.a.scale(), BigDecimal.ROUND_HALF_UP);
            text += "(" + compu_rate.b + fee_fiat.toString() + ")";
        }

        return text;
    }

    public String viewItemName() {
        return "";
    }

    public String viewAmount() {
        return "";
    }

    @SuppressWarnings("unchecked")
    protected JSONObject getJsonBase() {

        DCSet localDCSet = DCSet.getInstance();

        JSONObject transaction = new JSONObject();

        transaction.put("type", Byte.toUnsignedInt(this.typeBytes[0]));
        transaction.put("record_type", this.viewTypeName());
        transaction.put("confirmations", this.getConfirmations(localDCSet));
        transaction.put("type_name", this.viewTypeName());
        transaction.put("sub_type_name", this.viewSubTypeName());

        int height;
        if (this.creator == null) {
            transaction.put("creator", "genesis");
            transaction.put("signature", "genesis");
            height = 1;
        } else {
            height = this.getBlockHeight();
            transaction.put("publickey", Base58.encode(this.creator.getPublicKey()));
            transaction.put("creator", this.creator.getAddress());
            transaction.put("signature", this.signature == null ? "null" : Base58.encode(this.signature));
            transaction.put("fee", this.fee.toPlainString());
            transaction.put("timestamp", this.timestamp < 1000 ? "null" : this.timestamp);
            transaction.put("version", Byte.toUnsignedInt(this.typeBytes[1]));
            transaction.put("property1", Byte.toUnsignedInt(this.typeBytes[2]));
            transaction.put("property2", Byte.toUnsignedInt(this.typeBytes[3]));
            if (this.block != null) {
                transaction.put("height", height); //this.block.getHeightByParent(localDCSet));
                transaction.put("sequence", this.getSeqNo(localDCSet));
            }
        }

        transaction.put("size", this.viewSize(Transaction.FOR_NETWORK));
        return transaction;
    }

    public abstract JSONObject toJson();

    @SuppressWarnings("unchecked")
    public JSONObject rawToJson() {

        DCSet localDCSet = DCSet.getInstance();
        JSONObject transaction = new JSONObject();

        transaction.put("confirmations", this.getConfirmations(localDCSet));

        int height;
        if (this.creator == null) {
            height = 1;
        } else {
            height = this.getBlockHeight();
            transaction.put("publickey", Base58.encode(this.creator.getPublicKey()));
        }

        if (height > 0) {
            transaction.put("sequence", this.getSeqNo(localDCSet));
            transaction.put("block", Base58.encode(block.getSignature()));
            transaction.put("block_seq", viewHeightSeq(localDCSet));
            transaction.put("height", height);
        }

        boolean isSigned = this.signature != null;
        transaction.put("signature", isSigned ? Base58.encode(this.signature) : "null");

        transaction.put("raw", Base58.encode(this.toBytes(FOR_NETWORK, isSigned)));

        return transaction;
    }

    public void sign(PrivateKeyAccount creator, int forDeal) {

        // use this.reference in any case and for Pack too
        // but not with SIGN
        boolean withSign = false;
        byte[] data = this.toBytes(forDeal, false);
        if (data == null)
            return;

        // all test a not valid for main test
        // all other network must be invalid here!
        int port = Controller.getInstance().getNetworkPort();
        data = Bytes.concat(data, Ints.toByteArray(port));

        this.signature = Crypto.getInstance().sign(creator, data);
    }

    /*
     * public boolean isValidated() { for ( byte[] wiped:
     * BlockChain.VALID_RECORDS) { byte[] sign = wiped; if
     * (Arrays.equals(this.signature, sign)) { return true; } } return false; }
     */

    // VALIDATE

    // releaserReference == null - not as pack
    // releaserReference = reference of releaser - as pack
    public byte[] toBytes(int forDeal, boolean withSignature) {

        //boolean asPack = releaserReference != null;

        byte[] data = new byte[0];

        // WRITE TYPE
        data = Bytes.concat(data, this.typeBytes);

        if (forDeal > FOR_MYPACK) {
            // WRITE TIMESTAMP
            byte[] timestampBytes = Longs.toByteArray(this.timestamp);
            timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
            data = Bytes.concat(data, timestampBytes);
        }

        // WRITE REFERENCE - in any case as Pack or not - NOW it reserved FLAGS
        if (this.reference != null) {
            // NULL in imprints
            byte[] referenceBytes = Longs.toByteArray(this.reference);
            referenceBytes = Bytes.ensureCapacity(referenceBytes, REFERENCE_LENGTH, 0);
            data = Bytes.concat(data, referenceBytes);
        }

        // WRITE CREATOR
        data = Bytes.concat(data, this.creator.getPublicKey());

        if (forDeal > FOR_PACK) {
            // WRITE FEE POWER
            byte[] feePowBytes = new byte[1];
            feePowBytes[0] = this.feePow;
            data = Bytes.concat(data, feePowBytes);
        }

        // SIGNATURE
        if (withSignature)
            data = Bytes.concat(data, this.signature);

        if (forDeal == FOR_DB_RECORD) {
            // WRITE FEE
            byte[] feeBytes = Longs.toByteArray(this.fee.unscaledValue().longValue());
            data = Bytes.concat(data, feeBytes);
        }

        return data;

    }

    public abstract int getDataLength(int forDeal, boolean withSignature);

    // PROCESS/ORPHAN

    public boolean isWiped() {
        for (byte[] wiped : BlockChain.WIPED_RECORDS) {
            if (Arrays.equals(this.signature, wiped)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSignatureValid(DCSet dcSet) {

        if (this.signature == null || this.signature.length != Crypto.SIGNATURE_LENGTH
                || Arrays.equals(this.signature, new byte[Crypto.SIGNATURE_LENGTH]))
            return false;

        // validation with reference - not as a pack in toBytes - in any case!
        byte[] data = this.toBytes(FOR_NETWORK, false);
        if (data == null)
            return false;

        int height = dcSet.getBlocksHeadsMap().size();
        if (height < 100000) {
            // for skip NOT VALID SIGNs
            for (byte[] valid_item : VALID_SIGN) {
                if (Arrays.equals(signature, valid_item)) {
                    if (dcSet.getTransactionFinalMapSigns().contains(signature))
                        return false;
                    else
                        return true;
                }
            }
        }

        // all test a not valid for main test
        // all other network must be invalid here!
        int port = Controller.getInstance().getNetworkPort();
        data = Bytes.concat(data, Ints.toByteArray(port));

        if (!Crypto.getInstance().verify(this.creator.getPublicKey(), this.signature, data)) {
            boolean wrong = true;
            for (byte[] item : DISCREDIR_ADDRESSES) {
                if (Arrays.equals(this.creator.getPublicKey(), item)
                        && height < 200000) {
                    byte[] digest = Crypto.getInstance().digest(data);
                    digest = Bytes.concat(digest, digest);
                    if (Arrays.equals(this.signature, digest)) {
                        wrong = false;
                    }
                    break;
                }
            }

            if (wrong)
                return false;

        }

        return true;
    }

    /*
     *  flags
     *   = 1 - not check fee
     *   = 2 - not check person
     *   = 4 - not check PublicText
     */
    public int isValid(int asDeal, long flags) {

        // CHECK IF REFERENCE IS OK
        //Long reference = asDeal == null ? this.creator.getLastTimestamp(dcSet) : asDeal;
        if (asDeal > Transaction.FOR_MYPACK) {
            Long reference = this.creator.getLastTimestamp(dcSet);
            if (this.isReferenced() && reference.compareTo(this.timestamp) >= 0) {
                return INVALID_TIMESTAMP;
            }
        }

        // CHECK CREATOR
        if (!Crypto.getInstance().isValidAddress(this.creator.getAddress())) {
            return INVALID_ADDRESS;
        }

        int height = this.getBlockHeightByParentOrLast(dcSet);
        //if (height <= 0 || height > 1000)
        //    return INVALID_TIMESTAMP;

        // CHECK IT AFTER isPERSON ! because in ignored in IssuePerson
        // CHECK IF CREATOR HAS ENOUGH FEE MONEY
        if ((flags & NOT_VALIDATE_FLAG_FEE) == 0l
                && height > BlockChain.ALL_BALANCES_OK_TO
                && this.creator.getBalance(dcSet, FEE_KEY).a.b.compareTo(this.fee) < 0) {
            return NOT_ENOUGH_FEE;
        }

        if ( (flags & NOT_VALIDATE_FLAG_PUBLIC_TEXT) == 0l
                && this.hasPublicText()
                && (!BlockChain.TRUSTED_ANONYMOUS.contains(this.creator.getAddress())
                || BlockChain.NOVA_ASSETS.get(this.creator.getAddress()) == null)
                && !this.creator.isPerson(dcSet, height)) {
            if (BlockChain.DEVELOP_USE) {
                boolean good = false;
                for (String admin : BlockChain.GENESIS_ADMINS) {
                    if (this.creator.equals(admin)) {
                        good = true;
                        break;
                    }
                }
                if (!good)
                    return CREATOR_NOT_PERSONALIZED;
            } else {
                return CREATOR_NOT_PERSONALIZED;
            }
        }

        if ((flags & NOT_VALIDATE_KEY_COLLISION) == 0l
                && this.dcSet.getTransactionFinalMapSigns().contains(this.signature)) {
            // потому что мы ключ урезали до 12 байт - могут быть коллизии
            return KEY_COLLISION;
        }

        return VALIDATE_OK;

    }

    public void process_gifts(int level, long fee_gift, Account creator, boolean asOrphan,
                              List<R_Calculated> txCalculated, String message) {

        if (fee_gift <= 0l)
            return;

        String messageLevel = message + " level:" + level;
        Tuple4<Long, Integer, Integer, Integer> personDuration = creator.getPersonDuration(this.dcSet);
        if (personDuration == null) {
            // USE all GIFT for current ACCOUNT
            BigDecimal giftBG = BigDecimal.valueOf(fee_gift, BlockChain.FEE_SCALE);
            creator.changeBalance(this.dcSet, asOrphan, FEE_KEY, giftBG, false);
            if (txCalculated != null && !asOrphan) {
                txCalculated.add(new R_Calculated(creator, FEE_KEY, giftBG,
                        messageLevel, this.dbRef));
            }
            return;
        }

        // CREATOR is PERSON
        // FIND person
        ItemCls person = this.dcSet.getItemPersonMap().get(personDuration.a);
        Long inviteredDBRef = this.dcSet.getTransactionFinalMapSigns().get(person.getReference());

        Transaction issueRecord = this.dcSet.getTransactionFinalMap().get(inviteredDBRef);
        Account inviterAccount = issueRecord.getCreator();
        Tuple4<Long, Integer, Integer, Integer> inviterPersonDuration = inviterAccount.getPersonDuration(this.dcSet);

        if (inviterPersonDuration != null && inviterPersonDuration.a.equals(personDuration.a)) {
            // if it SAME perdsn - skip
            process_gifts(--level, fee_gift, inviterAccount, asOrphan, txCalculated, message);
            return;
        }

        if (creator.equals(inviterAccount)
                // EXCLUDE ME
                || Arrays.equals(BlockChain.BONUS_STOP_ACCOUNT, inviterAccount.getShortAddressBytes())
        ) {
            // IT IS ME - all fee!
            BigDecimal giftBG = BigDecimal.valueOf(fee_gift, BlockChain.FEE_SCALE);
            creator.changeBalance(this.dcSet, asOrphan, FEE_KEY, giftBG, false);
            if (txCalculated != null && !asOrphan) {
                txCalculated.add(new R_Calculated(creator, FEE_KEY, giftBG,
                        messageLevel, this.dbRef));
            }
            return;
        }

        // IS INVITER ALIVE ???
        Tuple4<Long, Integer, Integer, Integer> inviterDuration = inviterAccount.getPersonDuration(this.dcSet);
        if (inviterDuration != null) {
            PersonCls inviter = (PersonCls) this.dcSet.getItemPersonMap().get(inviterDuration.a);
            if (!inviter.isAlive(this.timestamp)) {
                // SKIP this LEVEL for DEAD persons
                process_gifts(level, fee_gift, inviterAccount, asOrphan, txCalculated, message);
                return;
            }
        }

        if (level > 1 ) {

            long fee_gift_next = fee_gift >> BlockChain.FEE_INVITED_SHIFT_IN_LEVEL;
            long fee_gift_get = fee_gift - fee_gift_next;

            BigDecimal giftBG = BigDecimal.valueOf(fee_gift_get, BlockChain.FEE_SCALE);
            inviterAccount.changeBalance(this.dcSet, asOrphan, FEE_KEY, giftBG, false);
            if (txCalculated != null && !asOrphan) {
                txCalculated.add(new R_Calculated(inviterAccount, FEE_KEY, giftBG,
                        messageLevel, this.dbRef));
            }

            if (fee_gift_next > 0) {
                process_gifts(--level, fee_gift_next, inviterAccount, asOrphan, txCalculated, message);
            }

        } else {
            // this is END LEVEL
            // GET REST of GIFT
            BigDecimal giftBG = BigDecimal.valueOf(fee_gift, BlockChain.FEE_SCALE);
            inviterAccount.changeBalance(this.dcSet, asOrphan, FEE_KEY, BigDecimal.valueOf(fee_gift, BlockChain.FEE_SCALE), false);
            if (txCalculated != null && !asOrphan) {
                txCalculated.add(new R_Calculated(inviterAccount, FEE_KEY, giftBG,
                        messageLevel, this.dbRef));
            }
        }
    }

    // REST

    // public abstract void process(DBSet db);
    public void process(Block block, int asDeal) {

        if (this.signature != null && Base58.encode(this.signature)
                .equals("nQhYYc4tSM2sPLpiceCWGKhdt5MKhu82LrTM9hCKgh3iyQzUiZ8H7s4niZrgy4LR4Zav1zXD7kra4YWRd3Fstd")) {
            int error = 0;
            error ++;
        }

        this.block = block;

        if (asDeal > Transaction.FOR_PACK) {
            // this.calcFee();

            if (this.fee != null && this.fee.compareTo(BigDecimal.ZERO) != 0) {
                // NOT update INCOME balance
                this.creator.changeBalance(this.dcSet, true, FEE_KEY, this.fee, true);

            }

            // Multi Level Referal
            if (this.height < BlockChain.VERS_4_11 && BlockChain.VERS_4_11_USE_OLD_FEE)
                process_gifts(BlockChain.FEE_INVITED_DEEP, getInvitedFee(), this.creator, false,
                        this.block != null && this.block.txCalculated != null?
                                this.block.txCalculated : null, "referal");

            String creatorAddress = this.creator.getAddress();
            AddressTime_SignatureMap dbASmap = this.dcSet.getAddressTime_SignatureMap();
            if (!dbASmap.contains(creatorAddress)) {
                // for quick search public keys by address - use PUB_KEY from Person DATA owner
                // used in - controller.Controller.getPublicKeyByAddress
                dbASmap.set(creatorAddress, signature); // for quick search
                // public keys
            }
            dbASmap.set(creatorAddress, timestamp, signature); // for search
            // org.erachain.records by
            // time

            // UPDATE REFERENCE OF SENDER
            if (this.isReferenced())
                // IT IS REFERENCED RECORD?
                this.creator.setLastTimestamp(this.timestamp, this.dcSet);
        }

    }

    public void orphan(int asDeal) {

        if (Base58.encode(this.signature)
                .equals("nQhYYc4tSM2sPLpiceCWGKhdt5MKhu82LrTM9hCKgh3iyQzUiZ8H7s4niZrgy4LR4Zav1zXD7kra4YWRd3Fstd")) {
            int error = 0;
            error ++;
        }

        if (asDeal > Transaction.FOR_PACK) {
            if (this.fee != null && this.fee.compareTo(BigDecimal.ZERO) != 0) {
                // NOT update INCOME balance
                this.creator.changeBalance(this.dcSet, false, FEE_KEY, this.fee, true);

            }

            // calc INVITED FEE
            if (this.height < BlockChain.VERS_4_11 && BlockChain.VERS_4_11_USE_OLD_FEE)
                process_gifts(BlockChain.FEE_INVITED_DEEP, getInvitedFee(), this.creator, true,
                        null, null);

            // UPDATE REFERENCE OF SENDER
            if (this.isReferenced()) {
                // IT IS REFERENCED RECORD?
                // set last transaction signature for this ACCOUNT
                this.creator.removeLastTimestamp(this.dcSet);
            }

            this.dcSet.getAddressTime_SignatureMap().delete(creator, timestamp);

        }
    }

    public Transaction copy() {
        try {
            return TransactionFactory.getInstance().parse(this.toBytes(FOR_NETWORK, true), Transaction.FOR_NETWORK);
        } catch (Exception e) {
            return null;
        }
    }

    public abstract HashSet<Account> getInvolvedAccounts();

    /*
     * public boolean isConfirmed() { return
     * this.isConfirmed(DBSet.getInstance()); }
     */

    public abstract HashSet<Account> getRecipientAccounts();

    public abstract boolean isInvolved(Account account);

    @Override
    public boolean equals(Object object) {
        if (object instanceof Transaction) {
            Transaction transaction = (Transaction) object;

            return Arrays.equals(this.getSignature(), transaction.getSignature());
        }

        return false;
    }

    public boolean isConfirmed(DCSet db) {
        if (this.getType() == Transaction.CALCULATED_TRANSACTION) {
            // USE referenced transaction
            return db.getTransactionFinalMap().contains(this.reference);
        }

        return db.getTransactionFinalMapSigns().contains(this.getSignature());
    }

    public int getConfirmations(DCSet db) {

        try {
            // CHECK IF IN UNCONFIRMED TRANSACTION
            if (this.getType() != Transaction.CALCULATED_TRANSACTION) {
                if (!db.getTransactionMap().contains(this)) {
                    return -db.getTransactionMap().getBroadcasts(this);
                }
            }

            // CALCULATE CONFIRMATIONS
            // int lastBlockHeight =
            // db.getBlockSignsMap().getHeight(db.getBlocksHeadMap().getLastBlockSignature());
            // Block block =
            // DBSet.getInstance().getTransactionRef_BlockRef_Map().getParent(this.signature);
            Block block = this.getBlock(db);

            if (block == null)
                return 0;

            int transactionBlockHeight = db.getBlockSignsMap().get(block);

            // RETURN
            return 1 + db.getBlockMap().size() - transactionBlockHeight;

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return 0;
        }
    }

    public int getBlockVersion(DCSet db) {
        // IF ALREADY IN THE BLOCK. CONFIRMED
        if (this.isConfirmed(db)) {
            // return
            // DBSet.getInstance().getTransactionRef_BlockRef_Map().getParent(this.getSignature()).getVersion();
            return this.getBlock(db).getVersion();
        }

        // IF UNCONFIRMED
        return Controller.getInstance().getLastBlock().getNextBlockVersion(db);
    }

    @Override
    public String toString() {
        return Base58.encode(this.signature);
    }

}
