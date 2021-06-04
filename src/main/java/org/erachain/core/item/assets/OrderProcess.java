package org.erachain.core.item.assets;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.transaction.CreateOrderTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.CompletedOrderMap;
import org.erachain.datachain.OrderMap;
import org.erachain.datachain.TradeMap;

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
     * @param asChange
     */
    public static void process(Order orderThis, Block block, Transaction transaction, boolean asChange) {

        // GET HEIGHT from ID
        long id = orderThis.getId();
        int height = (int) (id >> 32);

        long haveAssetKey = orderThis.getHaveAssetKey();
        BigDecimal amountHave = orderThis.getAmountHave();
        long wantAssetKey = orderThis.getWantAssetKey();
        int haveAssetScale = orderThis.getHaveAssetScale();
        //BigDecimal amountWant = orderThis.getAmountWant();
        int wantAssetScale = orderThis.getWantAssetScale();

        BigDecimal price = orderThis.getPrice();
        Account creator = orderThis.getCreator();

        CompletedOrderMap completedMap = orderThis.dcSet.getCompletedOrderMap();
        OrderMap ordersMap = orderThis.dcSet.getOrderMap();
        TradeMap tradesMap = orderThis.dcSet.getTradeMap();

        boolean debug = false;

        if (BlockChain.CHECK_BUGS > 1 &&
                //orderThis.creator.equals("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5") &&
                //orderThis.id.equals(Transaction.makeDBRef(12435, 1))
                //orderThis.id.equals(770667456757788l) // 174358 ---- 	255979-3	255992-1
                //height == 255979 // 133236 //  - тут остаток неисполнимый и у ордера нехватка - поэтому иницалицирующий отменяется
                //// 	255979-3	255992-1
                //|| height == 255992
                Transaction.viewDBRef(id).equals("2685-1")
            //id == 3644468729217028L


            //|| height == 133232 // - здесь хвостики какието у сделки с 1 в последнем знаке
            //|| height == 253841 // сработал NEW_FLOR 2-й
            //|| height == 255773 // тут мизерные остатки - // 70220 - 120.0000234 - обратный сработал
            //|| (orderThis.haveAssetKey == 12L && orderThis.wantAssetKey == 95L)
            //|| (orderThis.wantAssetKey == 95L && orderThis.haveAssetKey == 12L)
            //Arrays.equals(Base58.decode("3PVq3fcMxEscaBLEYgmmJv9ABATPasYjxNMJBtzp4aKgDoqmLT9MASkhbpaP3RNPv8CECmUyH5sVQtEAux2W9quA"), transaction.getSignature())
            //Arrays.equals(Base58.decode("2GnkzTNDJtMgDHmKKxkZSQP95S7DesENCR2HRQFQHcspFCmPStz6yn4XEnpdW4BmSYW5dkML6xYZm1xv7JXfbfNz"), transaction.getSignature()
            //orderThis.id.equals(new BigInteger(Base58.decode("4NxUYDifB8xuguu5gVkma4V1neseHXYXhFoougGDzq9m7VdZyn7hjWUYiN6M7vkj4R5uwnxauoxbrMaavRMThh7j")))
            //&& !db.isFork()
        ) {
            debug = true;
        }

        ////// NEED FOR making secondary keys in TradeMap
        /// not need now ordersMap.add(this);

        if (!asChange) {
            //REMOVE HAVE
            //orderThis.creator.setBalance(orderThis.have, orderThis.creator.getBalance(db, orderThis.have).subtract(orderThis.amountHave), db);
            orderThis.getCreator().changeBalance(orderThis.dcSet, true, false, haveAssetKey, amountHave,
                    false, false,
                    // accounting on PLEDGE position
                    true, Account.BALANCE_POS_PLEDGE);
        }

        BigDecimal thisPriceReverse = orderThis.calcPriceReverse();

        //GET ALL ORDERS(WANT, HAVE) LOWEST PRICE FIRST
        //TRY AND COMPLETE ORDERS
        List<Order> orders = ordersMap.getOrdersForTradeWithFork(orderThis.getWantAssetKey(), haveAssetKey, thisPriceReverse);

        /// ЭТО ПРОВЕРКА на правильную сортировку - все пашет
        if (id > BlockChain.LEFT_PRICE_HEIGHT_SEQ && (debug || BlockChain.CHECK_BUGS > 5) && !orders.isEmpty()) {
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
                // потому что сравнивается потом обратная цена то тут должно быть возрастание
                // и если не так то ошибка
                int comp = priceTst.compareTo(item.calcLeftPrice());
                if (comp > 0) {
                    // RISE ERROR
                    timestamp = null;
                    ++timestamp;
                } else if (comp == 0) {
                    // здесь так же должно быть возростание
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
                    // здесь так же должно быть возростание
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
        int compareLeft = 0;

        if (debug) {
            debug = true;
        }

        boolean completedOrder = false;
        // используется для порядка отражения ордеров при поиске
        int index = 0;

        while (!completedOrder && index < orders.size()) {
            //GET ORDER
            Order order;
            if (orderThis.dcSet.inMemory()) {
                // так как это все в памяти расположено то нужно создать новый объект
                // иначе везде будет ссылка на один и тот же объект и
                // при переходе на MAIN базу возьмется уже обновленный ордер из памяти с уже пересчитанными остатками
                order = orders.get(index).copy();
            } else {
                order = orders.get(index);
            }

            index++;

            if (debug ||
                    Transaction.viewDBRef(id).equals("2685-1")
                //id == 3644468729217028L
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
            String orderREF = Transaction.viewDBRef(order.getId());

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

            boolean willUnResolvedFor = false;
            orderAmountHaveLeft = order.getAmountHaveLeft();
            // SCALE for HAVE in ORDER
            // цену ему занижаем так как это держатель позиции
            if (order.getFulfilledHave().signum() == 0) {
                orderAmountWantLeft = order.getAmountWant();
            } else {
                orderAmountWantLeft = orderAmountHaveLeft.multiply(orderPrice).setScale(haveAssetScale, RoundingMode.HALF_DOWN);
            }

            compareLeft = orderAmountWantLeft.compareTo(thisAmountHaveLeft);
            if (compareLeft <= 0) {

                // У позиции меньше чем нам надо - берем все данные с позиции
                tradeAmountForHave = orderAmountHaveLeft;
                tradeAmountForWant = orderAmountWantLeft;

                if (compareLeft == 0)
                    completedOrder = true;

            } else {

                tradeAmountForWant = thisAmountHaveLeft;

                if (debug) {
                    debug = true;
                }

                if (compare == 0) {
                    // цена совпала (возможно с округлением) то без пересчета берем что раньше посчитали
                    tradeAmountForHave = orderThis.getAmountWantLeft();

                } else {

                    // RESOLVE amount with SCALE
                    // тут округляем наоборот вверх - больше даем тому кто активный
                    tradeAmountForHave = tradeAmountForWant.multiply(orderReversePrice).setScale(wantAssetScale, RoundingMode.HALF_DOWN);
                    if (tradeAmountForHave.compareTo(orderAmountHaveLeft) >= 0) {
                        // если вылазим после округления за предел то берем что есть
                        tradeAmountForHave = orderAmountHaveLeft;

                    } else {

                        if (debug) {
                            debug = true;
                        }

                        // если исполненый ордер станет не исполняемым то попробуем его тут обработать особо
                        willUnResolvedFor = order.willUnResolvedFor(tradeAmountForHave);
                        if (willUnResolvedFor) {
                            BigDecimal priceUpdateTrade = Order.calcPrice(orderAmountHaveLeft,
                                    // orderThis.haveSacel for order.WANT
                                    tradeAmountForWant, haveAssetScale);
                            // если цена текущей сделки не сильно изменится
                            // или если остаток у ордера стенки уже очень маленький по сравнению с текущей сделкой
                            // то весь ордер в сделку сольем
                            if (Order.isPricesClose(orderPrice, priceUpdateTrade, true)
                                    || orderAmountHaveLeft.subtract(tradeAmountForHave)
                                    .divide(orderAmountHaveLeft,
                                            BlockChain.TRADE_PRICE_DIFF_LIMIT.scale(),
                                            RoundingMode.DOWN) // FOR compare! --RoundingMode.HALF_DOWN)
                                    .compareTo(BlockChain.TRADE_PRICE_DIFF_LIMIT) < 0) {

                                tradeAmountForHave = orderAmountHaveLeft;

                            }

                            // проверим еще раз может вылезло за рамки
                            if (tradeAmountForHave.compareTo(orderAmountHaveLeft) > 0) {
                                // если вылазим после округления за предел то берем что есть
                                tradeAmountForHave = orderAmountHaveLeft;
                            }
                        }
                    }
                }

                //THIS is COMPLETED
                completedOrder = true;

            }

            if (tradeAmountForHave.compareTo(BigDecimal.ZERO) <= 0
                    || tradeAmountForWant.compareTo(BigDecimal.ZERO) <= 0) {
                debug = true;
                logger.error("Order is EMPTY: " + orderREF);
                Long error = null;
                error++;
            }

            //CHECK IF AMOUNT AFTER ROUNDING IS NOT ZERO
            //AND WE CAN BUY ANYTHING
            if (tradeAmountForHave.compareTo(BigDecimal.ZERO) > 0) {
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

                trade = new Trade(orderThis.getId(), order.getId(), haveAssetKey, wantAssetKey,
                        tradeAmountForHave, tradeAmountForWant,
                        haveAssetScale, wantAssetScale, index);

                //ADD TRADE TO DATABASE
                tradesMap.put(trade);

                /// так как у нас Индексы высчитываются по плавающей цене для остатков и она сейчас изменится
                /// то сперва удалим Ордер - до изменения Остатков и цены по Остаткам
                /// тогда можно ключи делать по цене на Остатки
                //REMOVE FROM ORDERS
                ordersMap.delete(order);

                //UPDATE FULFILLED HAVE
                order.setFulfilledHave(order.getFulfilledHave().add(tradeAmountForHave)); // orderThis.amountHave));
                // accounting on PLEDGE position
                order.getCreator().changeBalance(orderThis.dcSet, true,
                        true, wantAssetKey, tradeAmountForHave, false, false,
                        true
                );


                orderThis.setFulfilledHave(orderThis.getFulfilledHave().add(tradeAmountForWant)); //orderThis.amountWant));

                if (order.isFulfilled()) {
                    //ADD TO COMPLETED ORDERS
                    completedMap.put(order);
                } else {
                    //UPDATE ORDER
                    if (willUnResolvedFor) {
                        // if left not enough for 1 buy by price this order
                        order.dcSet = orderThis.dcSet;
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
                    AssetCls assetWant = ((CreateOrderTransaction) transaction).getWantAsset();
                    AssetCls.processTrade(orderThis.dcSet, block, order.getCreator(),
                            false, assetWant,
                            ((CreateOrderTransaction) transaction).getHaveAsset(),
                            false, tradeAmountForWant, transaction.getTimestamp(), order.getId());

                } else {
                    order.getCreator().changeBalance(orderThis.dcSet, false, false, haveAssetKey,
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

                if (completedOrder)
                    break;

                // возможно схлопнулся?
                if (orderThis.isFulfilled()) {
                    completedOrder = true;
                    break;
                }

                // if can't trade by more good price than self - by orderPrice - then  auto cancel!
                if (orderThis.isUnResolved()) {

                    if (debug) {
                        debug = orderThis.isUnResolved();
                    }

                    completedOrder = true;
                    // REVERT not completed AMOUNT
                    orderThis.processOnUnresolved(block, transaction, false);
                    break;
                }

            }
        }

        if (debug) {
            debug = true;
        }

        if (!completedOrder) {
            ordersMap.put(orderThis);
        } else {
            completedMap.put(orderThis);
        }

        //TRANSFER FUNDS
        if (processedAmountFulfilledWant.signum() > 0) {
            if (height > BlockChain.VERS_5_3) {
                AssetCls.processTrade(orderThis.dcSet, block, creator,
                        true, ((CreateOrderTransaction) transaction).getHaveAsset(),
                        ((CreateOrderTransaction) transaction).getWantAsset(),
                        false, processedAmountFulfilledWant, transaction.getTimestamp(), id);
            } else {
                creator.changeBalance(orderThis.dcSet, false, false, wantAssetKey,
                        processedAmountFulfilledWant, false, false, false);
                transaction.addCalculated(block, creator, wantAssetKey, processedAmountFulfilledWant,
                        "Resolve Order @" + Transaction.viewDBRef(id));
            }
        }

        // с ордера сколько было продано моего актива? на это число уменьшаем залог
        thisAmountHaveLeftStart = thisAmountHaveLeftStart.subtract(orderThis.getAmountHaveLeft());
        if (thisAmountHaveLeftStart.signum() > 0) {
            // change PLEDGE
            creator.changeBalance(orderThis.dcSet, true, true, haveAssetKey,
                    thisAmountHaveLeftStart, false, false, true);
        }

    }

}
