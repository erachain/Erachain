package org.erachain.dapp;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
import org.erachain.dapp.epoch.DogePlanet;
import org.erachain.dapp.epoch.LeafFall;
import org.erachain.dapp.epoch.memoCards.MemoCardsDAPP;
import org.erachain.dapp.epoch.shibaverse.ShibaVerseDAPP;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import java.math.BigDecimal;

public abstract class DAPP {

    static protected Controller contr = Controller.getInstance();
    static protected Crypto crypto = Crypto.getInstance();

    protected final int id;
    protected final PublicKeyAccount stock;

    protected DAPP(int id, PublicKeyAccount stock) {
        this.id = id;
        assert (stock.isDAppOwned());
        this.stock = stock;
    }

    protected DAPP(int id) {
        this.id = id;
        this.stock = PublicKeyAccount.makeForDApp(crypto.digest(Longs.toByteArray(id)));
    }

    public int getID() {
        return this.id;
    }

    public abstract String getName();

    public PublicKeyAccount getStock() {
        return this.stock;
    }

    public String getHTML(JSONObject langObj) {
        return "ID: <b>" + id + "</b><br>" + Lang.T("Maker", langObj) + ": <b>" + stock.getAddress() + "</b>";
    }

    public Object[][] getItemsKeys() {
        return new Object[0][0];
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
    public void putState(DCSet dcSet, Object[] values) {
        dcSet.getSmartContractState().putState(id, values);
    }

    /**
     * Peek state values
     *
     * @param dcSet
     * @return
     */
    public Object[] peekState(DCSet dcSet) {
        return dcSet.getSmartContractState().peekState(id);
    }

    /**
     * Remove state values
     *
     * @param dcSet
     * @return
     */
    public Object[] removeState(DCSet dcSet) {
        return dcSet.getSmartContractState().removeState(id);
    }

    public boolean valueSet(DCSet dcSet, Object key, Object value) {
        return dcSet.getSmartContractValues().set(new Fun.Tuple2(id, key), value);
    }

    public void valuePut(DCSet dcSet, Object key, Object value) {
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
            case LeafFall.ID:
                return LeafFall.Parse(data, position, forDeal);
            case DogePlanet.ID:
                return DogePlanet.Parse(data, position, forDeal);
            case ShibaVerseDAPP.ID:
                return ShibaVerseDAPP.Parse(data, position, forDeal);
            case MemoCardsDAPP.ID:
                return MemoCardsDAPP.Parse(data, position, forDeal);
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

    abstract public boolean processByTime(DCSet dcSet, Block block, Transaction transaction);

    abstract public void orphan(DCSet dcSet, Transaction transaction);

    abstract public void orphanByTime(DCSet dcSet, Block block, Transaction transaction);

}
