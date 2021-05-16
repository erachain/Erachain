package org.erachain.controller;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.erachain.core.BlockChain;
import org.erachain.core.block.Block;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.item.assets.TradePair;
import org.erachain.database.DLSet;
import org.erachain.database.PairMap;
import org.erachain.database.PairMapImpl;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetMap;
import org.erachain.datachain.OrderMapImpl;
import org.erachain.datachain.TradeMapImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.settings.Settings;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PairsController {
    public HashMap<String, TradePair> spotPairs = new HashMap();
    public JSONObject pairsJson = new JSONObject();
    public JSONObject spotPairsJson;

    public JSONObject spotPairsList;
    public List<Fun.Tuple2<Long, Long>> commonPairsList;


    private static final Logger LOGGER = LoggerFactory.getLogger(PairsController.class.getSimpleName());

    PairsController() {
        updateList();
    }

    long updateInit;

    public void init() {

        if (System.currentTimeMillis() - updateInit < 600000) {
            return;
        }
        updateInit = System.currentTimeMillis();

        //OPEN FILE
        File file = new File(Settings.getInstance().getUserPath() + "market.json");

        //CREATE FILE IF IT DOESNT EXIST
        if (BlockChain.MAIN_MODE && file.exists()) {
            String jsonString = "";
            try {
                //READ PEERS FILE
                List<String> lines = Files.readLines(file, Charsets.UTF_8);

                for (String line : lines) {
                    if (line.trim().startsWith("/")) {
                        // пропускаем //
                        continue;
                    }
                    jsonString += line;
                }
            } catch (Exception e) {
            }
            //CREATE JSON OBJECT
            this.spotPairsJson = (JSONObject) JSONValue.parse(jsonString);

        } else {
            JSONArray spot = new JSONArray();

            JSONArray array = new JSONArray();
            array.add(2L);
            array.add(1L);
            array.add(Boolean.TRUE);

            spot.add(array);

            this.spotPairsJson = new JSONObject();
            spotPairsJson.put("spot", spot);
        }

    }

    int cacheTime = 10 * 60 * 1000; // in ms
    long updateList;
    public void updateList() {

        init();
        if (System.currentTimeMillis() - updateList < cacheTime) {
            return;
        }
        updateList = System.currentTimeMillis();

        spotPairsList = new JSONObject();
        commonPairsList = new ArrayList<>();

        ItemAssetMap mapAssets = DCSet.getInstance().getItemAssetMap();
        PairMapImpl mapPairs = Controller.getInstance().dlSet.getPairMap();
        JSONArray spotJson = (JSONArray) spotPairsJson.get("spot");
        for (Object item : spotJson) {
            JSONArray array = (JSONArray) item;
            Long key1 = (Long) array.get(0);
            AssetCls asset1 = mapAssets.get(key1);
            if (asset1 == null) {
                LOGGER.warn("asset [" + key1 + "] not found");
                continue;
            }
            Long key2 = (Long) array.get(1);
            AssetCls asset2 = mapAssets.get(key2);
            if (asset2 == null) {
                LOGGER.warn("asset [" + key2 + "] not found");
                continue;
            }

            String pairJsonKey = asset1.getName() + "_" + asset2.getName();
            spotPairsList.put(pairJsonKey, array);

            TradePair tradePair = reCalcAndUpdate(asset1, asset2, mapPairs, 10);
            spotPairs.put(pairJsonKey, tradePair);
            spotPairsJson.put(pairJsonKey, tradePair.toJson());
            commonPairsList.add(new Fun.Tuple2<>(key1, key2));

        }
    }

    /**
     * Обновить данные
     *
     * @param asset1
     * @param asset2
     * @param currentPair
     */
    public static TradePair reCalc(AssetCls asset1, AssetCls asset2, TradePair currentPair) {
        TradeMapImpl tradesMap = DCSet.getInstance().getTradeMap();
        long key1 = asset1.getKey();
        long key2 = asset2.getKey();

        int heightStart = Controller.getInstance().getMyHeight();
        int heightEnd = heightStart - BlockChain.BLOCKS_PER_DAY(heightStart);

        int count24 = 0;
        BigDecimal minPrice = new BigDecimal(Long.MAX_VALUE);
        BigDecimal maxPrice = new BigDecimal(Long.MIN_VALUE);
        BigDecimal lastPrice = null;
        BigDecimal baseVolume = BigDecimal.ZERO;
        BigDecimal quoteVolume = BigDecimal.ZERO;
        long lastTime = 0;
        BigDecimal price = BigDecimal.ZERO;

        boolean reversed;
        try (IteratorCloseable<Fun.Tuple2<Long, Long>> iterator = (tradesMap.getPairIterator(key1, key2, heightStart, heightEnd))) {
            Trade trade;
            if (iterator.hasNext()) {
                Trade lastTrade = null;
                while (iterator.hasNext()) {
                    trade = tradesMap.get(iterator.next());
                    if (trade == null) {
                        LOGGER.warn("trade for pair [" + key1 + "/" + key2 + "] not found");
                        continue;
                    }

                    if (trade.isCancel())
                        continue;

                    if (currentPair != null && lastTrade == null) {
                        // изменений не было
                        if (trade.getTimestamp() == currentPair.getLastTime()) {
                            currentPair.setUpdateTime(Block.getTimestamp(heightStart));
                            return currentPair;
                        }
                        lastTrade = trade;
                    }

                    count24++;

                    reversed = trade.getHaveKey() == key2;

                    // у сделки обратные Have Want
                    price = reversed ? trade.calcPrice() : trade.calcPriceRevers();
                    if (lastPrice == null) {
                        lastPrice = price;
                        lastTime = trade.getTimestamp();
                    }

                    if (minPrice.compareTo(price) > 0)
                        minPrice = price;
                    if (maxPrice.compareTo(price) < 0)
                        maxPrice = price;

                    baseVolume = baseVolume.add(reversed ? trade.getAmountHave() : trade.getAmountWant());
                    quoteVolume = quoteVolume.add(reversed ? trade.getAmountWant() : trade.getAmountHave());

                }

                // тут подсчет отклонения за сутки
            } else {
                // за последние сутки не было сделок, значит смотрим просто последнюю цену
                trade = tradesMap.getLastTrade(key1, key2, false);
                if (trade != null) {
                    reversed = trade.getHaveKey() == key2;
                    price = lastPrice = maxPrice = minPrice = reversed ? trade.calcPrice() : trade.calcPriceRevers();
                } else {
                    price = lastPrice = maxPrice = minPrice = BigDecimal.ZERO;
                }
            }

        } catch (IOException e) {
        }

        OrderMapImpl ordersMap = DCSet.getInstance().getOrderMap();

        Order bidLastOrder = ordersMap.getHaveWanFirst(key2, key1);
        BigDecimal highest_bidPrice = bidLastOrder == null ? BigDecimal.ZERO : bidLastOrder.calcLeftPriceReverse();

        Order askLastOrder = ordersMap.getHaveWanFirst(key1, key2);
        BigDecimal lower_askPrice = askLastOrder == null ? BigDecimal.ZERO : askLastOrder.calcLeftPrice();

        int countOrdersBid = ordersMap.getCountHave(key2, 100);
        int countOrdersAsk = ordersMap.getCountHave(key1, 100);

        return new TradePair(asset1, asset2, lastPrice, lastTime,
                highest_bidPrice, lower_askPrice, baseVolume, quoteVolume, price,
                minPrice, maxPrice, count24, Block.getTimestamp(heightStart), countOrdersBid, countOrdersAsk);

    }

    /**
     * @param asset1
     * @param asset2
     * @param pairMap
     * @param cacheTimeMin time not use recalc
     * @return
     */
    public static TradePair reCalcAndUpdate(AssetCls asset1, AssetCls asset2, PairMap pairMap, int cacheTimeMin) {
        TradePair tradePairOld = pairMap.get(asset1.getKey(), asset2.getKey());
        if (tradePairOld != null && System.currentTimeMillis() - tradePairOld.updateTime < cacheTimeMin * 60000) {
            return tradePairOld;
        }

        TradePair tradePair = reCalc(asset1, asset2, tradePairOld);
        if (tradePair.equals(tradePairOld)) {
            if (tradePair.updateTime != tradePairOld.updateTime) {
                pairMap.put(tradePair);
            }
        } else {
            pairMap.put(tradePair);
        }
        return tradePair;
    }

    public static void foundPairs(DCSet dcSet, DLSet dlSet, int days) {
        TradeMapImpl tradesMap = dcSet.getTradeMap();
        PairMapImpl pairMap = dlSet.getPairMap();
        int foundDepth = days * 24 * 3600000;

        LOGGER.info("update TRADE PAIRS");
        try (IteratorCloseable<Fun.Tuple2<Long, Long>> iterator = tradesMap.getDescendingIterator()) {
            Trade lastTrade = null;
            while (iterator.hasNext()) {
                Trade trade = tradesMap.get(iterator.next());
                if (trade == null)
                    continue;

                if (trade.isCancel())
                    continue;

                if (lastTrade == null)
                    lastTrade = trade;

                if (lastTrade.getTimestamp() - trade.getTimestamp() > foundDepth)
                    break;

                Long key1 = trade.getHaveKey();
                Long key2 = trade.getWantKey();
                AssetCls asset1 = dcSet.getItemAssetMap().get(key1);
                AssetCls asset2 = dcSet.getItemAssetMap().get(key2);
                reCalcAndUpdate(asset1, asset2, pairMap, 30);
            }
        } catch (IOException e) {
        }
        LOGGER.info("TRADE PAIRS updated for days:" + foundDepth / 24 / 3600000);
    }
}
