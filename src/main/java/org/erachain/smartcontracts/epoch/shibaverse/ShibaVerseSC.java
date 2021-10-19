package org.erachain.smartcontracts.epoch.shibaverse;

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
import org.erachain.datachain.DCSet;
import org.erachain.datachain.SmartContractValues;
import org.erachain.lang.Lang;
import org.erachain.smartcontracts.epoch.EpochSmartContract;
import org.erachain.webserver.WebResource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Arrays;

public class ShibaVerseSC extends EpochSmartContract {

    int WAIT_RAND = 3;

    static public final int ID = 1001;

    // 7G6sJRb7vf8ABEr3ENvV1fo1hwt197r35e
    final public static PublicKeyAccount MAKER = new PublicKeyAccount(crypto.digest(Longs.toByteArray(ID)));

    /**
     * admin account
     */
    final static public Account adminAddress = new Account("7C6cEeHw739uQm8PhdnS9yENdLhT8LUERP");

    final static public String COMMAND_CATH_COMET = "catch comets";
    /**
     * GRAVUTA KEY
     */
    static final private Tuple2 INIT_KEY = new Tuple2(ID, "i");

    private String command;
    private String status;
    private Long gravitaKey;

    public ShibaVerseSC(String command, String status) {
        super(ID, MAKER);

        this.command = command;
        this.status = status;
    }

    public String getHTML(JSONObject langObj) {
        String out = super.getHTML(langObj) + "<br>";
        return out + Lang.T("Command", langObj) + ":" + (command == null ? "" : command) + "<br>"
                + Lang.T("Status", langObj) + ":" + (status == null ? "" : status);
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
            if (transaction instanceof RSend) {
                RSend rsend = (RSend) transaction;
                if (rsend.getAssetKey() == gravitaKey
                        && rsend.hasAmount() && rsend.getAmount().signum() > 0
                        && !rsend.isBackward() && rsend.balancePosition() == Account.BALANCE_POS_OWN) {
                    return true;
                }
            }
            status = "error: " + COMMAND_CATH_COMET + " - wrong data";
            return false;
        }

        status = "error: unknown command";
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

    public static ShibaVerseSC Parse(byte[] data, int pos, int forDeal) {

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

        return new ShibaVerseSC(command, status);
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

                    comet = new AssetVenture(null, maker, name, null, null,
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
            maker.changeBalance(dcSet, !asOrphan, false, assetKey,
                    BigDecimal.ONE, false, false, false);

        } while (--count > 0);
    }


    //////// PROCESSES

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
            AssetVenture gravita = new AssetVenture(null, maker, "GR", null, null,
                    null, AssetCls.AS_INSIDE_ASSETS, 6, 0);
            gravita.setReference(commandTX.getSignature(), commandTX.getDBRef());

            //INSERT INTO DATABASE
            gravitaKey = dcSet.getItemAssetMap().incrementPut(gravita);
            dcSet.getSmartContractValues().put(INIT_KEY, gravitaKey);
        }

        // TRANSFER GRAVITA to ADMIN
        admin.changeBalance(dcSet, asOrphan, false, gravitaKey,
                new BigDecimal("10000"), false, false, false);
        maker.changeBalance(dcSet, !asOrphan, false, gravitaKey,
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
                if (COMMAND_CATH_COMET.equals(command)
                        // это не проверка вне блока - в ней блока нет
                        && block != null) {
                    // рождение комет
                    dcSet.getTimeTXWaitMap().put(transaction.getDBRef(), block.heightBlock + WAIT_RAND);
                    return false;
                }
            }
        }

        return false;
    }

    @Override
    public boolean processByTime(DCSet dcSet, Block block, Transaction transaction) {

        if (COMMAND_CATH_COMET.equals(command)) {
            catchComets(dcSet, block, (RSend) transaction, false);
        }

        return false;
    }

    public boolean orphanAdminCommands(DCSet dcSet, RSend commandTX, Account admin) {
        if ("init".equals(command)) {
            init(dcSet, commandTX, admin, true);
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

        if (COMMAND_CATH_COMET.equals(command)) {
            // отмена рождения комет
            dcSet.getTimeTXWaitMap().remove(transaction.getDBRef());
            return false;
        }

        return false;
    }

    @Override
    public boolean orphanByTime(DCSet dcSet, Block block, Transaction transaction) {
        if (COMMAND_CATH_COMET.equals(command)) {
            catchComets(dcSet, block, (RSend) transaction, true);
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

}
