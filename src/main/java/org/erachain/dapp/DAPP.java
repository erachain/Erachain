package org.erachain.dapp;

import com.google.common.primitives.Ints;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
import org.erachain.dapp.epoch.DogePlanet;
import org.erachain.dapp.epoch.LeafFall;
import org.erachain.dapp.epoch.OddEvenDAPP;
import org.erachain.dapp.epoch.Refi;
import org.erachain.dapp.epoch.memoCards.MemoCardsDAPP;
import org.erachain.dapp.epoch.shibaverse.ShibaVerseDAPP;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.Pair;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class DAPP {

    static protected Controller contr = Controller.getInstance();
    static protected Crypto crypto = Crypto.getInstance();

    protected final int id;
    protected PublicKeyAccount stock;
    // заполнить один раз для показа в ИНФО - address = {commands}
    final public List<Pair<PublicKeyAccount, String[]>> accountsInfo = new ArrayList<>();

    protected DAPP(int id) {
        this.id = id;
        //this.stock = PublicKeyAccount.makeForDApp(crypto.digest(Longs.toByteArray(id)));
    }

    protected DAPP(int id, PublicKeyAccount stock) {
        this.id = id;
        assert (stock.isDAppOwned());
        this.stock = stock;
    }

    public int getID() {
        return this.id;
    }

    public abstract String getName();

    public PublicKeyAccount getStock() {
        return this.stock;
    }

    public String getHTML(JSONObject langObj) {
        return "ID: <b>" + id + "</b><br>" + Lang.T("Address", langObj) + ": <b>" + stock.getAddress() + "</b>";
    }

    /**
     * Информация по смарт-контракту для сканера
     *
     * @param langObj
     * @return
     */
    public JSONObject getInfoShort(JSONObject langObj) {
        JSONObject out = new JSONObject();
        String key = String.format("DAPP_%d_", getID());
        out.put("ID", getID());
        out.put("type", "DAPP");
        out.put("name", Lang.T(key + "NAME", langObj));
        out.put("short", Lang.T(key + "SHORT", langObj));
        if (isDisabled())
            out.put("disabled", Lang.T("Disabled", langObj));

        return out;
    }

    public String getCommandInfo(String command, String format) {
        return format;
    }

    public JSONObject getInfo(JSONObject langObj) {
        JSONObject out = getInfoShort(langObj);
        if (isDisabled())
            out.put("disabled", Lang.T("Disabled in this Chain", langObj));

        String key = String.format("DAPP_%d_", getID());
        out.put("desc", Lang.T(key + "DESC", langObj));

        if (!accountsInfo.isEmpty()) {
            JSONObject accs = new JSONObject();
            accountsInfo.forEach(pair -> {
                JSONObject acc = new JSONObject();
                String address = pair.getA().getAddress();
                accs.put(address, acc);
                acc.put("desc", Lang.T(key + address, langObj)); //"Счет для JOB");
                if (pair.getB() != null && pair.getB().length > 0) {
                    JSONObject commands = new JSONObject();
                    Arrays.stream(pair.getB()).forEach(command -> commands.put(command, getCommandInfo(command, Lang.T(key + address + "_" + command, langObj))));
                    acc.put("commands", commands);
                }
            });

            out.put("accs", accs);
        }

        return out;
    }

    public Object[][] getItemsKeys() {
        return new Object[0][0];
    }

    public boolean isDisabled() {
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
     * make public key from Base with Nonce
     *
     * @param base
     * @param nonce
     * @return
     */
    public static PublicKeyAccount noncePubKey(byte[] base, byte nonce) {
        byte[] hash = new byte[base.length];
        System.arraycopy(base, 0, hash, 0, base.length);
        hash[base.length - 1] += nonce;
        return PublicKeyAccount.makeForDApp(hash);
    }

    /**
     * @param dcSet
     * @param block
     * @param commandTX
     * @param from
     * @param to
     * @param amount
     * @param assetKey
     * @param asOrphan
     * @param memoFrom  если задано, создает Вычисленную транзакцию для показа для адреса From
     * @param memoTo    если задано, создает Вычисленную транзакцию для показа для адреса To
     */
    public static void transfer(DCSet dcSet, Block block, Transaction commandTX,
                                Account from, Account to, BigDecimal amount, long assetKey, boolean asOrphan,
                                String memoFrom, String memoTo) {
        // TRANSFER ASSET
        from.changeBalance(dcSet, !asOrphan, false, assetKey,
                amount, false, false, false);
        to.changeBalance(dcSet, asOrphan, false, assetKey,
                amount, false, false, false);

        if (block != null) {
            if (memoFrom != null)
                block.addCalculated(from, assetKey, amount, memoFrom, commandTX.getDBRef());
            if (memoTo != null)
                block.addCalculated(to, assetKey, amount, memoTo, commandTX.getDBRef());
        }

    }

    /**
     * save current state values
     *
     * @param dcSet
     * @param values
     */
    public void putState(DCSet dcSet, Long dbRef, Object[] values) {
        dcSet.getSmartContractState().put(new Fun.Tuple2<>(id, dbRef), values);
    }

    /**
     * Remove state values
     *
     * @param dcSet
     * @return
     */
    public Object[] removeState(DCSet dcSet, Long seqNo) {
        return dcSet.getSmartContractState().remove(new Fun.Tuple2<>(id, seqNo));
    }

    public Object valueGet(DCSet dcSet, String key) {
        return dcSet.getSmartContractValues().get(new Fun.Tuple2(id, key));
    }

    public boolean valueSet(DCSet dcSet, String key, Object value) {
        if (value == null) {
            return dcSet.getSmartContractValues().remove(new Fun.Tuple2(id, key)) != null;
        } else
            return dcSet.getSmartContractValues().set(new Fun.Tuple2(id, key), value);
    }

    public void valuePut(DCSet dcSet, String key, Object value) {
        if (value == null) {
            dcSet.getSmartContractValues().delete(new Fun.Tuple2(id, key));
        } else
            dcSet.getSmartContractValues().put(new Fun.Tuple2(id, key), value);
    }

    public void valuesDelete(DCSet dcSet, String key) {
        dcSet.getSmartContractValues().delete(new Fun.Tuple2(id, key));
    }

    public Object valuesRemove(DCSet dcSet, String key) {
        return dcSet.getSmartContractValues().remove(new Fun.Tuple2(id, key));
    }

    public int length(int forDeal) {
        return 4 + 32;
    }

    public byte[] toBytes(int forDeal) {
        byte[] pubKey = stock.getPublicKey();
        byte[] data = new byte[4 + pubKey.length];
        System.arraycopy(Ints.toByteArray(id), 0, data, 0, 4);
        System.arraycopy(pubKey, 0, data, 4, pubKey.length);

        return data;
    }

    public static DAPP Parses(byte[] data, int position, int forDeal) throws Exception {

        byte[] idBuffer = new byte[4];
        System.arraycopy(data, position, idBuffer, 0, 4);
        int id = Ints.fromByteArray(idBuffer);
        switch (id) {
            case OddEvenDAPP.ID:
                return OddEvenDAPP.Parse(data, position, forDeal);
            case LeafFall.ID:
                return LeafFall.Parse(data, position, forDeal);
            case DogePlanet.ID:
                return DogePlanet.Parse(data, position, forDeal);
            case ShibaVerseDAPP.ID:
                return ShibaVerseDAPP.Parse(data, position, forDeal);
            case MemoCardsDAPP.ID:
                return MemoCardsDAPP.Parse(data, position, forDeal);
            case Refi.ID:
                return Refi.Parse(data, position, forDeal);
        }

        throw new Exception("wrong smart-contract id:" + id);
    }

    public boolean isValid(DCSet dcset, Transaction transaction) {
        return true;
    }

    /**
     * @param dcSet
     * @param block
     * @param asOrphan
     */
    public static void processByBlock(DCSet dcSet, Block block, boolean asOrphan) {
        ShibaVerseDAPP.blockAction(dcSet, block, asOrphan);
    }

    abstract public boolean process(DCSet dcSet, Block block, Transaction transaction);

    abstract public void orphan(DCSet dcSet, Transaction commandTX);

}
