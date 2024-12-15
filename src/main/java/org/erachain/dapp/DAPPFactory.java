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
import org.erachain.dapp.epoch.*;
import org.erachain.dapp.epoch.memoCards.MemoCardsDAPP;
import org.erachain.dapp.epoch.shibaverse.ShibaVerseDAPP;
import org.erachain.utils.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.TreeMap;

public abstract class DAPPFactory {

    static protected Controller contr = Controller.getInstance();
    static protected Crypto crypto = Crypto.getInstance();

    public static final TreeMap<Integer, DAPP> dAppsById = new TreeMap();
    public static final TreeMap<Integer, DAPP> dAppsByPopularity = new TreeMap();

    static final HashMap<Account, Integer> stocks = new HashMap();
    public static JSONObject settingsJSON;

    static {
        try {
            settingsJSON = FileUtils.readCommentedJSONObject("settings_servers.json");
        } catch (IOException e) {
            settingsJSON = new JSONObject();
        }

        dAppsById.put(LeafFall.ID, LeafFall.initInfo());
        dAppsByPopularity.put(500, dAppsById.get(LeafFall.ID));

        dAppsById.put(OddEvenDAPP.ID, OddEvenDAPP.initInfo(stocks));
        dAppsByPopularity.put(2000, dAppsById.get(OddEvenDAPP.ID));

        if (!BlockChain.MAIN_MODE) {
            dAppsById.put(MoneyStaking.ID, MoneyStaking.initInfo(stocks));
            dAppsByPopularity.put(1000, dAppsById.get(MoneyStaking.ID));

            // Пример
            dAppsById.put(Refi.ID, Refi.initInfo(stocks));
            dAppsByPopularity.put(1000, dAppsById.get(Refi.ID));

            ShibaVerseDAPP.setDAPPFactory(stocks);
            MemoCardsDAPP.setDAPPFactory(stocks);

        }

    }

    static JSONParser jsonParser = new JSONParser();

    /**
     * Делает смотр-контракт протокольный (на эпоху).
     *
     * @param transaction
     * @return
     */
    static public DAPP make(Transaction transaction) {

        /////////// Make DAPP by any EVENTS
        if (!LeafFall.DISABLED
                && transaction.getType() == Transaction.CREATE_ORDER_TRANSACTION) {
            CreateOrderTransaction createOrder = (CreateOrderTransaction) transaction;
            if (createOrder.getHaveKey() == AssetCls.ERA_KEY
                    && createOrder.getAmountHave().compareTo(new BigDecimal(100)) >= 0 //  && createOrder.getWantKey() == AssetCls.USD_KEY
                    || createOrder.getWantKey() == AssetCls.ERA_KEY
                    && createOrder.getAmountWant().compareTo(new BigDecimal(100)) >= 0 // && createOrder.getHaveKey() == AssetCls.USD_KEY
            ) {
                Order order = createOrder.getDCSet().getCompletedOrderMap().get(createOrder.getOrderId());
                if (false && order != null)
                    return new LeafFall();
            }
        }

        RSend txSend = (RSend) transaction;

        if (transaction.getAssetKey() > 0) {
            AssetCls asset = transaction.getAsset();
            if (asset.isUseDAPP()) {
                JSONObject jsonObject;
                String jsonStr = asset.getDescription();
                if (jsonStr == null)
                    return new ErrorDAPP("JSON is empty");

                try {
                    jsonObject = (JSONObject) jsonParser.parse(jsonStr);
                } catch (ParseException e) {
                    return new ErrorDAPP("JSON parse error: " + e.getMessage());
                }

                if (jsonObject == null) {
                    return new ErrorDAPP("JSON parse error");
                }

                Integer dappID;
                try {
                    dappID = (Integer) jsonObject.get("id");
                } catch (Exception e) {
                    return new ErrorDAPP("JSON parse `id` error: " + e.getMessage());
                }

                DAPP dappInfo = dAppsById.get(dappID);
                if (dappInfo == null)
                    return new ErrorDAPP("DAPP `id` not found");
                if (dappInfo.isDisabled())
                    return new ErrorDAPP("DAPP is disabled");
                if (!(dappInfo instanceof EpochDAPPjson))
                    return new ErrorDAPP("DAPP not EpochDAPPjson class");

                return ((EpochDAPPjson) dappInfo).of(jsonStr, jsonObject);
            } else {

                if (BlockChain.TEST_MODE || !Refi.DISABLED) {
                    Refi dapp = Refi.tryMakeJob(txSend);
                    if (dapp != null)
                        return dapp;
                }
            }

        }


        if (transaction.getType() != Transaction.SEND_ASSET_TRANSACTION) {
            return null;
        }

        if (!txSend.getRecipient().isDAppOwned()) {
            ///// OLD VERSION
            if (BlockChain.TEST_MODE &&
                    txSend.balancePosition() == TransactionAmount.ACTION_SPEND && txSend.hasAmount()
            ) {
                if (txSend.hasPacket()) {

                } else if (txSend.getAmount().signum() < 0 && !DogePlanet.DISABLED) {
                    return new DogePlanet(Math.abs(transaction.getAmount().intValue()));
                }
            }
            //////

            return null;
        }

        /////// NEW VERSION

        ///////////////////// MAKE DAPPS HERE
        // TRY BY RECIPIENT
        Integer dappID = stocks.get(txSend.getRecipient());
        if (dappID == null) {
            // TODO сюда не дойдет из-за проверки isDAppOwned
            // TRY BY ASSET
            if (txSend.hasAmount()) {
                dappID = stocks.get(txSend.getAsset().getMaker());
            }

            if (dappID == null)
                return null;
        }

        DAPP dappInfo = dAppsById.get(dappID);
        if (dappInfo == null || dappInfo.isDisabled())
            return null;

        String dataStr = txSend.isText() && !txSend.isEncrypted() ? new String(txSend.getData(), StandardCharsets.UTF_8) : null;

        switch (dappID) {
            case OddEvenDAPP.ID:
                return OddEvenDAPP.make(txSend.getTitle());
            case ShibaVerseDAPP.ID:
                return ShibaVerseDAPP.make(txSend, dataStr);
            case MemoCardsDAPP.ID:
                return MemoCardsDAPP.make(txSend, dataStr);
        }

        return null;

    }

    static public String getName(Account stock) {
        Integer dappID = stocks.get(stock);
        if (dappID == null)
            return null;

        switch (dappID) {
            case MoneyStaking.ID:
                return MoneyStaking.NAME;
            case OddEvenDAPP.ID:
                return OddEvenDAPP.NAME;
            case LeafFall.ID:
                return LeafFall.NAME;
            case DogePlanet.ID:
                return DogePlanet.NAME;
            case ShibaVerseDAPP.ID:
                return ShibaVerseDAPP.NAME;
            case MemoCardsDAPP.ID:
                return MemoCardsDAPP.NAME;
            case Refi.ID:
                return Refi.NAME;
        }

        return null;
    }
}
