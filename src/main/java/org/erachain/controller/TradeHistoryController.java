package org.erachain.controller;

import org.erachain.core.BlockChain;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DTHSet;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TradeMapImpl;
import org.erachain.dbs.IteratorCloseable;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Map;

public class TradeHistoryController extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(TradeHistoryController.class.getSimpleName());
    Controller cnt;
    DTHSet dthSet;

    TradeHistoryMakerM tradeHistoryMakerM;
    TradeHistoryMakerD tradeHistoryMakerD;

    TradeHistoryController(Controller cnt) {

        this.cnt = cnt;

        dthSet = DTHSet.reCreateDB();

        tradeHistoryMakerD = new TradeHistoryMakerD(cnt, dthSet);
        tradeHistoryMakerM = new TradeHistoryMakerM(cnt, dthSet);

        this.start();

    }

    boolean run = true;

    @Override
    public void run() {

        DCSet dcSet = cnt.getDCSet();
        TradeMapImpl tradesTable = dcSet.getTradeMap();

        try {
            while (run) {

                Thread.sleep(5000);
                while (cnt.isStatusSynchronizing()) {
                    Thread.sleep(5000);
                }

                int height = dthSet.getDatabase().getAtomicInteger("height").get();
                int myHeight = cnt.getMyHeight();
                if (height >= myHeight) {
                    // что-то с синхронизацией цепочки - выход
                    break;
                }

                // оставим для сборки дневных графиков время - минус 150 дней
                int toDailyHeight = myHeight - (int) (150L * 24L * 60L * 60000L / BlockChain.GENERATING_MIN_BLOCK_TIME_MS(myHeight));
                int toSleep = 0;
                int toCommit = 0;
                Fun.Tuple2<Long, Long> nextKey;
                int tradeHeight;
                BigDecimal[] pointValues;
                String pintKey;

                // сборка месячных графиков
                try (IteratorCloseable<Fun.Tuple2<Long, Long>> iterator = tradesTable.getIterator(
                        // поиск делаем со следующего
                        new Fun.Tuple2(Transaction.makeDBRef(height + 1, 0), 0L),
                        // так как номер трнзакции = - то включая myHeight
                        new Fun.Tuple2(Transaction.makeDBRef(myHeight + 1, 0), 0L), false)) {

                    while (run && iterator.hasNext()) {

                        if (cnt.isStatusSynchronizing()) {
                            break;
                        }

                        nextKey = iterator.next();
                        Trade trade = tradesTable.get(nextKey);
                        if (trade == null) {
                            // что-то пошло не так
                            logger.warn("Empty TRADE: " + nextKey.toString());
                            continue;
                        }
                        if (!trade.isTrade())
                            continue;

                        ++toCommit;
                        tradeHeight = Transaction.parseHeightDBRef(nextKey.a);

                        if (tradeHeight != height) {

                            height = tradeHeight;

                            if (toCommit > 10000) {
                                toCommit = 0;
                                dthSet.getDatabase().getAtomicInteger("height").set(height);
                                dthSet.commit();
                                logger.info("Remake Trade history at height: {}", height);
                            }
                        }

                        // сборка только месячных графиков
                        //tradeHistoryMakerM.make(tradeHeight, trade);
                        long haveKey;
                        long wantKey;
                        BigDecimal volume;
                        BigDecimal price;
                        String paitKey;

                        haveKey = trade.getHaveKey();
                        wantKey = trade.getWantKey();
                        if (haveKey < wantKey) {
                            volume = trade.getAmountHave();
                            price = trade.calcPrice();
                            paitKey = haveKey + "/" + wantKey;
                        } else {
                            price = trade.calcPriceRevers();
                            volume = trade.getAmountWant();
                            paitKey = wantKey + "/" + haveKey;
                        }

                        // PROCESS MONTH HISTORY
                        pintKey = tradeHistoryMakerM.makeKey(paitKey, new Timestamp(cnt.blockChain.getTimestamp(tradeHeight)));
                        pointValues = tradeHistoryMakerM.getMap().get(pintKey);
                        tradeHistoryMakerM.updatePointValues(pointValues, price, volume);
                        tradeHistoryMakerM.getMap().set(pintKey, pointValues);

                        if (tradeHeight > toDailyHeight) {
                            // PROCESS DAILY HISTORY
                            pintKey = tradeHistoryMakerD.makeKey(paitKey, new Timestamp(cnt.blockChain.getTimestamp(tradeHeight)));
                            pointValues = tradeHistoryMakerD.getMap().get(pintKey);
                            tradeHistoryMakerD.updatePointValues(pointValues, price, volume);
                            tradeHistoryMakerD.getMap().set(pintKey, pointValues);
                            // TODO добавить удаление хоста более 150 дней
                        }

                        if (++toSleep > 1000) {
                            toSleep = 0;
                            Thread.sleep(10);
                        }
                    }

                    // Там коммит мог и не случиться, поэтому обнови высоту и зальем
                    dthSet.getDatabase().getAtomicInteger("height").set(myHeight);
                    dthSet.commit();
                    logger.info("Remake Trade history at height: {} - DONE", dthSet.getDatabase().getAtomicInteger("height").get());

                    Thread.sleep(600000);
                }

            }

        } catch (InterruptedException e) {
            dthSet.rollback();
            logger.warn("Remake Trade history at height: {} - STOP", dthSet.getDatabase().getAtomicInteger("height").get());
            dthSet.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.error("Remake Trade history at height: {} - ERROR", dthSet.getDatabase().getAtomicInteger("height").get());
            dthSet.rollback();
        }

    }

    public String getPoints(Long baseAssetKey, Long quoteAssetKey, String period) {
        String key;
        boolean reverse;
        if (baseAssetKey < quoteAssetKey) {
            key = baseAssetKey + "/" + quoteAssetKey;
            reverse = false;
        } else {
            key = quoteAssetKey + "/" + baseAssetKey;
            reverse = true;
        }

        Map<String, BigDecimal[]> points;
        switch (period.toLowerCase()) {
            case "d":
                points = tradeHistoryMakerD.getPoints(key);
                break;
            default:
                points = tradeHistoryMakerM.getPoints(key);
        }

        StringBuilder strArray = new StringBuilder().append("[");
        if (reverse) {
            points.forEach((k, v) -> {
                if (strArray.length() > 2)
                    strArray.append(",");

                strArray.append("[\"").append(k.split(" ")[1]).append("\",")
                        .append(v[0].toPlainString()).append(",")
                        .append(v[1].toPlainString()).append(",")
                        .append(v[2].toPlainString()).append(",")
                        .append(v[3].toPlainString()).append(",")
                        .append(v[4].toPlainString())
                        .append("]");
            });

        } else {
            points.forEach((k, v) -> {
                if (strArray.length() > 2)
                    strArray.append(",");

                //Order.AMOUNT_LENGTH
                strArray.append("[\"").append(k.split(" ")[1]).append("\",")
                        .append(Order.calcPrice(v[0], BigDecimal.ONE).toPlainString()).append(",")
                        .append(Order.calcPrice(v[1], BigDecimal.ONE).toPlainString()).append(",")
                        // среднее между началом и окончанием умножить на Объем
                        .append((v[0].add(v[1]).multiply(BigDecimal.valueOf(0.5d)).multiply(v[2])).setScale((v[0].scale() + v[0].scale()) / 2, RoundingMode.HALF_DOWN).toPlainString()).append(",")
                        // тут места Мин Макс меняются
                        .append(Order.calcPrice(v[4], BigDecimal.ONE).toPlainString()).append(",")
                        .append(Order.calcPrice(v[3], BigDecimal.ONE).toPlainString())
                        .append("]");
            });
        }

        strArray.append("]");

        return strArray.toString();
    }

    public void close() {
        run = false;
        interrupt();
    }
}
