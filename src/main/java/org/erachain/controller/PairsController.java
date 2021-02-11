package org.erachain.controller;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.erachain.core.BlockChain;
import org.erachain.core.block.Block;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.item.assets.TradePair;
import org.erachain.datachain.*;
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
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;

public class PairsController {
    public HashMap<String, TradePair> spotPairs = new HashMap();
    public JSONObject pairsJson = new JSONObject();
    public JSONObject spotPairsJson;

    public JSONObject spotPairsList;

    private static final Logger LOGGER = LoggerFactory.getLogger(PairsController.class.getSimpleName());

    PairsController() {
        init();
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
        if (file.exists()) {
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
            JSONObject spot = new JSONObject();
            spot.put("1/2", Boolean.TRUE);
            spot.put("1/12", Boolean.TRUE);
            spot.put("1/95", Boolean.TRUE);
            spot.put("12/95", Boolean.TRUE);
            spot.put("21/95", Boolean.TRUE);
            this.spotPairsJson = new JSONObject();
            spotPairsJson.put("spot", spot);
        }

    }

    long updateList;
    public void updateList() {

        init();
        if (System.currentTimeMillis() - updateList < 300000) {
            return;
        }
        updateList = System.currentTimeMillis();

        spotPairsList = new JSONObject();

        ItemAssetMap mapAssets = DCSet.getInstance().getItemAssetMap();
        PairMapImpl mapPairs = DCSet.getInstance().getPairMap();
        JSONObject spotJson = (JSONObject) spotPairsJson.get("spot");
        for (Object pairKey : spotJson.keySet()) {
            String[] pairStr = ((String) pairKey).split("/");
            Long key1 = Long.parseLong(pairStr[0]);
            AssetCls asset1 = mapAssets.get(key1);
            if (asset1 == null) {
                LOGGER.warn("asset [" + key1 + "] not found");
                continue;
            }
            Long key2 = Long.parseLong(pairStr[1]);
            AssetCls asset2 = mapAssets.get(key2);
            if (asset2 == null) {
                LOGGER.warn("asset [" + key2 + "] not found");
                continue;
            }

            String pairJsonKey = asset1.getName() + "_" + asset2.getName();
            JSONArray array = new JSONArray();
            array.add(spotJson.get(pairKey));
            array.add(key1);
            array.add(key2);
            spotPairsList.put(pairJsonKey, array);

            TradePair tradePair = mapPairs.get(key1, key2);
            if (true || tradePair == null) {
                tradePair = reCalc(asset1, asset2);
            }
            spotPairs.put(pairJsonKey, tradePair);
            spotPairsJson.put(pairJsonKey, tradePair.toJson());

        }
    }

    /**
     * Обновить данные
     *
     * @param asset1
     * @param asset2
     */
    public static TradePair reCalc(AssetCls asset1, AssetCls asset2) {
        TradeMapImpl tradesMap = DCSet.getInstance().getTradeMap();
        Long key1 = asset1.getKey();
        Long key2 = asset2.getKey();

        int heightStart = Controller.getInstance().getMyHeight();
        int heightEnd = heightStart - BlockChain.BLOCKS_PER_DAY(heightStart);

        int count24 = 0;
        BigDecimal minPrice = new BigDecimal(Long.MAX_VALUE);
        BigDecimal maxPrice = new BigDecimal(Long.MIN_VALUE);
        BigDecimal lastPrice = null;
        BigDecimal baseVolume = BigDecimal.ZERO;
        BigDecimal quoteVolume = BigDecimal.ZERO;
        long lastTime = 0;
        BigDecimal price = null;
        BigDecimal priceChangePercent24h = BigDecimal.ZERO;

        boolean reversed;
        try (IteratorCloseable<Fun.Tuple2<Long, Long>> iterator = (tradesMap.getPairIterator(key1, key2, heightStart, heightEnd))) {
            Trade trade;
            if (iterator.hasNext()) {
                while (iterator.hasNext()) {
                    trade = tradesMap.get(iterator.next());
                    if (trade == null) {
                        LOGGER.warn("trade for pair [" + key1 + "/" + key2 + "] not found");
                        continue;
                    }
                    count24++;

                    reversed = trade.getHaveKey().equals(key2);

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
                if (price != null) {
                    priceChangePercent24h = lastPrice.subtract(price).movePointRight(2).divide(price, 3, RoundingMode.DOWN);
                }
            } else {
                // за последние сутки не было сделок, значит смотрим просто последнюю цену
                trade = tradesMap.getLastTrade(key1, key2);
                if (trade != null) {
                    reversed = trade.getHaveKey().equals(key2);
                    lastPrice = maxPrice = minPrice = reversed ? trade.calcPrice() : trade.calcPriceRevers();
                    priceChangePercent24h = BigDecimal.ZERO;
                } else {
                    lastPrice = maxPrice = minPrice = BigDecimal.ZERO;
                }
            }

        } catch (IOException e) {
        }

        OrderMapImpl ordersMap = DCSet.getInstance().getOrderMap();

        Order bidLastOrder = ordersMap.getHaveWanFirst(key2, key1);
        BigDecimal highest_bidPrice = bidLastOrder == null ? BigDecimal.ZERO : bidLastOrder.calcLeftPriceReverse();

        Order askLastOrder = ordersMap.getHaveWanFirst(key1, key2);
        BigDecimal lower_askPrice = askLastOrder == null ? BigDecimal.ZERO : askLastOrder.calcLeftPrice();

        return new TradePair(asset1, asset2, lastPrice, lastTime,
                highest_bidPrice, lower_askPrice, baseVolume, quoteVolume, priceChangePercent24h,
                minPrice, maxPrice, count24, Block.getTimestamp(heightStart));

    }
}
