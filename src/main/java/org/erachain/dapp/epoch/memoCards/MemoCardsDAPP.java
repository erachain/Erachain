package org.erachain.dapp.epoch.memoCards;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.apache.commons.net.util.Base64;
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
import org.erachain.datachain.CreditAddressesMap;
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

    final public static byte[] HASH = crypto.digest(Longs.toByteArray(ID));
    // APPC45p29ZjcEEvSzhgUe8RfUzMZ1i2GFG
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
    final static public String COMMAND_CATH_COMET = "catch comets";
    /**
     * in Title: buy
     * in message - asset key
     */
    final static public String COMMAND_BUY = "buy";
    final static public long buster01Key = 11111L;
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

    final static public String COMMAND_FARM = "farm";
    final static public String COMMAND_CHARGE = "charge";
    final static public String COMMAND_PICK_UP = "pick up";
    /**
     * GRAVUTA KEY
     */
    static final private Tuple2 INIT_KEY = new Tuple2(ID, "i");

    private Long gravitaKey;

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

        Account recipent = txSend.getRecipient();

        if (recipent.equals(FARM_01_PUBKEY)) {
            if (txSend.balancePosition() == Account.BALANCE_POS_DEBT && txSend.hasAmount()) {
                return new MemoCardsDAPP(txSend.isBackward() ? COMMAND_PICK_UP : COMMAND_FARM, "");
            } else if (txSend.balancePosition() == Account.BALANCE_POS_OWN) {
                return new MemoCardsDAPP(COMMAND_CHARGE, "");
            }
        }

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

    public static byte[] getRandHash(Block block, Transaction transaction, int nonce) {

        byte[] hash = new byte[32];
        System.arraycopy(block.getSignature(), 0, hash, 0, 14);
        System.arraycopy(Ints.toByteArray(nonce), 0, hash, 14, 4);
        System.arraycopy(transaction.getSignature(), 0, hash, 18, 14);

        hash = crypto.digest(hash);
        int slot = 0;
        int slotRare;
        int slotRareLvl;

        byte[] randomArray = new byte[8];

        // GET 4 rabdom levels of Rarity
        int index = 0;
        do {
            slotRare = Ints.fromBytes((byte) 0, (byte) 0, hash[index++], hash[index++]);
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
            randomArray[slot] = (byte) slotRareLvl;

        } while (slot++ < 3);

        // GET 4 rabdom values
        do {
            randomArray[slot] = hash[index++];
        } while (slot++ < 7);

        return randomArray;

    }

    /**
     * @param dcSet
     * @param commandTX
     * @param asOrphan
     */
    private boolean catchComets(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {
        // рождение комет

        if (gravitaKey == null)
            gravitaKey = (Long) dcSet.getSmartContractValues().get(INIT_KEY);

        if (commandTX.getAssetKey() != gravitaKey) {
            error("Wrong asset key. Need " + gravitaKey);
            return false;
        } else if (!commandTX.hasAmount() || !commandTX.hasPacket() && commandTX.getAmount().signum() <= 0) {
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
        int count = 5 * commandTX.getAmount().intValue();

        // need select direction by asOrphan, else decrementDelete will not work!
        int nonce;
        if (asOrphan)
            nonce = 1;
        else
            nonce = count;

        AssetVenture comet;
        Long assetKey;
        do {

            // GET RANDOM
            byte[] randomArray = getRandHash(block, commandTX, nonce);
            if (asOrphan)
                nonce++;
            else
                nonce--;

            // make object name: "c" - comet, "0" - era, Rarity1,2, Value1,2,
            int value1 = Byte.toUnsignedInt(randomArray[7]) >>> 5;
            int value2 = Byte.toUnsignedInt(randomArray[6]) >>> 5;
            String name = "c0" + randomArray[0] + randomArray[1] + value1 + value2;
            Tuple2 keyID = new Tuple2(ID, name);

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
                    json.put("value1", value1);
                    json.put("value2", value2);
                    json.put("rare1", Byte.toUnsignedInt(randomArray[6]));
                    json.put("rare2", Byte.toUnsignedInt(randomArray[7]));
                    json.put("type", "Comet");
                    json.put("random", Base64.encodeBase64StringUnChunked(randomArray));
                    String description = json.toJSONString();

                    comet = new AssetVenture(null, stock, name, null, null,
                            description, AssetCls.AS_INSIDE_ASSETS, 0, 0);
                    comet.setReference(commandTX.getSignature(), commandTX.getDBRef());

                    //INSERT INTO BLOCKCHAIN DATABASE
                    assetKey = dcSet.getItemAssetMap().incrementPut(comet);
                    //INSERT INTO CONTRACT DATABASE
                    dcSet.getSmartContractValues().put(keyID, assetKey);
                }
            }

            // TRANSFER ASSET
            transfer(dcSet, block, commandTX, stock, creator, BigDecimal.ONE, assetKey, asOrphan, null, "catchComets");

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
            case (int) buster01Key:
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

                if (false && shopAssetKey != buster01Key) {
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

    private boolean stakeAction(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {
        if (commandTX.getAssetKey() != gravitaKey) {
            error("Wrong asset key. Need " + gravitaKey);
            return false;
        } else if (!commandTX.hasAmount() || !commandTX.hasPacket() && commandTX.getAmount().signum() <= 0) {
            error("Wrong amount. Need > 0");
            return false;
        } else if (commandTX.isBackward()) {
            error("Wrong direction - backward");
            return false;
        } else if (commandTX.balancePosition() != Account.BALANCE_POS_OWN) {
            error("Wrong balance position. Need [1]OWN");
            return false;
        }

        error("-");
        return false;
    }

    // CHARGE all from FARM 01
    private void farmChargeAction(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {
        SmartContractValues mapValues = dcSet.getSmartContractValues();
        Tuple2<Integer, String> farmKeyValue;
        BigDecimal farmedValue;
        try (IteratorCloseable iterator = mapValues.getIterator()) {
            while (iterator.hasNext()) {
                Object key = iterator.next();
                farmKeyValue = new Tuple2<>(ID, "farm1" + key.toString());
                farmedValue = (BigDecimal) mapValues.get(farmKeyValue);
            }
        } catch (IOException e) {
        }

    }

    private boolean farmAction(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {

        error("-");

        if (asOrphan) {
            return true;

        } else {
            AssetCls asset = dcSet.getItemAssetMap().get(commandTX.getAssetKey());
            if (!asset.getMaker().equals(MAKER)) {
                error("Wrong asset maker. Need " + MAKER.getAddress());
                return false;
            } else if (!commandTX.hasAmount()) {
                error("Wrong amount. Need > 0");
                return false;
            } else if (commandTX.isBackward()) {
                error("Wrong direction - backward");
                return false;
            } else if (commandTX.balancePosition() != Account.BALANCE_POS_DEBT) {
                error("Wrong balance position. Need [2]DEBT");
                return false;
            }
        }

        if (commandTX.isBackward()) {
            // WITHDRAW
            farmChargeAction(dcSet, block, commandTX, asOrphan);
        } else {
            // DEPOSITE

        }

        return false;
    }

    private static void farm(DCSet dcSet, Block block, boolean asOrphan) {
        CreditAddressesMap map = dcSet.getCreditAddressesMap();
        SmartContractValues mapValues = dcSet.getSmartContractValues();
        Fun.Tuple3<String, Long, String> key;
        BigDecimal credit;
        Tuple2<Integer, String> farmKeyValue;
        BigDecimal farmedValue;
        HashMap<String, BigDecimal> results = new HashMap<>();
        try (IteratorCloseable<Fun.Tuple3<String, Long, String>> iterator = map.getDebitorsIterator(FARM_01_PUBKEY.getAddress())) {
            while (iterator.hasNext()) {
                key = iterator.next();
                credit = map.get(key);
                if (credit.signum() < 1)
                    continue;

                farmKeyValue = new Tuple2<>(ID, "farm1" + key.a);
                farmedValue = (BigDecimal) mapValues.get(farmKeyValue);
                if (farmedValue.signum() > 0) {
                    // not charged yet
                    continue;
                }

                if (key.b == 1048579L || key.b == 1048587) {
                    farmedValue = results.get(key.a);
                    if (farmedValue == null)
                        farmedValue = BigDecimal.ZERO;

                    farmedValue = farmedValue.add(BigDecimal.ONE);
                    results.put(key.a, farmedValue);
                }
            }
        } catch (IOException e) {
        }

        // SAVE RESULTS
        for (String address : results.keySet()) {
            farmKeyValue = new Tuple2<>(ID, "farm1" + address);
            mapValues.put(farmKeyValue, results.get(address));
        }


    }

    //////// PROCESSES
    public static void blockAction(DCSet dcSet, Block block, boolean asOrphan) {
        if (true && block.heightBlock % 100 == 0) {
            farm(dcSet, block, asOrphan);
        }
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
        if (asOrphan) {
            // need to remove INIT_KEY - for reinit after orphans
            gravitaKey = (Long) dcSet.getSmartContractValues().remove(INIT_KEY);

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
            BigDecimal amount = new BigDecimal("10000");
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
                if (COMMAND_CATH_COMET.equals(command) || COMMAND_RANDOM.equals(command)
                        // это не проверка вне блока - в ней блока нет
                        && block != null) {
                    // рождение комет
                    dcSet.getTimeTXWaitMap().put(transaction.getDBRef(), block.heightBlock + WAIT_RAND);
                    status = "wait";
                    return false;
                } else if (COMMAND_BUY.equals(command)) {
                    return shopBuy(dcSet, block, rsend, false);
                } else if (COMMAND_STAKE.equals(command)) {
                    return stakeAction(dcSet, block, rsend, false);
                } else if (COMMAND_FARM.equals(command) || COMMAND_PICK_UP.equals(command)) {
                    return farmAction(dcSet, block, rsend, false);
                }
            }
        }

        error("unknow command");
        return false;

    }

    @Override
    public boolean processByTime(DCSet dcSet, Block block, Transaction transaction) {

        if (COMMAND_CATH_COMET.equals(command)) {
            return catchComets(dcSet, block, (RSend) transaction, false);
        } else if (COMMAND_RANDOM.equals(command)) {
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

        if (COMMAND_CATH_COMET.equals(command) || COMMAND_RANDOM.equals(command)) {
            // отмена рождения комет
            dcSet.getTimeTXWaitMap().remove(transaction.getDBRef());
        } else if (COMMAND_BUY.equals(command)) {
            shopBuy(dcSet, null, (RSend) transaction, true);
        } else if (COMMAND_STAKE.equals(command)) {
            farmAction(dcSet, null, (RSend) transaction, true);
        }

    }

    @Override
    public void orphanByTime(DCSet dcSet, Block block, Transaction transaction) {
        if (COMMAND_CATH_COMET.equals(command)) {
            catchComets(dcSet, block, (RSend) transaction, true);
        } else if (COMMAND_RANDOM.equals(command)) {
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
