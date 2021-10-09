package org.erachain.core.item.assets;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.smartcontracts.epoch.DogePlanet;
import org.erachain.webserver.WebResource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AssetUnique extends AssetCls {

    private static final int TYPE_ID = UNIQUE;

    public AssetUnique(byte[] typeBytes, byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description, int assetType) {
        super(typeBytes, appData, maker, name, icon, image, description, assetType);
    }

    public AssetUnique(int props, byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description, int assetType) {
        this(new byte[]{(byte) TYPE_ID, (byte) props}, appData, maker, name, icon, image, description, assetType);
    }

    public AssetUnique(byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description, int assetType) {
        this(new byte[]{(byte) TYPE_ID, (byte) 0}, appData, maker, name, icon, image, description, assetType);
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
                        new String[]{"1050863", WebResource.TYPE_IMAGE.toString()},
                        new String[]{"1050862", WebResource.TYPE_IMAGE.toString()},
                },
                new String[][]{
                        new String[]{"1050860", WebResource.TYPE_IMAGE.toString()},
                },
                new String[][]{
                        new String[]{"1050866", WebResource.TYPE_IMAGE.toString()},
                },
                new String[][]{
                        new String[]{"1050859", WebResource.TYPE_IMAGE.toString()},
                        new String[]{"1050858", WebResource.TYPE_IMAGE.toString()},
                        new String[]{"1050857", WebResource.TYPE_IMAGE.toString()},
                },
                new String[][]{
                        new String[]{"1050856", WebResource.TYPE_IMAGE.toString()},
                        new String[]{"1050855", WebResource.TYPE_IMAGE.toString()},
                        new String[]{"1050854", WebResource.TYPE_IMAGE.toString()},
                },
                new String[][]{
                        new String[]{"1050866", WebResource.TYPE_IMAGE.toString()},
                        new String[]{"1050860", WebResource.TYPE_IMAGE.toString()},
                },
                new String[][]{
                        new String[]{"1050852", WebResource.TYPE_IMAGE.toString()},
                        new String[]{"1050851", WebResource.TYPE_IMAGE.toString()},
                },
        };
    }

    //GETTERS/SETTERS
    @Override
    public String getImageURL() {
        if (!maker.equals(DogePlanet.MAKER))
            return super.getImageURL();

        JSONArray arrayJson = new JSONArray();

        int height = Transaction.parseHeightDBRef(dbRef);
        Block.BlockHead blockHead = DCSet.getInstance().getBlocksHeadsMap().get(height + 10);
        if (blockHead == null) {
            JSONObject item = new JSONObject();
            item.put("url", "/apiasset/image/1050869");
            item.put("type", WebResource.TYPE_IMAGE);
            arrayJson.add(item);
            return arrayJson.toJSONString();
            //return "<img width=350 style='position:absolute;'src=/smartcontract/epoch/000001/01/000.png>";
        }

        byte[] hash = blockHead.signature;
        byte[] hash2 = Ints.toByteArray((int) key);
        System.arraycopy(hash2, 0, hash, 0, hash2.length);
        //hash = Crypto.getInstance().digest(hash);
        hash = Crypto.getInstance().digest(Longs.toByteArray(System.currentTimeMillis()));
        int slot = 0;
        int slotRare;
        int slotRareLvl;
        String html = "";

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

            String[][] slotArray = imgsStr[slot];
            if (slotArray.length <= slotRareLvl) {
                slotRareLvl = slotArray.length - 1;
            }

            String[] itemArray = slotArray[slotRareLvl];

            JSONObject item = new JSONObject();
            item.put("url", "/apiasset/image/" + itemArray[0]);
            item.put("type", itemArray[1]);
            arrayJson.add(item);

        } while (slot++ < 7);

        return arrayJson.toJSONString();

    }

    @Override
    public MediaType getImageMediaType() {
        if (maker.equals(DogePlanet.MAKER))
            return WebResource.TYPE_ARRAY;

        return super.getImageMediaType();
    }

    @Override
    public String getItemSubType() {
        return "unique";
    }

    @Override
    public long getQuantity() {
        return 1L;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public int getScale() {
        return 0;
    }

    @Override
    public boolean isUnlimited(Account address, boolean notAccounting) {
        return false;
    }

    @Override
    public BigDecimal getReleased(DCSet dcSet) {
        Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> bals = this.getMaker().getBalance(this.getKey());
        return BigDecimal.ONE.subtract(bals.a.b).stripTrailingZeros();
    }

    @Override
    public BigDecimal getReleased() {
        return getReleased(DCSet.getInstance());
    }

    //PARSE
    // includeReference - TRUE only for store in local DB
    public static AssetUnique parse(int forDeal, byte[] data, boolean includeReference) throws Exception {

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        //READ CREATOR
        byte[] makerBytes = Arrays.copyOfRange(data, position, position + MAKER_LENGTH);
        PublicKeyAccount maker = new PublicKeyAccount(makerBytes);
        position += MAKER_LENGTH;

        //READ NAME
        int nameLength = Byte.toUnsignedInt(data[position]);
        position++;

        if (nameLength < 1 || nameLength > MAX_NAME_LENGTH) {
            throw new Exception("Invalid name length: " + nameLength);
        }

        byte[] nameBytes = Arrays.copyOfRange(data, position, position + nameLength);
        String name = new String(nameBytes, StandardCharsets.UTF_8);
        position += nameLength;

        //READ ICON
        byte[] iconLengthBytes = Arrays.copyOfRange(data, position, position + ICON_SIZE_LENGTH);
        int iconLength = Ints.fromBytes((byte) 0, (byte) 0, iconLengthBytes[0], iconLengthBytes[1]);
        position += ICON_SIZE_LENGTH;

        if (iconLength < 0 || iconLength > MAX_ICON_LENGTH) {
            throw new Exception("Invalid icon length" + name + ": " + iconLength);
        }

        byte[] icon = Arrays.copyOfRange(data, position, position + iconLength);
        position += iconLength;

        //READ IMAGE
        byte[] imageLengthBytes = Arrays.copyOfRange(data, position, position + IMAGE_SIZE_LENGTH);
        int imageLength = Ints.fromByteArray(imageLengthBytes);
        position += IMAGE_SIZE_LENGTH;

        // TEST APP DATA
        boolean hasAppData = (imageLength & APP_DATA_MASK) != 0;
        if (hasAppData)
            // RESET LEN
            imageLength &= ~APP_DATA_MASK;

        if (imageLength < 0 || imageLength > MAX_IMAGE_LENGTH) {
            throw new Exception("Invalid image length" + name + ": " + imageLength);
        }

        byte[] image = Arrays.copyOfRange(data, position, position + imageLength);
        position += imageLength;

        byte[] appData;
        if (hasAppData) {
            // READ APP DATA
            int appDataLen = Ints.fromByteArray(Arrays.copyOfRange(data, position, position + APP_DATA_LENGTH));
            position += APP_DATA_LENGTH;

            appData = Arrays.copyOfRange(data, position, position + appDataLen);
            position += appDataLen;

        } else {
            appData = null;
        }

        //READ DESCRIPTION
        byte[] descriptionLengthBytes = Arrays.copyOfRange(data, position, position + DESCRIPTION_SIZE_LENGTH);
        int descriptionLength = Ints.fromByteArray(descriptionLengthBytes);
        position += DESCRIPTION_SIZE_LENGTH;

        if (descriptionLength > BlockChain.MAX_REC_DATA_BYTES) {
            throw new Exception("Invalid description length" + name + ": " + descriptionLength);
        }

        byte[] descriptionBytes = Arrays.copyOfRange(data, position, position + descriptionLength);
        String description = new String(descriptionBytes, StandardCharsets.UTF_8);
        position += descriptionLength;

        byte[] reference = null;
        long dbRef = 0;
        if (includeReference) {
            //READ REFERENCE
            reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
            position += REFERENCE_LENGTH;

            //READ SEQNO
            byte[] dbRefBytes = Arrays.copyOfRange(data, position, position + DBREF_LENGTH);
            dbRef = Longs.fromByteArray(dbRefBytes);
            position += DBREF_LENGTH;
        }

        //READ ASSET TYPE
        int assetType = Byte.toUnsignedInt(Arrays.copyOfRange(data, position, position + ASSET_TYPE_LENGTH)[0]);
        position += ASSET_TYPE_LENGTH;

        //RETURN
        AssetUnique unique = new AssetUnique(typeBytes, appData, maker, name, icon, image, description, assetType);
        if (includeReference) {
            unique.setReference(reference, dbRef);
        }

        return unique;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean includeReference, boolean forMakerSign) {

        byte[] data = super.toBytes(forDeal, includeReference, forMakerSign);

        //WRITE ASSET TYPE
        data = Bytes.concat(data, new byte[]{(byte) this.getAssetType()});

        return data;
    }

    //OTHER

}
