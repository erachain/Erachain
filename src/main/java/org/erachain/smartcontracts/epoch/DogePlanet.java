package org.erachain.smartcontracts.epoch;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetUnique;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.SmartContractValues;
import org.erachain.webserver.WebResource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import java.math.BigDecimal;

public class DogePlanet extends EpochSmartContract {

    static public final int ID = 1000;
    static Controller contr = Controller.getInstance();
    static Crypto crypto = Crypto.getInstance();

    static public final PublicKeyAccount MAKER = new PublicKeyAccount(Base58.encode(Longs.toByteArray(ID)));
    private int count;
    private long keyEnd;

    static final Fun.Tuple2 COUNT_KEY = new Fun.Tuple2(ID, "c");

    public DogePlanet(int count) {
        super(ID);
        this.count = count;
    }

    public DogePlanet(int count, long keyEnd) {
        super(ID);
        this.count = count;
        this.keyEnd = keyEnd;
    }

    public int getCount() {
        return count;
    }

    public long getKeyEnd() {
        return keyEnd;
    }

    @Override
    public Object[][] getItemsKeys() {
        if (keyEnd == 0) {
            // not confirmed yet
            return null;
        }

        Object[][] itemKeys = new Object[count][];

        int i = 0;
        do {
            itemKeys[i] = new Object[]{ItemCls.ASSET_TYPE, keyEnd - i};
        } while (++i < count);

        return itemKeys;

    }

    @Override
    public int length(int forDeal) {
        if (forDeal == Transaction.FOR_DB_RECORD)
            return 16;

        return 8;
    }

    @Override
    public byte[] toBytes(int forDeal) {

        byte[] data = Ints.toByteArray(id);
        data = Bytes.concat(data, Ints.toByteArray(count));

        if (forDeal == Transaction.FOR_DB_RECORD) {
            return Bytes.concat(data, Longs.toByteArray(keyEnd));
        }

        return data;

    }

    public static DogePlanet Parse(byte[] data, int pos, int forDeal) {

        // skip ID
        pos += 4;

        byte[] countBuffer = new byte[4];
        System.arraycopy(data, pos, countBuffer, 0, 4);
        pos += 4;

        if (forDeal == Transaction.FOR_DB_RECORD) {
            // возьмем в базе готовый ключ актива
            byte[] keyBuffer = new byte[8];
            System.arraycopy(data, pos, keyBuffer, 0, 8);
            return new DogePlanet(Ints.fromByteArray(countBuffer), Longs.fromByteArray(keyBuffer));
        }

        return new DogePlanet(Ints.fromByteArray(countBuffer));
    }

    @Override
    public boolean process(DCSet dcSet, Block block, Transaction transaction) {

        AssetUnique planet;
        int i = count;

        //SmartContractState stateMap = dcSet.getSmartContractState(); // not used here

        SmartContractValues valuesMap = dcSet.getSmartContractValues();
        Integer totalIssuedObj = (Integer) valuesMap.get(COUNT_KEY);
        int totalIssued;
        if (totalIssuedObj == null)
            totalIssued = 0;
        else
            totalIssued = totalIssuedObj;

        PublicKeyAccount creator = transaction.getCreator();
        do {

            totalIssued++;

            planet = new AssetUnique(null, maker, "Shiba Planet #" + totalIssued, null, null,
                    null, AssetCls.AS_NON_FUNGIBLE);
            planet.setReference(transaction.getSignature(), transaction.getDBRef());

            //INSERT INTO DATABASE
            keyEnd = dcSet.getItemAssetMap().incrementPut(planet);
            creator.changeBalance(dcSet, false, false, keyEnd,
                    BigDecimal.ONE, false, false, false);

            if (block != null) {
                // add remark for action
                block.addCalculated(creator, keyEnd, BigDecimal.ONE,
                        "Produce: " + planet.getName(), transaction.getDBRef());
            }


        } while (--i > 0);

        valuesMap.put(COUNT_KEY, totalIssued);


        return false;
    }

    @Override
    public boolean processByTime(DCSet dcSet, Block block, Transaction transaction) {
        return false;
    }


    @Override
    public boolean orphan(DCSet dcSet, Transaction transaction) {

        SmartContractValues valuesMap = dcSet.getSmartContractValues();
        Integer totalIssued = (Integer) valuesMap.get(COUNT_KEY);

        int i = 0;
        do {

            transaction.getCreator().changeBalance(dcSet, true, false, keyEnd,
                    BigDecimal.ONE, false, false, false);

            //DELETE FROM DATABASE
            dcSet.getItemAssetMap().decrementDelete(keyEnd - i);
        } while (++i < count);

        valuesMap.put(COUNT_KEY, totalIssued - count);

        return false;
    }

    @Override
    public boolean orphanByTime(DCSet dcSet, Transaction transaction) {
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

        //hash = crypto.digest(hash);
        hash = crypto.digest(Longs.toByteArray(System.currentTimeMillis()));
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

}
