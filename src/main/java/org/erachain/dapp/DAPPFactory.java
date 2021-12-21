package org.erachain.dapp;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.transaction.CreateOrderTransaction;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.dapp.epoch.DogePlanet;
import org.erachain.dapp.epoch.LeafFall;
import org.erachain.dapp.epoch.shibaverse.ShibaVerseDAPP;
import org.erachain.utils.FileUtils;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public abstract class DAPPFactory {

    static protected Controller contr = Controller.getInstance();
    static protected Crypto crypto = Crypto.getInstance();
    static final HashMap<Account, Integer> skocks = new HashMap();
    public static JSONObject settingsJSON;

    static {
        try {
            settingsJSON = FileUtils.readCommentedJSONObject("settings_servers.json");
        } catch (IOException e) {
            settingsJSON = new JSONObject();
        }

        ShibaVerseDAPP.setDAPPFactory(skocks);

    }


    /**
     * Делает смотр-контракт протокольный (на эпоху).
     *
     * @param transaction
     * @return
     */
    static public DAPP make(Transaction transaction) {

        /////////// EVENTS
        if (BlockChain.TEST_MODE
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

        if (transaction.getType() != Transaction.SEND_ASSET_TRANSACTION)
            return null;

        RSend txSend = (RSend) transaction;
        if (!txSend.getRecipient().isDAppOwned())
            return null;

        ///// OLD VERSION
        if (txSend.balancePosition() == TransactionAmount.ACTION_SPEND && txSend.hasAmount()
        ) {
            if (txSend.hasPacket()) {

            } else if (txSend.getAmount().signum() < 0) {
                return new DogePlanet(Math.abs(transaction.getAmount().intValue()));
            }
        }
        //////

        /////// NEW VERSION
        String dataStr = txSend.isText() && !txSend.isEncrypted() ? new String(txSend.getData(), StandardCharsets.UTF_8).toLowerCase() : null;

        ///////////////////// CALL DAPPS HERE
        Integer dappID = skocks.get(txSend.getRecipient());
        if (dappID == null)
            return null;

        switch (dappID) {
            case ShibaVerseDAPP.ID:
                return ShibaVerseDAPP.make(txSend, dataStr);
        }

        return null;

    }

    static public String getName(Account stock) {
        Integer dappID = skocks.get(stock);
        if (dappID == null)
            return null;

        switch (dappID) {
            case LeafFall.ID:
                return LeafFall.NAME;
            case DogePlanet.ID:
                return DogePlanet.NAME;
            case ShibaVerseDAPP.ID:
                return ShibaVerseDAPP.NAME;
        }

        return null;
    }
}
