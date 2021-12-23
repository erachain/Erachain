package org.erachain.dapp.epoch.memoCards;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.dapp.DAPPFactory;
import org.erachain.dapp.EpochDAPPjson;
import org.erachain.dapp.epoch.shibaverse.server.Farm_01;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.erachain.datachain.SmartContractValues;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.webserver.WebResource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;

public class MemoCardsDAPP extends EpochDAPPjson {

    int WAIT_RAND = 3;

    static public final int ID = 1002;
    static public final String NAME = "Memo Cards";

    final public static HashSet<PublicKeyAccount> accounts = new HashSet<>();

    // APPBQyonEPbk2ZazbUuHZ2ffN1QJYaK1ow
    final public static PublicKeyAccount MAKER = PublicKeyAccount.makeForDApp(crypto.digest(Longs.toByteArray(ID)));

    static {
        accounts.add(MAKER);
    }

    private static JSONObject farm_01_settings = new JSONObject();

    public static Farm_01 FARM_01_SERVER = null;

    static {

        if (DAPPFactory.settingsJSON.containsKey("memaCards")) {
            boolean farm_01 = (boolean) ((JSONObject) DAPPFactory.settingsJSON.get("memaCards")).getOrDefault("farm_01", false);
            if (false && farm_01)
                FARM_01_SERVER = new Farm_01(farm_01_settings);
        }
    }


    /**
     * admin account
     */
    final static public Account adminAddress = new Account("7NhZBb8Ce1H2S2MkPerrMnKLZNf9ryNYtP");

    final static public String COMMAND_WITHDRAW = "withdraw";
    /**
     * in Title: buy
     * in message - asset key
     * Example of message: ["buy", 1001]
     */
    final static public String COMMAND_BUY = "buy";
    final static public long BUSTER_1_KEY = 1001L;
    /**
     * use as JSONArray in TX message. Title will be ignoged.
     * ["set price", { "shop assetKey1": {"price assetKey1": "price value", ...}}]<br>For example:
     * ["set price",{"1": {"2":"0.1","18":2}}]
     */
    final static public String COMMAND_SET_PRICE = "set price";

    final static public String COMMAND_STAKE = "stake";

    /**
     * make random from future
     */
    final static public String COMMAND_RANDOM = "random";

    /**
     * GRAVUTA KEY
     */
    static final private Tuple2 INIT_KEY = new Tuple2(ID, "i");

    private Long gravitaKey;

    public static final int RARE_COMMON = 0;
    public static final int RARE_UNCOMMON = 1;
    public static final int RARE_RARE = 2;
    public static final int RARE_EPIC = 3;

    public MemoCardsDAPP(String data, String status) {
        super(ID, MAKER, data, status);
    }

    public String getName() {
        return NAME;
    }


    public static MemoCardsDAPP make(RSend txSend, String dataStr) {
        // dataStr = null
        if (dataStr == null || dataStr.isEmpty())
            return null;

        return new MemoCardsDAPP(dataStr, "");

    }

    private boolean isAdminCommand(Transaction transaction) {
        return transaction.getCreator().equals(adminAddress);
    }

    @Override
    public boolean isValid(DCSet dcSet, Transaction transaction) {
        if (isAdminCommand(transaction)) {
            return true;
        }

        if (!dcSet.getSmartContractValues().contains(INIT_KEY)) {
            error("not initated yet");
            return false;
        }

        if (gravitaKey == null)
            gravitaKey = (Long) dcSet.getSmartContractValues().get(INIT_KEY);

        if (gravitaKey == null) {
            error("not initated yet");
            return false;
        }

        return true;

    }

    /// PARSE / TOBYTES

    public static MemoCardsDAPP Parse(byte[] bytes, int pos, int forDeal) {

        // skip ID
        pos += 4;

        String data;
        String status;
        if (forDeal == Transaction.FOR_DB_RECORD) {
            byte[] dataSizeBytes = Arrays.copyOfRange(bytes, pos, pos + 4);
            int dataSize = Ints.fromByteArray(dataSizeBytes);
            pos += 4;
            byte[] dataBytes = Arrays.copyOfRange(bytes, pos, pos + dataSize);
            pos += dataSize;
            data = new String(dataBytes, StandardCharsets.UTF_8);

            byte[] statusSizeBytes = Arrays.copyOfRange(bytes, pos, pos + 4);
            int statusLen = Ints.fromByteArray(statusSizeBytes);
            pos += 4;
            byte[] statusBytes = Arrays.copyOfRange(bytes, pos, pos + statusLen);
            pos += statusLen;
            status = new String(statusBytes, StandardCharsets.UTF_8);

        } else {
            data = "";
            status = "";
        }

        return new MemoCardsDAPP(data, status);
    }

    ///////// COMMANDS

    /**
     * @param block
     * @param transaction
     * @param nonce
     * @return
     */
    public static byte[] getRandHash(Block block, Transaction transaction, int nonce) {

        byte[] hash = new byte[32];
        System.arraycopy(block.getSignature(), 0, hash, 0, 14);
        System.arraycopy(Ints.toByteArray(nonce), 0, hash, 14, 4);
        System.arraycopy(transaction.getSignature(), 0, hash, 18, 14);

        hash = crypto.digest(hash);
        int slot = 0;
        int slotRare;
        int slotRareLvl;

        byte[] randomArray = new byte[28];

        // GET 2 rabdom levels of Rarity
        int index = 0;
        do {
            slotRare = Ints.fromBytes((byte) 0, (byte) 0, hash[index++], hash[index++]);
            if (slotRare == 0)
                slotRareLvl = 15;
            else if (slotRare < 3)
                slotRareLvl = 14;
            else if (slotRare < 7)
                slotRareLvl = 13;
            else if (slotRare < 15)
                slotRareLvl = 12;
            else if (slotRare < 31)
                slotRareLvl = 11;
            else if (slotRare < 63)
                slotRareLvl = 10;
            else if (slotRare < 127)
                slotRareLvl = 9;
            else if (slotRare < 255)
                slotRareLvl = 8;
            else if (slotRare < 511)
                slotRareLvl = 7;
            else if (slotRare < 1023)
                slotRareLvl = 6;
            else if (slotRare < 2047)
                slotRareLvl = 5;
            else if (slotRare < 4095)
                slotRareLvl = 4;
            else if (slotRare < 8192)
                slotRareLvl = 3;
            else if (slotRare < 16383)
                slotRareLvl = 2;
            else if (slotRare < 32767)
                slotRareLvl = 1;
            else
                slotRareLvl = 0;

            randomArray[slot] = (byte) slotRareLvl;

        } while (++slot < 2);

        // GET 32 - 2*2 rabdom values
        do {
            randomArray[slot] = hash[index++];
        } while (++slot < 28);

        return randomArray;

    }

    private int openBuster_1_getSetCount(int setID, int rareLevel) {
        switch (setID) {
            case 1:
                switch (rareLevel) {
                    case RARE_COMMON:
                        return 10;
                    case RARE_UNCOMMON:
                        return 3;
                    case RARE_RARE:
                        return 1;
                    case RARE_EPIC:
                        return 0;
                }
        }
        return 256;
    }

    /**
     * @param dcSet
     * @param block
     * @param commandTX
     * @param setID     ID of set
     * @param rareLevel level of card rarity
     * @param charValue characterictic value
     * @param asOrphan
     */
    private void makeAsset(DCSet dcSet, Block block, RSend commandTX, int setID, int rareLevel, int charValue, boolean asOrphan) {
        int setCount = openBuster_1_getSetCount(setID, rareLevel);
        charValue = setCount * (2 * Short.MAX_VALUE - 1) / charValue;

        String name = "ca" + setID + "." + rareLevel + "." + charValue;
        Tuple2 keyID = new Tuple2(ID, name);

        Long assetKey;
        SmartContractValues valuesMap = dcSet.getSmartContractValues();

        if (asOrphan) {
            assetKey = (Long) valuesMap.get(keyID);

            AssetCls asset = dcSet.getItemAssetMap().get(assetKey);
            if (asset.getReleased(dcSet).equals(BigDecimal.ONE)) {
                // DELETE FROM BLOCKCHAIN DATABASE
                dcSet.getItemAssetMap().decrementDelete(assetKey);

                // DELETE FROM CONTRACT DATABASE
                valuesMap.delete(keyID);

            }

        } else {
            // seek if already exist
            if (valuesMap.contains(keyID)) {
                assetKey = (Long) valuesMap.get(keyID);
            } else {
                // make new COMET
                JSONObject json = new JSONObject();
                json.put("value", charValue);
                json.put("rare", rareLevel);
                json.put("set", setID);
                json.put("type", "card");
                String description = json.toJSONString();

                AssetVenture randomAsset = new AssetVenture(null, stock, name, null, null,
                        description, AssetCls.AS_INSIDE_ASSETS, 0, 0);
                randomAsset.setReference(commandTX.getSignature(), commandTX.getDBRef());

                //INSERT INTO BLOCKCHAIN DATABASE
                assetKey = dcSet.getItemAssetMap().incrementPut(randomAsset);
                //INSERT INTO CONTRACT DATABASE
                dcSet.getSmartContractValues().put(keyID, assetKey);
            }
        }

        // TRANSFER ASSET
        transfer(dcSet, block, commandTX, stock, commandTX.getCreator(), BigDecimal.ONE, assetKey, asOrphan, null, "buster_1");

    }

    /**
     * make pack by RARE
     *
     * @return
     */
    private void openBuster_1_getPack(DCSet dcSet, Block block, RSend commandTX, int nonce, boolean asOrphan) {
        // GET RANDOM
        byte[] randomArray = getRandHash(block, commandTX, nonce);

        int index = 2;
        if (randomArray[0] < 2) {
            // make COMMON cards
            makeAsset(dcSet, block, commandTX, 1, RARE_COMMON, Ints.fromBytes((byte) 0, (byte) 0, randomArray[index++], randomArray[index++]), asOrphan);
            makeAsset(dcSet, block, commandTX, 1, RARE_COMMON, Ints.fromBytes((byte) 0, (byte) 0, randomArray[index++], randomArray[index++]), asOrphan);
            makeAsset(dcSet, block, commandTX, 1, RARE_COMMON, Ints.fromBytes((byte) 0, (byte) 0, randomArray[index++], randomArray[index++]), asOrphan);
            makeAsset(dcSet, block, commandTX, 1, RARE_COMMON, Ints.fromBytes((byte) 0, (byte) 0, randomArray[index++], randomArray[index++]), asOrphan);

            // make UNCOMMON cards
            makeAsset(dcSet, block, commandTX, 1, RARE_UNCOMMON, Ints.fromBytes((byte) 0, (byte) 0, randomArray[index++], randomArray[index++]), asOrphan);

        } else if (randomArray[0] < 4) {
            // make COMMON cards
            makeAsset(dcSet, block, commandTX, 1, RARE_COMMON, Ints.fromBytes((byte) 0, (byte) 0, randomArray[index++], randomArray[index++]), asOrphan);

            // make UNCOMMON cards
            makeAsset(dcSet, block, commandTX, 1, RARE_UNCOMMON, Ints.fromBytes((byte) 0, (byte) 0, randomArray[index++], randomArray[index++]), asOrphan);
            makeAsset(dcSet, block, commandTX, 1, RARE_UNCOMMON, Ints.fromBytes((byte) 0, (byte) 0, randomArray[index++], randomArray[index++]), asOrphan);
        } else {
            // make COMMON cards
            makeAsset(dcSet, block, commandTX, 1, RARE_COMMON, Ints.fromBytes((byte) 0, (byte) 0, randomArray[index++], randomArray[index++]), asOrphan);

            // make UNCOMMON cards
            makeAsset(dcSet, block, commandTX, 1, RARE_UNCOMMON, Ints.fromBytes((byte) 0, (byte) 0, randomArray[index++], randomArray[index++]), asOrphan);

            // make RARE cards
            makeAsset(dcSet, block, commandTX, 1, RARE_RARE, Ints.fromBytes((byte) 0, (byte) 0, randomArray[index++], randomArray[index++]), asOrphan);
        }

    }

    /**
     * @param dcSet
     * @param commandTX
     * @param asOrphan
     */
    private boolean openBuster_1(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {
        // открываем бустер

        if (!commandTX.hasAmount() || !commandTX.hasPacket() && commandTX.getAmount().signum() <= 0) {
            error("Wrong amount. Need > 0");
            return false;
        } else if (commandTX.isBackward()) {
            error("Wrong direction - backward");
            return false;
        } else if (commandTX.balancePosition() != Account.BALANCE_POS_OWN) {
            error("Wrong balance position. Need OWN[1]");
            return false;
        }

        SmartContractValues valuesMap = dcSet.getSmartContractValues();
        PublicKeyAccount creator = commandTX.getCreator();
        int count = commandTX.getAmount().intValue();

        // need select direction by asOrphan, else decrementDelete will not work!
        int nonce;
        if (asOrphan)
            nonce = 1;
        else
            nonce = count;

        do {

            if (asOrphan)
                nonce++;
            else
                nonce--;

            openBuster_1_getPack(dcSet, block, commandTX, nonce, asOrphan);

        } while (--count > 0);

        if (asOrphan)
            status = "wait";
        else
            status = "done";

        return true;

    }

    private static Tuple2<Integer, String> priceKey(long shopAssetKey, long priceAssetKey) {
        return new Tuple2(ID, "pr" + ("" + shopAssetKey + priceAssetKey).hashCode());
    }

    /**
     * get current price
     *
     * @param dcSet
     * @param shopAssetKey
     * @param priceAssetKey
     * @return
     */
    private BigDecimal shopPrice(DCSet dcSet, long shopAssetKey, long priceAssetKey) {

        SmartContractValues map = dcSet.getSmartContractValues();
        BigDecimal price = (BigDecimal) map.get(priceKey(shopAssetKey, priceAssetKey));

        if (price != null)
            return price;

        switch ((int) shopAssetKey) {
            case (int) BUSTER_1_KEY:
                switch ((int) priceAssetKey) {
                    case 18:
                        return new BigDecimal("0.1");
                    default:
                        // for TEST ONLY
                        return new BigDecimal("0.01");
                }
        }
        if (true) {
            // for TEST ONLY
            return new BigDecimal("0.1");
        }

        return BigDecimal.ZERO;
    }

    private boolean random(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {
        if (commandTX.getAssetKey() == BUSTER_1_KEY)
            return openBuster_1(dcSet, block, commandTX, asOrphan);
        return true;
    }

    /**
     * shop for sell items. Example of message: ["buy", 1001]
     *
     * @param dcSet
     * @param block
     * @param commandTX
     * @param asOrphan
     */
    private boolean shopBuy(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {
        PublicKeyAccount creator = commandTX.getCreator();

        if (asOrphan) {
            long priceAssetKey = commandTX.getAssetKey();
            long shopAssetKey = Long.parseLong(pars.get(1).toString());
            Object[] result = removeState(dcSet);
            if (result.length > 0) {
                BigDecimal amountToSell = (BigDecimal) result[0];
                transfer(dcSet, null, commandTX, creator, stock, amountToSell, shopAssetKey, true, null, null);

                BigDecimal leftAmount = (BigDecimal) result[1];
                if (leftAmount.signum() > 0) {
                    transfer(dcSet, null, commandTX, creator, stock, leftAmount, priceAssetKey, true, null, null);
                }
            }
        } else {

            long shopAssetKey;
            AssetCls shopAsset;
            if (!commandTX.hasAmount()) {
                error("Has not amount");
                return false;
            } else if (commandTX.getAssetKey() != 1 && commandTX.getAssetKey() != 18) {
                error("Wrong asset key. Need 1 or 18");
                return false;
            } else if (commandTX.balancePosition() != Account.BALANCE_POS_OWN) {
                error("Wrong balance position. Need OWN[1]");
                return false;
            } else if (!commandTX.hasPacket() && commandTX.getAmount().signum() <= 0) {
                error("Wrong amount. Need > 0");
                return false;
            } else if (commandTX.isBackward()) {
                error("Wrong direction - backward");
                return false;
            } else if (pars == null) {
                error("Empty pars");
                return false;
            } else {
                try {
                    shopAssetKey = Long.parseLong(pars.get(1).toString());
                } catch (Exception e) {
                    error("Wrong asset key: " + pars.get(1));
                    return false;
                }

                if (shopAssetKey != BUSTER_1_KEY) {
                    error("Wrong asset key");
                    return false;
                }
            }

            long priceAssetKey = commandTX.getAssetKey();
            BigDecimal sellPrice = shopPrice(dcSet, shopAssetKey, priceAssetKey);
            if (sellPrice == null || sellPrice.signum() < 1) {
                error("not priced");
                return false;
            }

            shopAsset = dcSet.getItemAssetMap().get(shopAssetKey);
            if (shopAsset == null) {
                error("Shop asset not exist");
                return false;
            }

            AssetCls priceAsset = commandTX.getAsset();
            BigDecimal leftAmount = commandTX.getAmount();

            BigDecimal amountToSell = leftAmount.multiply(sellPrice).setScale(shopAsset.getScale(), BigDecimal.ROUND_HALF_DOWN);
            if (amountToSell.signum() > 0 && !priceAsset.isUnlimited(stock, false)) {
                Tuple2<BigDecimal, BigDecimal> stockBal = stock.getBalance(dcSet, shopAssetKey, Account.BALANCE_POS_OWN);
                if (amountToSell.compareTo(stockBal.b) > 0) {
                    // if not enought amount
                    if (stockBal.b.signum() > 0) {
                        amountToSell = stockBal.b;
                    } else {
                        amountToSell = BigDecimal.ZERO;
                    }
                }
            }

            if (amountToSell.signum() > 0) {
                // TRANSFER ASSET
                transfer(dcSet, block, commandTX, stock, creator, amountToSell, shopAssetKey, false, null, "buy");

                // RETURN change
                leftAmount = leftAmount.subtract(amountToSell.divide(sellPrice, priceAsset.getScale(), BigDecimal.ROUND_DOWN));
            }

            status = "done x" + sellPrice.toPlainString();

            if (leftAmount.signum() > 0) {
                transfer(dcSet, block, commandTX, stock, creator, leftAmount, priceAssetKey, false, null, "change");
                status += ", change: " + leftAmount.toPlainString();
            }

            // store results for orphan
            putState(dcSet, new Object[]{amountToSell, leftAmount});

        }

        return true;
    }

    //////////////////// ADMIN PROCCESS

    /**
     * Example of command: ["set price", {"1001": {"1": 0.1, "18":"0.01"}}]
     *
     * @param dcSet
     * @param block
     * @param commandTX
     * @param asOrphan
     */
    private boolean shopSetPrices(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {

        SmartContractValues map = dcSet.getSmartContractValues();
        if (asOrphan) {
            Object[] result = removeState(dcSet);
            if (result.length > 0) {
                for (Fun.Tuple3<Long, Long, BigDecimal> item : (Fun.Tuple3<Long, Long, BigDecimal>[]) result) {
                    map.put(priceKey(item.a, item.b), item.c);
                }
            }

        } else {

            Long shopAssetKey;
            Long priceAssetKey;
            JSONObject prices;
            BigDecimal price;

            if (pars == null) {
                error("Wrong JSON params");
                return false;
            } else if (pars.size() < 2) {
                error("Wrong params size <2");
                return false;
            } else {
                prices = (JSONObject) pars.get(1);
                for (Map.Entry<String, Object> item : (Set<Map.Entry<String, Object>>) prices.entrySet()) {
                    try {
                        long assetKey = Long.parseLong(item.getKey());
                        if (!dcSet.getItemAssetMap().contains(assetKey)) {
                            error("Asset not exist for Key: " + item.getKey());
                            return false;
                        }
                    } catch (Exception e) {
                        error("Wrong assetKey: " + item.getKey());
                        return false;
                    }
                    if (!(item.getValue() instanceof JSONObject)) {
                        error("Not JSON: " + item.getValue().toString());
                        return false;
                    }

                    for (Map.Entry<String, Object> priceItem : (Set<Map.Entry<String, Object>>) ((JSONObject) item.getValue()).entrySet()) {
                        try {
                            long assetKey = Long.parseLong(priceItem.getKey());
                            if (!dcSet.getItemAssetMap().contains(assetKey)) {
                                error("Asset not exist for Key: " + priceItem.getKey());
                                return false;
                            }
                        } catch (Exception e) {
                            error("Wrong assetKey: " + priceItem.getKey());
                            return false;
                        }

                        try {
                            new BigDecimal(priceItem.getValue().toString());
                        } catch (Exception e) {
                            error("Wrong price value: " + priceItem.getValue());
                            return false;
                        }

                    }
                }
            }

            List<Fun.Tuple3<Long, Long, BigDecimal>> oldPrices = new ArrayList();
            for (Map.Entry<String, Object> item : (Set<Map.Entry<String, Object>>) prices.entrySet()) {
                shopAssetKey = Long.parseLong(item.getKey());

                for (Map.Entry<String, Object> priceItem : (Set<Map.Entry<String, Object>>) ((JSONObject) item.getValue()).entrySet()) {
                    priceAssetKey = Long.parseLong(priceItem.getKey());

                    // OLD PRICE SAVE
                    price = (BigDecimal) map.get(priceKey(shopAssetKey, priceAssetKey));
                    if (price != null) {
                        oldPrices.add(new Fun.Tuple3<>(shopAssetKey, priceAssetKey, price));
                    }

                    // NEW PRICE
                    price = new BigDecimal(priceItem.getValue().toString());
                    map.put(priceKey(shopAssetKey, priceAssetKey), price);

                }
            }

            // store results for orphan
            putState(dcSet, oldPrices.toArray());

            status = "done";
        }

        return true;

    }

    private boolean adminWithdraw(DCSet dcSet, Block block, RSend commandTX, Account admin, boolean asOrphan) {
        if (asOrphan) {
            // restore results for orphan
            List<Tuple2<Long, BigDecimal>> results = (List<Tuple2<Long, BigDecimal>>) removeState(dcSet)[0];

            for (Tuple2<Long, BigDecimal> row : results) {
                // RE-TRANSFER ASSET from ADMIN
                transfer(dcSet, null, commandTX, admin, stock, row.b, row.a, true, null, null);
            }

        } else {

            if (!dcSet.getSmartContractValues().contains(INIT_KEY)) {
                error("not initated yet");
                return false;
            }

            ItemAssetBalanceMap map = DCSet.getInstance().getAssetBalanceMap();
            try (IteratorCloseable<byte[]> iterator = map.getIteratorByAccount(stock)) {
                List<Tuple2<byte[], Fun.Tuple5<
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> assetsBalances
                        = map.getBalancesList(stock);
                byte[] key;
                long assetKey;
                List<Tuple2<Long, BigDecimal>> results = new ArrayList<>();
                while (iterator.hasNext()) {
                    key = iterator.next();

                    assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(key);
                    if (assetKey == AssetCls.LIA_KEY) {
                        continue;
                    }

                    Fun.Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
                            itemBals = map.get(key);

                    if (itemBals == null)
                        continue;

                    // TRANSFER ASSET to ADMIN
                    transfer(dcSet, block, commandTX, stock, admin, itemBals.a.b, assetKey, false, null, "adminWithdraw");

                    results.add(new Tuple2(assetKey, itemBals.a.b));

                }

                // store results for orphan
                putState(dcSet, new Object[]{results});

                status = "done";

            } catch (IOException e) {
                error(e.getMessage());
            }
        }

        return true;

    }

    private boolean init(DCSet dcSet, Block block, RSend commandTX, Account admin, boolean asOrphan) {

        /**
         * issue main currency
         */
        BigDecimal amount = new BigDecimal("10000");
        if (asOrphan) {

            // need to remove INIT_KEY - for reinit after orphans
            gravitaKey = (Long) dcSet.getSmartContractValues().remove(INIT_KEY);

            // BACKWARDS from ADMIN
            transfer(dcSet, block, commandTX, stock, admin, amount, gravitaKey, true, null, null);

            // orphan GRAVITA ASSET
            dcSet.getItemAssetMap().decrementDelete(gravitaKey);

        } else {

            if (dcSet.getSmartContractValues().contains(INIT_KEY)) {
                error("already initated");
                return false;
            }

            AssetVenture gravita = new AssetVenture(null, stock, "GR", null, null,
                    null, AssetCls.AS_INSIDE_ASSETS, 6, 0);
            gravita.setReference(commandTX.getSignature(), commandTX.getDBRef());

            //INSERT INTO DATABASE
            gravitaKey = dcSet.getItemAssetMap().incrementPut(gravita);
            dcSet.getSmartContractValues().put(INIT_KEY, gravitaKey);

            // TRANSFER GRAVITA to ADMIN
            transfer(dcSet, block, commandTX, stock, admin, amount, gravitaKey, false, null, "init");

            status = "done";
        }

        return true;
    }

    /**
     * admin commands
     *
     * @param dcSet
     * @param block
     * @param rSend
     * @param admin
     * @return
     */
    public boolean processAdminCommands(DCSet dcSet, Block block, RSend rSend, Account admin) {
        if ("init".equals(command)) {
            return init(dcSet, block, rSend, admin, false);
        } else if (command.startsWith("emite")) {
        } else if (command.startsWith(COMMAND_WITHDRAW)) {
            return adminWithdraw(dcSet, block, rSend, admin, false);
        } else if (COMMAND_SET_PRICE.equals(command)) {
            return shopSetPrices(dcSet, block, rSend, false);
        }

        error("unknow command");
        return false;
    }

    @Override
    public boolean process(DCSet dcSet, Block block, Transaction transaction) {

        if (!isValid(dcSet, transaction))
            return true;

        if (transaction instanceof RSend) {
            RSend rsend = (RSend) transaction;
            if (isAdminCommand(transaction)) {
                return processAdminCommands(dcSet, block, rsend,
                        rsend.getCreator() // need for TEST - not adminAddress
                );
            } else {
                if (COMMAND_RANDOM.equals(command)) {
                    if (block == null) {
                        status = "wait block";
                        return false;
                    }
                    // это не проверка вне блока - в ней блока нет
                    dcSet.getTimeTXWaitMap().put(transaction.getDBRef(), block.heightBlock + WAIT_RAND);
                    status = "wait";
                    return false;

                } else if (COMMAND_BUY.equals(command)) {
                    return shopBuy(dcSet, block, rsend, false);
                }
            }
        }

        error("unknow command");
        return false;

    }

    @Override
    public boolean processByTime(DCSet dcSet, Block block, Transaction transaction) {

        if (COMMAND_RANDOM.equals(command)) {
            return random(dcSet, block, (RSend) transaction, false);
        }

        error("unknow command");
        return false;

    }

    public void orphanAdminCommands(DCSet dcSet, RSend rSend, Account admin) {
        if ("init".equals(command)) {
            init(dcSet, null, rSend, admin, true);
        } else if (command.startsWith(COMMAND_WITHDRAW)) {
            adminWithdraw(dcSet, null, rSend, admin, true);
        } else if (COMMAND_SET_PRICE.equals(command)) {
            shopSetPrices(dcSet, null, rSend, true);
        }

    }

    @Override
    public void orphan(DCSet dcSet, Transaction transaction) {

        if (status.startsWith("error")) {
            // not processed
            return;
        }

        if (isAdminCommand(transaction)) {
            orphanAdminCommands(dcSet, (RSend) transaction,
                    transaction.getCreator() // need for TEST - not adminAddress
            );
        }

        if (COMMAND_RANDOM.equals(command)) {
            // отмена рождения комет
            dcSet.getTimeTXWaitMap().remove(transaction.getDBRef());
        } else if (COMMAND_BUY.equals(command)) {
            shopBuy(dcSet, null, (RSend) transaction, true);
        }

    }

    @Override
    public void orphanByTime(DCSet dcSet, Block block, Transaction transaction) {
        if (COMMAND_RANDOM.equals(command)) {
            random(dcSet, block, (RSend) transaction, true);
        }

    }

    private static String[][][] imgsStr;

    {
        imgsStr = new String[][][]{
                new String[][]{
                        new String[]{"1050868", WebResource.TYPE_IMAGE.toString()},
                        new String[]{"1050867", WebResource.TYPE_IMAGE.toString()},
                },
                new String[][]{
                        new String[]{"1050864", WebResource.TYPE_IMAGE.toString()},
                        new String[]{"1050862", WebResource.TYPE_IMAGE.toString()},
                        null,
                        new String[]{"1050863", WebResource.TYPE_IMAGE.toString()},
                },
                new String[][]{
                        new String[]{"1050860", WebResource.TYPE_IMAGE.toString()},
                },
                new String[][]{
                        null,
                        new String[]{"1050866", WebResource.TYPE_IMAGE.toString()},
                },
                new String[][]{
                        new String[]{"1050857", WebResource.TYPE_IMAGE.toString()},
                        new String[]{"1050859", WebResource.TYPE_IMAGE.toString()},
                        new String[]{"1050858", WebResource.TYPE_IMAGE.toString()},
                },
                new String[][]{
                        new String[]{"1050856", WebResource.TYPE_IMAGE.toString()},
                        new String[]{"1050855", WebResource.TYPE_IMAGE.toString()},
                        null,
                        new String[]{"1050854", WebResource.TYPE_IMAGE.toString()},
                },
                null,
                new String[][]{
                        null,
                        null,
                        new String[]{"1050852", WebResource.TYPE_IMAGE.toString()},
                        null,
                        new String[]{"1050851", WebResource.TYPE_IMAGE.toString()},
                },
        };
    }

    static int confirms = 10;
    static int deploy_period = 3;

    public static String getImageURL(AssetCls asset) {

        JSONArray arrayJson = new JSONArray();
        JSONObject item;


        int height = Transaction.parseHeightDBRef(asset.getDBref());

        if (contr.getMyHeight() < height + deploy_period + confirms) {
            item = new JSONObject();
            item.put("url", "/apiasset/image/1050869");
            item.put("type", WebResource.TYPE_IMAGE.toString());
            arrayJson.add(item);
            return arrayJson.toJSONString();
        }

        Block.BlockHead blockHead = DCSet.getInstance().getBlocksHeadsMap().get(height + deploy_period);

        byte[] hash = blockHead.signature;
        byte[] hash2 = Ints.toByteArray((int) asset.getKey());
        System.arraycopy(hash2, 0, hash, 0, hash2.length);

        hash = crypto.digest(hash);
        int slot = 0;
        int slotRare;
        int slotRareLvl;

        String[][] slotArray;
        do {
            slotRare = Ints.fromBytes((byte) 0, (byte) 0, hash[slot << 1], hash[(slot << 1) + 1]);
            if ((slotRare >> 11) == 0) {
                slotRareLvl = 5;
            } else if ((slotRare >> 12) == 0) {
                slotRareLvl = 4;
            } else if ((slotRare >> 13) == 0) {
                slotRareLvl = 3;
            } else if ((slotRare >> 14) == 0) {
                slotRareLvl = 2;
            } else if ((slotRare >> 15) == 0) {
                slotRareLvl = 1;
            } else {
                slotRareLvl = 0;
            }

            slotArray = imgsStr[slot];
            if (slotArray == null)
                continue;

            if (slotArray.length <= slotRareLvl) {
                slotRareLvl = slotArray.length - 1;
            }

            String[] itemArray;
            do {
                itemArray = slotArray[slotRareLvl];
            } while (itemArray == null && slotRareLvl-- > 0);

            if (itemArray == null)
                continue;

            item = new JSONObject();
            item.put("url", "/apiasset/image/" + itemArray[0]);
            item.put("type", itemArray[1]);
            arrayJson.add(item);

        } while (slot++ < 7);

        item = new JSONObject();
        item.put("url", "/apiasset/image/1050853");
        item.put("type", WebResource.TYPE_IMAGE.toString());
        arrayJson.add(item);
        item = new JSONObject();
        item.put("url", "/apiasset/image/1050865");
        item.put("type", WebResource.TYPE_IMAGE.toString());
        arrayJson.add(item);

        return arrayJson.toJSONString();

    }

    static DecimalFormat format2 = new DecimalFormat("#.##");

    public static String viewDescription(AssetCls asset, String description) {
        int released = asset.getReleased(DCSet.getInstance()).intValue();
        double rary = Math.sqrt(1.0d / released);
        return "<html>RARY: <b>" + format2.format(rary) + "</b><br>" + description + "</html>";
    }

    public static void setDAPPFactory(HashMap<Account, Integer> skocks) {
        for (Account account : accounts) {
            skocks.put(account, ID);
        }
    }

}
