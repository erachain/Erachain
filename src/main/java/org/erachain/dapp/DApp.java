package org.erachain.dapp;

import com.google.common.primitives.Ints;
import lombok.Getter;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
import org.erachain.dapp.epoch.*;
import org.erachain.dapp.epoch.memoCards.MemoCardsDApp;
import org.erachain.dapp.epoch.shibaverse.ShibaVerseDApp;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import java.math.BigDecimal;

public abstract class DApp {

    static protected Controller contr = Controller.getInstance();
    static protected Crypto crypto = Crypto.getInstance();

    protected final int id;

    protected DCSet dcSet;
    @Getter
    protected Transaction commandTx;
    protected Block block;
    protected int height;
    protected Long blockTimestamp;

    protected DApp(int id) {
        this.id = id;
    }

    protected DApp(int id, Transaction commandTx, Block block) {
        this.id = id;
        set(commandTx, block);
    }

    public DApp set(Transaction commandTx, Block block) {
        this.dcSet = commandTx.getDCSet();
        this.commandTx = commandTx;
        this.height = commandTx.getBlockHeight();
        this.blockTimestamp = Controller.getInstance().getBlockChain().getTimestamp(height);
        this.block = block;
        return this;
    }

    public abstract DApp of(Transaction commandTx, Block block);

    public int getID() {
        return this.id;
    }

    public abstract String getName();

    public String getHTML(JSONObject langObj) {
        return "ID: <b>" + id + "</b><br>";
    }

    /**
     * Информация по смарт-контракту для сканера
     *
     * @param langObj
     * @return
     */
    public JSONObject getInfo(JSONObject langObj) {
        JSONObject out = new JSONObject();
        String key = String.format("DApp_%d_", getID());
        out.put("ID", getID());
        out.put("type", "DApp");
        out.put("name", Lang.T(key + "NAME", langObj));
        out.put("short", Lang.T(key + "SHORT", langObj));
        if (isDisabled(height))
            out.put("disabled", Lang.T("Disabled in this Chain", langObj));

        return out;
    }

    public JSONObject getInfo(JSONObject langObj, int height) {
        this.height = height;
        return getInfo(langObj);
    }

    public Object[][] getItemsKeys() {
        return new Object[0][0];
    }

    /**
     * Можно использовать даже если в нем еще не задана породившая его транзакция
     * @param height
     * @return
     */
    public boolean isDisabled(int height) {
        return true;
    }

    /**
     * Эпохальный, запускается самим протоколом. Поэтому он не передается в сеть
     * Но для базы данных генерит данные, которые нужно читать и писать
     *
     * @return
     */
    public boolean isEpoch() {
        return false;
    }

    /**
     * Это конкретный для одной транзакции - его надо и парсить и десериализовать и подписывать вместе с транзакцией
     *
     * @return
     */
    public boolean isTxOwned() {
        return false;
    }

    /**
     * Переводит со счета на счет средства и для отображения в Сканере создает вычисляемые транзакции
     * @param dcSet
     * @param block
     * @param commandTx
     * @param from
     * @param to
     * @param amount
     * @param assetKey
     * @param asOrphan
     * @param memoFrom  если задано, создает Вычисленную транзакцию для показа для адреса From
     * @param memoTo    если задано, создает Вычисленную транзакцию для показа для адреса To
     */
    public static void transfer(DCSet dcSet, Block block, Transaction commandTx,
                                Account from, Account to, BigDecimal amount, long assetKey, boolean asOrphan,
                                String memoFrom, String memoTo) {
        // TRANSFER ASSET
        from.changeBalance(dcSet, !asOrphan, false, assetKey,
                amount, false, false, false);
        to.changeBalance(dcSet, asOrphan, false, assetKey,
                amount, false, false, false);

        if (block != null) {
            if (memoFrom != null)
                block.addCalculated(from, assetKey, amount, memoFrom, commandTx.getDBRef());
            if (memoTo != null)
                block.addCalculated(to, assetKey, amount, memoTo, commandTx.getDBRef());
        }

    }

    /**
     * save current state values
     *
     * @param values
     */
    public void putState(Long dbRef, Object[] values) {
        dcSet.getSmartContractState().put(new Fun.Tuple2<>(id, dbRef), values);
    }

    /**
     * Remove state values
     *
     * @return
     */
    public Object[] removeState(Long seqNo) {
        return dcSet.getSmartContractState().remove(new Fun.Tuple2<>(id, seqNo));
    }

    /**
     * Peek state values
     *
     * @return
     */
    public Object[] peekState(Long seqNo) {
        return dcSet.getSmartContractState().get(new Fun.Tuple2<>(id, seqNo));
    }

    public Object valueGet(String key) {
        return dcSet.getSmartContractValues().get(new Fun.Tuple2(id, key));
    }

    public Object valueGetOrNew(String key, Object newValue) {
        Object value = dcSet.getSmartContractValues().get(new Fun.Tuple2(id, key));
        return value == null ? newValue : value;
    }

    public boolean valueSet(String key, Object value) {
        if (value == null) {
            return dcSet.getSmartContractValues().remove(new Fun.Tuple2(id, key)) != null;
        } else
            return dcSet.getSmartContractValues().set(new Fun.Tuple2(id, key), value);
    }

    public void valuePut(String key, Object value) {
        if (value == null) {
            dcSet.getSmartContractValues().delete(new Fun.Tuple2(id, key));
        } else
            dcSet.getSmartContractValues().put(new Fun.Tuple2(id, key), value);
    }

    public void valuesDelete(String key) {
        dcSet.getSmartContractValues().delete(new Fun.Tuple2(id, key));
    }

    public Object valuesRemove(String key) {
        return dcSet.getSmartContractValues().remove(new Fun.Tuple2(id, key));
    }

    public abstract int length(int forDeal);

    public abstract byte[] toBytes(int forDeal);

    public static DApp Parses(byte[] data, int position, int forDeal) throws Exception {

        byte[] idBuffer = new byte[4];
        System.arraycopy(data, position, idBuffer, 0, 4);
        int id = Ints.fromByteArray(idBuffer);
        switch (id) {
            case MoneyStaking.ID:
                return MoneyStaking.Parse(data, position, forDeal);
            case ErrorDApp.ID:
                return ErrorDApp.Parse(data, position, forDeal);
            case OddEvenDApp.ID:
                return OddEvenDApp.Parse(data, position, forDeal);
            case LeafFall.ID:
                return LeafFall.Parse(data, position, forDeal);
            case DogePlanet.ID:
                return DogePlanet.Parse(data, position, forDeal);
            case ShibaVerseDApp.ID:
                return ShibaVerseDApp.Parse(data, position, forDeal);
            case MemoCardsDApp.ID:
                return MemoCardsDApp.Parse(data, position, forDeal);
        }

        throw new Exception("wrong smart-contract id:" + id);
    }

    public boolean isValid() {
        return true;
    }

    /**
     * @param dcSet
     * @param block
     * @param asOrphan
     */
    public static void processByBlock(DCSet dcSet, Block block, boolean asOrphan) {
        // ShibaVerseDApp job on block
        if (false)
            ShibaVerseDApp.blockAction(dcSet, block, asOrphan);
    }

    abstract public void process();

    abstract public void orphan();

}
