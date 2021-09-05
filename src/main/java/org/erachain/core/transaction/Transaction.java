package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.api.ApiErrorFactory;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.Jsonable;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.blockexplorer.ExplorerJsonLine;
import org.erachain.core.blockexplorer.WebTransactionsHTML;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.ExData;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMapImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.library.ASMutableTreeNode;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.smartcontracts.SmartContract;
import org.erachain.utils.DateTimeFormat;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * SEE in concrete TRANSACTIONS
 * public static final byte[][] VALID_RECORDS = new byte[][]{
 * };
 */

public abstract class Transaction implements ExplorerJsonLine, Jsonable {


    // toBYTE & PARSE fields for different DEALs
    public static final int FOR_MYPACK = 1; // not use this.timestamp & this.feePow
    public static final int FOR_PACK = 2; // not use feePow
    public static final int FOR_NETWORK = 3; // use all (but not calcalated)
    public static final int FOR_DB_RECORD = 4; // use all + calcalated fields (FEE, BlockNo + SeqNo)

    // FLAGS for VALIDATING
    public static final long NOT_VALIDATE_FLAG_FEE = 1L;
    public static final long NOT_VALIDATE_FLAG_PERSONAL = 2L;
    public static final long NOT_VALIDATE_FLAG_PUBLIC_TEXT = 4L;
    public static final long NOT_VALIDATE_FLAG_BALANCE = 8L;
    public static final long NOT_VALIDATE_KEY_COLLISION = 16L;
    public static final long NOT_VALIDATE_ITEM = 32L;

    //
    public static final int MAX_TITLE_BYTES_LENGTH = (1 << 8) - 2;
    public static final int MAX_DATA_BYTES_LENGTH = BlockChain.MAX_REC_DATA_BYTES;

    // VALIDATION CODE
    public static final int JSON_ERROR = -1;
    public static final int VALIDATE_OK = 1;
    public static final int FUTURE_ABILITY = 2;
    public static final int INVALID_WALLET_ADDRESS = 3;
    public static final int INVALID_MAKER_ADDRESS = 5;
    public static final int INVALID_REFERENCE = 6;
    /**
     * Если откат был в ДЕВЕЛОПе и в этом блоке была первая транзакция то потом откат
     */
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

    public static final int HASH_ALREADY_EXIST = 51;
    public static final int NOT_TRANSFERABLE_ASSET = 52;

    public static final int WRONG_SIGNER = 55;

    public static final int INVALID_BALANCE_POS = 56;
    public static final int INVALID_BALANCE_SIDE = 57;

    public static final int INVALID_CLAIM_DEBT_CREATOR = 61;

    public static final int ORDER_ALREADY_COMPLETED = 65;

    public static final int INVALID_AWARD = 81;
    public static final int INVALID_MAX_AWARD_COUNT = 82;

    public static final int INVALID_MAX_ITEMS_COUNT = 85;

    public static final int INVALID_MAX_COUNT = 87;
    public static final int INVALID_ITEM_INDEX = 88;

    public static final int NOT_ENOUGH_ERA_OWN = 101;
    public static final int NOT_ENOUGH_ERA_USE = 102;
    public static final int NOT_ENOUGH_ERA_OWN_10 = 103;
    public static final int NOT_ENOUGH_ERA_USE_10 = 104;
    public static final int NOT_ENOUGH_ERA_OWN_100 = 105;
    public static final int NOT_ENOUGH_ERA_USE_100 = 106;
    public static final int NOT_ENOUGH_ERA_OWN_1000 = 107;
    public static final int NOT_ENOUGH_ERA_USE_1000 = 108;

    public static final int INVALID_BACKWARD_ACTION = 117;
    public static final int INVALID_PERSONALIZY_ANOTHER_PERSON = 118;
    public static final int PUB_KEY_NOT_PERSONALIZED = 119;

    public static final int INVALID_ISSUE_PROHIBITED = 150;
    public static final int INVALID_NAME_LENGTH_MIN = 151;
    public static final int INVALID_NAME_LENGTH_MAX = 152;
    public static final int INVALID_ICON_LENGTH_MIN = 153;
    public static final int INVALID_ICON_LENGTH_MAX = 154;
    public static final int INVALID_IMAGE_LENGTH_MIN = 155;
    public static final int INVALID_IMAGE_LENGTH_MAX = 156;
    public static final int INVALID_DESCRIPTION_LENGTH_MIN = 157;
    public static final int INVALID_DESCRIPTION_LENGTH_MAX = 158;
    public static final int INVALID_VALUE_LENGTH_MIN = 159;
    public static final int INVALID_VALUE_LENGTH_MAX = 160;
    public static final int INVALID_TITLE_LENGTH_MIN = 161;
    public static final int INVALID_TITLE_LENGTH_MAX = 162;
    public static final int INVALID_TAGS_LENGTH_MAX = 163;
    public static final int INVALID_ICON_TYPE = 164;
    public static final int INVALID_IMAGE_TYPE = 165;

    public static final int NOT_DEBTABLE_ASSET = 171;
    public static final int NOT_HOLDABLE_ASSET = 172;
    public static final int NOT_SPENDABLE_ASSET = 173;

    /**
     * Прровека на коллизию ключа по подписи - проверяем только если усекаем его и нетпроверки на двойную трату -
     * BlockChain#CHECK_DOUBLE_SPEND_DEEP
     */
    public static final int KEY_COLLISION = 194;

    public static final int INVALID_MESSAGE_FORMAT = 195;
    public static final int INVALID_MESSAGE_LENGTH = 196;
    public static final int UNKNOWN_PUBLIC_KEY_FOR_ENCRYPT = 197;

    public static final int INVALID_FLAGS = 199;


    // ITEMS
    public static final int INVALID_ITEM_KEY = 201;
    public static final int INVALID_ITEM_VALUE = 202;
    public static final int ITEM_DOES_NOT_EXIST = 203;
    public static final int ITEM_ASSET_NOT_EXIST = 204;
    public static final int ITEM_IMPRINT_DOES_NOT_EXIST = 205;
    public static final int ITEM_TEMPLATE_NOT_EXIST = 206;
    public static final int ITEM_PERSON_NOT_EXIST = 207;
    public static final int ITEM_POLL_NOT_EXIST = 208;
    public static final int ITEM_STATUS_NOT_EXIST = 209;
    public static final int ITEM_UNION_NOT_EXIST = 210;
    public static final int ITEM_DOES_NOT_STATUSED = 211;
    public static final int ITEM_DOES_NOT_UNITED = 212;
    public static final int ITEM_DUPLICATE_KEY = 213;
    public static final int ITEM_DUPLICATE = 214;
    public static final int INVALID_TIMESTAMP_START = 215;
    public static final int INVALID_TIMESTAMP_END = 216;
    public static final int INVALID_OUTSIDE_VALIDATY_PERIOD = 217;

    public static final int INVALID_ASSET_TYPE = 222;

    public static final int ITEM_PERSON_IS_DEAD = 235;
    public static final int AMOUNT_LENGHT_SO_LONG = 236;
    public static final int AMOUNT_SCALE_SO_BIG = 237;
    public static final int AMOUNT_SCALE_WRONG = 238;

    public static final int ITEM_PERSON_LATITUDE_ERROR = 250;
    public static final int ITEM_PERSON_LONGITUDE_ERROR = 251;
    public static final int ITEM_PERSON_RACE_ERROR = 252;
    public static final int ITEM_PERSON_GENDER_ERROR = 253;
    public static final int ITEM_PERSON_SKIN_COLOR_ERROR = 254;
    public static final int ITEM_PERSON_EYE_COLOR_ERROR = 255;
    public static final int ITEM_PERSON_HAIR_COLOR_ERROR = 256;
    public static final int ITEM_PERSON_HEIGHT_ERROR = 257;
    public static final int ITEM_PERSON_OWNER_SIGNATURE_INVALID = 258;

    // NAMES
    public static final int NAME_DOES_NOT_EXIST = 5060;
    public static final int NAME_ALREADY_REGISTRED = 5061;
    public static final int NAME_ALREADY_ON_SALE = 5062;
    public static final int NAME_NOT_FOR_SALE = 5063;
    public static final int BUYER_ALREADY_OWNER = 5064;
    public static final int NAME_NOT_LOWER_CASE = 5065;
    public static final int NAME_WITH_SPACE = 5066;

    public static final int CREATOR_NOT_MAKER = 366;
    public static final int CREATOR_NOT_OWNER = 367;
    public static final int NAME_KEY_ALREADY_EXISTS = 368;
    public static final int NAME_KEY_NOT_EXISTS = 369;
    public static final int LAST_KEY_IS_DEFAULT_KEY = 370;

    // POLL
    public static final int INVALID_OPTIONS_LENGTH = 380;
    public static final int INVALID_OPTION_LENGTH = 381;
    public static final int DUPLICATE_OPTION = 382;
    public static final int POLL_ALREADY_CREATED = 383;
    public static final int POLL_ALREADY_HAS_VOTES = 384;
    public static final int POLL_NOT_EXISTS = 385;
    public static final int POLL_OPTION_NOT_EXISTS = 386;
    public static final int ALREADY_VOTED_FOR_THAT_OPTION = 387;
    public static final int INVALID_DATA_LENGTH = 388;
    public static final int INVALID_DATA = 389;
    public static final int INVALID_PARAMS_LENGTH = 390;
    public static final int INVALID_URL_LENGTH = 391;
    public static final int INVALID_TITLE_LENGTH = 392;
    public static final int INVALID_DATA_FORMAT = 393;

    public static final int TX_NOT_FOUND = 400;

    public static final int INVALID_EX_LINK_TYPE = 401;
    public static final int INVALID_EX_LINK_REF = 402;
    public static final int INVALID_RECEIVERS_LIST = 403;

    public static final int INVALID_EPOCH_SMART_CONTRCT = 451;

    public static final int INVALID_BLOCK_TRANS_SEQ_ERROR = 501;
    public static final int ACCOUNT_ACCSES_DENIED = 520;

    public static final int PRIVATE_KEY_NOT_FOUND = 530;
    public static final int INVALID_UPDATE_VALUE = 540;
    public static final int INVALID_TRANSACTION_TYPE = 550;
    public static final int INVALID_BLOCK_HEIGHT = 599;
    public static final int TELEGRAM_DOES_NOT_EXIST = 541;
    public static final int NOT_YET_RELEASED = 599;
    public static final int AT_ERROR = 600; // END error for org.erachain.api.ApiErrorFactory.ERROR

    // 
    // TYPES *******
    // universal
    public static final int EXTENDED = 1;
    // genesis
    public static final int GENESIS_ISSUE_ASSET_TRANSACTION = 1;
    public static final int GENESIS_ISSUE_TEMPLATE_TRANSACTION = 2;
    public static final int GENESIS_ISSUE_PERSON_TRANSACTION = 3;
    public static final int GENESIS_ISSUE_STATUS_TRANSACTION = 4;
    public static final int GENESIS_ISSUE_UNION_TRANSACTION = 5;
    public static final int GENESIS_SEND_ASSET_TRANSACTION = 6;
    public static final int GENESIS_SIGN_NOTE_TRANSACTION = 7;
    public static final int GENESIS_CERTIFY_PERSON_TRANSACTION = 8;
    public static final int GENESIS_ASSIGN_STATUS_TRANSACTION = 9;
    public static final int GENESIS_ADOPT_UNION_TRANSACTION = 10;
    // ISSUE ITEMS
    public static final int ISSUE_ASSET_TRANSACTION = 21;
    public static final int ISSUE_IMPRINT_TRANSACTION = 22;
    public static final int ISSUE_TEMPLATE_TRANSACTION = 23;
    public static final int ISSUE_PERSON_TRANSACTION = 24;
    public static final int ISSUE_STATUS_TRANSACTION = 25;
    public static final int ISSUE_UNION_TRANSACTION = 26;
    public static final int ISSUE_STATEMENT_TRANSACTION = 27;
    public static final int ISSUE_POLL_TRANSACTION = 28;
    // SEND ASSET
    public static final int SEND_ASSET_TRANSACTION = 31;
    // OTHER
    public static final int SIGN_NOTE_TRANSACTION = 35;
    public static final int CERTIFY_PUB_KEYS_TRANSACTION = 36;
    public static final int SET_STATUS_TO_ITEM_TRANSACTION = 37;
    public static final int SET_UNION_TO_ITEM_TRANSACTION = 38;
    public static final int SET_UNION_STATUS_TO_ITEM_TRANSACTION = 39;
    // confirm other transactions
    public static final int SIGN_TRANSACTION = 40;
    // HASHES
    public static final int HASHES_RECORD = 41;

    public static final int ISSUE_ASSET_SERIES_TRANSACTION = 42;

    // exchange of assets
    public static final int CREATE_ORDER_TRANSACTION = 50;
    public static final int CANCEL_ORDER_TRANSACTION = 51;
    public static final int CHANGE_ORDER_TRANSACTION = 52;
    // voting
    public static final int CREATE_POLL_TRANSACTION = 61;
    public static final int VOTE_ON_POLL_TRANSACTION = 62;
    public static final int VOTE_ON_ITEM_POLL_TRANSACTION = 63;
    public static final int RELEASE_PACK = 70;

    public static final int CALCULATED_TRANSACTION = 100;

    // old
    public static final int ARBITRARY_TRANSACTION = 12 + 130;
    public static final int MULTI_PAYMENT_TRANSACTION = 13 + 130;
    public static final int DEPLOY_AT_TRANSACTION = 14 + 130;

    // FEE PARAMETERS
    public static final long RIGHTS_KEY = AssetCls.ERA_KEY;
    public static final long BTC_KEY = AssetCls.ERA_KEY;

    public static final long FEE_KEY = AssetCls.FEE_KEY;

    public static final int TIMESTAMP_LENGTH = 8;

    public static final int REFERENCE_LENGTH = TIMESTAMP_LENGTH;

    public static final int KEY_LENGTH = 8;
    public static final int SIGNATURE_LENGTH = Crypto.SIGNATURE_LENGTH;

    // PROPERTIES LENGTH
    protected static final int SIMPLE_TYPE_LENGTH = 1;
    public static final int TYPE_LENGTH = 4;
    protected static final int HEIGHT_LENGTH = 4;
    public static final int DATA_JSON_PART_LENGTH = 4;
    public static final int DATA_VERSION_PART_LENGTH = 6;
    public static final int DATA_TITLE_PART_LENGTH = 4;
    protected static final int DATA_NUM_FILE_LENGTH = 4;
    protected static final int SEQ_LENGTH = Integer.BYTES;
    public static final int DBREF_LENGTH = Long.BYTES;
    public static final int DATA_SIZE_LENGTH = Integer.BYTES;
    public static final int ENCRYPTED_LENGTH = 1;
    public static final int IS_TEXT_LENGTH = 1;
    protected static final int FEE_POWER_LENGTH = 1;
    public static final int FEE_LENGTH = 8;
    public static final int CREATOR_LENGTH = PublicKeyAccount.PUBLIC_KEY_LENGTH;
    protected static final int BASE_LENGTH_AS_MYPACK = TYPE_LENGTH;
    protected static final int BASE_LENGTH_AS_PACK = BASE_LENGTH_AS_MYPACK + TIMESTAMP_LENGTH
            + CREATOR_LENGTH + SIGNATURE_LENGTH;
    protected static final int BASE_LENGTH = BASE_LENGTH_AS_PACK + FEE_POWER_LENGTH + REFERENCE_LENGTH;
    protected static final int BASE_LENGTH_AS_DBRECORD = BASE_LENGTH + TIMESTAMP_LENGTH + FEE_LENGTH;

    /**
     * Используется для разделения строки поисковых слов для всех трнзакций.<br>
     * % @ # - пусть они будут служебные и по ним не делать разделения
     * так чтобы можно было найти @P указатель на персон например
     * % - это указатель на параметр например иак - %1
     * see https://regex101.com/
     */
    public static String SPLIT_CHARS = "[!?/_.,\\~+&^№*=;:][\\s$]|[()<>\\\"\\'|\\[\\]{}\\\\]|[\\s]";

    // in pack toByte and Parse - reference not included
    static Logger LOGGER = LoggerFactory.getLogger(Transaction.class.getName());

    protected DCSet dcSet;
    protected String TYPE_NAME = "unknown";

    /////////   MASKS amd PARS
    public static final byte HAS_EXLINK_MASK = 32;
    public static final byte HAS_SMART_CONTRACT_MASK = 16;
    /**
     * typeBytes[2] | HAS_EXLINK_MASK | HAS_SMART_CONTRACT_MASK
     */
    protected byte[] typeBytes;

    protected int height;
    protected int seqNo;
    protected long dbRef; // height + SeqNo

    // TODO REMOVE REFERENCE - use TIMESTAMP as reference
    protected Long reference = 0L;
    protected BigDecimal fee = BigDecimal.ZERO; // - for genesis
    /**
     * Если еще и комиссия с перечисляемого актива - то не НУЛЬ
     */
    public BigDecimal assetFee = null;
    /**
     * Если еще и комиссия с перечисляемого актива - то не НУЛЬ
     */
    public BigDecimal assetFeeBurn = null;

    // transactions
    protected byte feePow = 0;
    protected byte[] signature;
    protected long timestamp;
    protected PublicKeyAccount creator;
    protected Fun.Tuple4<Long, Integer, Integer, Integer> creatorPersonDuration;
    protected PersonCls creatorPerson;

    /**
     * Для создания поисковых Меток - Тип сущности + номер ее (например @P12 - персона 12) + Метки (Tags) от самой Сущности
     */
    protected Object[][] itemsKeys;

    protected ExLink exLink;
    protected SmartContract smartContract;

    /**
     * если да то значит взята из Пула трнзакций и на двойную трату проверялась
     */
    public boolean checkedByPool;

    public String errorValue;

    // need for genesis
    protected Transaction(byte type, String type_name) {
        this.typeBytes = new byte[]{type, 0, 0, 0}; // for GENESIS
        this.TYPE_NAME = type_name;
    }

    protected Transaction(byte[] typeBytes, String type_name, PublicKeyAccount creator, ExLink exLink, SmartContract smartContract, byte feePow, long timestamp,
                          Long reference) {
        this.typeBytes = typeBytes;
        this.TYPE_NAME = type_name;
        this.creator = creator;
        if (exLink != null) {
            typeBytes[2] = (byte) (typeBytes[2] | HAS_EXLINK_MASK);
            this.exLink = exLink;
        }

        this.smartContract = smartContract;

        // this.props = props;
        this.timestamp = timestamp;
        this.reference = reference;
        if (feePow < 0)
            feePow = 0;
        else if (feePow > BlockChain.FEE_POW_MAX)
            feePow = BlockChain.FEE_POW_MAX;
        this.feePow = feePow;
    }

    protected Transaction(byte[] typeBytes, String type_name, PublicKeyAccount creator, ExLink exLink, byte feePow, long timestamp,
                          Long reference, byte[] signature) {
        this(typeBytes, type_name, creator, exLink, null, feePow, timestamp, reference);
        this.signature = signature;
    }

    public static int getVersion(byte[] typeBytes) {
        return Byte.toUnsignedInt(typeBytes[1]);
    }


    public static Transaction findByHeightSeqNo(DCSet db, int height, int seq) {
        return db.getTransactionFinalMap().get(height, seq);
    }

    @Override
    public int hashCode() {
        return Ints.fromByteArray(signature);
    }

    @Override
    public boolean equals(Object transaction) {
        if (transaction instanceof Transaction)
            return Arrays.equals(this.signature, ((Transaction) transaction).signature);
        return false;
    }

    public boolean trueEquals(Object transaction) {
        if (transaction == null)
            return false;
        else if (transaction instanceof Transaction)
            return Arrays.equals(this.toBytes(FOR_NETWORK, true),
                    ((Transaction) transaction).toBytes(FOR_NETWORK, true));
        return false;
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
                return db.getTransactionTab().get(dbRef);
            }
        } else {
            int heightBlock = Ints.fromByteArray(Arrays.copyOfRange(dbRef, 0, 4));
            int seqNo = Ints.fromByteArray(Arrays.copyOfRange(dbRef, 4, 8));
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

    public void setHeightSeq(long seqNo) {
        this.dbRef = seqNo;
        this.height = parseHeightDBRef(seqNo);
        this.seqNo = (int) seqNo;
    }

    public void setHeightSeq(int height, int seqNo) {
        this.dbRef = makeDBRef(height, seqNo);
        this.height = height;
        this.seqNo = seqNo;
    }

    public void setErrorValue(String value) {
        errorValue = value;
    }

    public static boolean isValidTransactionType(int type) {
        return !viewTypeName(type).equals("unknown");
    }

    public String getErrorValue() {
        return errorValue;
    }

    /**
     * NEED FOR DB SECONDATY KEYS see org.mapdb.Bind.secondaryKeys
     *
     * @param dcSet
     * @param andUpdateFromState если нужно нарастить мясо на скелет из базв Финал. Не нужно для неподтвержденных
     *                           и если ее нет в базе еще. Используется только для вычисления номера Сущности для отображения Выпускающих трнзакций - после их обработки, например в Блокэксплоере чтобы посмотреть какой актив был этой трнзакцией выпущен.
     */
    public void setDC(DCSet dcSet, boolean andUpdateFromState) {
        this.dcSet = dcSet;

        if (BlockChain.TEST_DB == 0 && creator != null) {
            creatorPersonDuration = creator.getPersonDuration(dcSet);
            if (creatorPersonDuration != null) {
                creatorPerson = (PersonCls) dcSet.getItemPersonMap().get(creatorPersonDuration.a);
            }
        }

        if (andUpdateFromState && !isWiped())
            updateFromStateDB();
    }

    public void setDC(DCSet dcSet) {
        setDC(dcSet, false);
    }

    /**
     * Нужно для наполнения данными для isValid & process
     *
     * @param dcSet
     * @param forDeal
     * @param blockHeight
     * @param seqNo
     * @param andUpdateFromState если нужно нарастить мясо на скелет из базв Финал. Не нужно для неподтвержденных
     *                           и если ее нет в базе еще. Используется только для вычисления номера Сущности для отображения Выпускающих трнзакций - после их обработки, например в Блокэксплоере чтобы посмотреть какой актив был этой трнзакцией выпущен.
     */
    public void setDC(DCSet dcSet, int forDeal, int blockHeight, int seqNo, boolean andUpdateFromState) {
        setDC(dcSet, false);
        this.height = blockHeight; //this.getBlockHeightByParentOrLast(dcSet);
        this.seqNo = seqNo;
        this.dbRef = Transaction.makeDBRef(height, seqNo);
        if (forDeal > Transaction.FOR_PACK && (this.fee == null || this.fee.signum() == 0))
            this.calcFee(true);

        if (andUpdateFromState && !isWiped())
            updateFromStateDB();
    }

    public void setDC(DCSet dcSet, int forDeal, int blockHeight, int seqNo) {
        setDC(dcSet, forDeal, blockHeight, seqNo, false);
    }

    /**
     * Нарастить мясо на скелет из базы состояния - нужно для:<br>
     * - записи в FinalMap b созданим вторичных ключей и Номер Сущности<br>
     * - для внесения в кошелек когда блок прилетел и из него сырые транзакции берем
     */
    public void updateFromStateDB() {
    }

    public boolean noDCSet() {
        return this.dcSet == null;
    }

    public DCSet getDCSet() {
        return this.dcSet;
    }

    public int getType() {
        return Byte.toUnsignedInt(this.typeBytes[0]);
    }

    public static Integer[] getTransactionTypes(boolean onlyUsed) {

        // SEND ASSET
        // OTHER
        // confirm other transactions
        // HASHES
        // exchange of assets
        // voting
        Integer[] list = new Integer[]{
                0,
                ISSUE_ASSET_TRANSACTION,
                ISSUE_IMPRINT_TRANSACTION,
                ISSUE_TEMPLATE_TRANSACTION,
                ISSUE_PERSON_TRANSACTION,
                ISSUE_STATUS_TRANSACTION,
                onlyUsed ? -1 : ISSUE_UNION_TRANSACTION,
                ISSUE_STATEMENT_TRANSACTION,
                ISSUE_POLL_TRANSACTION,

                // SEND ASSET
                SEND_ASSET_TRANSACTION,

                // OTHER
                SIGN_NOTE_TRANSACTION,
                CERTIFY_PUB_KEYS_TRANSACTION,
                SET_STATUS_TO_ITEM_TRANSACTION,
                onlyUsed ? -1 : SET_UNION_TO_ITEM_TRANSACTION,
                onlyUsed ? -1 : SET_UNION_STATUS_TO_ITEM_TRANSACTION,

                // confirm other transactions
                SIGN_TRANSACTION,

                // HASHES
                HASHES_RECORD,

                // exchange of assets
                CREATE_ORDER_TRANSACTION,
                CANCEL_ORDER_TRANSACTION,
                CHANGE_ORDER_TRANSACTION,

                ISSUE_ASSET_SERIES_TRANSACTION,

                // voting
                VOTE_ON_ITEM_POLL_TRANSACTION

        };

        if (onlyUsed) {
            ArrayList<Integer> tmp = new ArrayList<>();
            for (Integer type : list) {
                if (type < 0)
                    continue;
                tmp.add(type);
            }
            return tmp.toArray(new Integer[tmp.size()]);

        }
        return list;
    }

    public static String viewTypeName(int type) {
        switch (type) {
            case ISSUE_ASSET_TRANSACTION:
                return IssueAssetTransaction.TYPE_NAME;
            case ISSUE_IMPRINT_TRANSACTION:
                return IssueImprintRecord.TYPE_NAME;
            case ISSUE_TEMPLATE_TRANSACTION:
                return IssueTemplateRecord.TYPE_NAME;
            case ISSUE_PERSON_TRANSACTION:
                return IssuePersonRecord.TYPE_NAME;
            case ISSUE_STATUS_TRANSACTION:
                return IssueStatusRecord.TYPE_NAME;
            case ISSUE_UNION_TRANSACTION:
                return IssueUnionRecord.TYPE_NAME;
            case ISSUE_STATEMENT_TRANSACTION:
                return IssueStatementRecord.TYPE_NAME;
            case ISSUE_POLL_TRANSACTION:
                return IssuePollRecord.TYPE_NAME;

            // SEND ASSET
            case SEND_ASSET_TRANSACTION:
                return RSend.TYPE_NAME;

            // OTHER
            case SIGN_NOTE_TRANSACTION:
                return RSignNote.TYPE_NAME;
            case CERTIFY_PUB_KEYS_TRANSACTION:
                return RCertifyPubKeys.TYPE_NAME;
            case SET_STATUS_TO_ITEM_TRANSACTION:
                return RSetStatusToItem.TYPE_NAME;
            case SET_UNION_TO_ITEM_TRANSACTION:
                return RSetUnionToItem.TYPE_NAME;
            case SET_UNION_STATUS_TO_ITEM_TRANSACTION:
                return RSetUnionStatusToItem.TYPE_NAME;

            // confirm other transactions
            case SIGN_TRANSACTION:
                return RVouch.TYPE_NAME;

            // HASHES
            case HASHES_RECORD:
                return RHashes.TYPE_NAME;

            // exchange of assets
            case CREATE_ORDER_TRANSACTION:
                return CreateOrderTransaction.TYPE_NAME;
            case CANCEL_ORDER_TRANSACTION:
                return CancelOrderTransaction.TYPE_NAME;
            case CHANGE_ORDER_TRANSACTION:
                return ChangeOrderTransaction.TYPE_NAME;
            case ISSUE_ASSET_SERIES_TRANSACTION:
                return IssueAssetSeriesTransaction.TYPE_NAME;

            // voting
            case VOTE_ON_ITEM_POLL_TRANSACTION:
                return VoteOnItemPollTransaction.TYPE_NAME;

        }
        return "unknown";
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

    // for test signature only!!!
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getDeadline() {
        return this.timestamp + 5 * BlockChain.UNCONFIRMED_DEADTIME_MS(this.timestamp);
    }

    public long getKey() {
        return 0L;
    }

    public Object[][] getItemsKeys() {
        if (itemsKeys == null)
            makeItemsKeys();

        return itemsKeys;
    }

    public long getAbsKey() {
        long key = this.getKey();
        if (key < 0)
            return -key;
        return key;
    }

    public String getTypeKey() {
        return "";
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
        if (this.creator != null)
            if (this.creator.getAddress().equals(account))
                return this.fee;
        return BigDecimal.ZERO;
    }

    public BigDecimal getFee() {
        return this.fee;
    }

    public long getFeeLong() {
        return this.fee.unscaledValue().longValue();
    }

    public String getTitle() {
        return "";
    }

    public String getTitle(JSONObject langObj) {
        return getTitle();
    }

    public ExLink getExLink() {
        return exLink;
    }

    public SmartContract getSmartContract() {
        return smartContract;
    }

    public void makeItemsKeys() {
        if (isWiped()) {
            itemsKeys = new Object[][]{};
        }

        if (creatorPersonDuration != null) {
            itemsKeys = new Object[][]{
                    new Object[]{ItemCls.PERSON_TYPE, creatorPersonDuration.a, creatorPerson.getTags()}
            };
        }
    }

    public static String[] tags(int typeID, String type, String tags, String words, Object[][] itemsKeys) {

        String allTags = "@TT" + typeID;

        if (type != null)
            allTags += " " + type;

        if (tags != null)
            allTags += " " + tags;


        if (words != null)
            allTags += " " + words;

        String[] tagsWords = allTags.toLowerCase().split(SPLIT_CHARS);

        if (itemsKeys == null || itemsKeys.length == 0)
            return tagsWords;

        String[] tagsArray = new String[tagsWords.length + itemsKeys.length];

        System.arraycopy(tagsWords, 0, tagsArray, 0, tagsWords.length);
        List<String> exTagsList = new ArrayList();
        for (int i = tagsWords.length; i < tagsArray.length; i++) {
            try {
                Object[] itemKey = itemsKeys[i - tagsWords.length];
                tagsArray[i] = ItemCls.getItemTypeAndKey((int) itemKey[0], (Long) itemKey[1]).toLowerCase();
                // возможно там есть дополнительные метка
                if (// false && // пока отключим
                        typeID != CALCULATED_TRANSACTION && // все форжинговые и вычисляемые пропустим
                                itemKey.length > 2 && itemKey[2] != null) {
                    for (Object exTag : (Object[]) itemKey[2]) {
                        exTagsList.add(ItemCls.getItemTypeAndTag((int) itemKey[0], exTag.toString()).toLowerCase());
                    }
                }
            } catch (Exception e) {
                LOGGER.error("itemsKeys[" + i + "] = " + itemsKeys[i - tagsWords.length].toString());
                throw (e);
            }
        }

        if (!exTagsList.isEmpty()) {
            exTagsList.addAll(Arrays.asList(tagsArray));
            tagsArray = exTagsList.toArray(tagsArray);
        }

        return tagsArray;
    }

    public String getExTags() {
        return null;
    }

    /**
     * При удалении - транзакция то берется из базы для создания индексов к удалению.
     * И она скелет - нужно базу данных задать и водтянуть номера сущностей и все заново просчитать чтобы правильно удалить метки.
     * Для этого проверку делаем в таблтцк при создании индексов
     *
     * @return
     */
    public String[] getTags() {

        if (itemsKeys == null)
            makeItemsKeys();

        try {
            return tags(getType(), viewTypeName(), getExTags(), getTitle(), itemsKeys);
        } catch (Exception e) {
            LOGGER.error(toString() + " - itemsKeys.len: " + itemsKeys.length);
            throw e;
        }
    }

    /*
     * public Long getReference() { return this.reference; }
     */

    public byte getFeePow() {
        return this.feePow;
    }

    public long getAssetKey() {
        return 0L;
    }

    public AssetCls getAsset() {
        return null;
    }

    public byte[] getSignature() {
        return this.signature;
    }

    public long getReference() {
        return this.reference;
    }

    public List<byte[]> getOtherSignatures() {
        return null;
    }

    public static boolean checkIsFinal(DCSet dcSet, Transaction transaction) {
        Long dbRefFinal = dcSet.getTransactionFinalMapSigns().get(transaction.getSignature());
        if (dbRefFinal == null)
            return false;
        Tuple2<Integer, Integer> ref = parseDBRef(dbRefFinal);
        transaction.setDC(dcSet, FOR_DB_RECORD, ref.a, ref.b, true);

        return true;
    }

    /**
     * Постраничный поиск по строке поиска
     *
     * @param offest
     * @param filterStr
     * @param useForge
     * @param pageSize
     * @param fromID
     * @param fillFullPage
     * @return
     */
    public static Tuple3<Long, Long, List<Transaction>> searchTransactions(
            DCSet dcSet, String filterStr, boolean useForge, int pageSize, Long fromID, int offset, boolean fillFullPage) {

        List<Transaction> transactions = new ArrayList<>();

        TransactionFinalMapImpl map = dcSet.getTransactionFinalMap();

        if (filterStr != null && !filterStr.isEmpty()) {
            if (Base58.isExtraSymbols(filterStr)) {
                try {
                    Long dbRef = parseDBRef(filterStr);
                    if (dbRef != null) {
                        Transaction one = map.get(dbRef);
                        if (one != null) {
                            transactions.add(one);
                        }
                    }
                } catch (Exception e1) {
                }

            } else {
                try {
                    byte[] signature = Base58.decode(filterStr);
                    Transaction one = map.get(signature);
                    if (one != null) {
                        transactions.add(one);
                    }
                } catch (Exception e2) {
                }
            }
        }

        if (filterStr == null) {
            transactions = map.getTransactionsFromID(fromID, offset, pageSize, !useForge, fillFullPage);
        } else {
            transactions.addAll(map.getTransactionsByTitleFromID(filterStr, fromID,
                    offset, pageSize, fillFullPage));
        }

        if (transactions.isEmpty()) {
            // возможно вниз вышли за границу
            return new Tuple3<>(fromID, null, transactions);
        } else {
            return new Tuple3<>(
                    // включим ссылки на листание вверх
                    transactions.get(0).dbRef,
                    // это не самый конец - включим листание вниз
                    transactions.get(transactions.size() - 1).dbRef,
                    transactions);
        }

    }


    /**
     * Общий для всех проверка на допуск публичного сообщения
     *
     * @param title
     * @param data
     * @param isText
     * @param isEncrypted
     * @param message
     * @return
     */
    public static boolean hasPublicText(String title, byte[] data, boolean isText, boolean isEncrypted, String message) {
        String[] words = title.split(Transaction.SPLIT_CHARS);
        int length = 0;
        for (String word : words) {
            word = word.trim();
            if (Base58.isExtraSymbols(word)) {
                // все слова сложим по длинне
                length += word.length();
                if (length > (BlockChain.TEST_MODE ? 100 : 100))
                    return true;
            }
        }

        if ((data == null || data.length == 0) && (message == null || message.isEmpty()))
            return false;

        if (isText && !isEncrypted) {
            String text = message == null ? new String(data, StandardCharsets.UTF_8) : message;
            if ((text.contains(" ") || text.contains("_") || text.contains("-")) && text.length() > 100)
                return true;
        }
        return false;
    }

    /**
     * может ли быть трнзакция бесплатной?
     *
     * @return
     */
    public boolean isFreeFee() {
        return true;
    }

    public abstract boolean hasPublicText();

    public int getJobLevel() {
        return 0;
    }

    // get fee
    public long calcBaseFee(boolean withFreeProtocol) {
        int len = getFeeLength();
        if (withFreeProtocol && height > BlockChain.FREE_FEE_FROM_HEIGHT && seqNo <= BlockChain.FREE_FEE_TO_SEQNO
                && len < BlockChain.FREE_FEE_LENGTH) {
            // не учитываем комиссию если размер блока маленький
            return 0L;
        }

        return len * BlockChain.FEE_PER_BYTE;
    }

    // calc FEE by recommended and feePOW
    public void calcFee(boolean withFreeProtocol) {

        long fee_long = calcBaseFee(withFreeProtocol);
        if (fee_long == 0) {
            this.fee = BigDecimal.ZERO;
            return;
        }

        BigDecimal fee = new BigDecimal(fee_long).multiply(BlockChain.FEE_RATE).setScale(BlockChain.FEE_SCALE, BigDecimal.ROUND_UP);

        if (this.feePow > 0) {
            this.fee = fee.multiply(new BigDecimal(BlockChain.FEE_POW_BASE).pow(this.feePow)).setScale(BlockChain.FEE_SCALE, BigDecimal.ROUND_UP);
        } else {
            this.fee = fee;
        }
    }

    // GET forged FEE without invited FEE
    public long getForgedFee() {
        long fee = this.fee.unscaledValue().longValue();
        return fee - this.getInvitedFee() - this.getRoyaltyFee();
    }

    /**
     * Сколько на другие проценты уйдет - например создателю шаблона
     *
     * @return
     */
    public long getRoyaltyFee() {
        return 0L;
    }

    // GET only INVITED FEE
    public long getInvitedFee() {

        if (BlockChain.FEE_INVITED_DEEP <= 0 || !BlockChain.REFERAL_BONUS_FOR_PERSON(height)) {
            // SWITCH OFF REFERRAL
            return 0L;
        }

        Tuple4<Long, Integer, Integer, Integer> personDuration = creator.getPersonDuration(this.dcSet);
        if (personDuration == null
                || personDuration.a <= BlockChain.BONUS_STOP_PERSON_KEY) {
            // ANONYMOUS or ME
            return 0L;
        }

        long fee = this.fee.unscaledValue().longValue() - getRoyaltyFee();
        if (fee <= 0)
            return 0L;

        // Если слишком большая комиссия, то и награду чуток увеличим
        if (fee > BlockChain.BONUS_REFERAL << 4)
            return BlockChain.BONUS_REFERAL << 1;
        else if (fee < BlockChain.BONUS_REFERAL << 1) {
            // стандартно если обычная то половину отправим на подарки
            return fee >> 1;
        }

        // если повышенная то не будем изменять
        return BlockChain.BONUS_REFERAL;
    }

    public BigDecimal feeToBD(int fee) {
        return BigDecimal.valueOf(fee, BlockChain.FEE_SCALE);
    }

    public Tuple2<Integer, Integer> getHeightSeqNo() {
        return new Tuple2<Integer, Integer>(this.height, this.seqNo);
    }

    public int getBlockHeight() {

        if (this.height > 0)
            return this.height;

        return -1;
    }

    // get current or last
    public int getBlockHeightByParentOrLast(DCSet dc) {

        if (this.height > 0)
            return this.height;

        return dc.getBlocksHeadsMap().size() + 1;
    }

    public int getSeqNo() {
        return this.seqNo;
    }

    public long getDBRef() {
        return this.dbRef;
    }

    public byte[] getDBRefAsBytes() {
        return Longs.toByteArray(this.dbRef);
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
        Bytes.concat(ref, Ints.toByteArray(this.getSeqNo()));
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

    public static Long parseDBRef(String refStr) {
        if (refStr == null)
            return null;

        Long seqNo = parseDBRefSeqNo(refStr);
        if (seqNo != null)
            return seqNo;

        try {
            return Long.parseLong(refStr);
        } catch (Exception e1) {
        }

        return null;
    }

    public static Long parseDBRefSeqNo(String refStr) {
        if (refStr == null)
            return null;

        try {
            String[] strA = refStr.split("\\-");
            if (strA.length > 2)
                // это скорее всег время типа 2020-10-11
                return null;

            int height = Integer.parseInt(strA[0]);
            int seq = Integer.parseInt(strA[1]);
            byte[] ref = Ints.toByteArray(height);
            return Longs.fromByteArray(Bytes.concat(ref, Ints.toByteArray(seq)));
        } catch (Exception e) {
        }
        return null;
    }

    public static Tuple2<Integer, Integer> parseDBRef(Long dbRef) {

        byte[] bytes = Longs.toByteArray(dbRef);

        int blockHeight = Ints.fromByteArray(Arrays.copyOfRange(bytes, 0, 4));
        int seqNo = Ints.fromByteArray(Arrays.copyOfRange(bytes, 4, 8));

        return new Tuple2<Integer, Integer>(blockHeight, seqNo);

    }

    public static int parseHeightDBRef(long dbRef) {
        return (int) (dbRef >> 32);
    }

    public boolean addCalculated(Block block, Account creator, long assetKey, BigDecimal amount,
                                 String message) {

        if (block != null) {
            block.addCalculated(creator, assetKey, amount,
                    message, this.dbRef);
            return true;
        }
        return false;
    }

    public Fun.Tuple4<Long, Integer, Integer, Integer> getCreatorPersonDuration() {
        return creatorPersonDuration;
    }

    public boolean isCreatorPersonalized() {
        return creatorPersonDuration != null;
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

    public static String viewDBRef(int blockHeight, int seqNo) {
        return blockHeight + "-" + seqNo;
    }

    public String viewHeightSeq() {
        return this.height + "-" + this.seqNo;
    }

    public String viewAmount(Account account) {
        return account == null ? "" : viewAmount(account.getAddress());
    }

    public String viewAmount(String address) {
        return "";
    }

    public String viewCreator() {
        return viewAccount(creator);
    }

    public static String viewAccount(Account account) {
        return account == null ? "GENESIS" : account.getPersonAsString();
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
        return viewTimestamp(timestamp);
    }

    public static String viewTimestamp(long timestamp) {
        return timestamp < 1000 ? "null" : DateTimeFormat.timestamptoString(timestamp);
    }

    public int viewSize(int forDeal) {
        return getDataLength(forDeal, true);
    }

    // PARSE/CONVERT

    public String viewFeeLong() {
        return feePow + ":" + this.fee.unscaledValue().longValue();
    }

    public String viewFeeAndFiat(int fontSize) {

        int imgSize = (int) (1.4 * fontSize);
        String fileName = "images" + File.separator + "icons" + File.separator + "assets" + File.separator + AssetCls.FEE_NAME + ".png";
        String text = "<span style='vertical-align: 10px; font-size: 1.4em' ><b>" + fee.toString() + "</b>"
                + "<img width=" + imgSize + " height=" + imgSize
                + " src='file:" + fileName + "'></span>";

        boolean useDEX = Settings.getInstance().getCompuRateUseDEX();

        AssetCls asset = Controller.getInstance().getAsset(Settings.getInstance().getCompuRateAsset());
        if (asset == null)
            asset = Controller.getInstance().getAsset(840L); // ISO-USD

        if (asset == null)
            asset = Controller.getInstance().getAsset(1L); // ERA

        BigDecimal compuRate;
        if (useDEX) {
            Trade lastTrade = DCSet.getInstance().getTradeMap().getLastTrade(AssetCls.FEE_KEY, asset.getKey(), false);
            if (lastTrade == null) {
                compuRate = BigDecimal.ZERO;
            } else {
                compuRate = lastTrade.getHaveKey() == AssetCls.FEE_KEY ? lastTrade.calcPriceRevers() : lastTrade.calcPrice();
            }

        } else {
            compuRate = new BigDecimal(Settings.getInstance().getCompuRate());
        }

        if (compuRate.signum() > 0) {
            BigDecimal fee_fiat = fee.multiply(compuRate).setScale(asset.getScale(), BigDecimal.ROUND_HALF_UP);
            if (asset.getKey() != AssetCls.FEE_KEY) {
                text += " (" + fee_fiat.toString();
                fileName = "images" + File.separator + "icons" + File.separator + "assets" + File.separator + asset.getName() + ".png";
                File file = new File(fileName);
                if (file.exists()) {
                    text += "<img width=" + imgSize + " height=" + imgSize
                            + " src='file:" + fileName + "'>";
                } else {
                    text += " " + asset.getTickerName();
                }

                text += ")";

            }
        }

        return text;
    }

    public String viewItemName() {
        return "";
    }

    public String viewAmount() {
        return "";
    }

    public boolean hasLinkRecipients() {
        return false;
    }

    public DefaultMutableTreeNode viewLinksTree() {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(this);

        exLink = getExLink();
        if (exLink != null) {
            Transaction parentTX = dcSet.getTransactionFinalMap().get(getExLink().getRef());
            ASMutableTreeNode parent = new ASMutableTreeNode(
                    Lang.T(exLink.viewTypeName(hasLinkRecipients())) + " "
                            + Lang.T("for # для"));
            parent.add(new DefaultMutableTreeNode(parentTX));
            root.add(parent);
        }

        try (IteratorCloseable<Tuple3<Long, Byte, Long>> iterator = dcSet.getExLinksMap()
                .getTXLinksIterator(dbRef, ExData.LINK_APPENDIX_TYPE, false)) {
            if (iterator.hasNext()) {
                ASMutableTreeNode list = new ASMutableTreeNode(Lang.T("Appendixes"));
                while (iterator.hasNext()) {
                    list.add(new DefaultMutableTreeNode(dcSet.getTransactionFinalMap().get(iterator.next().c)));
                }
                root.add(list);
            }
        } catch (IOException e) {
        }

        try (IteratorCloseable<Tuple3<Long, Byte, Long>> iterator = dcSet.getExLinksMap()
                .getTXLinksIterator(dbRef, ExData.LINK_SOURCE_TYPE, false)) {
            if (iterator.hasNext()) {
                ASMutableTreeNode list = new ASMutableTreeNode(Lang.T("Usage"));
                while (iterator.hasNext()) {
                    list.add(new DefaultMutableTreeNode(dcSet.getTransactionFinalMap().get(iterator.next().c)));
                }
                root.add(list);
            }
        } catch (IOException e) {
        }

        try (IteratorCloseable<Tuple3<Long, Byte, Long>> iterator = dcSet.getExLinksMap()
                .getTXLinksIterator(dbRef, ExData.LINK_REPLY_COMMENT_TYPE, false)) {
            if (iterator.hasNext()) {
                ASMutableTreeNode list = new ASMutableTreeNode(Lang.T("Replays and Comments"));
                while (iterator.hasNext()) {
                    list.add(new DefaultMutableTreeNode(dcSet.getTransactionFinalMap().get(iterator.next().c)));
                }
                root.add(list);
            }
        } catch (IOException e) {
        }

        if (root.isLeaf())
            return null;

        return root;
    }


    @SuppressWarnings("unchecked")
    protected JSONObject getJsonBase() {

        if (dcSet == null) {
            setDC(DCSet.getInstance(), true);
        }

        JSONObject transaction = new JSONObject();

        transaction.put("version", Byte.toUnsignedInt(this.typeBytes[1]));
        transaction.put("property1", Byte.toUnsignedInt(this.typeBytes[2]));
        transaction.put("property2", Byte.toUnsignedInt(this.typeBytes[3]));

        transaction.put("confirmations", this.getConfirmations(dcSet));
        transaction.put("type", getType());
        transaction.put("record_type", this.viewTypeName());
        transaction.put("type_name", this.viewTypeName());
        transaction.put("sub_type_name", this.viewSubTypeName());

        if (exLink != null) {
            transaction.put("exLink", getExLink().toJson());
        }

        // getSignature - make in GENEIS
        transaction.put("signature", this.getSignature() == null ? "null" : Base58.encode(this.signature));

        int height;
        if (this.creator == null) {
            transaction.put("creator", "genesis");
            height = 1;
        } else {
            transaction.put("feePow", getFeePow());
            transaction.put("forgedFee", getForgedFee());
            transaction.put("royaltyFee", getRoyaltyFee());
            transaction.put("invitedFee", getInvitedFee());
            transaction.put("title", getTitle());
            transaction.put("deadLine", getDeadline());
            transaction.put("publickey", Base58.encode(this.creator.getPublicKey()));
            transaction.put("creator", this.creator.getAddress());
            transaction.put("fee", this.fee.toPlainString());
            transaction.put("timestamp", this.timestamp < 1000 ? "null" : this.timestamp);
        }

        if (this.height > 0) {
            transaction.put("height", this.height);
            transaction.put("sequence", this.seqNo);
            transaction.put("seqNo", viewHeightSeq());
            if (isWiped()) {
                transaction.put("wiped", true);
            }
        }

        transaction.put("size", this.viewSize(Transaction.FOR_NETWORK));

        transaction.put("tags", Arrays.asList(this.getTags()));

        return transaction;
    }

    public JSONObject jsonForExplorerPage(JSONObject langObj, Object[] args) {
        return toJson();
    }

    /**
     * Version 2 maker for BlockExplorer
     */
    public void makeJSONforHTML(JSONObject output, JSONObject langObj) {

        String title = getTitle();
        if (title != null && !title.isEmpty()) {
            output.put("Label_title", Lang.T("Title", langObj));
            output.put("title", title);
        }

        WebTransactionsHTML.getAppLink(output, this, langObj);
        WebTransactionsHTML.getApps(output, this, langObj);

    }

    public String makeHTMLView() {
        return "";
    }

    public String makeHTMLHeadView() {

        String text = "<h2>" + Lang.T(Lang.T(viewTypeName()) + "</h2>"
                + Lang.T("Creator") + ":&nbsp;<b>" + getCreator().getPersonAsString() + "</b><br>"
                + (exLink == null ? "" : Lang.T("Append to") + ":&nbsp;<b>" + exLink.viewRef() + "</b><br>"));
        return text;

    }

    public String makeHTMLFootView() {

        String text = "";
        return text;

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
            transaction.put("seqNo", this.getSeqNo());
            transaction.put("height", height);
            transaction.put("block", height);
        }

        boolean isSigned = this.signature != null;
        transaction.put("signature", isSigned ? Base58.encode(this.signature) : "null");

        transaction.put("raw", Base64.getEncoder().encodeToString(this.toBytes(FOR_NETWORK, isSigned)));

        return transaction;
    }

    /**
     * for RPC
     *
     * @param jsonObject
     * @return
     */
    static public Object decodeJson(String creatorStr, String x) {

        JSONObject out = new JSONObject();
        JSONObject jsonObject;
        int error;
        try {
            //READ JSON
            jsonObject = (JSONObject) JSONValue.parse(x);
        } catch (NullPointerException | ClassCastException e) {
            Transaction.updateMapByErrorSimple(ApiErrorFactory.ERROR_JSON, out);
            return out;
        }

        if (jsonObject == null) {
            Transaction.updateMapByErrorSimple(ApiErrorFactory.ERROR_JSON, out);
            return out;
        }

        creatorStr = creatorStr == null ? (String) jsonObject.get("creator") : creatorStr;

        Account creator = null;
        if (creatorStr == null) {
            Transaction.updateMapByErrorSimple(Transaction.INVALID_CREATOR, out);
            return out;
        } else {
            Fun.Tuple2<Account, String> resultCreator = Account.tryMakeAccount(creatorStr);
            if (resultCreator.a == null) {
                Transaction.updateMapByErrorValue(Transaction.INVALID_CREATOR, resultCreator.b, out);
                return out;
            }
            creator = resultCreator.a;
        }

        String password = (String) jsonObject.get("password");

        String error_value = null;
        int feePow;
        ExLink linkTo;
        try {
            error_value = "feePow error";
            feePow = Integer.valueOf(jsonObject.getOrDefault("feePow", 0).toString());

            String linkToRefStr = (String) jsonObject.get("linkTo");
            if (linkToRefStr == null) {
                linkTo = null;
            } else {
                Long linkToRef = Transaction.parseDBRef(linkToRefStr);
                if (linkToRef == null) {
                    error = Transaction.INVALID_BLOCK_TRANS_SEQ_ERROR;
                    Transaction.updateMapByErrorValue(error, "for 'linkTo'", out);
                    return out;
                } else {
                    linkTo = new ExLinkAppendix(linkToRef);
                }
            }
        } catch (Exception e) {
            Transaction.updateMapByErrorValue(ApiErrorFactory.ERROR_JSON, error_value, out);
            return out;
        }

        return new Fun.Tuple5(creator, feePow, linkTo, password, jsonObject);

    }

    public void sign(PrivateKeyAccount creator, int forDeal) {

        // use this.reference in any case and for Pack too
        // but not with SIGN
        boolean withSign = false;
        byte[] data = this.toBytes(forDeal, false);
        if (data == null)
            return;

        if (BlockChain.CLONE_MODE) {
            // чтобы из других цепочек не срабатывало
            data = Bytes.concat(data, Controller.getInstance().blockChain.getGenesisBlock().getSignature());
        } else {
            // чтобы из TestNEt не сработало
            int port = BlockChain.NETWORK_PORT;
            data = Bytes.concat(data, Ints.toByteArray(port));
        }

        this.signature = Crypto.getInstance().sign(creator, data);
    }

    /**
     * У неоторых трнзакций этот флаг занят и он другой - для toByte()
     *
     * @return
     */
    protected byte HAS_SMART_CONTRACT_MASK() {
        return HAS_SMART_CONTRACT_MASK;
    }

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
            data = Bytes.concat(data, timestampBytes);
        }

        // WRITE REFERENCE - in any case as Pack or not - NOW it reserved FLAGS
        if (this.reference != null) {
            // NULL in imprints
            byte[] referenceBytes = Longs.toByteArray(this.reference);
            data = Bytes.concat(data, referenceBytes);
        }

        // WRITE CREATOR
        data = Bytes.concat(data, this.creator.getPublicKey());

        if ((typeBytes[2] & HAS_EXLINK_MASK) > 0) {
            data = Bytes.concat(data, exLink.toBytes());
        }

        if (smartContract != null) {
            if (forDeal == FOR_DB_RECORD || !smartContract.isEpoch()) {
                typeBytes[2] = (byte) (typeBytes[2] | HAS_SMART_CONTRACT_MASK());
                data = Bytes.concat(data, smartContract.toBytes(forDeal));
            } else {
                typeBytes[2] &= ~HAS_SMART_CONTRACT_MASK();
            }
        }

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
            // WRITE DBREF
            byte[] dbRefBytes = Longs.toByteArray(this.dbRef);
            data = Bytes.concat(data, dbRefBytes);

            // WRITE FEE
            byte[] feeBytes = Longs.toByteArray(this.fee.unscaledValue().longValue());
            data = Bytes.concat(data, feeBytes);
        }

        return data;

    }

    /**
     * Transaction bytes Length for calc FEE
     *
     * @return
     */
    public int getFeeLength() {
        int len = getDataLength(Transaction.FOR_NETWORK, true);

        len += BlockChain.ADD_FEE_BYTES_FOR_COMMON_TX;

        return len;
    }

    public int getDataLength(int forDeal, boolean withSignature) {
        // not include item reference

        int base_len;
        if (forDeal == FOR_MYPACK)
            base_len = BASE_LENGTH_AS_MYPACK;
        else if (forDeal == FOR_PACK)
            base_len = BASE_LENGTH_AS_PACK;
        else if (forDeal == FOR_DB_RECORD)
            base_len = BASE_LENGTH_AS_DBRECORD;
        else
            base_len = BASE_LENGTH;

        if (!withSignature)
            base_len -= SIGNATURE_LENGTH;

        if (exLink != null)
            base_len += exLink.length();

        if (smartContract != null) {
            if (forDeal == FOR_DB_RECORD || !smartContract.isEpoch()) {
                base_len += smartContract.length(forDeal);
            }
        }

        return base_len;

    }

    // PROCESS/ORPHAN
    public boolean isWiped() {
        if (getType() == CALCULATED_TRANSACTION)
            return false;

        return BlockChain.isWiped(this.signature);
    }

    public boolean isSignatureValid(DCSet dcSet) {

        if (this.signature == null || this.signature.length != Crypto.SIGNATURE_LENGTH
                || Arrays.equals(this.signature, new byte[Crypto.SIGNATURE_LENGTH]))
            return false;

        // validation with reference - not as a pack in toBytes - in any case!
        byte[] data = this.toBytes(FOR_NETWORK, false);
        if (data == null)
            return false;

        int height = getBlockHeightByParentOrLast(dcSet);
        if (height < BlockChain.SKIP_VALID_SIGN_BEFORE) {
            return true;
        }

        // for skip NOT VALID SIGNs
        for (byte[] valid_item : BlockChain.VALID_SIGN) {
            if (Arrays.equals(signature, valid_item)) {
                if (dcSet.getTransactionFinalMapSigns().contains(signature))
                    return false;
                else
                    return true;
            }
        }

        if (BlockChain.CLONE_MODE) {
            // чтобы из других цепочек не срабатывало
            data = Bytes.concat(data, Controller.getInstance().blockChain.getGenesisBlock().getSignature());
        } else {
            // чтобы из TestNEt не сработало
            int port = BlockChain.NETWORK_PORT;
            data = Bytes.concat(data, Ints.toByteArray(port));
        }

        if (!Crypto.getInstance().verify(this.creator.getPublicKey(), this.signature, data)) {
            boolean wrong = true;
            for (byte[] item : BlockChain.DISCREDIR_ADDRESSES) {
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

    /**
     *  flags
     *   = 1 - not check fee
     *   = 2 - not check person
     *   = 4 - not check PublicText
     */
    public int isValid(int forDeal, long flags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        if (typeBytes[0] == -1 || typeBytes[1] == -1 || typeBytes[2] == -1 || typeBytes[3] == -1) {
            // не может быть чтобы все флаги были подняты - скорее всего это и JS ошибка
            errorValue = (typeBytes[0] == -1 ? "[0]" : typeBytes[1] == -1 ? "[1]" : typeBytes[2] == -1 ? "[2]" : "[3]") + " = -1";
            return INVALID_FLAGS;
        }

        // CHECK IF REFERENCE IS OK
        //Long reference = forDeal == null ? this.creator.getLastTimestamp(dcSet) : forDeal;
        if (forDeal > Transaction.FOR_MYPACK && height > BlockChain.ALL_BALANCES_OK_TO) {
            if (BlockChain.CHECK_DOUBLE_SPEND_DEEP < 0) {
                /// вообще не проверяем в тесте
                if (BlockChain.TEST_DB == 0 && timestamp < Controller.getInstance().getBlockChain().getTimestamp(height - 1)) {
                    // тут нет проверок на двойную трату поэтому только в текущем блоке транзакции принимаем
                    errorValue = "diff sec: " + (Controller.getInstance().getBlockChain().getTimestamp(height) - timestamp) / 1000;
                    if (BlockChain.CHECK_BUGS > 2) {
                        LOGGER.debug(errorValue);
                    }
                    return INVALID_TIMESTAMP;
                }
            } else if (BlockChain.CHECK_DOUBLE_SPEND_DEEP > 0) {
                if (timestamp < Controller.getInstance().getBlockChain().getTimestamp(height - BlockChain.CHECK_DOUBLE_SPEND_DEEP)) {
                    // тут нет проверок на двойную трату поэтому только в текущем блоке транзакции принимаем
                    errorValue = "diff sec: " + (Controller.getInstance().getBlockChain().getTimestamp(height) - timestamp) / 1000;
                    if (BlockChain.CHECK_BUGS > 2) {
                        LOGGER.debug(errorValue);
                    }
                    return INVALID_TIMESTAMP;
                }

            } else {
                long[] reference = this.creator.getLastTimestamp(dcSet);
                if (reference != null && reference[0] >= this.timestamp
                        && height > BlockChain.VERS_4_11
                ) {
                    if (BlockChain.TEST_DB == 0) {
                        errorValue = "INVALID TIME!!! REFERENCE: " + DateTimeFormat.timestamptoString(reference[0])
                                + "  TX[timestamp]: " + viewTimestamp() + " diff: " + (this.timestamp - reference[0])
                                + " BLOCK time: " + Controller.getInstance().getBlockChain().getTimestamp(height);
                        if (BlockChain.CHECK_BUGS > 2)
                            LOGGER.debug(errorValue);
                    }
                    return INVALID_TIMESTAMP;
                }
            }
        }

        // CHECK CREATOR
        if (!Crypto.getInstance().isValidAddress(this.creator.getAddressBytes())) {
            return INVALID_ADDRESS;
        }

        int height = this.getBlockHeightByParentOrLast(dcSet);
        //if (height <= 0 || height > 1000)
        //    return INVALID_TIMESTAMP;

        // CHECK IT AFTER isPERSON ! because in ignored in IssuePerson
        // CHECK IF CREATOR HAS ENOUGH FEE MONEY
        if ((flags & NOT_VALIDATE_FLAG_FEE) == 0L
                && height > BlockChain.ALL_BALANCES_OK_TO
                && !BlockChain.isFeeEnough(height, creator)
                && this.creator.getForFee(dcSet).compareTo(this.fee) < 0) {
            return NOT_ENOUGH_FEE;
        }

        if ((flags & NOT_VALIDATE_FLAG_PUBLIC_TEXT) == 0L
                && this.hasPublicText()
                && !BlockChain.TRUSTED_ANONYMOUS.contains(this.creator.getAddress())
                && !this.creator.isPerson(dcSet, height)) {
            errorValue = creator.getBase58();
            return CREATOR_NOT_PERSONALIZED;
        }

        if (false &&  // теперь не проверяем так как ключ сделал длинный dbs.rocksDB.TransactionFinalSignsSuitRocksDB.KEY_LEN
                (flags & NOT_VALIDATE_KEY_COLLISION) == 0l
                && BlockChain.CHECK_DOUBLE_SPEND_DEEP == 0
                && !checkedByPool // транзакция не существует в ожидании - иначе там уже проверили
                && this.signature != null
                && this.dcSet.getTransactionFinalMapSigns().contains(this.signature)) {
            // потому что мы ключ урезали до 12 байт - могут быть коллизии
            return KEY_COLLISION;
        }

        if (creatorPerson != null && !creatorPerson.isAlive(this.timestamp)) {
            return ITEM_PERSON_IS_DEAD;
        }

        return VALIDATE_OK;

    }

    @Deprecated
    public JSONObject makeErrorJSON(int error) {
        JSONObject out = new JSONObject();
        out.put("error", error);
        out.put("message", OnDealClick.resultMess(error));
        if (errorValue != null) {
            out.put("value", errorValue);
        }
        return out;
    }

    public JSONObject makeErrorJSON2(int error) {
        JSONObject out = new JSONObject();
        out.put("code", error);
        out.put("message", OnDealClick.resultMess(error));
        if (errorValue != null) {
            out.put("value", errorValue);
        }
        return out;
    }

    @Deprecated
    public void updateMapByError(int error, HashMap out) {
        out.put("error", error);
        out.put("message", OnDealClick.resultMess(error));
        if (errorValue != null) {
            out.put("value", errorValue);
        }
    }

    @Deprecated
    public void updateMapByError(int error, HashMap out, String lang) {
        out.put("error", error);
        if (lang == null) {
            out.put("message", OnDealClick.resultMess(error));
        } else {
            out.put("lang", lang);
            JSONObject langObj = Lang.getInstance().getLangJson(lang);
            out.put("message", langObj == null ? OnDealClick.resultMess(error) : Lang.T(OnDealClick.resultMess(error), langObj));
        }
        if (errorValue != null) {
            out.put("value", errorValue);
        }
    }

    public static void updateMapByError2static(HashMap out, int error, String lang) {
        JSONObject json = new JSONObject();
        json.put("code", error);
        json.put("message", OnDealClick.resultMess(error));
        if (lang != null) {
            JSONObject langObj = Lang.getInstance().getLangJson(lang);
            if (langObj != null) {
                json.put("lang", lang);
                json.put("local", Lang.T(OnDealClick.resultMess(error), langObj));
            }
        }
        out.put("error", json);
    }

    public static void updateMapByError2static(HashMap out, int error, String errorValue, String lang) {
        JSONObject json = new JSONObject();
        json.put("code", error);
        json.put("message", OnDealClick.resultMess(error));
        if (lang != null) {
            JSONObject langObj = Lang.getInstance().getLangJson(lang);
            if (langObj != null) {
                json.put("lang", lang);
                json.put("local", Lang.T(OnDealClick.resultMess(error), langObj));
            }
        }
        if (errorValue != null) {
            json.put("value", errorValue);
        }
        out.put("error", json);
    }

    public void updateMapByError2(HashMap out, int error, String lang) {
        updateMapByError2static(out, error, errorValue, lang);
    }

    @Deprecated
    public void updateMapByError(int error, String errorMess, HashMap out) {
        out.put("error", error);
        out.put("message", errorMess);
        if (errorValue != null) {
            out.put("value", errorValue);
        }
    }

    @Deprecated
    public void updateMapByError(int error, String errorMess, HashMap out, String lang) {
        out.put("error", error);
        if (lang == null) {
            out.put("message", errorMess);
        } else {
            out.put("lang", lang);
            JSONObject langObj = Lang.getInstance().getLangJson(lang);
            out.put("message", langObj == null ? errorMess : Lang.T(errorMess, langObj));
        }
        if (errorValue != null) {
            out.put("value", errorValue);
        }
    }

    @Deprecated
    public static void updateMapByErrorSimple(int error, String errorMess, HashMap out) {
        out.put("error", error);
        out.put("message", errorMess);
    }

    public static void updateMapByErrorSimple2(HashMap out, int error, String errorMess, String lang) {
        JSONObject json = new JSONObject();
        json.put("code", error);
        json.put("message", errorMess);
        if (lang != null) {
            JSONObject langObj = Lang.getInstance().getLangJson(lang);
            if (langObj != null) {
                json.put("lang", lang);
                json.put("local", Lang.T(errorMess, langObj));
            }
        }
        out.put("error", json);
    }

    @Deprecated
    public static void updateMapByErrorSimple(int error, HashMap out) {
        out.put("error", error);
        out.put("message", OnDealClick.resultMess(error));
    }

    @Deprecated
    public static void updateMapByErrorValue(int error, String errorValue, HashMap out) {
        out.put("error", error);
        out.put("message", OnDealClick.resultMess(error));
        out.put("value", errorValue);
    }

    public static void process_gifts_turn(DCSet dcSet, int level, long fee_gift, Account invitedAccount,
                                          long invitedPersonKey, boolean asOrphan,
                                          long royaltyAssetKey, int royaltyAssetScale,
                                          List<RCalculated> txCalculated, String message, long dbRef, long timestamp) {

        if (fee_gift <= 0L)
            return;

        String messageLevel;

        // CREATOR is PERSON
        // FIND person
        Account issuerAccount = PersonCls.getIssuer(dcSet, invitedPersonKey);
        Tuple4<Long, Integer, Integer, Integer> issuerPersonDuration = issuerAccount.getPersonDuration(dcSet);
        long issuerPersonKey;
        if (issuerPersonDuration == null) {
            // в тестовой сети возможно что каждый создает с неудостоверенного
            issuerPersonKey = -1;
        } else {
            issuerPersonKey = issuerPersonDuration.a;
        }

        if (issuerPersonKey < 0 // это возможно только для первой персоны и то если не она сама себя зарегала и в ДЕВЕЛОПЕ так что пусть там и будет
                || issuerPersonKey == invitedPersonKey // это возможно только в ДЕВЕЛОПЕ так что пусть там и будет
                || issuerPersonKey <= BlockChain.BONUS_STOP_PERSON_KEY
        ) {
            // break loop
            BigDecimal giftBG = BigDecimal.valueOf(fee_gift, royaltyAssetScale);
            invitedAccount.changeBalance(dcSet, asOrphan, false, royaltyAssetKey,
                    giftBG, false, false, false);
            // учтем что получили бонусы
            if (royaltyAssetKey == BlockChain.FEE_KEY) {
                invitedAccount.changeCOMPUStatsBalances(dcSet, asOrphan, giftBG, Account.FEE_BALANCE_SIDE_REFERAL_AND_GIFTS);
            }

            if (txCalculated != null && !asOrphan) {
                messageLevel = message + " top level";
                txCalculated.add(new RCalculated(invitedAccount, royaltyAssetKey, giftBG,
                        messageLevel, 0L, dbRef));

            }
            return;
        }

        // IS INVITER ALIVE ???
        PersonCls issuer = (PersonCls) dcSet.getItemPersonMap().get(issuerPersonKey);
        if (!issuer.isAlive(timestamp)) {
            // SKIP this LEVEL for DEAD persons
            process_gifts_turn(dcSet, level, fee_gift, issuerAccount, issuerPersonKey, asOrphan,
                    royaltyAssetKey, royaltyAssetScale,
                    txCalculated, message, dbRef, timestamp);
            return;
        }

        if (level > 1) {

            long fee_gift_next = fee_gift >> BlockChain.FEE_INVITED_SHIFT_IN_LEVEL;
            long fee_gift_get = fee_gift - fee_gift_next;

            BigDecimal giftBG = BigDecimal.valueOf(fee_gift_get, royaltyAssetScale);
            issuerAccount.changeBalance(dcSet, asOrphan, false, royaltyAssetKey, giftBG,
                    false, false, false);

            // учтем что получили бонусы
            if (royaltyAssetKey == BlockChain.FEE_KEY) {
                issuerAccount.changeCOMPUStatsBalances(dcSet, asOrphan, giftBG, Account.FEE_BALANCE_SIDE_REFERAL_AND_GIFTS);
            }

            if (txCalculated != null && !asOrphan) {
                messageLevel = message + " @P:" + invitedPersonKey + " level." + (1 + BlockChain.FEE_INVITED_DEEP - level);
                txCalculated.add(new RCalculated(issuerAccount, royaltyAssetKey, giftBG,
                        messageLevel, 0L, dbRef));
            }

            if (fee_gift_next > 0) {
                process_gifts_turn(dcSet, --level, fee_gift_next, issuerAccount, issuerPersonKey, asOrphan,
                        royaltyAssetKey, royaltyAssetScale,
                        txCalculated, message, dbRef, timestamp);
            }

        } else {
            // this is END LEVEL
            // GET REST of GIFT
            BigDecimal giftBG = BigDecimal.valueOf(fee_gift, royaltyAssetScale);
            issuerAccount.changeBalance(dcSet, asOrphan, false, royaltyAssetKey,
                    BigDecimal.valueOf(fee_gift, royaltyAssetScale), false, false, false);

            // учтем что получили бонусы
            if (royaltyAssetKey == BlockChain.FEE_KEY) {
                issuerAccount.changeCOMPUStatsBalances(dcSet, asOrphan, giftBG, Account.FEE_BALANCE_SIDE_REFERAL_AND_GIFTS);
            }

            if (txCalculated != null && !asOrphan) {
                messageLevel = message + " @P:" + invitedPersonKey + " level." + (1 + BlockChain.FEE_INVITED_DEEP - level);
                txCalculated.add(new RCalculated(issuerAccount, royaltyAssetKey, giftBG,
                        messageLevel, 0L, dbRef));
            }
        }
    }

    public static void process_gifts(DCSet dcSet, int level, long fee_gift, Account creator, boolean asOrphan,
                                     AssetCls royaltyAsset,
                                     Block block,
                                     String message, long dbRef, long timestamp) {

        if (fee_gift <= 0L)
            return;

        List<RCalculated> txCalculated = block == null ? null : block.getTXCalculated();
        long royaltyAssetKey = royaltyAsset.getKey();
        int royaltyAssetScale = royaltyAsset.getScale();

        Tuple4<Long, Integer, Integer, Integer> personDuration = creator.getPersonDuration(dcSet);
        if (personDuration == null
                || personDuration.a <= BlockChain.BONUS_STOP_PERSON_KEY) {

            // если рефералку никому не отдавать то она по сути исчезает - надо это отразить в общем балансе
            if (royaltyAssetKey == BlockChain.FEE_KEY) {
                BlockChain.FEE_ASSET_EMITTER.changeBalance(dcSet, !asOrphan, false, FEE_KEY,
                        BigDecimal.valueOf(fee_gift, BlockChain.FEE_SCALE), false, false, true);

            } else {
                // если рефералку никому не отдавать то она по сути исчезает - надо это отразить в общем балансе
                royaltyAsset.getMaker().changeBalance(dcSet, !asOrphan, false, royaltyAssetKey,
                        BigDecimal.valueOf(fee_gift, royaltyAssetScale), false, false, true);
            }

            return;
        }

        process_gifts_turn(dcSet, level, fee_gift, creator, personDuration.a, asOrphan,
                royaltyAssetKey, royaltyAssetScale,
                txCalculated, message, dbRef, timestamp);

    }

    // previous forging block or changed ERA volume
    public Tuple3<Long, Long, Long> peekRoyaltyData(Long personKey) {
        return dcSet.getTimeRoyaltyMap().peek(personKey);
    }

    public void pushRoyaltyData(Long personKey, Long royaltyBalance, Long royaltyValue) {
        dcSet.getTimeRoyaltyMap().push(personKey, dbRef, royaltyBalance, royaltyValue);
    }

    public Tuple3<Long, Long, Long> popRoyaltyData(Long personKey) {
        return dcSet.getTimeRoyaltyMap().pop(personKey);
    }

    private void calcRoyalty(Block block, Account account, long koeff, boolean asOrphan) {

        if (account.equals(GenesisBlock.CREATOR)
                || getKey() != FEE_KEY)
            return;

        Tuple4<Long, Integer, Integer, Integer> personDuration;
        Long royaltyID;
        if (BlockChain.ACTION_ROYALTY_PERSONS_ONLY) {
            personDuration = creator.getPersonDuration(dcSet);
            if (personDuration == null || personDuration.a == null) {
                return;
            }
            royaltyID = personDuration.a;
        } else {
            royaltyID = account.hashCodeLong();
        }

        BigDecimal royaltyBG;
        if (asOrphan) {
            // это откат - списываем
            Tuple3<Long, Long, Long> lastValue = peekRoyaltyData(royaltyID);
            if (lastValue == null || lastValue.c == 0) {
                return;
            }

            royaltyBG = BigDecimal.valueOf(lastValue.c, BlockChain.FEE_SCALE);

        } else {
            // это прямое начисление
            BigDecimal balanceEXO;
            BigDecimal balanceBAL = null;
            if (BlockChain.ACTION_ROYALTY_PERSONS_ONLY) {
                // по всем счетам персоны
                if (BlockChain.ACTION_ROYALTY_ASSET_2 > 0)
                    balanceBAL = PersonCls.getTotalBalance(dcSet, royaltyID, BlockChain.ACTION_ROYALTY_ASSET_2, TransactionAmount.ACTION_SEND);
                balanceEXO = PersonCls.getTotalBalance(dcSet, royaltyID, AssetCls.FEE_KEY, TransactionAmount.ACTION_SEND);
            } else {
                if (BlockChain.ACTION_ROYALTY_ASSET_2 > 0)
                    balanceBAL = account.getBalance(dcSet, BlockChain.ACTION_ROYALTY_ASSET_2, TransactionAmount.ACTION_SEND).b;
                balanceEXO = account.getBalance(dcSet, AssetCls.FEE_KEY, TransactionAmount.ACTION_SEND).b;
            }

            if (balanceBAL == null)
                balanceBAL = BigDecimal.ZERO;
            if (balanceEXO == null)
                balanceEXO = BigDecimal.ZERO;

            balanceEXO = balanceBAL.min(balanceEXO);
            if (balanceEXO.signum() <= 0)
                return;

            Long royaltyBalance = balanceEXO.setScale(BlockChain.FEE_SCALE).unscaledValue().longValue();
            Tuple3<Long, Long, Long> lastRoyaltyPoint = peekRoyaltyData(royaltyID);
            if (lastRoyaltyPoint == null) {
                // уще ничего не было - считать нечего
                pushRoyaltyData(royaltyID, royaltyBalance, 0L);
                return;
            }

            if (royaltyBalance == 0) {
                return;
            }

            Long previousForgingSeqNo = lastRoyaltyPoint.a;
            int diff = height - (int) (previousForgingSeqNo >> 32);
            if (diff < 1) {
                pushRoyaltyData(royaltyID, royaltyBalance, 0L);
                return;
            }

            int dayBlocks = BlockChain.BLOCKS_PER_DAY(height);
            int diffDays = diff / dayBlocks;
            if (diffDays > BlockChain.ACTION_ROYALTY_MAX_DAYS) {
                diff = BlockChain.ACTION_ROYALTY_MAX_DAYS * dayBlocks;
            }

            long percent = diff * koeff;

            royaltyBG = BigDecimal.valueOf(percent, BlockChain.FEE_SCALE)
                    // 6 от коэфф + (3+3) от процентов И сдвиг выше в valueOf происходит на BlockChain.ACTION_ROYALTY_ASSET_SCALE
                    .movePointLeft(3)
                    .multiply(balanceEXO)
                    .setScale(BlockChain.FEE_SCALE, RoundingMode.DOWN);

            if (royaltyBG.compareTo(BlockChain.ACTION_ROYALTY_MIN) < 0) {
                pushRoyaltyData(royaltyID, royaltyBalance, 0L);
                return;

            }

            Long royaltyValue = royaltyBG.unscaledValue().longValue();

            pushRoyaltyData(royaltyID, royaltyBalance, royaltyValue);

        }

        account.changeBalance(this.dcSet, asOrphan, false, FEE_KEY, royaltyBG, false, false, false);
        // учтем что получили бонусы
        account.changeCOMPUStatsBalances(dcSet, asOrphan, royaltyBG, Account.FEE_BALANCE_SIDE_TOTAL_EARNED);

        if (block != null && !asOrphan) {
            block.addCalculated(account, FEE_KEY, royaltyBG,
                    "EXO-mining", this.dbRef);
        }

        // учтем эмиссию
        BlockChain.FEE_ASSET_EMITTER.changeBalance(this.dcSet, !asOrphan, false, FEE_KEY,
                royaltyBG, false, false, true);

        // учтем начисления для держателей долей
        BlockChain.FEE_ASSET_EMITTER.changeBalance(this.dcSet, !asOrphan, false, -FEE_KEY,
                royaltyBG.multiply(BlockChain.ACTION_ROYALTY_TO_HOLD_ROYALTY_PERCENT).setScale(BlockChain.FEE_SCALE, RoundingMode.DOWN),
                false, false, true);


    }

    /**
     * Time Royalty for Person
     *
     * @param asOrphan
     */
    public void processRoyalty(Block block, boolean asOrphan) {
        if (BlockChain.ACTION_ROYALTY_START <= 0)
            return;

        long koeff = 1000000L * (long) BlockChain.ACTION_ROYALTY_PERCENT / (30L * (long) BlockChain.BLOCKS_PER_DAY(height));
        Tuple4<Long, Integer, Integer, Integer> personDuration;

        if (asOrphan) {
            if (creator != null) {
                calcRoyalty(block, creator, koeff, asOrphan);
            }

            HashSet<Account> recipients = getRecipientAccounts();
            if (recipients != null && !recipients.isEmpty()) {
                for (Account recipient : recipients) {
                    calcRoyalty(block, recipient, koeff, asOrphan);
                }
            }
        } else {

            if (creator != null) {
                calcRoyalty(block, creator, koeff, asOrphan);
            }

            HashSet<Account> recipients = getRecipientAccounts();
            if (recipients != null && !recipients.isEmpty()) {
                for (Account recipient : recipients) {
                    calcRoyalty(block, recipient, koeff, asOrphan);
                }
            }
        }

    }

    // REST

    //////////////////////////////////// PROCESS

    public void processHead(Block block, int forDeal) {
    }

    public void processBody(Block block, int forDeal) {

        if (forDeal > Transaction.FOR_PACK) {

            // CALC ROYALTY
            processRoyalty(block, false);

            if (this.fee != null && this.fee.compareTo(BigDecimal.ZERO) != 0) {
                // NOT update INCOME balance
                this.creator.changeBalance(this.dcSet, true, false, FEE_KEY, this.fee, false, false, true);
                // учтем траты
                this.creator.changeCOMPUStatsBalances(this.dcSet, false, this.fee, Account.FEE_BALANCE_SIDE_SPEND);
            }

            // Multi Level Referal
            if (BlockChain.FEE_INVITED_DEEP > 0) {
                long invitedFee = getInvitedFee();
                if (invitedFee > 0) {
                    process_gifts(dcSet, BlockChain.FEE_INVITED_DEEP, invitedFee, this.creator, false,
                            BlockChain.FEE_ASSET, block,
                            "Referral bonus " + "@" + this.viewHeightSeq(), dbRef, timestamp);
                }
            }

            if (exLink != null) {
                exLink.process(this);
            }

            // UPDATE REFERENCE OF SENDER
            this.creator.setLastTimestamp(new long[]{this.timestamp, dbRef}, this.dcSet);
        }

    }

    public void processTail(Block block, int forDeal) {
        ///////// SMART CONTRACTS SESSION
        if (smartContract == null) {
            // если у транзакции нет изначально контракта то попробуем сделать эпохальныый
            // потом он будет записан в базу данных и его можно найти загрузив эту трнзакцию
            smartContract = SmartContract.make(this);
        }

        if (smartContract != null)
            smartContract.process(dcSet, block, this);

    }

    public void process(Block block, int forDeal) {
        processHead(block, forDeal);
        processBody(block, forDeal);
        processTail(block, forDeal);
    }

    //////////////////////////////////// ORPHAN

    public void orphanHead(Block block, int forDeal) {
        ///////// SMART CONTRACTS SESSION
        if (smartContract == null) {
            // если у транзакции нет изначально контракта то попробуем сделать эпохальныый
            // для Отката нужно это сделать тут
            smartContract = SmartContract.make(this);
        }

        if (smartContract != null) {
            // если смарт-контракт найден, то тут он Голый и
            // его надо загружать из баазы данных чтобы восстановить все значения связанные с этой транзакцией
            Transaction txInDB = dcSet.getTransactionFinalMap().get(dbRef);
            smartContract = txInDB.getSmartContract();

            smartContract.orphan(dcSet, this);
        }

    }

    public void orphanBody(Block block, int forDeal) {

        if (forDeal > Transaction.FOR_PACK) {
            if (this.fee != null && this.fee.compareTo(BigDecimal.ZERO) != 0) {
                // NOT update INCOME balance
                this.creator.changeBalance(this.dcSet, false, false, FEE_KEY, this.fee, false, false, true);
                // учтем траты
                this.creator.changeCOMPUStatsBalances(this.dcSet, true, this.fee, Account.FEE_BALANCE_SIDE_SPEND);

            }

            // calc INVITED FEE
            if (BlockChain.FEE_INVITED_DEEP > 0) {
                long invitedFee = getInvitedFee();
                if (invitedFee > 0)
                    process_gifts(dcSet, BlockChain.FEE_INVITED_DEEP, invitedFee, this.creator, true,
                            BlockChain.FEE_ASSET, null, null, dbRef, timestamp);
            }

            // UPDATE REFERENCE OF SENDER
            // set last transaction signature for this ACCOUNT
            this.creator.removeLastTimestamp(this.dcSet, timestamp);

            // CALC ROYALTY
            processRoyalty(block, true);

        }

        if (exLink != null) {
            exLink.orphan(this);
        }

        // CLEAR all FOOTPRINTS and empty data
        this.dcSet.getVouchRecordMap().delete(dbRef);

    }

    public void orphanTail(Block block, int forDeal) {
    }

    public void orphan(Block block, int forDeal) {
        orphanHead(block, forDeal);
        orphanBody(block, forDeal);
        orphanTail(block, forDeal);
    }

    public Transaction copy() {
        try {
            return TransactionFactory.getInstance().parse(this.toBytes(FOR_NETWORK, true), Transaction.FOR_NETWORK);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    public Transaction copy(int forDeal) {
        try {
            return TransactionFactory.getInstance().parse(this.toBytes(forDeal, true), forDeal);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    public abstract HashSet<Account> getInvolvedAccounts();

    public abstract HashSet<Account> getRecipientAccounts();

    public abstract boolean isInvolved(Account account);

    // TODO перевести все на проверку height
    // Это используется только в ГУУИ поэтому по высоте можно делать точно
    public boolean isConfirmed(DCSet db) {
        if (height > 0)
            return true;

        if (this.getType() == Transaction.CALCULATED_TRANSACTION) {
            // USE referenced transaction
            return db.getTransactionFinalMap().contains(this.reference);
        }

        return db.getTransactionFinalMapSigns().contains(this.getSignature());
    }

    public int getConfirmations(int chainHeight) {

        if (this.height == 0)
            return 0;

        return 1 + chainHeight - this.height;
    }

    public int getConfirmations(DCSet db) {

        // CHECK IF IN UNCONFIRMED TRANSACTION

        if (this.height == 0)
            return 0;

        return 1 + db.getBlockMap().size() - this.height;

    }

    /**
     * ОЧЕНЬ ВАЖНО чтобы Finalizer мог спокойно удалять их и DCSet.fork
     * иначе Финализер не можеи зацикленные сслки порвать и не очищает HEAP.
     * Возможно можно еще освободить объекты
     */
    public void resetDCSet() {
        dcSet = null;
        itemsKeys = null;
    }

    public void resetSeqNo() {
        dbRef = 0l;
        height = 0;
        seqNo = 0;
    }

    // ПРОЫЕРЯЛОСЬ! действует в совокупк с Финализе в Блоке
    @Override
    protected void finalize() throws Throwable {
        dcSet = null;
        super.finalize();
    }

    @Override
    public String toString() {
        if (height > 0) {
            return viewHeightSeq() + "(" + viewTypeName() + ") " + getTitle();
        }

        if (signature == null) {
            return "(" + viewTypeName() + ") " + getTitle();
        }
        return "(" + viewTypeName() + ") " + getTitle() + " - " + Base58.encode(signature);
    }

    public String toStringShort() {
        return viewTypeName() + ": " + getTitle();
    }

    public String toStringFullAndCreatorLang() {
        return Lang.T(viewFullTypeName())
                + ": " + getTitle() + (creator == null ? "" : " - " + creator.getPersonAsString());
    }

}
