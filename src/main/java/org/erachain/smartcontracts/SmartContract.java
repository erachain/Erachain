package org.erachain.smartcontracts;

import com.google.common.primitives.Ints;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.CreateOrderTransaction;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.datachain.DCSet;
import org.erachain.smartcontracts.epoch.DogePlanet;
import org.erachain.smartcontracts.epoch.LeafFall;

public abstract class SmartContract {

    protected final int id;
    protected final PublicKeyAccount maker;

    protected SmartContract(int id, PublicKeyAccount maker) {
        this.id = id;
        this.maker = maker;
    }

    public int getID() {
        return this.id;
    }

    public PublicKeyAccount getMaker() {
        return this.maker;
    }

    public Object[][] getItemsKeys() {
        return null;
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
        }

        throw new Exception("wrong smart-contract id:" + id);
    }

    public String isValid(Transaction transaction) {
        return null;
    }

    abstract public boolean process(DCSet dcSet, Block block, Transaction transaction);

    abstract public boolean orphan(DCSet dcSet, Transaction transaction);

    /**
     * Делает смотр-контракт протокольный (на эпоху).
     *
     * @param transaction
     * @return
     */
    static public SmartContract make(Transaction transaction) {

        if (BlockChain.TEST_MODE && transaction.getBlockHeight() > 115744
                && transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
            RSend txSend = (RSend) transaction;
            if (txSend.balancePosition() == TransactionAmount.ACTION_SPEND
                    && txSend.hasAmount() && txSend.getAmount().signum() < 0
                // && txSend.getAbsKey() == 10234L
            ) {
                return new DogePlanet(Math.abs(transaction.getAmount().intValue()));
            }

        } else if (false && BlockChain.TEST_MODE && transaction.getType() == Transaction.CREATE_ORDER_TRANSACTION) {
            CreateOrderTransaction createOrder = (CreateOrderTransaction) transaction;
            if (createOrder.getHaveKey() == AssetCls.ERA_KEY && createOrder.getWantKey() == AssetCls.USD_KEY
                    || createOrder.getHaveKey() == AssetCls.USD_KEY && createOrder.getWantKey() == AssetCls.ERA_KEY)
                return new LeafFall(1);

        }

        return null;

    }

}
