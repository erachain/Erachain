package org.erachain.controller;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.erachain.core.BlockChain;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Pair;
import org.erachain.core.item.assets.Trade;
import org.erachain.datachain.*;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.settings.Settings;
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
    HashMap pairs = new HashMap();
    JSONObject pairsList;

    private static final Logger LOGGER = LoggerFactory.getLogger(PairsController.class.getSimpleName());

    PairsController() {
        init();
    }

    public void init() {
        //OPEN FILE
        File file = new File(Settings.getInstance().getUserPath() + "market.json");

        //CREATE FILE IF IT DOESNT EXIST
        if (file.exists()) {
            String jsonString = "";
            try {
                //READ PEERS FILE
                List<String> lines = Files.readLines(file, Charsets.UTF_8);

                for (String line : lines) {
                    if (line.trim().startsWith("//")) {
                        // пропускаем //
                        continue;
                    }
                    jsonString += line;
                }
            } catch (Exception e) {
            }
            //CREATE JSON OBJECT
            this.pairsList = (JSONObject) JSONValue.parse(jsonString);

        } else {
            this.pairsList = new JSONObject();
            pairsList.put("1/2", Boolean.TRUE);
        }


        ItemAssetMap mapAssets = DCSet.getInstance().getItemAssetMap();
        PairMapImpl mapPairs = DCSet.getInstance().getPairMap();
        for (Object pairKey : pairsList.keySet()) {
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

            Pair pair = mapPairs.get(key1, key2);
            if (pair == null) {
                LOGGER.warn("pair [" + key1 + "/" + key2 + "] not found");
                continue;
            }

            pairs.put(key1 + "/" + key2, pair);

        }
    }

    /**
     * Обновить данные
     *
     * @param key1
     * @param key2
     */
    public void update(Long key1, Long key2) {
        TradeMapImpl tradesMap = DCSet.getInstance().getTradeMap();

        ItemAssetMap mapAssets = DCSet.getInstance().getItemAssetMap();
        AssetCls asset1 = mapAssets.get(key1);
        if (asset1 == null)
            return;
        AssetCls asset2 = mapAssets.get(key2);
        if (asset2 == null)
            return;

        int heightStart = Controller.getInstance().getMyHeight();
        int heightEnd = heightStart - BlockChain.BLOCKS_PER_DAY(heightStart);

        int count24 = 0;
        BigDecimal minPrice = new BigDecimal(Long.MIN_VALUE);
        BigDecimal maxPrice = new BigDecimal(Long.MAX_VALUE);
        BigDecimal lastPrice = null;
        BigDecimal baseVolume = BigDecimal.ZERO;
        BigDecimal quoteVolume = BigDecimal.ZERO;
        long lastTime = 0;
        BigDecimal price = null;
        BigDecimal priceChangePercent24h = BigDecimal.ZERO;

        try (IteratorCloseable<Fun.Tuple2<Long, Long>> iterator = (tradesMap.getPairIterator(key1, key2, heightStart, heightEnd)) {
            Trade trade;
            while (iterator.hasNext()) {
                trade = tradesMap.get(iterator.next());
                if (trade == null) {
                    LOGGER.warn("trade for pair [" + key1 + "/" + key2 + "] not found");
                    continue;
                }
                count24++;
                price = trade.calcPrice();
                if (lastPrice == null) {
                    lastPrice = price;
                    lastTime = trade.getTimestamp();
                }

                if (minPrice.compareTo(price) > 0)
                    minPrice = price;
                if (maxPrice.compareTo(price) < 0)
                    maxPrice = price;

                baseVolume = baseVolume.add(trade.getAmountHave());
                quoteVolume = quoteVolume.add(trade.getAmountWant());

            }
            // тут подсчет отклонения за сутки
            if (price != null) {
                priceChangePercent24h = lastPrice.subtract(price).multiply(price).setScale(3, RoundingMode.DOWN);
            }

        } catch (IOException e) {
        }

        OrderMapImpl ordersMap = DCSet.getInstance().getOrderMap();
        Order askLastOrder = ordersMap.getHaveWanFirst(key1, key2);
        BigDecimal askPrice = askLastOrder == null ? null : askLastOrder.calcLeftPrice();

        Order bidLastOrder = ordersMap.getHaveWanFirst(key2, key1);
        BigDecimal bidPrice = bidLastOrder == null ? null : bidLastOrder.calcLeftPriceReverse();

        Pair pair = new Pair(key1, key2, asset1.getScale(), asset2.getScale(), lastPrice, lastTime,
                bidPrice, askPrice, baseVolume, quoteVolume, priceChangePercent24h,
                maxPrice, minPrice, count24);

        pairs.put(key1 + "/" + key2, pair);

        DCSet.getInstance().getPairMap().put(pair);

    }
}
