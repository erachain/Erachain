package org.erachain.core.item.assets;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.transaction.CreateOrderTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.CompletedOrderMap;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.OrderMap;
import org.erachain.datachain.TradeMap;
import org.erachain.dbs.IteratorCloseable;
import org.mapdb.Fun;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
/**
 * Для простых переходов на функции Ордера при отладке и программировании
 */
public class OrderProcess {

    /**
     * По идее тут ордер активный должен себе получить лучшие условия если округление пошло в сторону,
     * так как он в мне выгодных условиях по цене
     *
     * @param block
     * @param transaction
     */
    public static void process(Order orderThis, Block block, Transaction transaction) {

        DCSet dcSet = orderThis.dcSet;
        long haveAssetKey = orderThis.getHaveAssetKey();
        BigDecimal amountHave = orderThis.getAmountHave();
        long wantAssetKey = orderThis.getWantAssetKey();
        int haveAssetScale = orderThis.getHaveAssetScale();
        //BigDecimal amountWant = orderThis.getAmountWant();
        int wantAssetScale = orderThis.getWantAssetScale();

        AssetCls assetHave;
        AssetCls assetWant;
        if (transaction instanceof CreateOrderTransaction) {
            assetHave = ((CreateOrderTransaction) transaction).getHaveAsset();
            assetWant = ((CreateOrderTransaction) transaction).getWantAsset();
        } else {
            assetHave = dcSet.getItemAssetMap().get(haveAssetKey);
            assetWant = dcSet.getItemAssetMap().get(wantAssetKey);
        }

        long id = orderThis.getId();
        // GET HEIGHT from ID
        int height = (int) (id >> 32);
        // нужно так как при сдвиге цены Заказ может быть уже початый и тут на ОстатокЦены проверку делаем
        BigDecimal price = id > BlockChain.LEFT_PRICE_HEIGHT_SEQ ? orderThis.calcLeftPrice() : orderThis.getPrice();
        BigDecimal thisPriceReverse = id > BlockChain.LEFT_PRICE_HEIGHT_SEQ ? orderThis.calcLeftPriceReverse() : orderThis.calcPriceReverse();

        Account creator = orderThis.getCreator();


        CompletedOrderMap completedMap = dcSet.getCompletedOrderMap();
        OrderMap ordersMap = dcSet.getOrderMap();
        TradeMap tradesMap = dcSet.getTradeMap();

        boolean debug = false;

        if (BlockChain.CHECK_BUGS > 1 &&
                //creator.equals("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5") &&
                //id.equals(Transaction.makeDBRef(12435, 1))
                //c id == 1132136199356417L // 174358 ---- 	255979-3	255992-1
                //height == 255979 // 133236 //  - тут остаток неисполнимый и у ордера нехватка - поэтому иницалицирующий отменяется
                //// 	255979-3	255992-1
                //|| height == 255992
                Transaction.viewDBRef(id).equals("255992-1")
                || Transaction.viewDBRef(id).equals("255979-3")
                || Transaction.viewDBRef(id).equals("791319-1")
                || transaction.viewHeightSeq().equals("695143-1")
            //id == 3644468729217028L


            //|| height == 133232 // - здесь хвостики какието у сделки с 1 в последнем знаке
            //|| height == 253841 // сработал NEW_FLOR 2-й
            //|| height == 255773 // тут мизерные остатки - // 70220 - 120.0000234 - обратный сработал
            //|| (haveAssetKey == 12L && wantAssetKey == 95L)
            //|| (wantAssetKey == 95L && haveAssetKey == 12L)
            //Arrays.equals(Base58.decode("3PVq3fcMxEscaBLEYgmmJv9ABATPasYjxNMJBtzp4aKgDoqmLT9MASkhbpaP3RNPv8CECmUyH5sVQtEAux2W9quA"), transaction.getSignature())
            //Arrays.equals(Base58.decode("2GnkzTNDJtMgDHmKKxkZSQP95S7DesENCR2HRQFQHcspFCmPStz6yn4XEnpdW4BmSYW5dkML6xYZm1xv7JXfbfNz"), transaction.getSignature()
            //id.equals(new BigInteger(Base58.decode("4NxUYDifB8xuguu5gVkma4V1neseHXYXhFoougGDzq9m7VdZyn7hjWUYiN6M7vkj4R5uwnxauoxbrMaavRMThh7j")))
            //&& !db.isFork()
        ) {
            debug = true;
        }

        ////// NEED FOR making secondary keys in TradeMap
        /// not need now ordersMap.add(this);

        //GET ALL ORDERS(WANT, HAVE) LOWEST PRICE FIRST
        //TRY AND COMPLETE ORDERS
        List<Order> orders = ordersMap.getOrdersForTradeWithFork(wantAssetKey, haveAssetKey, thisPriceReverse);

        /// ЭТО ПРОВЕРКА на правильную сортировку - все пашет
        if (false && id > BlockChain.LEFT_PRICE_HEIGHT_SEQ && (debug || BlockChain.CHECK_BUGS > 5) && !orders.isEmpty()) {
            BigDecimal priceTst = orders.get(0).calcLeftPrice();
            Long timestamp = orders.get(0).getId();
            Long idTst = 0L;
            for (Order item : orders) {
                if (item.getId().equals(idTst)) {
                    // RISE ERROR
                    List<Order> orders_test = ordersMap.getOrdersForTradeWithFork(wantAssetKey, haveAssetKey, thisPriceReverse);
                    timestamp = null;
                    ++timestamp;
                }
                idTst = item.getId();

                if (item.getHaveAssetKey() != wantAssetKey
                        || item.getWantAssetKey() != haveAssetKey) {
                    // RISE ERROR
                    timestamp = null;
                    ++timestamp;
                }
                // потому что сранивается потом обратная цена то тут должно быть возрастание
                // и если не так то ошибка
                int comp = priceTst.compareTo(item.calcLeftPrice());
                if (comp > 0) {
                    // RISE ERROR
                    timestamp = null;
                    ++timestamp;
                } else if (comp == 0) {
                    // здесь так же должно быть возрастание
                    // если не так то ошибка
                    if (timestamp.compareTo(item.getId()) > 0) {
                        // RISE ERROR
                        timestamp = null;
                        ++timestamp;
                    }
                }

                priceTst = item.calcLeftPrice();
                timestamp = item.getId();
            }

            List<Order> ordersAll = ordersMap.getOrdersForTradeWithFork(wantAssetKey, haveAssetKey, null);
            priceTst = orders.get(0).calcLeftPrice();
            timestamp = orders.get(0).getId();
            for (Order item : ordersAll) {
                int comp = priceTst.compareTo(item.calcLeftPrice()); // по остаткам цены());
                if (comp > 0) {
                    // RISE ERROR
                    timestamp = null;
                    ++timestamp;
                } else if (comp == 0) {
                    // здесь так же должно быть возрастание
                    // если не так то ошибка
                    if (timestamp.compareTo(item.getId()) > 0) {
                        // RISE ERROR
                        timestamp = null;
                        ++timestamp;
                    }
                }
                priceTst = item.calcLeftPrice();
                timestamp = item.getId();
            }
        }

        BigDecimal thisAmountHaveLeft = orderThis.getAmountHaveLeft();
        BigDecimal thisAmountHaveLeftStart = thisAmountHaveLeft;
        BigDecimal processedAmountFulfilledWant = BigDecimal.ZERO;

        int compare = 0;
        int compareThisLeft = 0;

        if (debug) {
            debug = true;
        }

        boolean completedThisOrder = false;
        // используется для порядка отражения ордеров при поиске
        int index = 0;

        while (!completedThisOrder && index < orders.size()) {
            //GET ORDER
            Order order;
            if (dcSet.inMemory()) {
                // так как это все в памяти расположено то нужно создать новый объект
                // иначе везде будет ссылка на один и тот же объект и
                // при переходе на MAIN базу возьмется уже обновленный ордер из памяти с уже пересчитанными остатками
                order = orders.get(index).copy();
            } else {
                order = orders.get(index);
            }

            index++;

            String orderREF = Transaction.viewDBRef(order.getId());
            if (debug ||
                    orderREF.equals("255992-1")
                    || orderREF.equals("255979-3")
                    || orderREF.equals("695143-1")
                //id == 1132136199356417L
            ) {
                debug = true;
            }

            BigDecimal orderAmountHaveLeft;
            BigDecimal orderAmountWantLeft;

            // REVERSE
            ////////// по остаткам цену берем!
            BigDecimal orderReversePrice = id > BlockChain.LEFT_PRICE_HEIGHT_SEQ ? order.calcLeftPriceReverse() : order.calcPriceReverse();
            // PRICE
            ////////// по остаткам цену берем!
            BigDecimal orderPrice = id > BlockChain.LEFT_PRICE_HEIGHT_SEQ ? order.calcLeftPrice() : order.getPrice();

            Trade trade;
            BigDecimal tradeAmountForHave;
            BigDecimal tradeAmountForWant; // GET
            BigDecimal tradeAmountAccurate;
            BigDecimal differenceTrade;
            //BigDecimal differenceTradeThis;

            /////////////// - разность точности цены из-за того что у одного ордера значение больше на порядки и этот порядок в точность уходит
            //CHECK IF BUYING PRICE IS HIGHER OR EQUAL THEN OUR SELLING PRICE
            //////// old compare = thisPrice.compareTo(orderReversePrice);
            compare = orderPrice.compareTo(thisPriceReverse);
            if (compare > 0) {
                // Делаем просто проверку на обратную цену и все - без игр с округлением и проверки дополнительной
                // и сравним так же по прямой цене со сниженной точностью у Заказа
                // так мы нивелируем разброс точности по цене выше
                if (orderReversePrice.compareTo(price) == 0) {
                    compare = 0;
                } else {
                    break;
                }
            }

            boolean willOrderUnResolved = false;
            orderAmountHaveLeft = order.getAmountHaveLeft();
            if (order.getFulfilledHave().signum() == 0) {
                orderAmountWantLeft = order.getAmountWant();
            } else {
                orderAmountWantLeft = orderAmountHaveLeft.multiply(orderPrice).setScale(haveAssetScale, RoundingMode.HALF_DOWN);
            }

            compareThisLeft = orderAmountWantLeft.compareTo(thisAmountHaveLeft);
            if (compareThisLeft == 0) {
                // оба ордера полностью исполнены
                tradeAmountForHave = orderAmountHaveLeft;
                tradeAmountForWant = thisAmountHaveLeft;

                completedThisOrder = true;

            } else if (compareThisLeft < 0) {
                // берем из текущего (Order) заказа данные для сделки - он полностью будет исполнен
                tradeAmountForHave = orderAmountHaveLeft;

                // возможно что у нашего ордера уже ничего не остается почти и он станет неисполняемым
                // и при этом остаток для енго незначительный
                if (orderThis.willUnResolvedFor(orderAmountWantLeft, false)
                        // и если цена не сильно у Нашего Заказа остаток большой
                        && !orderThis.isInitiatorLeftDeviationOut(orderAmountWantLeft)) {
                    tradeAmountForWant = thisAmountHaveLeft;
                    completedThisOrder = true;
                } else {
                    tradeAmountForWant = orderAmountWantLeft;
                }

            } else {
                // берем из нашего (OrderThis) заказа данные для сделки - он полностью будет исполнен

                tradeAmountForWant = thisAmountHaveLeft;

                if (debug) {
                    debug = true;
                }

                if (compare == 0) {
                    // цена совпала (возможно с округлением) то без пересчета берем что раньше посчитали
                    tradeAmountForHave = orderThis.getAmountWantLeft();
                    if (tradeAmountForHave.compareTo(orderAmountHaveLeft) >= 0) {
                        // если вылазим после округления за предел то берем что есть
                        tradeAmountForHave = orderAmountHaveLeft;

                    } else {
                        // тут возможны округления и остатки неисполнимые
                        // если текущий ордер станет не исполняемым, то попробуем его тут обработать особо
                        willOrderUnResolved = order.willUnResolvedFor(tradeAmountForHave, true);
                        if (willOrderUnResolved
                                // и остаток небольшой для всего Заказа
                                && !order.isTargetLeftDeviationOut(tradeAmountForHave)) {
                            tradeAmountForHave = orderAmountHaveLeft;
                        }
                    }

                } else {

                    tradeAmountForHave = tradeAmountForWant.multiply(orderReversePrice).setScale(wantAssetScale, RoundingMode.HALF_DOWN);
                    if (tradeAmountForHave.compareTo(orderAmountHaveLeft) >= 0) {
                        // если вылазим после округления за предел то берем что есть
                        tradeAmountForHave = orderAmountHaveLeft;

                    } else {

                        if (debug) {
                            debug = true;
                        }

                        // если текущий ордер станет не исполняемым, то попробуем его тут обработать особо
                        willOrderUnResolved = order.willUnResolvedFor(tradeAmountForHave, true);
                        if (willOrderUnResolved
                                // и остаток небольшой для всего Заказа
                                && !order.isTargetLeftDeviationOut(tradeAmountForHave)) {
                            tradeAmountForHave = orderAmountHaveLeft;
                        }
                    }
                }

                //THIS is COMPLETED
                completedThisOrder = true;

            }

            if (tradeAmountForHave.compareTo(BigDecimal.ZERO) <= 0
                    || tradeAmountForWant.compareTo(BigDecimal.ZERO) <= 0) {
                debug = true;
                logger.error("Order is EMPTY: " + orderREF);
                Long error = null;
                error++;
            }

            //CREATE TRADE

            // CUT PRECISION in bytes
            tradeAmountForHave = tradeAmountForHave.stripTrailingZeros();
            byte[] amountBytes = tradeAmountForHave.unscaledValue().toByteArray();
            while (amountBytes.length > Order.FULFILLED_LENGTH) {
                tradeAmountForHave.setScale(tradeAmountForHave.scale() - 1, BigDecimal.ROUND_HALF_UP);
                amountBytes = tradeAmountForHave.unscaledValue().toByteArray();
            }
            tradeAmountForWant = tradeAmountForWant.stripTrailingZeros();
            amountBytes = tradeAmountForWant.unscaledValue().toByteArray();
            while (amountBytes.length > Order.FULFILLED_LENGTH) {
                tradeAmountForWant.setScale(tradeAmountForWant.scale() - 1, BigDecimal.ROUND_HALF_UP);
                amountBytes = tradeAmountForWant.unscaledValue().toByteArray();
            }

            if (debug) {
                debug = true;
            }

            //////////////////////////// TRADE /////////////////
            if (tradeAmountForHave.scale() > wantAssetScale
                    || tradeAmountForWant.scale() > haveAssetScale) {
                Long error = null;
                error++;
            }
            if (tradeAmountForHave.signum() <= 0
                    || tradeAmountForWant.signum() < 0) {
                Long error = null;
                error++;
            }

            trade = new Trade(id, order.getId(), haveAssetKey, wantAssetKey,
                    tradeAmountForHave, tradeAmountForWant,
                    haveAssetScale, wantAssetScale, index);

            if (BlockChain.CHECK_BUGS > 1) {
                boolean testDeviation = orderPrice.subtract(trade.calcPrice()).abs().divide(orderPrice, 6, RoundingMode.HALF_DOWN)
                        .compareTo(new BigDecimal("0.001")) > 0;
                if (testDeviation) {
                    logger.error("TRADE Deviation fo big: " + orderPrice.subtract(trade.calcPrice()).abs()
                            .divide(orderPrice, 6, RoundingMode.HALF_DOWN).toPlainString());
                    Long error = null;
                    error++;
                }
            }

            //ADD TRADE TO DATABASE
            tradesMap.put(trade);

            /// так как у нас Индексы высчитываются по плавающей цене для остатков и она сейчас изменится
            /// то сперва удалим Ордер - до изменения Остатков и цены по Остаткам
            /// тогда можно ключи делать по цене на Остатки
            //REMOVE FROM ORDERS
            ordersMap.delete(order);

            //UPDATE FULFILLED HAVE
            order.fulfill(tradeAmountForHave); // amountHave));
            // accounting on PLEDGE position
            order.getCreator().changeBalance(dcSet, true,
                    true, wantAssetKey, tradeAmountForHave, false, false,
                    true
            );


            orderThis.fulfill(tradeAmountForWant); //amountWant));

            if (order.isFulfilled()) {
                //ADD TO COMPLETED ORDERS
                completedMap.put(order);
            } else {
                //UPDATE ORDER
                if (willOrderUnResolved) {
                    // if left not enough for 1 buy by price this order
                    order.dcSet = dcSet;
                    order.processOnUnresolved(block, transaction, true);

                    //ADD TO COMPLETED ORDERS
                    completedMap.put(order);
                } else {
                    // тут цена по остаткам поменяется
                    ordersMap.put(order);
                }
            }

            //TRANSFER FUNDS
            if (height > BlockChain.VERS_5_3) {
                AssetCls.processTrade(dcSet, block, order.getCreator(),
                        false, assetWant, assetHave,
                        false, tradeAmountForWant, transaction.getTimestamp(), order.getId());

            } else {
                order.getCreator().changeBalance(dcSet, false, false, haveAssetKey,
                        tradeAmountForWant, false, false, false);
                transaction.addCalculated(block, order.getCreator(), order.getWantAssetKey(), tradeAmountForWant,
                        "Trade Order @" + Transaction.viewDBRef(order.getId()));
            }

            // Учтем что у стороны ордера обновилась форжинговая информация
            if (haveAssetKey == Transaction.RIGHTS_KEY && block != null) {
                block.addForgingInfoUpdate(order.getCreator());
            }

            // update new values
            thisAmountHaveLeft = orderThis.getAmountHaveLeft();
            processedAmountFulfilledWant = processedAmountFulfilledWant.add(tradeAmountForHave);

            if (debug) {
                debug = true;
            }

            if (completedThisOrder)
                break;

            // возможно схлопнулся?
            if (orderThis.isFulfilled()) {
                completedThisOrder = true;
                break;
            }

            // if can't trade by more good price than self - by orderOrice - then  auto cancel!
            if (orderThis.isInitiatorUnResolved()) {

                if (debug) {
                    debug = orderThis.isInitiatorUnResolved();
                }

                // cancel order if it not fulfiled isDivisible

                // or HAVE not enough to one WANT  = price
                ///CancelOrderTransaction.process_it(dcSet, this);
                //and stop resolve
                completedThisOrder = true;
                // REVERT not completed AMOUNT
                orderThis.processOnUnresolved(block, transaction, false);
                break;
            }

        }

        if (debug) {
            debug = true;
        }

        if (completedThisOrder) {
            completedMap.put(orderThis);
        } else {
            ordersMap.put(orderThis);
        }

        //TRANSFER FUNDS
        if (processedAmountFulfilledWant.signum() > 0) {
            if (height > BlockChain.VERS_5_3) {
                AssetCls.processTrade(dcSet, block, creator,
                        true, assetHave, assetWant,
                        false, processedAmountFulfilledWant, transaction.getTimestamp(), id);
            } else {
                creator.changeBalance(dcSet, false, false, wantAssetKey,
                        processedAmountFulfilledWant, false, false, false);
                transaction.addCalculated(block, creator, wantAssetKey, processedAmountFulfilledWant,
                        "Resolve Order @" + Transaction.viewDBRef(id));
            }
        }

        // с ордера сколько было продано моего актива? на это число уменьшаем залог
        thisAmountHaveLeftStart = thisAmountHaveLeftStart.subtract(orderThis.getAmountHaveLeft());
        if (thisAmountHaveLeftStart.signum() > 0) {
            // change PLEDGE
            creator.changeBalance(dcSet, true, true, haveAssetKey,
                    thisAmountHaveLeftStart, false, false, true);
        }

    }

    public static Order orphan(DCSet dcSet, Long id, Block block, long blockTime) {

        CompletedOrderMap completedMap = dcSet.getCompletedOrderMap();
        OrderMap ordersMap = dcSet.getOrderMap();
        TradeMap tradesMap = dcSet.getTradeMap();


        //REMOVE FROM COMPLETED ORDERS - он может быть был отменен, поэтому нельзя проверять по Fulfilled
        // - на всякий случай удалим его в любом случае
        //// тут нужно получить остатки все из текущего состояния иначе индексы по измененной цене с остатков не удалятся
        /// поэтому смотрим что есть в таблице и если есть то его грузим с ценой по остаткам той что в базе
        Order orderThis = completedMap.remove(id);
        if (orderThis == null) {
            orderThis = ordersMap.remove(id);
        }

        long haveAssetKey = orderThis.getHaveAssetKey();
        long wantAssetKey = orderThis.getWantAssetKey();

        Account creator = orderThis.getCreator();

        // GET HEIGHT from ID
        int height = (int) (id >> 32);

        if (BlockChain.CHECK_BUGS > 1 &&
                //Transaction.viewDBRef(id).equals("776446-1")
                id == 3644468729217028L
        ) {
            boolean debug = false;
        }


        BigDecimal thisAmountFulfilledWant = BigDecimal.ZERO;

        BigDecimal thisAmountHaveLeft = orderThis.getAmountHaveLeft();
        BigDecimal thisAmountHaveLeftEnd = thisAmountHaveLeft; //this.getAmountHaveLeft();

        AssetCls assetHave = dcSet.getItemAssetMap().get(haveAssetKey);
        AssetCls assetWant = dcSet.getItemAssetMap().get(wantAssetKey);

        //ORPHAN TRADES
        Trade trade;
        try (IteratorCloseable<Fun.Tuple2<Long, Long>> iterator = tradesMap.getIteratorByInitiator(id)) {
            while (iterator.hasNext()) {

                trade = tradesMap.get(iterator.next());
                if (!trade.isTrade()) {
                    continue;
                }
                Order target = trade.getTargetOrder(dcSet);

                //REVERSE FUNDS
                BigDecimal tradeAmountHave = trade.getAmountHave();
                BigDecimal tradeAmountWant = trade.getAmountWant();

                //DELETE FROM COMPLETED ORDERS- он может быть был отменен, поэтому нельзя проверять по Fulfilled
                // - на всякий случай удалим его в любом случае
                completedMap.delete(target);

                //// Пока не изменились Остатки и цена по Остаткм не съехала, удалим из таблицы ордеров
                /// иначе вторичный ключ останется так как он не будет найден из-за измененой "цены по остаткам"
                ordersMap.delete(target);

                //REVERSE FULFILLED
                target.fulfill(tradeAmountHave.negate());
                // accounting on PLEDGE position
                target.getCreator().changeBalance(dcSet, false,
                        true, wantAssetKey, tradeAmountHave, false, false,
                        true
                );

                thisAmountFulfilledWant = thisAmountFulfilledWant.add(tradeAmountHave);

                if (height > BlockChain.VERS_5_3) {
                    AssetCls.processTrade(dcSet, block, target.getCreator(),
                            false, assetWant, assetHave,
                            true, tradeAmountWant,
                            blockTime,
                            0L);
                } else {

                    target.getCreator().changeBalance(dcSet, true, false, haveAssetKey,
                            tradeAmountWant, false, false, false);
                }
                // Учтем что у стороны ордера обновилась форжинговая информация
                if (haveAssetKey == Transaction.RIGHTS_KEY && block != null) {
                    block.addForgingInfoUpdate(target.getCreator());
                }

                //UPDATE ORDERS
                ordersMap.put(target);

                //REMOVE TRADE FROM DATABASE
                tradesMap.delete(trade);

                if (BlockChain.CHECK_BUGS > 3) {
                    if (tradesMap.contains(new Fun.Tuple2<>(trade.getInitiator(), trade.getTarget()))) {
                        Long err = null;
                        err++;
                    }
                }


            }
        } catch (IOException e) {
        }

        // с ордера сколько было продано моего актива? на это число уменьшаем залог
        thisAmountHaveLeftEnd = orderThis.getAmountHaveLeft().subtract(thisAmountHaveLeftEnd);
        if (thisAmountHaveLeftEnd.signum() > 0) {
            // change PLEDGE
            creator.changeBalance(dcSet, false, true, haveAssetKey,
                    thisAmountHaveLeftEnd, false, false, true);
        }

        //REVERT WANT
        if (height > BlockChain.VERS_5_3) {
            AssetCls.processTrade(dcSet, block, creator,
                    true, assetHave, assetWant,
                    true, thisAmountFulfilledWant, blockTime, 0L);
        } else {
            creator.changeBalance(dcSet, true, false, wantAssetKey,
                    thisAmountFulfilledWant, false, false, false);
        }

        return orderThis;
    }

}
