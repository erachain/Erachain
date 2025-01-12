package org.erachain.dapp;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.transaction.*;
import org.erachain.dapp.epoch.*;
import org.erachain.dapp.epoch.memoCards.MemoCardsDApp;
import org.erachain.dapp.epoch.shibaverse.ShibaVerseDApp;
import org.erachain.utils.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.TreeMap;

import static org.erachain.core.transaction.TransactionAmount.PACKET_ROW_ASSET_NO;

public abstract class DAppFactory {

    static protected Controller contr = Controller.getInstance();
    static protected Crypto crypto = Crypto.getInstance();

    public static final HashMap<Integer, DApp> DAPP_BY_ID = new HashMap();
    public static final HashMap<Account, DApp> STOCKS = new HashMap();
    public static final TreeMap<Integer, DApp> DAPP_BY_POPULARITY = new TreeMap();

    public static JSONObject settingsJSON;

    static {
        try {
            settingsJSON = FileUtils.readCommentedJSONObject("settings_servers.json");
        } catch (IOException e) {
            settingsJSON = new JSONObject();
        }

        LeafFall.setDAppFactory();
        DAPP_BY_POPULARITY.put(500, DAPP_BY_ID.get(LeafFall.ID));

        OddEvenDApp.setDAppFactory();
        DAPP_BY_POPULARITY.put(2000, DAPP_BY_ID.get(OddEvenDApp.ID));

        if (!BlockChain.MAIN_MODE) {
            MoneyStaking.setDAppFactory();
            DAPP_BY_POPULARITY.put(100, DAPP_BY_ID.get(MoneyStaking.ID));

            // Пример
            if (false) {
                // пока не будем показывать
                MoneyStakingReferal.setDAppFactory();
                DAPP_BY_POPULARITY.put(1000, DAPP_BY_ID.get(MoneyStakingReferal.ID));
            }

            ShibaVerseDApp.setDAppFactory();
            MemoCardsDApp.setDAppFactory();
            DogePlanet.setDAppFactory();

        }

    }

    public static JSONParser JSON_PARSER = new JSONParser();

    /**
     * Делает смотр-контракт протокольный (на эпоху).
     *
     * @param commandTx
     * @param block
     * @return
     */
    static public DApp make(Transaction commandTx, Block block) {
        if (block == null)
            // Для неподтвержденных не создаем и не исполняем, так как нет блока даже
            return null;

        /////////// Make DApp by any EVENTS
        if (!LeafFall.DISABLED
                && commandTx.getType() == Transaction.CREATE_ORDER_TRANSACTION) {
            CreateOrderTransaction createOrder = (CreateOrderTransaction) commandTx;
            if (createOrder.getHaveKey() == AssetCls.ERA_KEY
                    && createOrder.getAmountHave().compareTo(new BigDecimal(100)) >= 0 //  && createOrder.getWantKey() == AssetCls.USD_KEY
                    || createOrder.getWantKey() == AssetCls.ERA_KEY
                    && createOrder.getAmountWant().compareTo(new BigDecimal(100)) >= 0 // && createOrder.getHaveKey() == AssetCls.USD_KEY
            ) {
                Order orderCompleted = createOrder.getDCSet().getCompletedOrderMap().get(createOrder.getOrderId());
                if (orderCompleted != null)
                    // Если полностью исполнен
                    return new LeafFall(commandTx, block);
                else {
                    Order orderExist = createOrder.getDCSet().getOrderMap().get(createOrder.getOrderId());
                    if (orderExist != null && orderExist.getStatus() == Order.FULFILLED)
                        // Если кого-то покусал
                        return new LeafFall(commandTx, block);
                }
            }
        }

        // Выявления ДАПП - по активу - для любого вида транзакции
        if (commandTx instanceof IssueItemRecord) {
            // Пропускаем само создание
            ;
        } else if (commandTx instanceof CreateOrderTransaction) {
            // TODO надо сделать списки ДАПП в одной транзакции - так как там могут быть разные дапп-Активы
            CreateOrderTransaction txCreateOrder = (CreateOrderTransaction) commandTx;
            if (txCreateOrder.getHaveAsset().isUseDApp())
                return EpochDAppItemJson.of(txCreateOrder.getHaveAsset(), commandTx, block);
            else if (txCreateOrder.getWantAsset().isUseDApp())
                return EpochDAppItemJson.of(txCreateOrder.getWantAsset(), commandTx, block);

        } else if (commandTx instanceof TransactionAmount) {
            // TODO надо сперва сделать списки ДАПП в одной транзакции - так как там могут быть разные дапп-Активы
            TransactionAmount txAmount = (TransactionAmount) commandTx;
            if (txAmount.hasPacket()) {
                AssetCls asset;
                for (Object[] row : txAmount.getPacket()) {
                    asset = (AssetCls) row[PACKET_ROW_ASSET_NO];
                    if (asset.isUseDApp()) {
                        return EpochDAppItemJson.of(asset, commandTx, block);
                    }
                }

            } else if (txAmount.hasAmount()) {
                AssetCls asset = txAmount.getAsset();
                if (asset != null && asset.isUseDApp()) {
                    return EpochDAppItemJson.of(asset, commandTx, block);
                }
            }

        } else if (commandTx.getAssetKey() != 0) {
            AssetCls asset = commandTx.getAsset();
            if (asset != null && asset.isUseDApp()) {
                return EpochDAppItemJson.of(asset, commandTx, block);
            }
        }

        // далее только для RSend
        if (commandTx.getType() != Transaction.SEND_ASSET_TRANSACTION) {
            return null;
        }

        RSend txSend = (RSend) commandTx;

        if (!txSend.getRecipient().isDAppOwned()) {
            ///// OLD VERSION

            /// Decentralized Game "DogePlanet" for FOIL project
            if (BlockChain.TEST_MODE &&
                    txSend.balancePosition() == TransactionAmount.ACTION_SPEND && txSend.hasAmount()
            ) {
                if (txSend.hasPacket()) {

                } else if (txSend.getAmount().signum() < 0 && !DogePlanet.DISABLED) {
                    return new DogePlanet(Math.abs(commandTx.getAmount().intValue()), commandTx, block);
                }
            }

            //////
            return null;
        }

        ///////////////////// MAKE DAPPS HERE
        /////// NEW VERSION - нова версия выявления контрактов в транзакциях
        // TRY BY RECIPIENT - Если это платеж на счет Контракта

        DApp dAppInfo = STOCKS.get(txSend.getRecipient());
        if (dAppInfo == null) {
            // Для контрактов, которые работают с обычными активами,
            // а не только с теми которые созданы конкретно Для них (проверка asset.isUseDApp() выше была)
            // TRY BY ASSET
            if (txSend.hasAmount()) {
                dAppInfo = STOCKS.get(txSend.getAsset().getMaker());
            }
        }

        if (dAppInfo == null || dAppInfo.isDisabled())
            return null;

        return dAppInfo.of(commandTx, block);

    }

    static public String getName(Account stock) {
        DApp dAppInfo = STOCKS.get(stock);
        return dAppInfo == null ? null : dAppInfo.getName();
    }
}
