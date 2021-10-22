package org.erachain.smartcontracts;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.transaction.CreateOrderTransaction;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.smartcontracts.epoch.DogePlanet;
import org.erachain.smartcontracts.epoch.LeafFall;
import org.erachain.smartcontracts.epoch.shibaverse.ShibaVerseSC;
import org.erachain.utils.FileUtils;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;

public abstract class SmartContract {

    static protected Controller contr = Controller.getInstance();
    static protected Crypto crypto = Crypto.getInstance();

    public static JSONObject settingsJSON;

    {
        try {
            settingsJSON = FileUtils.readCommentedJSONObject("settings_servers.json");
        } catch (IOException e) {
            settingsJSON = new JSONObject();
        }
    }

    protected final int id;
    protected final PublicKeyAccount maker;

    protected SmartContract(int id, PublicKeyAccount maker) {
        this.id = id;
        this.maker = maker;
    }

    protected SmartContract(int id) {
        this.id = id;
        this.maker = new PublicKeyAccount(crypto.digest(Longs.toByteArray(id)));
    }

    public int getID() {
        return this.id;
    }

    public PublicKeyAccount getMaker() {
        return this.maker;
    }

    public String getHTML(JSONObject langObj) {
        return "ID: <b>" + id + "</b><br>" + Lang.T("Maker", langObj) + ": <b>" + maker.getAddress() + "</b>";
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
        return new PublicKeyAccount(hash);
    }

    public int length(int forDeal) {
        return 4 + 32;
    }

    public byte[] toBytes(int forDeal) {
        byte[] pubKey = maker.getPublicKey();
        byte[] data = new byte[4 + pubKey.length];
        System.arraycopy(Ints.toByteArray(id), 0, data, 0, 4);
        System.arraycopy(pubKey, 0, data, 4, pubKey.length);

        return data;
    }

    public static SmartContract Parses(byte[] data, int position, int forDeal) throws Exception {

        byte[] idBuffer = new byte[4];
        System.arraycopy(data, position, idBuffer, 0, 4);
        int id = Ints.fromByteArray(idBuffer);
        switch (id) {
            case LeafFall.ID:
                return LeafFall.Parse(data, position, forDeal);
            case DogePlanet.ID:
                return DogePlanet.Parse(data, position, forDeal);
            case ShibaVerseSC.ID:
                return ShibaVerseSC.Parse(data, position, forDeal);
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
        ShibaVerseSC.blockAction(dcSet, block, asOrphan);
    }

    abstract public boolean process(DCSet dcSet, Block block, Transaction transaction);

    abstract public boolean processByTime(DCSet dcSet, Block block, Transaction transaction);

    abstract public boolean orphan(DCSet dcSet, Transaction transaction);

    abstract public boolean orphanByTime(DCSet dcSet, Block block, Transaction transaction);

    /**
     * Делает смотр-контракт протокольный (на эпоху).
     *
     * @param transaction
     * @return
     */
    static public SmartContract make(Transaction transaction) {

        SmartContract contract = ShibaVerseSC.make(transaction);
        if (contract != null)
            return contract;

        if (BlockChain.TEST_MODE
                && transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
            RSend txSend = (RSend) transaction;

            if (txSend.balancePosition() == TransactionAmount.ACTION_SPEND
                    && txSend.hasAmount() && txSend.getAmount().signum() < 0
                // && txSend.getAbsKey() == 10234L
            ) {
                return new DogePlanet(Math.abs(transaction.getAmount().intValue()));
            }


        } else if (BlockChain.TEST_MODE
                && transaction.getType() == Transaction.CREATE_ORDER_TRANSACTION) {
            CreateOrderTransaction createOrder = (CreateOrderTransaction) transaction;
            if (createOrder.getHaveKey() == AssetCls.ERA_KEY
                    && createOrder.getAmountHave().compareTo(new BigDecimal(100)) >= 0 //  && createOrder.getWantKey() == AssetCls.USD_KEY
                    || createOrder.getWantKey() == AssetCls.ERA_KEY
                    && createOrder.getAmountWant().compareTo(new BigDecimal(100)) >= 0 // && createOrder.getHaveKey() == AssetCls.USD_KEY
            ) {
                Order order = createOrder.getDCSet().getCompletedOrderMap().get(createOrder.getOrderId());
                if (order != null)
                    return new LeafFall();
            }
        }

        return null;

    }

}
