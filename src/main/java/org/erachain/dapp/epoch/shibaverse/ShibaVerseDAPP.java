package org.erachain.dapp.epoch.shibaverse;

import com.google.common.primitives.Bytes;
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
import org.erachain.dapp.epoch.EpochDAPP;
import org.erachain.dapp.epoch.shibaverse.server.Farm_01;
import org.erachain.datachain.CreditAddressesMap;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.erachain.datachain.SmartContractValues;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.lang.Lang;
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

public class ShibaVerseDAPP extends EpochDAPP {

    int WAIT_RAND = 3;

    static public final int ID = 1001;
    static public final String NAME = "Shiba Verse";

    final public static HashSet<PublicKeyAccount> accounts = new HashSet<>();

    final public static byte[] HASH = crypto.digest(Longs.toByteArray(ID));
    // APPC45p29ZjcEEvSzhgUe8RfUzMZ1i2GFG
    final public static PublicKeyAccount MAKER = PublicKeyAccount.makeForDApp(crypto.digest(Longs.toByteArray(ID)));

    // APPBttBTR6pSEg6FBny3reRG4rkdp8dtG8
    final public static PublicKeyAccount FARM_01_PUBKEY = noncePubKey(HASH, (byte) 1);

    static {
        accounts.add(MAKER);
        accounts.add(FARM_01_PUBKEY);
    }

    private static JSONObject farm_01_settings = new JSONObject();

    public static Farm_01 FARM_01_SERVER = null;

    static {
        farm_01_settings.put("account", FARM_01_PUBKEY.getAddress());

        if (DAPPFactory.settingsJSON.containsKey("shiba")) {
            boolean farm_01 = (boolean) ((JSONObject) DAPPFactory.settingsJSON.get("shiba")).getOrDefault("farm_01", false);
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

    final static public String COMMAND_STAKE = "stake";

    final static public String COMMAND_RANDOM = "random";

    final static public String COMMAND_FARM = "farm";
    final static public String COMMAND_CHARGE = "charge";
    final static public String COMMAND_PICK_UP = "pick up";
    /**
     * GRAVUTA KEY
     */
    static final private Tuple2 INIT_KEY = new Tuple2(ID, "i");

    private String command;
    private String status;

    private Long gravitaKey;

    public ShibaVerseDAPP(String command, String status) {
        super(ID, MAKER);

        this.command = command;
        this.status = status;
    }

    public String getName() {
        return NAME;
    }

    public String getHTML(JSONObject langObj) {
        String out = super.getHTML(langObj) + "<br>";
        return out + Lang.T("Command", langObj) + ": <b>" + (command == null ? "" : command) + "</b><br>"
                + Lang.T("Status", langObj) + ": <b>" + (status == null ? "" : status) + "</b>";
    }

    public static ShibaVerseDAPP make(RSend txSend, String command) {

        Account recipent = txSend.getRecipient();
        if (!accounts.contains(recipent)) {
            return null;
        }

        if (recipent.equals(MAKER)) {
            return new ShibaVerseDAPP(command, "");
        } else if (recipent.equals(FARM_01_PUBKEY)) {
            if (txSend.balancePosition() == Account.BALANCE_POS_DEBT && txSend.hasAmount()) {
                return new ShibaVerseDAPP(txSend.isBackward() ? COMMAND_PICK_UP : COMMAND_FARM, "");
            } else if (txSend.balancePosition() == Account.BALANCE_POS_OWN) {
                return new ShibaVerseDAPP(COMMAND_CHARGE, "");
            }
        }

        return new ShibaVerseDAPP(command, "");

    }

    private boolean isAdminCommand(Transaction transaction) {
        return transaction.getCreator().equals(adminAddress);
    }

    @Override
    public boolean isValid(DCSet dcSet, Transaction transaction) {
        if (isAdminCommand(transaction)) {
            if (command.equals("init") && dcSet.getSmartContractValues().contains(INIT_KEY)) {
                status = "error: already initated";
                return false;
            }
            return true;
        }

        if (gravitaKey == null)
            gravitaKey = (Long) dcSet.getSmartContractValues().get(INIT_KEY);

        if (gravitaKey == null) {
            status = "error: not initated yet";
            return false;
        }

        if (COMMAND_CATH_COMET.equals(command)) {
            if (transaction.getType() != Transaction.SEND_ASSET_TRANSACTION) {
                status = "Wrong transaction type. Need SEND";
            } else {
                RSend rsend = (RSend) transaction;
                if (rsend.getAssetKey() != gravitaKey) {
                    status = "Wrong asset key. Need " + gravitaKey;
                } else if (!rsend.hasAmount() || !rsend.hasPacket() && rsend.getAmount().signum() <= 0) {
                    status = "Wrong amount. Need > 0";
                } else if (rsend.isBackward()) {
                    status = "Wrong direction - backward";
                } else if (rsend.balancePosition() != Account.BALANCE_POS_OWN) {
                    status = "Wrong balance position. Need OWN[1]";
                } else {
                    return true;
                }
            }
        } else if (COMMAND_BUY.equals(command)) {
            if (transaction.getType() != Transaction.SEND_ASSET_TRANSACTION) {
                status = "Wrong transaction type. Need SEND";
            } else {
                RSend rsend = (RSend) transaction;
                if (rsend.getAssetKey() != 1 && rsend.getAssetKey() != 18) {
                    status = "Wrong asset key. Need 1 or 18";
                } else if (!rsend.hasAmount() || !rsend.hasPacket() && rsend.getAmount().signum() <= 0) {
                    status = "Wrong amount. Need > 0";
                } else if (rsend.isBackward()) {
                    status = "Wrong direction - backward";
                } else if (rsend.balancePosition() != Account.BALANCE_POS_OWN) {
                    status = "Wrong balance position. Need OWN[1]";
                } else if (!rsend.isText()) {
                    status = "Not text message";
                } else if (rsend.isEncrypted()) {
                    status = "Encrypted message";
                } else {
                    long assetKey = 0;
                    try {
                        assetKey = Long.parseLong(new String(rsend.getData(), StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        status = "Wrong asset key in message";
                        return false;
                    }

                    if (false && assetKey != buster01Key) {
                        status = "Wrong asset key";
                    } else {
                        return true;
                    }
                }
            }

        } else if (false && COMMAND_STAKE.equals(command)) {
            if (transaction.getType() != Transaction.SEND_ASSET_TRANSACTION) {
                status = "Wrong transaction type. Need SEND";
            } else {
                RSend rsend = (RSend) transaction;
                if (rsend.getAssetKey() != gravitaKey) {
                    status = "Wrong asset key. Need " + gravitaKey;
                } else if (!rsend.hasAmount() || !rsend.hasPacket() && rsend.getAmount().signum() <= 0) {
                    status = "Wrong amount. Need > 0";
                } else if (rsend.isBackward()) {
                    status = "Wrong direction - backward";
                } else if (rsend.balancePosition() != Account.BALANCE_POS_OWN) {
                    status = "Wrong balance position. Need [1]OWN";
                } else {
                    return true;
                }
            }
        } else if (COMMAND_FARM.equals(command)) {
            if (transaction.getType() != Transaction.SEND_ASSET_TRANSACTION) {
                status = "Wrong transaction type. Need SEND";
            } else {
                RSend rsend = (RSend) transaction;
                AssetCls asset = dcSet.getItemAssetMap().get(rsend.getAssetKey());
                if (!asset.getMaker().equals(MAKER)) {
                    status = "Wrong asset maker. Need " + MAKER.getAddress();
                } else if (!rsend.hasAmount()) {
                    status = "Wrong amount. Need > 0";
                } else if (rsend.isBackward()) {
                    status = "Wrong direction - backward";
                } else if (rsend.balancePosition() != Account.BALANCE_POS_DEBT) {
                    status = "Wrong balance position. Need [2]DEBT";
                } else {
                    return true;
                }
            }
        } else if (COMMAND_PICK_UP.equals(command)) {
            if (transaction.getType() != Transaction.SEND_ASSET_TRANSACTION) {
                status = "Wrong transaction type. Need SEND";
            } else {
                RSend rsend = (RSend) transaction;
                AssetCls asset = dcSet.getItemAssetMap().get(rsend.getAssetKey());
                if (!asset.getMaker().equals(MAKER)) {
                    status = "Wrong asset maker. Need " + MAKER.getAddress();
                } else if (!rsend.hasAmount()) {
                    status = "Wrong amount. Need > 0";
                } else if (!rsend.isBackward()) {
                    status = "Wrong direction. Need BACKWARD";
                } else if (rsend.balancePosition() != Account.BALANCE_POS_DEBT) {
                    status = "Wrong balance position. Need [2]DEBT";
                } else {
                    return true;
                }
            }
        } else if (COMMAND_CHARGE.equals(command)) {
            return true;
        } else {
            status = "Unknown command";
        }

        status = "error: " + command + " - " + status;
        return false;

    }

    /// PARSE / TOBYTES
    @Override
    public int length(int forDeal) {

        int len = 4;
        if (forDeal == Transaction.FOR_DB_RECORD) {
            len += 8;
            if (status != null)
                len += status.length();
            if (command != null)
                len += command.length();
        }

        return len;
    }

    @Override
    public byte[] toBytes(int forDeal) {

        byte[] data = Ints.toByteArray(id);

        if (forDeal == Transaction.FOR_DB_RECORD) {
            byte[] commandBytes;
            byte[] statusBytes;

            if (command != null) {
                commandBytes = command.getBytes(StandardCharsets.UTF_8);
            } else {
                commandBytes = new byte[0];
            }
            if (status != null) {
                statusBytes = status.getBytes(StandardCharsets.UTF_8);
            } else {
                statusBytes = new byte[0];
            }
            data = Bytes.concat(data, Ints.toByteArray(commandBytes.length));
            data = Bytes.concat(data, commandBytes);

            data = Bytes.concat(data, Ints.toByteArray(statusBytes.length));
            data = Bytes.concat(data, statusBytes);
        }

        return data;

    }

    public static ShibaVerseDAPP Parse(byte[] data, int pos, int forDeal) {

        // skip ID
        pos += 4;

        String command;
        String status;
        if (forDeal == Transaction.FOR_DB_RECORD) {
            byte[] commandSizeBytes = Arrays.copyOfRange(data, pos, pos + 4);
            int commandSize = Ints.fromByteArray(commandSizeBytes);
            pos += 4;
            byte[] commandBytes = Arrays.copyOfRange(data, pos, pos + commandSize);
            pos += commandSize;
            command = new String(commandBytes, StandardCharsets.UTF_8);

            byte[] statusSizeBytes = Arrays.copyOfRange(data, pos, pos + 4);
            int statusLen = Ints.fromByteArray(statusSizeBytes);
            pos += 4;
            byte[] statusBytes = Arrays.copyOfRange(data, pos, pos + statusLen);
            pos += statusLen;
            status = new String(statusBytes, StandardCharsets.UTF_8);

        } else {
            command = "-";
            status = "-";
        }

        return new ShibaVerseDAPP(command, status);
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
    private void catchComets(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {
        // рождение комет
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
            creator.changeBalance(dcSet, asOrphan, false, assetKey,
                    BigDecimal.ONE, false, false, false);
            stock.changeBalance(dcSet, !asOrphan, false, assetKey,
                    BigDecimal.ONE, false, false, false);

        } while (--count > 0);

        if (asOrphan)
            status = "wait";
        else
            status = "done";

    }

    private BigDecimal shopPrice(long incomedAssetKey, long assetToSell) {
        switch ((int) assetToSell) {
            case (int) buster01Key:
                switch ((int) incomedAssetKey) {
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

    private void random(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {
    }

    /**
     * shop for sell items
     *
     * @param dcSet
     * @param block
     * @param commandTX
     * @param asOrphan
     */
    private void shopBuy(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {
        PublicKeyAccount creator = commandTX.getCreator();
        long incomedAssetKey = commandTX.getAssetKey();
        long assetKeyToSell = Long.parseLong(new String(commandTX.getData(), StandardCharsets.UTF_8));

        if (asOrphan) {
            Object[] result = removeState(dcSet);
            if (result.length > 0) {
                BigDecimal amountToSell = (BigDecimal) result[0];
                creator.changeBalance(dcSet, true, false, assetKeyToSell,
                        amountToSell, false, false, false);
                stock.changeBalance(dcSet, false, false, assetKeyToSell,
                        amountToSell, false, false, false);

                BigDecimal leftAmount = (BigDecimal) result[1];
                if (leftAmount.signum() > 0) {
                    creator.changeBalance(dcSet, true, false, incomedAssetKey,
                            leftAmount, false, false, false);
                    stock.changeBalance(dcSet, false, false, incomedAssetKey,
                            leftAmount, false, false, false);
                }
            }
        } else {
            AssetCls incomedAsset = commandTX.getAsset();
            AssetCls assetToSell = dcSet.getItemAssetMap().get(assetKeyToSell);
            BigDecimal leftAmount = commandTX.getAmount();

            BigDecimal sellPrice = shopPrice(incomedAssetKey, assetKeyToSell);
            BigDecimal amountToSell = leftAmount.multiply(sellPrice).setScale(assetToSell.getScale(), BigDecimal.ROUND_HALF_DOWN);
            if (amountToSell.signum() > 0 && !incomedAsset.isUnlimited(stock, false)) {
                Tuple2<BigDecimal, BigDecimal> stockBal = stock.getBalance(dcSet, assetKeyToSell, Account.BALANCE_POS_OWN);
                if (amountToSell.compareTo(stockBal.b) > 0) {
                    // if not enought amount
                    amountToSell = stockBal.b;
                }
            }

            if (amountToSell.signum() > 0) {
                // TRANSFER ASSET
                creator.changeBalance(dcSet, false, false, assetKeyToSell,
                        amountToSell, false, false, false);
                stock.changeBalance(dcSet, true, false, assetKeyToSell,
                        amountToSell, false, false, false);
                // RETURN change
                leftAmount = leftAmount.subtract(amountToSell.divide(sellPrice, incomedAsset.getScale(), BigDecimal.ROUND_DOWN));
            }

            if (leftAmount.signum() > 0) {
                creator.changeBalance(dcSet, false, false, incomedAssetKey,
                        leftAmount, false, false, false);
                stock.changeBalance(dcSet, true, false, incomedAssetKey,
                        leftAmount, false, false, false);
            }

            // store results for orphan
            putState(dcSet, new Object[]{amountToSell, leftAmount});


            status = "done";
        }

    }

    private void stakeAction(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {
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

    private void farmAction(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {
        if (commandTX.isBackward()) {
            // WITHDRAW
            farmChargeAction(dcSet, block, commandTX, asOrphan);
        } else {
            // DEPOSITE

        }
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

    private void adminWithdraw(DCSet dcSet, RSend commandTX, Account admin, boolean asOrphan) {
        if (asOrphan) {
            // restore results for orphan
            List<Tuple2<Long, BigDecimal>> results = (List<Tuple2<Long, BigDecimal>>) removeState(dcSet)[0];

            for (Tuple2<Long, BigDecimal> row : results) {
                // RE-TRANSFER ASSET from ADMIN
                admin.changeBalance(dcSet, true, false, row.a,
                        row.b, false, false, false);
                stock.changeBalance(dcSet, false, false, row.a,
                        row.b, false, false, false);

            }

        } else {
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
                    admin.changeBalance(dcSet, false, false, assetKey,
                            itemBals.a.b, false, false, false);
                    stock.changeBalance(dcSet, true, false, assetKey,
                            itemBals.a.b, false, false, false);

                    results.add(new Tuple2(assetKey, itemBals.a.b));
                }

                // store results for orphan
                putState(dcSet, new Object[]{results});

            } catch (IOException e) {
            }
        }
    }

    private void init(DCSet dcSet, RSend commandTX, Account admin, boolean asOrphan) {

        /**
         * issue main currency
         */
        if (asOrphan) {
            // need to remove INIT_KEY - for reinit after orphans
            gravitaKey = (Long) dcSet.getSmartContractValues().remove(INIT_KEY);

            // orphan GRAVITA ASSET
            dcSet.getItemAssetMap().decrementDelete(gravitaKey);

        } else {
            AssetVenture gravita = new AssetVenture(null, stock, "GR", null, null,
                    null, AssetCls.AS_INSIDE_ASSETS, 6, 0);
            gravita.setReference(commandTX.getSignature(), commandTX.getDBRef());

            //INSERT INTO DATABASE
            gravitaKey = dcSet.getItemAssetMap().incrementPut(gravita);
            dcSet.getSmartContractValues().put(INIT_KEY, gravitaKey);

        }

        // TRANSFER GRAVITA to ADMIN
        admin.changeBalance(dcSet, asOrphan, false, gravitaKey,
                new BigDecimal("10000"), false, false, false);
        stock.changeBalance(dcSet, !asOrphan, false, gravitaKey,
                new BigDecimal("10000"), false, false, false);

    }

    /**
     * admin commands
     *
     * @param dcSet
     * @param block
     * @param commandTX
     * @param admin
     * @return
     */
    public boolean processAdminCommands(DCSet dcSet, Block block, RSend commandTX, Account admin) {
        if ("init".equals(command)) {
            init(dcSet, commandTX, admin, false);
        } else if (command.startsWith("emite")) {
        } else if (command.startsWith(COMMAND_WITHDRAW)) {
            adminWithdraw(dcSet, commandTX, admin, false);
        }

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
                if (COMMAND_RANDOM.equals(command)
                        // это не проверка вне блока - в ней блока нет
                        && block != null) {
                    // рождение комет
                    dcSet.getTimeTXWaitMap().put(transaction.getDBRef(), block.heightBlock + WAIT_RAND);
                    status = "wait";
                    return false;
                } else if (COMMAND_BUY.equals(command)) {
                    shopBuy(dcSet, block, (RSend) transaction, false);
                } else if (COMMAND_STAKE.equals(command)) {
                    stakeAction(dcSet, block, (RSend) transaction, false);
                } else if (COMMAND_FARM.equals(command) || COMMAND_PICK_UP.equals(command)) {
                    farmAction(dcSet, block, (RSend) transaction, false);
                }
            }
        }

        return false;
    }

    @Override
    public boolean processByTime(DCSet dcSet, Block block, Transaction transaction) {

        if (COMMAND_CATH_COMET.equals(command)) {
            catchComets(dcSet, block, (RSend) transaction, false);
        } else if (COMMAND_RANDOM.equals(command)) {
            random(dcSet, block, (RSend) transaction, false);
        }

        return false;
    }

    public boolean orphanAdminCommands(DCSet dcSet, RSend commandTX, Account admin) {
        if ("init".equals(command)) {
            init(dcSet, commandTX, admin, true);
        } else if (command.startsWith(COMMAND_WITHDRAW)) {
            adminWithdraw(dcSet, commandTX, admin, true);
        }

        return false;
    }

    @Override
    public boolean orphan(DCSet dcSet, Transaction transaction) {

        if (status.startsWith("error")) {
            // not processed
            return true;
        }

        if (isAdminCommand(transaction)) {
            return orphanAdminCommands(dcSet, (RSend) transaction,
                    transaction.getCreator() // need for TEST - not adminAddress
            );
        }

        if (COMMAND_CATH_COMET.equals(command) || COMMAND_RANDOM.equals(command)) {
            // отмена рождения комет
            dcSet.getTimeTXWaitMap().remove(transaction.getDBRef());
            return false;
        } else if (COMMAND_BUY.equals(command)) {
            shopBuy(dcSet, null, (RSend) transaction, true);
        } else if (COMMAND_STAKE.equals(command)) {
            farmAction(dcSet, null, (RSend) transaction, true);
        }

        return false;
    }

    @Override
    public boolean orphanByTime(DCSet dcSet, Block block, Transaction transaction) {
        if (COMMAND_CATH_COMET.equals(command)) {
            catchComets(dcSet, block, (RSend) transaction, true);
        } else if (COMMAND_RANDOM.equals(command)) {
            random(dcSet, block, (RSend) transaction, true);
        }

        return false;
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
