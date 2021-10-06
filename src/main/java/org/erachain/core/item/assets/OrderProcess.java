package org.erachain.core.item.assets;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.transaction.CancelOrderTransaction;
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
        long wantAssetKey = orderThis.getWantAssetKey();
        int haveAssetScale = orderThis.getHaveAssetScale();
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
        BigDecimal price = orderThis.calcLeftPrice();
        BigDecimal thisPriceReverse = orderThis.calcLeftPriceReverse();
        BigDecimal thisPriceReverseShifted = thisPriceReverse.multiply(BlockChain.COMPARE_TRADE_DEVIATION);

        Account creator = orderThis.getCreator();


        CompletedOrderMap completedMap = dcSet.getCompletedOrderMap();
        OrderMap ordersMap = dcSet.getOrderMap();
        TradeMap tradesMap = dcSet.getTradeMap();

        boolean debug = false;

        //GET ALL ORDERS(WANT, HAVE) LOWEST PRICE FIRST
        //TRY AND COMPLETE ORDERS
        List<Order> orders = ordersMap.getOrdersForTradeWithFork(wantAssetKey, haveAssetKey, thisPriceReverse);

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

        Order target;
        while (!completedThisOrder && index < orders.size()) {
            //GET ORDER
            if (dcSet.inMemory()) {
                // так как это все в памяти расположено то нужно создать новый объект
                // иначе везде будет ссылка на один и тот же объект и
                // при переходе на MAIN базу возьмется уже обновленный ордер из памяти с уже пересчитанными остатками
                target = orders.get(index).copy();
            } else {
                target = orders.get(index);
            }

            index++;

            String orderREF = Transaction.viewDBRef(target.getId());
            if (debug
                //|| orderREF.equals("40046-1")
            ) {
                debug = true;
            }

            BigDecimal orderAmountHaveLeft;
            BigDecimal orderAmountWantLeft;

            // REVERSE
            ////////// по остаткам цену берем!
            BigDecimal orderReversePrice = target.calcLeftPriceReverse();
            // PRICE
            ////////// по остаткам цену берем!
            BigDecimal orderPrice = target.calcLeftPrice();

            Trade trade;
            BigDecimal tradeAmountForHave;
            BigDecimal tradeAmountForWant; // GET
            BigDecimal tradeAmountAccurate;
            BigDecimal differenceTrade;
            //BigDecimal differenceTradeThis;

            /////////////// - разность точности цены из-за того что у одного ордера значение больше на порядки и этот порядок в точность уходит
            //CHECK IF BUYING PRICE IS HIGHER OR EQUAL THEN OUR SELLING PRICE
            //////// old compare = thisPrice.compareTo(orderReversePrice);
            //compare = orderPrice.compareTo(thisPriceReverse);
            compare = orderPrice.compareTo(thisPriceReverseShifted);
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
            orderAmountHaveLeft = target.getAmountHaveLeft();
            if (target.getFulfilledHave().signum() == 0) {
                orderAmountWantLeft = target.getAmountWant();
            } else {
                orderAmountWantLeft = target.getAmountWantLeft();
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
                if (orderThis.willUnResolvedFor(orderAmountWantLeft, BlockChain.MAX_ORDER_DEVIATION_LOW)
                        // и отклонение будет небольшое для текущего Заказа
                        && !Order.isPricesNotClose(
                        orderPrice,
                        Order.calcPrice(tradeAmountForHave, thisAmountHaveLeft, haveAssetScale),
                        BlockChain.MAX_ORDER_DEVIATION)
                    //&& !order.isLeftDeviationOut(thisAmountHaveLeft.multiply(orderReversePrice), BlockChain.MAX_ORDER_DEVIATION)
                ) {
                    tradeAmountForWant = thisAmountHaveLeft;
                    completedThisOrder = true;
                } else {
                    tradeAmountForWant = orderAmountWantLeft;
                }

            } else {
                // берем из нашего (OrderThis) заказа данные для сделки - он полностью будет исполнен
                tradeAmountForWant = thisAmountHaveLeft;

                if (compare == 0) {
                    // цена совпала (возможно с округлением) то без пересчета берем что раньше посчитали
                    tradeAmountForHave = orderThis.getAmountWantLeft();
                    if (tradeAmountForHave.compareTo(orderAmountHaveLeft) >= 0) {
                        // если вылазим после округления за предел то берем что есть
                        tradeAmountForHave = orderAmountHaveLeft;

                    } else {
                        // тут возможны округления и остатки неисполнимые уже у Текущего Заказа
                        // если текущий ордер станет не исполняемым, то попробуем его тут обработать особо
                        willOrderUnResolved = target.willUnResolvedFor(tradeAmountForHave, BlockChain.MAX_ORDER_DEVIATION_LOW);
                        if (willOrderUnResolved
                                // и остаток небольшой для всего Заказа
                                && !Order.isPricesNotClose(
                                orderPrice,
                                Order.calcPrice(orderAmountHaveLeft, tradeAmountForWant, haveAssetScale),
                                BlockChain.MAX_ORDER_DEVIATION)
                            // && !order.isLeftDeviationOut(tradeAmountForHave, BlockChain.MAX_ORDER_DEVIATION)
                        ) {
                            tradeAmountForHave = orderAmountHaveLeft;
                        }
                    }

                } else {
                    // цена нашего Заказ - "по рынку", значит пересчитаем Хочу по цене текущего Заказа
                    tradeAmountForHave = tradeAmountForWant.multiply(orderReversePrice).setScale(wantAssetScale, BigDecimal.ROUND_HALF_UP);

                    if (tradeAmountForHave.compareTo(orderAmountHaveLeft) >= 0) {
                        // если вылазим после округления за предел то берем что есть
                        tradeAmountForHave = orderAmountHaveLeft;

                    } else {

                        // если текущий ордер станет не исполняемым, то попробуем его тут обработать особо
                        willOrderUnResolved = target.willUnResolvedFor(tradeAmountForHave, BlockChain.MAX_ORDER_DEVIATION_LOW);
                        if (willOrderUnResolved
                                // и остаток небольшой для всего Заказа
                                && !Order.isPricesNotClose(
                                orderPrice,
                                Order.calcPrice(orderAmountHaveLeft, tradeAmountForWant, haveAssetScale),
                                BlockChain.MAX_ORDER_DEVIATION)
                            //&& !order.isLeftDeviationOut(tradeAmountForHave, BlockChain.MAX_ORDER_DEVIATION)
                        ) {
                            tradeAmountForHave = orderAmountHaveLeft;
                        }
                    }

                    // теперь обязательно пересчет обратно по цене ордера делаем - так как у нас может быть ПО РЫНКУ
                    // и цена с Имею не та если слишком чильно округлилось (для штучных товаров)
                    tradeAmountForWant = tradeAmountForHave.multiply(orderPrice).setScale(haveAssetScale, BigDecimal.ROUND_HALF_UP);
                    BigDecimal diff = thisAmountHaveLeft.subtract(tradeAmountForWant);
                    if (diff.signum() > 0 && diff.divide(thisAmountHaveLeft, Order.MAX_PRICE_ACCURACY, BigDecimal.ROUND_HALF_UP)
                            .compareTo(BlockChain.MAX_ORDER_DEVIATION_LOW) < 0
                            || tradeAmountForWant.compareTo(thisAmountHaveLeft) >= 0) {
                        // если вылазим после округления за предел то берем что есть
                        tradeAmountForWant = thisAmountHaveLeft;

                        //THIS is COMPLETED
                        completedThisOrder = true;
                    } else {
                        // возможно что у нашего ордера уже ничего не остается почти и он станет неисполняемым
                        if (orderThis.willUnResolvedFor(tradeAmountForWant, BlockChain.MAX_ORDER_DEVIATION_LOW)
                                // и такая сделка сильно ухудшит цену текущего Заказа
                                && !Order.isPricesNotClose(
                                orderPrice,
                                Order.calcPrice(tradeAmountForHave, thisAmountHaveLeft, haveAssetScale),
                                BlockChain.MAX_ORDER_DEVIATION)
                            //&& !order.isLeftDeviationOut(thisAmountHaveLeft.multiply(orderReversePrice), BlockChain.MAX_ORDER_DEVIATION)
                        ) {
                            tradeAmountForWant = thisAmountHaveLeft;
                            completedThisOrder = true;
                        }
                    }
                }
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

            trade = new Trade(id, target.getId(), haveAssetKey, wantAssetKey,
                    tradeAmountForHave, tradeAmountForWant,
                    haveAssetScale, wantAssetScale, index);

            if (BlockChain.CHECK_BUGS > 7) {
                boolean testDeviation = orderPrice.subtract(trade.calcPrice()).abs().divide(orderPrice, Order.MAX_PRICE_ACCURACY, BigDecimal.ROUND_HALF_UP)
                        .compareTo(BlockChain.MAX_TRADE_DEVIATION_HI) > 0;
                if (testDeviation) {
                    logger.error("TRADE Deviation so big: " + orderPrice.subtract(trade.calcPrice()).abs()
                            .divide(orderPrice, Order.MAX_PRICE_ACCURACY, BigDecimal.ROUND_HALF_UP).toPlainString());
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
            ordersMap.delete(target);

            //UPDATE FULFILLED HAVE
            target.fulfill(tradeAmountForHave); // amountHave));
            // accounting on PLEDGE position
            target.getCreator().changeBalance(dcSet, true,
                    true, wantAssetKey, tradeAmountForHave, false, false,
                    true
            );


            orderThis.fulfill(tradeAmountForWant); //amountWant));

            if (target.isFulfilled()) {
                //ADD TO COMPLETED ORDERS
                completedMap.put(target);
            } else {
                //UPDATE ORDER
                if (willOrderUnResolved) {
                    // if left not enough for 1 buy by price this order
                    target.dcSet = dcSet;
                    target.processOnUnresolved(block, transaction, true);

                    //ADD TO COMPLETED ORDERS
                    completedMap.put(target);
                } else {
                    // тут цена по остаткам поменяется
                    ordersMap.put(target);
                }
            }

            //TRANSFER FUNDS
            AssetCls.processTrade(dcSet, block, target.getCreator(),
                    false, assetWant, assetHave,
                    false, tradeAmountForWant, transaction.getTimestamp(), target.getId());


            // Учтем что у стороны ордера обновилась форжинговая информация
            if (haveAssetKey == Transaction.RIGHTS_KEY && block != null) {
                block.addForgingInfoUpdate(target.getCreator());
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
            //////// если наш Заказ "ПО РЫНКУ"?
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

            // обработка уникальных только тут - тут все удалится для обоих сторон
            // это только если наш Ордер исполнен надо делать
            if (height > BlockChain.AUTO_CANCEL_ORDERS_FROM && (assetHave.isUnique() || assetWant.isUnique())) {
                // cancel all other orders
                // GET anew all orders - without break by price
                index = 0;
                if (assetWant.isUnique()) {
                    orders = dcSet.getOrderMap().getOrdersForTradeWithFork(haveAssetKey, wantAssetKey, null);
                } else {
                    orders = dcSet.getOrderMap().getOrdersForTradeWithFork(wantAssetKey, haveAssetKey, null);
                }

                // инициатор всегда наш Ордер - для быстрого поиска при откате
                // и для корректного отражения по дате и порядка в списке Сделок
                long cancelInitiatorID = orderThis.getId();

                while (index < orders.size()) {
                    //GET ORDER
                    if (dcSet.inMemory()) {
                        // так как это все в памяти расположено то нужно создать новый объект
                        // иначе везде будет ссылка на один и тот же объект и
                        // при переходе на MAIN базу возьмется уже обновленный ордер из памяти с уже пересчитанными остатками
                        target = orders.get(index).copy();
                    } else {
                        target = orders.get(index);
                    }

                    index++;

                    // удалим
                    ordersMap.delete(target);
                    // делаем его как отмененный - чтобы новый ордер создать
                    completedMap.put(target);

                    // запомним для отчета что произошло удаление и для отката
                    Trade trade = new Trade(Trade.TYPE_CANCEL_BY_ORDER,
                            cancelInitiatorID, // номер инициатора
                            target.getId(), // номер кого отменяем
                            target.getHaveAssetKey(), target.getWantAssetKey(),
                            target.getAmountHave(), target.getAmountWant(), // for price
                            target.getHaveAssetScale(), target.getWantAssetScale(),
                            // уже 1-й сработал = тут + 1 все другие чтобы вторичный индекс pairKeyMap на затерся
                            // см. dbs.mapDB.TradeSuitMapDB.pairKeyMap
                            index + 1);

                    // нужно запомнить чтобы при откате восстановить назад
                    dcSet.getTradeMap().put(trade);

                    //UPDATE BALANCE OF CREATOR
                    BigDecimal left = target.getAmountHaveLeft();
                    target.getCreator().changeBalance(dcSet, false, false, target.getHaveAssetKey(), left,
                            false, false,
                            // accounting on PLEDGE position
                            true, Account.BALANCE_POS_PLEDGE);
                    transaction.addCalculated(block, target.getCreator(), target.getHaveAssetKey(), left,
                            "Cancel By Order @" + Transaction.viewDBRef(cancelInitiatorID));

                }
            }

        } else {
            ordersMap.put(orderThis);
        }

        //TRANSFER FUNDS
        if (processedAmountFulfilledWant.signum() > 0) {
            AssetCls.processTrade(dcSet, block, creator,
                    true, assetHave, assetWant,
                    false, processedAmountFulfilledWant, transaction.getTimestamp(), id);
        }

        // с ордера сколько было продано моего актива? на это число уменьшаем залог
        thisAmountHaveLeftStart = thisAmountHaveLeftStart.subtract(orderThis.getAmountHaveLeft());
        if (thisAmountHaveLeftStart.signum() > 0) {
            // change PLEDGE
            creator.changeBalance(dcSet, true, true, haveAssetKey,
                    thisAmountHaveLeftStart, false, false, true);
        }

    }

    /**
     * @param dcSet
     * @param id
     * @param block
     * @param blockTime
     * @return Заказ перед откатом - чтоббы знать сколько у нго было исполнения
     */
    public static Order orphan(DCSet dcSet, Long id, Block block, long blockTime) {

        CompletedOrderMap completedMap = dcSet.getCompletedOrderMap();
        OrderMap ordersMap = dcSet.getOrderMap();
        TradeMap tradesMap = dcSet.getTradeMap();

        //REMOVE FROM COMPLETED ORDERS - он может быть был отменен, поэтому нельзя проверять по Fulfilled
        // - на всякий случай удалим его в любом случае
        //// тут нужно получить остатки все из текущего состояния иначе индексы по измененной цене с остатков не удалятся
        /// поэтому смотрим что есть в таблице и если есть то его грузим с ценой по остаткам той что в базе
        // Этот ордер передадим на верх БЕЗ ИЗМЕНЕНИЯ ТУТ - для восстановления остатков
        Order orderThis;
        // сначала с малой базе быстрый поиск
        if (ordersMap.contains(id)) {
            orderThis = ordersMap.remove(id);
        } else {
            orderThis = completedMap.remove(id);
            if (orderThis.isCanceled()
                    // возможно СТАТУС станет в будущем как Закрыт а не Отменен, тогда эта проверка сработает:
                    || orderThis.getAmountHaveLeft().signum() > 0) {
                // это значит что ордер был автоматически закрыт by Unresolved / outPrice
                // назад вернем этот остаток
                orderThis.getCreator().changeBalance(dcSet, true, false, orderThis.getHaveAssetKey(),
                        orderThis.getAmountHaveLeft(), false, false,
                        true, Account.BALANCE_POS_PLEDGE);
            }
        }

        long haveAssetKey = orderThis.getHaveAssetKey();
        long wantAssetKey = orderThis.getWantAssetKey();

        Account creator = orderThis.getCreator();

        // GET HEIGHT from ID
        int height = (int) (id >> 32);

        // for TEST
        if (BlockChain.CHECK_BUGS > 3
            // && Transaction.viewDBRef(id).equals("776446-1")
        ) {
            boolean debug = false;
        }


        BigDecimal thisAmountFulfilledWant = BigDecimal.ZERO;

        AssetCls assetHave = dcSet.getItemAssetMap().get(haveAssetKey);
        AssetCls assetWant = dcSet.getItemAssetMap().get(wantAssetKey);

        //ORPHAN TRADES
        try (IteratorCloseable<Fun.Tuple2<Long, Long>> iterator = tradesMap.getIteratorByInitiator(id)) {

            Order target;
            while (iterator.hasNext()) {

                final Trade trade = tradesMap.get(iterator.next());
                switch (trade.getType()) {
                    case Trade.TYPE_TRADE: {
                        // сначала с малой базе быстрый поиск
                        if (ordersMap.contains(trade.getTarget())) {
                            //// Пока не изменились Остатки и цена по Остатки не съехала, удалим из таблицы ордеров
                            /// иначе вторичный ключ останется так как он не будет найден из-за измененной "цены по остаткам"
                            target = ordersMap.remove(trade.getTarget());
                        } else {
                            //DELETE FROM COMPLETED ORDERS- он может быть был отменен, поэтому нельзя проверять по Fulfilled
                            // - на всякий случай удалим его в любом случае
                            target = completedMap.remove(trade.getTarget());
                            if (target.isCanceled()
                                    // возможно СТАТУС станет в будущем как Закрыт а не Отменен, тогда эта проверка сработает:
                                    || target.getAmountHaveLeft().signum() > 0) {
                                // это значит что ордер быо автоматически закрыт by Unresolved / outPrice
                                // назад вернем
                                target.getCreator().changeBalance(dcSet, true, false, wantAssetKey,
                                        target.getAmountHaveLeft(), false, false,
                                        true, Account.BALANCE_POS_PLEDGE);
                            }
                        }

                        //REVERSE FUNDS
                        BigDecimal tradeAmountHave = trade.getAmountHave();
                        BigDecimal tradeAmountWant = trade.getAmountWant();

                        //REVERSE FULFILLED
                        target.fulfill(tradeAmountHave.negate());
                        // accounting on PLEDGE position
                        target.getCreator().changeBalance(dcSet, false,
                                true, wantAssetKey, tradeAmountHave, false, false,
                                true
                        );

                        // REVERSE THIS ORDER
                        thisAmountFulfilledWant = thisAmountFulfilledWant.add(tradeAmountHave);

                        AssetCls.processTrade(dcSet, block, target.getCreator(),
                                false, assetWant, assetHave,
                                true, tradeAmountWant,
                                blockTime,
                                0L);

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
                        continue;
                    }
                    case Trade.TYPE_CANCEL_BY_ORDER: {
                        // сделку ищем по ордеру и своему дбРЕФ
                        // чтобы восстановить старую цену
                        dcSet.getTradeMap().delete(trade);

                        // удалим из исполненных
                        target = completedMap.remove(trade.getTarget());
                        ordersMap.put(target.getId(), target);

                        //REMOVE BALANCE OF CREATOR
                        target.getCreator().changeBalance(dcSet, true, false, target.getHaveAssetKey(),
                                target.getAmountHaveLeft(), false, false,
                                // accounting on PLEDGE position
                                true, Account.BALANCE_POS_PLEDGE);

                        continue;
                    }
                }

            }
        } catch (IOException e) {
        }

        if (orderThis.getFulfilledHave().signum() > 0) {
            // change PLEDGE
            creator.changeBalance(dcSet, false, true, haveAssetKey,
                    orderThis.getFulfilledHave(), false, false, true);
        }

        //REVERT WANT
        AssetCls.processTrade(dcSet, block, creator,
                true, assetHave, assetWant,
                true, thisAmountFulfilledWant, blockTime, 0L);

        return orderThis;
    }

    public static void clearOldOrders(DCSet dcSet, Block block, boolean asOrphan) {

        int height = block.getHeight();
        if (height < BlockChain.CLEAR_OLD_ORDERS_HEIGHT || height % 100 != 0)
            return;

        long blockTx_id = Transaction.makeDBRef(height, 0);
        CompletedOrderMap completedMap = dcSet.getCompletedOrderMap();
        OrderMap ordersMap = dcSet.getOrderMap();
        TradeMap tradesMap = dcSet.getTradeMap();

        long blockTx_id_end = Transaction.makeDBRef(height - BlockChain.CLEAR_OLD_ORDERS_PERIOD, 0);

        if (asOrphan) {
            Trade trade;
            try (IteratorCloseable<Fun.Tuple2<Long, Long>> iterator = tradesMap.getIteratorByInitiator(blockTx_id)) {
                while (iterator.hasNext()) {
                    trade = tradesMap.remove(iterator.next());
                    CancelOrderTransaction.orphanBody(dcSet, blockTx_id, trade.getTarget(), false);
                }
            } catch (IOException e) {
            }

        } else {
            try (IteratorCloseable<Long> iterator = ordersMap.getIterator()) {
                Long orderID;
                while (iterator.hasNext()) {
                    orderID = iterator.next();
                    if (orderID > blockTx_id_end)
                        break;

                    CancelOrderTransaction.processBody(dcSet, blockTx_id, orderID, block, false);
                }
            } catch (IOException e) {
            }

        }

    }
}
