package org.erachain.core;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.ArbitraryTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.BlocksHeadsMap;
import org.erachain.datachain.DCSet;
import org.erachain.network.Peer;
import org.erachain.settings.Settings;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

/**
 * для обработки цепочки блоков. Запоминает в себе генесиз-блок и базу данных.
 * Поидее именно тут должен быть метод FORK а не в базе данных - и отпочковывание новой цепочки.
 * А блоки должны добавляться в цепочку а не в базу данных напрямую. blockChain.add(BLOCK)
 */
public class BlockChain {

    // 1825 - 13189664557 - 2718
    // 1824 - 7635471

    //public static final int START_LEVEL = 1;

    public static final int TESTS_VERS = 0; // not use TESTs - or 411 (as version)
    public static final boolean DEVELOP_USE = true;
    public static final boolean HARD_WORK = false;

    public static final int BLOCK_COUNT = 0; ////
    static final public boolean TEST_DB_TXS_OFF = false;
    /**
     * если задан - первое подключение к нему
     */
    public static final byte[] START_PEER = null; //new byte[]{(byte)138, (byte)197, (byte)135, (byte)122};

    public static final boolean PERSON_SEND_PROTECT = true;
    //public static final int BLOCK_COUNT = 10000; // max count Block (if =<0 to the moon)

    public static final int TESTNET_PORT = DEVELOP_USE ? 9065 : 9045;
    public static final int MAINNET_PORT = DEVELOP_USE ? 9066 : 9046;

    public static final int DEFAULT_WEB_PORT = DEVELOP_USE ? 9067 : 9047;
    public static final int DEFAULT_RPC_PORT = DEVELOP_USE ? 9068 : 9048;

    public static final String DEFAULT_EXPLORER = "explorer.erachain.org";

    //public static final String TIME_ZONE = "GMT+3";
    //
    public static final boolean ROBINHOOD_USE = false;
    public static final boolean ANONIM_SERT_USE = false;
    public static final int NEED_PEERS_FOR_UPDATE = HARD_WORK ? 2 : 1;

    public static final int MAX_ORPHAN = 1000; // max orphan blocks in chain
    public static final int SYNCHRONIZE_PACKET = 300; // when synchronize - get blocks packet by transactions
    public static final int TARGET_COUNT_SHIFT = 10;
    public static final int TARGET_COUNT = 1 << TARGET_COUNT_SHIFT;
    public static final int BASE_TARGET = 100000;///1 << 15;
    public static final int REPEAT_WIN = DEVELOP_USE ? 4 : 40; // GENESIS START TOP ACCOUNTS

    // RIGHTs
    public static final int GENESIS_ERA_TOTAL = 10000000;
    public static final int GENERAL_ERA_BALANCE = GENESIS_ERA_TOTAL / 100;
    public static final int MAJOR_ERA_BALANCE = 33000;
    public static final int MINOR_ERA_BALANCE = 1000;
    public static final int MIN_GENERATING_BALANCE = 100;
    public static final BigDecimal MIN_GENERATING_BALANCE_BD = new BigDecimal(MIN_GENERATING_BALANCE);
    //public static final int GENERATING_RETARGET = 10;
    public static final int GENERATING_MIN_BLOCK_TIME = DEVELOP_USE ? 120 : 288; // 300 PER DAY
    public static final int GENERATING_MIN_BLOCK_TIME_MS = GENERATING_MIN_BLOCK_TIME * 1000;
    public static final int FLUSH_TIMEPOINT = GENERATING_MIN_BLOCK_TIME_MS - (GENERATING_MIN_BLOCK_TIME_MS >> 4);
    static final int WIN_TIMEPOINT = GENERATING_MIN_BLOCK_TIME_MS >> 2;
    public static final int WIN_BLOCK_BROADCAST_WAIT_MS = 10000; //
    // задержка на включение в блок для хорошей сортировки
    public static final int UNCONFIRMED_SORT_WAIT_MS = DEVELOP_USE? 5000: 15000;
    public static final int CHECK_PEERS_WEIGHT_AFTER_BLOCKS = DEVELOP_USE? 1 : 2; // проверить наше цепочку по силе с окружающими
    // хранить неподтвержденные долше чем то время когда мы делаем обзор цепочки по силе
    public static final int UNCONFIRMED_DEADTIME_MS = DEVELOP_USE? GENERATING_MIN_BLOCK_TIME_MS << 4 : GENERATING_MIN_BLOCK_TIME_MS << 3;
    public static final int ON_CONNECT_SEND_UNCONFIRMED_NEED_COUNT = 10;

    public static final int BLOCKS_PER_DAY = 24 * 60 * 60 / GENERATING_MIN_BLOCK_TIME; // 300 PER DAY
    //public static final int GENERATING_MAX_BLOCK_TIME = 1000;
    public static final int MAX_BLOCK_SIZE_BYTES = 1 << 24; //4 * 1048576;
    public static final int MAX_BLOCK_SIZE = MAX_BLOCK_SIZE_BYTES >> 8;
    public static final int MAX_REC_DATA_BYTES = 1 << 20; // MAX_BLOCK_SIZE_BYTES >>1;

    // отдельно для генерации блока - так чтобы от протокольных ограничений отвязаться
    ////public static final int MAX_BLOCK_SIZE_GEN = HARD_WORK? 26333: 5000;
    public static final int MAX_BLOCK_SIZE_GEN = HARD_WORK? 26333: MAX_BLOCK_SIZE;
    public static final int MAX_BLOCK_SIZE_BYTES_GEN = MAX_BLOCK_SIZE_GEN * 200;

    public static final int MAX_UNCONFIGMED_MAP_SIZE = MAX_BLOCK_SIZE_GEN<<3;
    public static final int ON_CONNECT_SEND_UNCONFIRMED_UNTIL = MAX_UNCONFIGMED_MAP_SIZE;

    public static final int GENESIS_WIN_VALUE = DEVELOP_USE ? 3000 : 22000;
    public static final String[] GENESIS_ADMINS = new String[]{"78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5",
            "7B3gTXXKB226bxTxEHi8cJNfnjSbuuDoMC"};

    public static final long BONUS_STOP_PERSON_KEY = 13l;

    public static final int VERS_4_11 = DEVELOP_USE ? 230000 : 194400;

    //public static final int ORDER_FEE_DOWN = VERS_4_11;
    public static final int HOLD_VALID_START = TESTS_VERS > 0? 0 : VERS_4_11;

    public static final int CANCEL_ORDERS_ALL_VALID = DEVELOP_USE ? 430000 : 260120;
    public static final int ALL_BALANCES_OK_TO = TESTS_VERS > 0? 0 : DEVELOP_USE? 425555 : 260120;

    public static final int VERS_4_12 = DEVELOP_USE ? VERS_4_11 + 20000 : VERS_4_11;

    public static final int DEVELOP_FORGING_START = 100;

    public static final byte[][] WIPED_RECORDS = DEVELOP_USE ?
            new byte[][]{
                    // ORDER on ERG
                    Base58.decode("4ycpev6jq5dagkCz49LHoMmo6MM7cQEyC36A7tHKLz6ex25NjjKMkd7hdfPnd8yEmuy3biYVSezQXUuEH8f3HZFv"),
                    Base58.decode("3JR3Wivtjm1uTmrnzkTHtXRdUvMdxrApcmu2Q5uha82HMucWqFWLgN82SwKqYB7EXQ7ThVtJD5s7iJqR8BwqGxF9"),
                    Base58.decode("5rh9StwPMPsxu7dRvsT5N3KmYkKwUTnRdfQ4Crhqmzbey6uDMh1i6SudphFSUZkexmDAJYJzrarUGsbdEycYytu4"),

            } :
            new byte[][]{

        // WRONG Issue Person #125
        Base58.decode("zDLLXWRmL8qhrU9DaxTTG4xrLHgb7xLx5fVrC2NXjRaw2vhzB1PArtgqNe2kxp655saohUcWcsSZ8Bo218ByUzH"),
        // WRONG orders by Person 90 Yakovlev
        Base58.decode("585CPBAusjDWpx9jyx2S2hsHByTd52wofYB3vVd9SvgZqd3igYHSqpS2gWu2THxNevv4LNkk4RRiJDULvHahPRGr"),
        Base58.decode("4xDHswuk5GsmHAeu82qysfdq9GyTxZ798ZQQGquprirrNBr7ACUeLZxBv7c73ADpkEvfBbhocGMhouM9y13sP8dK"),
        Base58.decode("3kFoA1giAr8irjA2iSC49ef8gJo9pftMg4Uif72Jcby6qvre9XAdFntpeLVZu2PAdWNi6DWiaeZRZQ8SHNT5GoZz"), // 23168
        Base58.decode("4x2NkCc9JysnCmyMcYib7NjKaNf2kPoLZ3ywifmTzjc9S8JeiJRfNEGsovCTFrTR6RA1Tazn9emASZ3mK5WBBniV"), // 23424-1
        Base58.decode("2Y81A7YjBji7NDKxYWMeNapSqFWFr8D4PSxBc4dCxSrCCVia6HPy2ZsezYKgeqZugNibAMra6DYT7NKCk6cSVUWX"), // 24732
        Base58.decode("4drnqT2e8uYdhqz2TqscPYLNa94LWHhMZk4UD2dgjT5fLGMuSRiKmHyyghfMUMKreDLMZ5nCK2EMzUGz3Ggbc6W9"), //  25242
        Base58.decode("L3cVbRemVECiPnLW3qdJixVkyc4QyUmjcbLmkAkz4SMMgmwHNq5KhBxNywmvfvAGGLcE3vjYFm4VT65rJktdALD"), //  25249
        Base58.decode("2hmDQkDU4zdBGdAkmpvjPhTQCHhjGQcvHGwCnyxMfDVSJPiKPiLWeW5CuBW6ZVhHq9N4H5tRFPdkKQimcykqnpv3"), //  25846

        // WRONG Vouch - from FUTURE
        Base58.decode("x9mDarBVKNuGLXWNNLxkD9hkFcRYDNo19PfSk6TpCWPGxeQ5PJqfmzKkLLp2QcF8fcukYnNQZsmqwdnATZN4Hm6"),

        // CANCEL ORDERS - wrongs after fix exchange
        ///Base58.decode("3kaJVDpKhiv4jgTt6D1z8J5rfbx9b7tFcCh928cpji3pQnLqEmUK8TKUgXEdw5zjVLQ2eUFgT3YKBBcJaKXz6DAp"), // Cancel Order 29311-1
        ///Base58.decode("2BiRnFY2hmJLCENMevcXsnC1cwmaJJgfaJtRby5xADmx7EGyFswiffTf23pGyohDmeeeFqB5LYAeatDrvvopHGqN"), // Cancel Order 86549-2
        ///Base58.decode("5iU3zefyZnWKpv2wd7E8m2hUCmLWASLM96SEfujAfvTKKDm4TA8n1HYFF5YWZkuk1k6vHVNLLv3XCG2MWrT7oHd9"), // Cancel Order 136754-1

        // DELETED base TOKEN (LIA, ERG, etc...
        Base58.decode("Fb2TRdSLPUY7GvYnLXxhhM27qhJUQTGCRB3sRnZV5uKK3fDa4cuyGLK21xcgJ34GAaWqyh7tx6DMorqgR9s2t5g"), // 924 - 1
        Base58.decode("sagBYvqLUVTbm6tjJ4rmkCxF1AY9SvC4jJEnfSnrc4F3T9JmqhNgGMZLzotXHxTwwQgGFprhWV4WQWYjv41Niq4"), // 926 - 1
        Base58.decode("3LppekMMVMMuRcNrJdo14FxWRiwUnVs3KNULpjPAL7wThgqSAcDYy6369pZzSENZEenamWmUVaRUDASr3D9XrfrK"), // 972 - 1
        Base58.decode("53gxbfbnp61Yjppf2aN33mbzEs5xWYGULsJTDBiUZ8zhdmsibZr7JFP3ZkEfiEXvsUApKmTiWvX1JVamD8YYgowo"), // 1823 - 1
        Base58.decode("3q8y3fFAqpv8rc6vzQPtXpxHt1RCbSewJ8To4JVmB1D9JzoV37XMgmk3uk9vHzdVfTzTagjNRK1Hm6edXsawsGab"), // 1823 - 2
        Base58.decode("4rEHZd1n5efcdKbbnodYrcURdyWhSLSQLUjPCwmDwjQ8m9BCzn8whZXrirxN8f94otiit2RSxJcUNggPHwhgK2r8"), // 2541 - 1
        Base58.decode("2i1jHNAEFDvdaC93d2RjYy22ymiJLRnDMV2NedXdRGZfxpavZL3QnwgWNNATcwUMSAbwG2RtZxQ6TqVx2PkoyDuD"), // 3422 - 1
        Base58.decode("1ArCghAasj2Jae6ynNEphHjQa1DsTskXqkHXCPLeTzChwzLw631d23FZjFHvnphnUJ6fw4mL2iu6AXBZQTFQkaA"), // 3735 - 1

        // VOTE 2
        // TODO добавить потом
        Base58.decode("Xq48dimwhwkXRkFun6pSQFHDSmrDnNqpUbFMkvQHC26nAyoQ3Srip3gE42axNWi5cXSPfTX5yrFkK6R4Hinuq6V"), // 253554 - 1

    };

    /*
     *  SEE in concrete TRANSACTIONS
     * public static final byte[][] VALID_RECORDS = new byte[][]{
     * };
     */

    public static final byte[][] VALID_ADDRESSES = new byte[][]{
            Base58.decode("1A3P7u56G4NgYfsWMms1BuctZfnCeqrYk3")
    };

    public static final byte[][] VALID_BAL = DEVELOP_USE ? new byte[][]{} :
            new byte[][]{
                    //Base58.decode("5sAJS3HeLQARZJia6Yzh7n18XfDp6msuaw8J5FPA8xZoinW4FtijNru1pcjqGjDqA3aP8HY2MQUxfdvk8GPC5kjh"),
                    //Base58.decode("3K3QXeohM3V8beSBVKSZauSiREGtDoEqNYWLYHxdCREV7bxqE4v2VfBqSh9492dNG7ZiEcwuhhk6Y5EEt16b6sVe"),
                    //Base58.decode("5JP71DmsBQAVTQFUHJ1LJXw4qAHHcoBCzXswN9Ez3H5KDzagtqjpWUU2UNofY2JaSC4qAzaC12ER11kbAFWPpukc"),
                    //Base58.decode("33okYP8EdKkitutgat1PiAnyqJGnnWQHBfV7NyYndk7ZRy6NGogEoQMiuzfwumBTBwZyxchxXj82JaQiQXpFhRcs"),
                    //Base58.decode("23bci9zcrPunGppKCm6hKvfRoAStWv4JV2xe16tBEVZSmkCrhw7bXAFzPvv2jqZJXcbA8cmr8oMUfdmS1HJGab7s"),
                    
                    //Base58.decode("54xdM25ommdxTbAVvP7C9cFYPmwaAexkWHfkhgb8yhfCVvvRNrs166q8maYuXWpk4w9ft2HvctaFaafnKNfjyoKR"),
                    //Base58.decode("61Fzu3PhsQ74EoMKrwwxKHMQi3z9fYAU5UeUfxtGdXPRfKbWdgpBQWgAojEnmDHK2LWUKtsmyqWb4WpCEatthdgK"),
            };

    // DEX precision
    ///public static final int TRADE_PRECISION = 4;
    /**
     * Если после исполнения торговой сделки оостатется статок у ордера-инициатора и
     * цена для остатка отклонится больше чем на эту величину то ему возвращаем остаток
     */
    final public static BigDecimal INITIATOR_PRICE_DIFF_LIMIT =     new BigDecimal("0.000001");
    /**
     * Если после исполнения торговой сделки оостатется статок у ордера-цели и
     * цена для остатка отклонится больше чем на эту величину то либо скидываем остаток в эту сделку либо ему возвращаем остаток
     */
    final public static BigDecimal TARGET_PRICE_DIFF_LIMIT =        new BigDecimal("0.000006");
    /**
     * Если цена сделки после скидывания в нее сотатка ордера-цели не выйдет за это ограничени то скидываем в сделку.
     * Инача отдаем обратно
     */
    ///final public static BigDecimal TRADE_PRICE_DIFF_LIMIT = new BigDecimal("2.0").scaleByPowerOfTen(-(BlockChain.TRADE_PRECISION - 1));
    final public static BigDecimal TRADE_PRICE_DIFF_LIMIT =         new BigDecimal("0.001");


    public static final int ITEM_POLL_FROM = DEVELOP_USE ? 77000 : VERS_4_11;

    public static final int AMOUNT_SCALE_FROM = DEVELOP_USE ? 1034 : 1033;
    public static final int AMOUNT_DEDAULT_SCALE = 8;
    public static final int FREEZE_FROM = DEVELOP_USE ? 12980 : 249222;
    // только на них можно замороженные средства вернуть из списка FOUNDATION_ADDRESSES (там же и замароженные из-за утраты)
    public static final String[] TRUE_ADDRESSES = new String[]{
            "7R2WUFaS7DF2As6NKz13Pgn9ij4sFw6ymZ"
            //"78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5",
            // "7S8qgSTdzDiBmyw7j3xgvXbVWdKSJVFyZv",
    };
    // CHAIN
    public static final int CONFIRMS_HARD = 3; // for reference by signature
    // MAX orphan CHAIN
    public static final int CONFIRMS_TRUE = MAX_ORPHAN; // for reference by ITEM_KEY
    //TESTNET
    //   1486444444444l
    //	 1487844444444   1509434273     1509434273
    public static final long DEFAULT_MAINNET_STAMP = DEVELOP_USE ? 1511164500000l : 1487844793333l;
    //public static final int FEE_MIN_BYTES = 200;
    public static final int FEE_PER_BYTE_4_10 = 64;
    public static final int FEE_PER_BYTE = 100;
    public static final int FEE_SCALE = 8;
    public static final BigDecimal FEE_RATE = BigDecimal.valueOf(1, FEE_SCALE);
    //public static final BigDecimal MIN_FEE_IN_BLOCK_4_10 = BigDecimal.valueOf(FEE_PER_BYTE_4_10 * 8 * 128, FEE_SCALE);
    public static final BigDecimal MIN_FEE_IN_BLOCK_4_10 = BigDecimal.valueOf(50000, FEE_SCALE);
    public static final BigDecimal MIN_FEE_IN_BLOCK = BigDecimal.valueOf(FEE_PER_BYTE * 6 * 128, FEE_SCALE);
    public static final float FEE_POW_BASE = (float) 1.5;
    public static final int FEE_POW_MAX = 6;
    public static final int FINANCIAL_FEE_LEVEL = 100;
    public static final int ISSUE_MULT_FEE = 1 << 10;
    public static final int ISSUE_ASSET_MULT_FEE = 1 << 8;
    public static final int TEST_FEE_ORPHAN = 0; //157000;

    public static final int FEE_FOR_ANONIMOUSE = 33;
    //
    public static final boolean VERS_4_11_USE_OLD_FEE = false;
    public static final int FEE_INVITED_DEEP = 2;

    // levels for deep
    public static final int FEE_INVITED_SHIFT = 1;
    public static final int BONUS_REFERAL = 50 * FEE_PER_BYTE;
    public static final int FEE_INVITED_SHIFT_IN_LEVEL = 1;

    // 0.0075 COMPU - is FEE for Issue Person - then >> 2 - всумме столько получают Форжер и кто привел
    // Бонус получает Персона, Вносит, Удостоверяет - 3 человека = Эмиссия
    // 0.0002 - цена за одну транзакцию
    public static final BigDecimal BONUS_FEE_LVL1 = new BigDecimal("0.01"); // < 3 000
    public static final BigDecimal BONUS_FEE_LVL2 = new BigDecimal("0.008"); // < 10 000
    public static final BigDecimal BONUS_FEE_LVL3 = new BigDecimal("0.005"); // < 100 000
    public static final BigDecimal BONUS_FEE_LVL4 = new BigDecimal("0.0025"); // < 1 000 000
    public static final BigDecimal BONUS_FEE_LVL5 = new BigDecimal("0.0015"); // else
    // SERTIFY
    // need RIGHTS for non PERSON account
    public static final BigDecimal MAJOR_ERA_BALANCE_BD = BigDecimal.valueOf(MAJOR_ERA_BALANCE);
    // need RIGHTS for PERSON account
    public static final BigDecimal MINOR_ERA_BALANCE_BD = BigDecimal.valueOf(MINOR_ERA_BALANCE);

    // GIFTS for RSertifyPubKeys
    public static final int GIFTED_COMPU_AMOUNT_4_10 = FEE_PER_BYTE_4_10 << 8;
    public static final BigDecimal GIFTED_COMPU_AMOUNT_BD_4_10 = BigDecimal.valueOf(GIFTED_COMPU_AMOUNT_4_10, FEE_SCALE);
    public static final int GIFTED_COMPU_AMOUNT_FOR_PERSON_4_10 = GIFTED_COMPU_AMOUNT_4_10 << 3;
    public static final BigDecimal GIFTED_COMPU_AMOUNT_FOR_PERSON_BD_4_10 = BigDecimal.valueOf(GIFTED_COMPU_AMOUNT_FOR_PERSON_4_10, FEE_SCALE);

    public static final int GIFTED_COMPU_AMOUNT = 50000; // FEE_PER_BYTE << 8;
    public static final BigDecimal GIFTED_COMPU_AMOUNT_BD = BigDecimal.valueOf(GIFTED_COMPU_AMOUNT, FEE_SCALE);
    public static final int GIFTED_COMPU_AMOUNT_FOR_PERSON = 250000; //GIFTED_COMPU_AMOUNT << 7;
    public static final BigDecimal GIFTED_COMPU_AMOUNT_FOR_PERSON_BD = BigDecimal.valueOf(GIFTED_COMPU_AMOUNT_FOR_PERSON, FEE_SCALE);

    //private int checkPoint = DEVELOP_USE?1:32400;
    public static final Tuple2<Integer, byte[]> CHECKPOINT = new Tuple2<Integer, byte[]>(
            DEVELOP_USE?289561 : 235267,
            Base58.decode(DEVELOP_USE?
                    "4MhxLvzH3svg5MoVi4sX8LZYVQosamoBubsEbeTo2fqu6Fcv14zJSVPtZDuu93Tc7RuS2nPJDYycWjpvdSYdmm1W"
                    :"2VTp79BBpK5E4aZYV5Tk3dYRS887W1devsrnyJeN6WTBQYQzoe2cTg819DdRs5o9Wh6tsGLsetYTbDu9okgriJce"));

    // issue PERSON
    //public static final BigDecimal PERSON_MIN_ERA_BALANCE = BigDecimal.valueOf(10000000);
    public static HashSet<String> TRUSTED_ANONYMOUS = new HashSet<String>();
    public static HashSet<String> ANONYMASERS = new HashSet<String>();
    public static HashSet<String> FOUNDATION_ADDRESSES = new HashSet<String>();
    public static HashMap<String, int[][]> FREEZED_BALANCES = new HashMap<String, int[][]>();
    public static HashMap<String, Pair<Integer, byte[]>> NOVA_ASSETS = new HashMap<String, Pair<Integer, byte[]>>();
    public static HashMap<String, String> LOCKED__ADDRESSES = new HashMap<String, String>();
    public static HashMap<String, Tuple3<String, Integer, Integer>> LOCKED__ADDRESSES_PERIOD = new HashMap<String, Tuple3<String, Integer, Integer>>();
    public static HashMap<Long, PublicKeyAccount> ASSET_OWNERS = new HashMap<Long, PublicKeyAccount>();
    static Logger LOGGER = LoggerFactory.getLogger(BlockChain.class.getName());
    private GenesisBlock genesisBlock;
    private long genesisTimestamp;
    private Block waitWinBuffer;

    //private int target = 0;
    //private byte[] lastBlockSignature;
    //private Tuple2<Integer, Long> HWeight;

    public long transactionWinnedTimingAverage;
    public long transactionWinnedTimingCounter;

    public long transactionValidateTimingAverage;
    public long transactionValidateTimingCounter;

    public long transactionProcessTimingAverage;
    public long transactionProcessTimingCounter;

    //private DLSet dcSet;

    // dcSet_in = db() - for test
    public BlockChain(DCSet dcSet_in) throws Exception {
        //CREATE GENESIS BLOCK
        genesisBlock = new GenesisBlock();
        genesisTimestamp = genesisBlock.getTimestamp();

        // GENERAL TRUST
        TRUSTED_ANONYMOUS.add("7BAXHMTuk1vh6AiZU65oc7kFVJGqNxLEpt");
        TRUSTED_ANONYMOUS.add("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh");
        //TRUSTED_ANONYMOUS.add("79ZVGgCFrQPoVTsFm6qCNTZNkRbYNsTY4u");


        if (DEVELOP_USE) {
            // права для Кибальникова
            ASSET_OWNERS.put(7L, new PublicKeyAccount("FgdfKGEQkP1RobtbGqVSQN61AZYGy6W1WSAJvE9weYMe"));
            ASSET_OWNERS.put(8L, new PublicKeyAccount("FgdfKGEQkP1RobtbGqVSQN61AZYGy6W1WSAJvE9weYMe"));

            // из p130 счета для прорверки
            NOVA_ASSETS.put("BTC",
                    new Pair<Integer, byte[]>(12, new Account("7EPhDbpjsaRDFwB2nY8Cvn7XukF58kGdkz").getShortAddressBytes()));
            NOVA_ASSETS.put("USD",
                    new Pair<Integer, byte[]>(95, new Account("7EPhDbpjsaRDFwB2nY8Cvn7XukF58kGdkz").getShortAddressBytes()));

            LOCKED__ADDRESSES.put("7EPhDbpjsaRDFwB2nY8Cvn7XukF58kGdkz", "7A94JWgdnNPZtbmbphhpMQdseHpKCxbrZ1");
            TRUSTED_ANONYMOUS.add("762eatKnsB3xbyy2t9fwjjqUG1GoxQ8Rhx");
            ANONYMASERS.add("7CzxxwH7u9aQtx5iNHskLQjyJvybyKg8rF");


            ANONYMASERS.add("7KC2LXsD6h29XQqqEa7EpwRhfv89i8imGK"); // face2face
        } else {
            // ANOMIMASER for incomes from PERSONALIZED
            ANONYMASERS.add("7BAXHMTuk1vh6AiZU65oc7kFVJGqNxLEpt");
            ANONYMASERS.add("79ZVGgCFrQPoVTsFm6qCNTZNkRbYNsTY4u");
            ANONYMASERS.add("7KC2LXsD6h29XQqqEa7EpwRhfv89i8imGK"); // face2face
            ANONYMASERS.add("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh"); // GATE-issuer


            // TICKER = KEY + CREATOR
            NOVA_ASSETS.put("BTC",
                    new Pair<Integer, byte[]>(12, new Account("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh").getShortAddressBytes()));
            NOVA_ASSETS.put("ETH",
                    new Pair<Integer, byte[]>(14, new Account("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh").getShortAddressBytes()));
            NOVA_ASSETS.put("USD",
                    new Pair<Integer, byte[]>(95, new Account("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh").getShortAddressBytes()));
            NOVA_ASSETS.put("EUR",
                    new Pair<Integer, byte[]>(94, new Account("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh").getShortAddressBytes()));
            NOVA_ASSETS.put("CNY",
                    new Pair<Integer, byte[]>(93, new Account("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh").getShortAddressBytes()));
            NOVA_ASSETS.put("RUB",
                    new Pair<Integer, byte[]>(92, new Account("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh").getShortAddressBytes()));
            NOVA_ASSETS.put("JPY",
                    new Pair<Integer, byte[]>(91, new Account("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh").getShortAddressBytes()));
            NOVA_ASSETS.put("GBP",
                    new Pair<Integer, byte[]>(90, new Account("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh").getShortAddressBytes()));
            NOVA_ASSETS.put("CHF",
                    new Pair<Integer, byte[]>(89, new Account("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh").getShortAddressBytes()));
            NOVA_ASSETS.put("AUD",
                    new Pair<Integer, byte[]>(88, new Account("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh").getShortAddressBytes()));
            NOVA_ASSETS.put("SGD",
                    new Pair<Integer, byte[]>(87, new Account("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh").getShortAddressBytes()));
            NOVA_ASSETS.put("TRY",
                    new Pair<Integer, byte[]>(86, new Account("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh").getShortAddressBytes()));


            // COMMODITY
            NOVA_ASSETS.put("GOLD",
                    new Pair<Integer, byte[]>(21, new Account("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh").getShortAddressBytes()));
            NOVA_ASSETS.put("OIL",
                    new Pair<Integer, byte[]>(22, new Account("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh").getShortAddressBytes()));
            NOVA_ASSETS.put("GAS",
                    new Pair<Integer, byte[]>(23, new Account("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh").getShortAddressBytes()));
            NOVA_ASSETS.put("BREND",
                    new Pair<Integer, byte[]>(24, new Account("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh").getShortAddressBytes()));


            /// Права для Кибальникова в Боевой Версии
            NOVA_ASSETS.put("ERG",
                    new Pair<Integer, byte[]>(20, new Account("7GiE2pKyrULF2iQhAXvdUusXYqiKRQx68m").getShortAddressBytes()));
            
            //NOVA_ASSETS.put("@@USD",
            //		new Pair<Integer, byte[]>(95, new Account("7JS4ywtcqrcVpRyBxfqyToS2XBDeVrdqZL").getShortBytes()));
            //NOVA_ASSETS.put("¤¤RUB",
            //		new Pair<Integer, byte[]>(93, new Account("7JS4ywtcqrcVpRyBxfqyToS2XBDeVrdqZL").getShortBytes()));
            //NOVA_ASSETS.put("ERARUB",
            //		new Pair<Integer, byte[]>(91, new Account("7JS4ywtcqrcVpRyBxfqyToS2XBDeVrdqZL").getShortBytes()));
            //NOVA_ASSETS.put("ERAUSD",
            //		new Pair<Integer, byte[]>(85, new Account("7JS4ywtcqrcVpRyBxfqyToS2XBDeVrdqZL").getShortBytes()));

            // LOCKED -> to TRUSTED for it address
            LOCKED__ADDRESSES.put("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh", "79ZVGgCFrQPoVTsFm6qCNTZNkRbYNsTY4u");
            LOCKED__ADDRESSES.put("7Rt6gdkrFzayyqNec3nLhEGjuK9UsxycZ6", "79ZVGgCFrQPoVTsFm6qCNTZNkRbYNsTY4u");

            // TEAM 0 LOCKS
            LOCKED__ADDRESSES_PERIOD.put("79kXsWXHRYEb7ESMohm9DXYjXBzPfi1seE", new Tuple3("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5", 137000, 240000)); // Vasya
            LOCKED__ADDRESSES_PERIOD.put("787H1wwYPwu33BEm2KbNeksAgVaRf41b2H", new Tuple3("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5", 137000, 240000)); // Natasha
            LOCKED__ADDRESSES_PERIOD.put("7CT5k4Qqhb53ciHfrxXaR3bGyribLgSoyZ", new Tuple3("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5", 137000, 240000)); // Lena
            LOCKED__ADDRESSES_PERIOD.put("74g61DcTa8qdfvWxzcbTjTf6PhMfAB77HK", new Tuple3("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5", 137000, 240000)); // Ivan
            LOCKED__ADDRESSES_PERIOD.put("7BfB66DpkEx7KJaMN9bzphTJcZR29wprMU", new Tuple3("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5", 137000, 240000)); // Ruslan
            LOCKED__ADDRESSES_PERIOD.put("1", new Tuple3("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5", 137000, 240000)); // Sergey
            LOCKED__ADDRESSES_PERIOD.put("1", new Tuple3("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5", 137000, 240000)); // Vladimir
            LOCKED__ADDRESSES_PERIOD.put("1", new Tuple3("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5", 137000, 240000)); // Roman

            // TEST
            //FOUNDATION_ADDRESSES.add("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7");

            // ERACHAIN FUNDATION
            FOUNDATION_ADDRESSES.add("74a73pykkNwmuwkZdh5Lt2xTbK7anG5B6i");
            FOUNDATION_ADDRESSES.add("7QTDHp15vcHN3F4zP2BTcDXJkeotzQZkG4");
            FOUNDATION_ADDRESSES.add("7FiXN8VTgjMsLrZUQY9ZBFNfek7SsDP6Uc");
            FOUNDATION_ADDRESSES.add("74QcLxHgPkuMSPsKTh7zGpJsd5aAxpWpFA");
            FOUNDATION_ADDRESSES.add("7BAXHMTuk1vh6AiZU65oc7kFVJGqNxLEpt");
            FOUNDATION_ADDRESSES.add("7P3HR8kdj4ojXPvpTnEtVnpEwenipvrcH1");
            FOUNDATION_ADDRESSES.add("75Mb8cGchcG4DF31wavhNrnoycWsoLQqP4");
            FOUNDATION_ADDRESSES.add("75LzKAoxx4TgAAkpMRStve26YEY625TCRE");

            FOUNDATION_ADDRESSES.add("73QYndpFQeFvyMvwBcMUwJRDTp7XaxkSmZ"); // STOLEN
            FOUNDATION_ADDRESSES.add("7FJUV5GLMuVdopUHSwTLsjmKF4wkPwFEcG"); // LOSED
            FOUNDATION_ADDRESSES.add("75LK84g7JHoLG2jRUmbJA6srLrFkaXEU5A"); // FREEZED


            // TEST
            //FREEZED_BALANCES.put("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7",
            //		new int[][]{{9000, 110000}, {3200, 90000}, {138000, 7000}, {547500, 5000}});

            // TEAM 2
            FREEZED_BALANCES.put("77QMFKSdY4ZsG8bFHynYdFNCmis9fNw5yP",
                    new int[][]{{225655, 90000}, {333655, 60000}});
            FREEZED_BALANCES.put("7N7d8juuSSeEd92rkcEsfXhdi9WXE8zYXs",
                    new int[][]{{225655, 80000}, {333655, 53000}});
            FREEZED_BALANCES.put("7LETj4cW4rLWBCN52CaXmzQDnhwkEcrv9G",
                    new int[][]{{225655, 97000}, {333655, 65000}});

            // TEAM 3
            FREEZED_BALANCES.put("7GMENsugxjV8PToyUyHNUQF7yr9Gy6tJou",
                    new int[][]{{225655, 197000}, {333655, 131000}});
            FREEZED_BALANCES.put("7DMJcs8kw7EXUSeEFfNwznRKRLHLrcXJFm",
                    new int[][]{{225655, 150000}, {333655, 100000}});
            FREEZED_BALANCES.put("7QUeuMiWQjoQ3MZiriwhKfEG558RJWUUis",
                    new int[][]{{225655, 150000}, {333655, 100000}});
            FREEZED_BALANCES.put("7MxscS3mS6VWim8B9K3wEzFAUWYbsMkVon",
                    new int[][]{{225655, 140000}, {333655, 90000}});
            FREEZED_BALANCES.put("79NMuuW7thad2JodQ5mKxbMoyf1DjNT9Ap",
                    new int[][]{{225655, 130000}, {333655, 90000}});
            FREEZED_BALANCES.put("7MhifBHaZsUcjgckwFN57bAE9fPJVDLDQq",
                    new int[][]{{225655, 110000}, {333655, 80000}});
            FREEZED_BALANCES.put("7FRWJ4ww3VstdyAyKFwYfZnucJBK7Y4zmT",
                    new int[][]{{225655, 100000}, {333655, 70000}});
            FREEZED_BALANCES.put("7FNAphtSYXtP5ycn88B2KEywuHXzM3XNLK",
                    new int[][]{{225655, 90000}, {333655, 60000}});
            FREEZED_BALANCES.put("79ZVGgCFrQPoVTsFm6qCNTZNkRbYNsTY4u",
                    new int[][]{{225655, 80000}, {333655, 60000}});

            // TEAM 1
            FREEZED_BALANCES.put("74rRXsxoKtVKJqN8z6t1zHfufBXsELF94y",
                    new int[][]{{225655, 20000}, {333655, 10000}});
            FREEZED_BALANCES.put("7PChKkoASF1eLtCnAMx8ynU2sMYdSPwkGV",
                    new int[][]{{225655, 60000}, {333655, 40000}});

            FREEZED_BALANCES.put("7Jhh3TPmfoLag8FxnJRBRYYfqnUduvFDbv",
                    new int[][]{{225655, 150000}, {333655, 100000}});
            FREEZED_BALANCES.put("7Rt6gdkrFzayyqNec3nLhEGjuK9UsxycZ6",
                    new int[][]{{115000, 656000}, {225655, 441000}});
        }

        DCSet dcSet = dcSet_in;
        if (dcSet == null) {
            dcSet = DCSet.getInstance();
        }

        if (Settings.getInstance().isTestnet()) {
            LOGGER.info(genesisBlock.getTestNetInfo());
        }

        int height = dcSet.getBlockMap().size();
        if (height == 0)
        // process genesis block
        {
            if (dcSet_in == null && dcSet.getBlockMap().getLastBlockSignature() != null) {
                LOGGER.info("reCreateDB Database...");

                try {
                    dcSet.close();
                    dcSet = Controller.getInstance().reCreateDC(Controller.getInstance().inMemoryDC);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    Controller.getInstance().stopAll(6);
                }
            }

            //PROCESS
            genesisBlock.process(dcSet);

        } else {

            // TRY compare GENESIS BLOCK SIGNATURE
            if (!Arrays.equals(dcSet.getBlockMap().get(1).getSignature(),
                    genesisBlock.getSignature())) {

                throw new Exception("wrong DB for GENESIS BLOCK");
            }

        }

        //lastBlockSignature = dcSet.getBlocksHeadMap().getLastBlockSignature();
        //HWeight = dcSet.getBlockSignsMap().get(lastBlockSignature);

    }

    //
    public static int getHeight(DCSet dcSet) {

        //GET LAST BLOCK
        ///byte[] lastBlockSignature = dcSet.getBlocksHeadMap().getLastBlockSignature();
        ///return dcSet.getBlockSignsMap().getHeight(lastBlockSignature);
        return dcSet.getBlockMap().size();
    }

    public static int getCheckPoint(DCSet dcSet) {

        Integer item = dcSet.getBlockSignsMap().get(CHECKPOINT.b);
        if (item == null || item == -1)
            return 2;

        int heightCheckPoint = item;
        int dynamicCheckPoint = getHeight(dcSet) - BlockChain.MAX_ORPHAN;

        if (dynamicCheckPoint > heightCheckPoint)
            return dynamicCheckPoint;
        return heightCheckPoint;
    }

    public static int getNetworkPort() {
        if (Settings.getInstance().isTestnet()) {
            return BlockChain.TESTNET_PORT;
        } else {
            return BlockChain.MAINNET_PORT;
        }
    }

    /**
     * Calculate Target (Average Win Value for 1024 last blocks) for this block
     * @param height - height of blockchain
     * @param targetPrevious - previous Target
     * @param winValue - current Win Value
     * @return
     */
    public static long calcTarget(int height, long targetPrevious, long winValue) {

        if (height < TARGET_COUNT) {
            return targetPrevious - (targetPrevious / height) + (winValue / height);
        }

        // CUT GROWTH
        long cut1 = targetPrevious + (targetPrevious >> 1);
        if (height > TARGET_COUNT && winValue > cut1) {
            winValue = cut1;
        }

        //return targetPrevios - (targetPrevios>>TARGET_COUNT_SHIFT) + (winValue>>TARGET_COUNT_SHIFT);
        // better accuracy
        long target = (((targetPrevious << TARGET_COUNT_SHIFT) - targetPrevious) + winValue) >> TARGET_COUNT_SHIFT;
        if (target < 1000 && DEVELOP_USE)
            target = 1000;

        return target;
    }

    // GET MIN TARGET
    // TODO GENESIS_CHAIN
    // SEE core.block.Block.calcWinValue(DLSet, Account, int, int)
    public static int getTargetedMin(int height) {
        int base;
        if (height < BlockChain.REPEAT_WIN)
            // FOR not repeated WINS - not need check BASE_TARGET
            /////base = BlockChain.BASE_TARGET>>1;
            base = BlockChain.BASE_TARGET - (BlockChain.BASE_TARGET >> 2); // ONLY UP
        else if (DEVELOP_USE)
            base = 1; //BlockChain.BASE_TARGET >>5;
        else if (height < 110000)
            base = (BlockChain.BASE_TARGET >> 3); // + (BlockChain.BASE_TARGET>>4);
        else if (height < 115000)
            base = (BlockChain.BASE_TARGET >> 1) - (BlockChain.BASE_TARGET >> 4);
        else
            base = (BlockChain.BASE_TARGET >> 1) + (BlockChain.BASE_TARGET >> 4);

        return base;

    }

    public static int calcWinValueTargeted(long win_value, long target) {

        if (target == 0) {
            // in forked chain in may be = 0
            return -1;
        }

        int result = (int) (BlockChain.BASE_TARGET * win_value / target);
        if (result < 1 || result > BlockChain.BASE_TARGET * 10)
            // fix overload
            return BlockChain.BASE_TARGET * 10;
        return result;

    }

    /**
     * calc WIN_VALUE for ACCOUNT in HEIGHT
     * @param dcSet
     * @param creator account of block creator
     * @param height current blockchain height
     * @param forgingBalance current forging Balance on account
     * @return (long) Win Value
     */
    public static long calcWinValue(DCSet dcSet, Account creator, int height, int forgingBalance) {

        if (forgingBalance < MIN_GENERATING_BALANCE) {
                return 0l;
        }

        Tuple2<Integer, Integer> previousForgingPoint = creator.getForgingData(dcSet, height);

        if (previousForgingPoint == null) {
            // IF BLOCK not inserted in MAP
            previousForgingPoint = creator.getLastForgingData(dcSet);
            if (previousForgingPoint == null)
                if (DEVELOP_USE)
                    // - (height > VERS_4_11? 100 : 10), 1000);
                    previousForgingPoint = new Tuple2<Integer, Integer>(height - DEVELOP_FORGING_START, forgingBalance);
                else
                    return 0l;
        }

        int previousForgingHeight = previousForgingPoint.a;

        // OWN + RENT balance - in USE
        if (forgingBalance > previousForgingPoint.b) {
            forgingBalance = previousForgingPoint.b;
        }

        if (forgingBalance < BlockChain.MIN_GENERATING_BALANCE) {
            if (!Controller.getInstance().isTestNet() && !DEVELOP_USE)
                return 0l;
            forgingBalance = BlockChain.MIN_GENERATING_BALANCE;
        }

        int difference = height - previousForgingHeight;
        if (Controller.getInstance().isTestNet() || BlockChain.DEVELOP_USE) {
            if (difference < 10)
                difference = 10;
            ;
        } else {

            int repeatsMin;

            if (height < BlockChain.REPEAT_WIN) {
                repeatsMin = height - 2;
            } else {
                repeatsMin = BlockChain.GENESIS_ERA_TOTAL / forgingBalance;
                repeatsMin = (repeatsMin >> 2);

                if (height < 40000) {
                    if (repeatsMin > 4)
                        repeatsMin = 4;
                } else if (height < 100000) {
                    if (repeatsMin > 6)
                        repeatsMin = 6;
                } else if (height < 110000) {
                    if (repeatsMin > 10) {
                        repeatsMin = 10;
                    }
                } else if (height < 120000) {
                    if (repeatsMin > 40)
                        repeatsMin = 40;
                } else if (height < VERS_4_11) {
                    if (repeatsMin > 200)
                        repeatsMin = 200;
                } else if (repeatsMin < 10) {
                    repeatsMin = 10;
                }
            }

            if (difference < repeatsMin) {
                return difference - repeatsMin;
            }
        }

        long win_value;

        if (difference > 1)
            win_value = (long) forgingBalance * (long) difference;
        else
            win_value = forgingBalance;

        if (Controller.getInstance().isTestNet() || DEVELOP_USE)
            return win_value;

        if (false) {
            if (height < BlockChain.REPEAT_WIN)
                win_value >>= 4;
            else if (BlockChain.DEVELOP_USE)
                win_value >>= 4;
            else if (height < BlockChain.TARGET_COUNT)
                win_value = (win_value >> 4) - (win_value >> 6);
            else if (height < BlockChain.TARGET_COUNT << 2)
                win_value >>= 5;
            else if (height < BlockChain.TARGET_COUNT << 6)
                win_value = (win_value >> 5) - (win_value >> 7);
            else if (height < BlockChain.TARGET_COUNT << 10)
                win_value >>= 6;
            else
                win_value = (win_value >> 7) - (win_value >> 9);
        } else {
            if (height < BlockChain.REPEAT_WIN)
                win_value >>= 2;
            else if (height < (BlockChain.REPEAT_WIN<<2))
                win_value >>= 5;
            else
                win_value >>= 7;
        }


        return win_value;

    }

    /**
     * Calculate targeted Win Value and cut by BASE
     * @param dcSet dataChainSet
     * @param height blockchain height
     * @param win_value win value
     * @param target average win value for blockchain by 1024 last blocks
     * @return targeted Win Value and cut by BASE
     */
    public static int calcWinValueTargetedBase(DCSet dcSet, int height, long win_value, long target) {

        if (win_value < 1)
            return (int) win_value;

        int base = BlockChain.getTargetedMin(height);
        int targetedWinValue = calcWinValueTargeted(win_value, target);
        if (!Controller.getInstance().isTestNet()
                && height > VERS_4_11
                && base > targetedWinValue) {
            return -targetedWinValue;
        }

        return targetedWinValue;

    }

    public GenesisBlock getGenesisBlock() {
        return this.genesisBlock;
    }

    //public long getGenesisTimestamp() {
    //    return this.genesisTimestamp;
    //}

	/*
	//public synchronized Tuple2<Integer, Long> getHWeight(DCSet dcSet, boolean withWinBuffer) {
	public Tuple2<Integer, Long> getHWeight(DCSet dcSet, boolean withWinBuffer) {

		if (dcSet.isStoped())
			return null;

		//GET LAST BLOCK
		byte[] lastBlockSignature = dcSet.getBlocksHeadMap().getLastBlockSignature();
		// test String b58 = Base58.encode(lastBlockSignature);

		int height;
		long weight;
		if (withWinBuffer && this.waitWinBuffer != null) {
			// with WIN BUFFER BLOCK
			height = 1;
			weight = this.waitWinBuffer.calcWinValueTargeted(dcSet);
		} else {
			height = 0;
			weight = 0l;
		}

		if (lastBlockSignature == null) {
			height++;
		} else {
			height += dcSet.getBlockSignsMap().getHeight(lastBlockSignature);
			weight += dcSet.getBlockSignsMap().getFullWeight();
		}

		return  new Tuple2<Integer, Long>(height, weight);

	}
	 */

    public long getTimestamp(int height) {
        return this.genesisTimestamp + (long) height * GENERATING_MIN_BLOCK_TIME_MS;
    }

    public long getTimestamp(DCSet dcSet) {
        return this.genesisTimestamp + (long) getHeight(dcSet) * GENERATING_MIN_BLOCK_TIME_MS;
    }

    public int getBlockOnTimestamp(long timestamp) {
        long diff = timestamp = genesisTimestamp;
        return (int) (diff / GENERATING_MIN_BLOCK_TIME_MS);
    }

    // BUFFER of BLOCK for WIN solving
    public Block getWaitWinBuffer() {
        return this.waitWinBuffer;
    }

	/*
	public void setCheckPoint(int checkPoint) {

		if (checkPoint > 1)
			this.checkPoint = checkPoint;
	}
	 */

    public void clearWaitWinBuffer() {
        this.waitWinBuffer = null;
    }

    public Block popWaitWinBuffer() {
        Block block = this.waitWinBuffer;
        this.waitWinBuffer = null;
        return block;
    }

    public int compareNewWin(DCSet dcSet, Block block) {
        return this.waitWinBuffer == null ? -1 : this.waitWinBuffer.compareWin(block);
    }

    // SOLVE WON BLOCK
    // 0 - unchanged;
    // 1 - changed, need broadcasting;
    public synchronized boolean setWaitWinBuffer(DCSet dcSet, Block block, Peer peer) {

        LOGGER.info("try set new winBlock: " + block.toString());

        if (this.waitWinBuffer != null && block.compareWin(waitWinBuffer) <= 0) {

            LOGGER.info("new winBlock is POOR!");
            return false;

        }

        // FULL VALIDATE because before was only HEAD validating
        if (!block.isValid(dcSet, false)) {

            LOGGER.info("new winBlock is BAD!");
            if (peer != null)
                Controller.getInstance().banPeerOnError(peer, "invalid block", 10);
            else
                LOGGER.error("MY WinBlock is INVALID! ignore...");

            return false;
        }

        this.waitWinBuffer = block;

        LOGGER.info("new winBlock setted!!!" + block.toString());
        return true;

    }

    /**
     * если идет синхронизация то записываем без проверки
     *
     * @param block
     */
    public void setWaitWinBufferUnchecked(Block block) {
        if (this.waitWinBuffer == null || block.compareWin(waitWinBuffer) > 0) {
            this.waitWinBuffer = block;
        }
    }

    public Tuple2<Integer, Long> getHWeightFull(DCSet dcSet) {
        return new Tuple2<Integer, Long>(dcSet.getBlocksHeadsMap().size(),
                dcSet.getBlocksHeadsMap().getFullWeight());
    }

    public long getFullWeight(DCSet dcSet) {

        return dcSet.getBlocksHeadsMap().getFullWeight();
    }

    public List<byte[]> getSignatures(DCSet dcSet, byte[] parentSignature) {

        //logger.debug("getSignatures for ->" + Base58.encode(parent));

        List<byte[]> headers = new ArrayList<byte[]>();

        //CHECK IF BLOCK EXISTS
        Integer height = dcSet.getBlockSignsMap().get(parentSignature);
        if (height != null && height > 0) {

            int packet;
            if (Arrays.equals(parentSignature, this.genesisBlock.getSignature())
                    || Arrays.equals(parentSignature, CHECKPOINT.b)) {
                packet = 3;
            } else {
                packet = SYNCHRONIZE_PACKET;
            }
            //BlocksHeads_2Map childsMap = dcSet.getBlockHeightsMap();
            //BlocksHeads_2Map map = dcSet.getBlockHeightsMap();
            BlocksHeadsMap map = dcSet.getBlocksHeadsMap();
            int counter = 0;
            do {
                headers.add(parentSignature);
                if (map.contains(++height))
                    parentSignature = map.get(height).signature;
                else
                    break;
            } while (parentSignature != null && counter++ < packet);
            //logger.debug("get size " + counter);
        } else if (Arrays.equals(parentSignature, this.CHECKPOINT.b)) {
            headers.add(parentSignature);
        } else {
            //logger.debug("*** getSignatures NOT FOUND !");
        }

        return headers;
    }

    public Block getBlock(DCSet dcSet, byte[] header) {

        return dcSet.getBlockSignsMap().getBlock(header);
    }

    public Block getBlock(DCSet dcSet, int height) {

        return dcSet.getBlockMap().get(height);
    }

    /**
     * Среднее время обработки транзакции при прилете блока из сети. Блок считается как одна транзакция
     *
     * @return
     */
    public void updateTXWinnedTimingAverage(long processTiming, int counter) {
        // при переполнении может быть минус
        // в миеросекундах подсчет делаем
        processTiming = processTiming / 1000 / (Controller.BLOCK_AS_TX_COUNT + counter);
        if (transactionWinnedTimingCounter < 1 << 5) {
            transactionWinnedTimingCounter++;
            transactionWinnedTimingAverage = ((transactionWinnedTimingAverage * transactionWinnedTimingCounter)
                    + processTiming - transactionWinnedTimingAverage) / transactionWinnedTimingCounter;
        } else
            transactionWinnedTimingAverage = ((transactionWinnedTimingAverage << 5)
                    + processTiming - transactionWinnedTimingAverage) >> 5;
    }

    public void updateTXValidateTimingAverage(long processTiming, int counter) {
        // тут всегда Количество больше 0 приходит
        processTiming = processTiming / 1000 / counter;
        if (transactionValidateTimingCounter < 1 << 5) {
            transactionValidateTimingCounter++;
            transactionValidateTimingAverage = ((transactionValidateTimingAverage * transactionValidateTimingCounter)
                    + processTiming - transactionValidateTimingAverage) / transactionValidateTimingCounter;
        } else
            transactionValidateTimingAverage = ((transactionValidateTimingAverage << 5)
                    + processTiming - transactionValidateTimingAverage) >> 5;
    }

    public void updateTXProcessTimingAverage(long processTiming, int counter) {
        if (processTiming < 999999999999l) {
            // при переполнении может быть минус
            // в микросекундах подсчет делаем
            processTiming = processTiming / 1000 / (Controller.BLOCK_AS_TX_COUNT + counter);
            if (transactionProcessTimingCounter < 1 << 5) {
                transactionProcessTimingCounter++;
                transactionProcessTimingAverage = ((transactionProcessTimingAverage * transactionProcessTimingCounter)
                        + processTiming - transactionProcessTimingAverage) / transactionProcessTimingCounter;
            } else
                transactionProcessTimingAverage = ((transactionProcessTimingAverage << 5)
                        + processTiming - transactionProcessTimingAverage) >> 5;
        }
    }

    public Pair<Block, List<Transaction>> scanTransactions(DCSet dcSet, Block block, int blockLimit, int transactionLimit, int type, int service, Account account) {
        //CREATE LIST
        List<Transaction> transactions = new ArrayList<Transaction>();
        int counter = 0;

        //IF NO BLOCK START FROM GENESIS
        if (block == null) {
            block = new GenesisBlock();
        }

        //START FROM BLOCK
        int scannedBlocks = 0;
        do {
            int seqNo = 0;
            //FOR ALL TRANSACTIONS IN BLOCK
            for (Transaction transaction : block.getTransactions()) {

                transaction.setDC(dcSet, Transaction.FOR_NETWORK, block.heightBlock, ++seqNo);

                //CHECK IF ACCOUNT INVOLVED
                if (account != null && !transaction.isInvolved(account)) {
                    continue;
                }

                //CHECK IF TYPE OKE
                if (type != -1 && transaction.getType() != type) {
                    continue;
                }

                //CHECK IF SERVICE OKE
                if (service != -1 && transaction.getType() == Transaction.ARBITRARY_TRANSACTION) {
                    ArbitraryTransaction arbitraryTransaction = (ArbitraryTransaction) transaction;

                    if (arbitraryTransaction.getService() != service) {
                        continue;
                    }
                }

                //ADD TO LIST
                transactions.add(transaction);
                counter++;
            }

            //SET BLOCK TO CHILD
            block = block.getChild(dcSet);
            scannedBlocks++;
        }
        //WHILE BLOCKS EXIST && NOT REACHED TRANSACTIONLIMIT && NOT REACHED BLOCK LIMIT
        while (block != null && (counter < transactionLimit || transactionLimit == -1) && (scannedBlocks < blockLimit || blockLimit == -1));

        //CHECK IF WE REACHED THE END
        if (block == null) {
            block = this.getLastBlock(dcSet);
        } else {
            block = block.getParent(dcSet);
        }

        //RETURN PARENT BLOCK AS WE GET CHILD RIGHT BEFORE END OF WHILE
        return new Pair<Block, List<Transaction>>(block, transactions);
    }

    public Block getLastBlock(DCSet dcSet) {
        return dcSet.getBlockMap().last();
    }


	/*
	// IT IS RIGHTS ONLY WHEN BLOCK is MAKING
	// MABE used only in isValid and in Block Generator
	public static int calcGeneratingBalance(DCSet dcSet, Account creator, int height)
	{

		long incomed_amount = 0l;
		//long amount;

		int previousForgingHeight = getPreviousForgingHeightForCalcWin(dcSet, creator, height);
		int previousForgingHeight = creator.getForgingData(dcSet, height);
		if (previousForgingHeight == -1) {
			// IF BLOCK not inserted in MAP
			previousForgingHeight = creator.getLastForgingData(dcSet);
		}

		if (false && !BlockChain.DEVELOP_USE) {
			if (height > 87090 && height - previousForgingHeight < 10 ) {
				return -1;
			}
		}

		if (previousForgingHeight > height) {
			return height;
		}

		previousForgingHeight++;
		if (previousForgingHeight < height) {

			// for recipient only
			List<Transaction> txs = dcSet.getTransactionFinalMap().findTransactions(null, null, creator.getAddress(),
					previousForgingHeight, height,
					0, 0, false, 0, 0);

			//amount = 0l;
			for(Transaction transaction: txs)
			{
				if ( transaction.getAbsKey() != Transaction.RIGHTS_KEY )
					continue;

				transaction.setDC(dcSet, false);
				if (transaction instanceof TransactionAmount) {
					TransactionAmount recordAmount = (TransactionAmount) transaction;
					if (recordAmount.isBackward())
						continue;

					int amo_sign = recordAmount.getAmount().signum();
					if (amo_sign > 0) {
						// SEND or DEBT
						incomed_amount += recordAmount.getAmount().longValue();
					} else {
						continue;
					}
					//} else if (transaction instanceof CreateOrderTransaction) {
					//	amount = transaction.getAmount().longValue();
				} else {
					continue;
				}
				//incomed_amount += amount;
			}

			// for creator
			txs = dcSet.getTransactionFinalMap().findTransactions(null, creator.getAddress(), null,
					previousForgingHeight, height,
					0, 0, false, 0, 0);

			//amount = 0l;
			for(Transaction transaction: txs)
			{
				transaction.setDC(dcSet, false);

				if (false && transaction instanceof RSertifyPubKeys) {
					//	amount = BlockChain.GIFTED_ERA_AMOUNT.intValue();
					//	incomed_amount += amount;

				} else if (transaction instanceof TransactionAmount) {

					if ( transaction.getAbsKey() != Transaction.RIGHTS_KEY )
						continue;

					TransactionAmount recordAmount = (TransactionAmount) transaction;
					// TODO: delete  on new CHAIN
					if (height > 45281 && recordAmount.isBackward()
							&& Account.actionType(recordAmount.getKey(), recordAmount.getAmount()) == 2) {
						// RE DEBT to me
						long amount = transaction.getAmount().abs().longValue();
						if (amount < 200) {
							continue;
						} else if (amount < 1000) {
							amount >>=2;
						} else {
							amount >>=1;
						}
						incomed_amount += amount;
					} else {
						continue;
					}
				} else {
					continue;
				}

				//incomed_amount += amount;
			}
			txs = null;
		}

		// OWN + RENT balance - in USE
		long used_amount = creator.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).longValue();
		if (used_amount < BlockChain.MIN_GENERATING_BALANCE)
			return 0;

		if (used_amount - incomed_amount < BlockChain.MIN_GENERATING_BALANCE ) {
			return BlockChain.MIN_GENERATING_BALANCE;
		} else {
			return (int)(used_amount - incomed_amount);
		}
	}
	OLD
	 */

    public byte[] getLastBlockSignature(DCSet dcSet) {
        return dcSet.getBlockMap().getLastBlockSignature();
    }

    // get last blocks for target
    public List<Block> getLastBlocksForTarget_old(DCSet dcSet) {

        Block last = dcSet.getBlockMap().last();

		/*
		if (this.lastBlocksForTarget != null
				&& Arrays.equals(this.lastBlocksForTarget.get(0).getSignature(), last.getSignature())) {
			return this.lastBlocksForTarget;
		}
		 */

        List<Block> list = new ArrayList<Block>();

        if (last == null || last.getVersion() == 0) {
            return list;
        }

        for (int i = 0; i < TARGET_COUNT && last.getVersion() > 0; i++) {
            list.add(last);
            last = last.getParent(dcSet);
        }

        return list;
    }

    // get Target by last blocks in chain
    public long getTarget(DCSet dcSet) {
        Block block = this.getLastBlock(dcSet);
        return block.getTarget();
    }

    // CLEAR UNCONFIRMED TRANSACTION from Invalid and DEAD
    public void clearUnconfirmedRecords(DCSet dcSet, boolean cutDeadTime) {

        dcSet.getTransactionMap().clearByDeadTimeAndLimit(this.getTimestamp(dcSet), cutDeadTime);

    }
}
