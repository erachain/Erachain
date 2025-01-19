package org.erachain.dapp.epoch;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.dapp.DApp;
import org.erachain.dapp.DAppFactory;
import org.erachain.dapp.DAppTimed;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.nio.charset.StandardCharsets;

public abstract class EpochDAppItemJson extends EpochDAppJson implements DAppTimed {

    protected int itemType;
    protected long itemKey;
    protected ItemCls item;
    protected String itemDescription;
    protected JSONObject itemPars;

    // Для одного объекта с Инфо
    public EpochDAppItemJson(int id, PublicKeyAccount maker) {
        super(id, maker);
    }

    public EpochDAppItemJson(int id, PublicKeyAccount maker, int itemType, long itemKey, String itemDescription, String dataStr, String status) {
        super(id, maker, dataStr, status);
        this.itemType = itemType;
        this.itemKey = itemKey;
        this.itemDescription = itemDescription;
        try {
            itemPars = (JSONObject) DAppFactory.JSON_PARSER.parse(itemDescription);
        } catch (ParseException e) {
            throw new RuntimeException("JSON in Asset Description parse field `id` or `dApp` error: " + e.getMessage());
        }
    }

    public EpochDAppItemJson(int id, PublicKeyAccount maker, ItemCls item, String itemDescription, JSONObject itemPars, Transaction commandTx, Block block) {
        super(id, maker, null, null, commandTx, block);
        this.item = item;
        this.itemKey = item.getKey();
        this.itemType = item.getItemType();
        this.itemDescription = itemDescription;
        this.itemPars = itemPars;
    }

    public abstract DApp of(String itemDescription, JSONObject jsonObject, ItemCls item, Transaction commandTx, Block block);

    @Override
    public DApp of(String dataStr, Transaction commandTx, Block block) {
        throw new RuntimeException("Wrong OF(...)");
    }

    @Override
    public DApp of(Transaction commandTx, Block block) {
        throw new RuntimeException("Wrong OF(...)");
    }

    public static DApp of(ItemCls item, Transaction commandTx, Block block) {
        JSONObject jsonObject;
        String itemDescription = item.getDescription();
        if (itemDescription == null)
            return new ErrorDApp("JSON in Asset Description is empty");

        try {
            jsonObject = (JSONObject) DAppFactory.JSON_PARSER.parse(itemDescription);
        } catch (ParseException e) {
            return new ErrorDApp("JSON in Asset Description parse error: " + e.getMessage());
        }

        if (jsonObject == null) {
            return new ErrorDApp("JSON in Asset Description parse error");
        }

        Integer dAppID;
        try {
            dAppID = ((Long) jsonObject.getOrDefault("dApp", jsonObject.get("id"))).intValue();
        } catch (Exception e) {
            return new ErrorDApp("JSON in Asset Description parse field `id` or `dApp` error: " + e.getMessage());
        }

        if (dAppID == null)
            return new ErrorDApp("DApp field `id` or `dApp` not found in data. Set Asset Description, for example: {\"dApp\":1012,...}");

        DApp dAppInfo = DAppFactory.DAPP_BY_ID.get(dAppID);
        if (dAppInfo == null)
            return new ErrorDApp("DApp not found for ID: " + dAppID);
        if (dAppInfo.isDisabled(commandTx.getBlockHeight()))
            return new ErrorDApp("DApp is disabled");
        if (!(dAppInfo instanceof EpochDAppItemJson))
            return new ErrorDApp("DApp with that ID is not EpochDAppJson class");

        return ((EpochDAppItemJson) dAppInfo).of(itemDescription, jsonObject, item, commandTx, block);

    }

    public String getHTML(JSONObject langObj) {
        String out = super.getHTML(langObj) + "<br>";
        return out
                + Lang.T("ItemData", langObj) + ": <b>" + (itemDescription == null ? "" : itemDescription) + "</b><br>";
    }

    /// PARSE / TOBYTES
    @Override
    public int length(int forDeal) {

        int len = super.length(forDeal) + 2 + 8;
        if (forDeal == Transaction.FOR_DB_RECORD) {
            len += 4;
            if (itemDescription != null)
                len += itemDescription.length();
        }

        return len;
    }

    @Override
    public byte[] toBytes(int forDeal) {

        byte[] bytes = super.toBytes(forDeal);

        bytes = Bytes.concat(bytes, Shorts.toByteArray((short) itemType));
        bytes = Bytes.concat(bytes, Longs.toByteArray(itemKey));

        if (forDeal == Transaction.FOR_DB_RECORD) {
            byte[] itemDescBytes;

            if (itemDescription != null) {
                itemDescBytes = itemDescription.getBytes(StandardCharsets.UTF_8);
            } else {
                itemDescBytes = new byte[0];
            }

            bytes = Bytes.concat(bytes, Ints.toByteArray(itemDescBytes.length));
            bytes = Bytes.concat(bytes, itemDescBytes);

        }

        return bytes;

    }

}
