package org.erachain.dapp.epoch;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.HasDataString;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransferredBalances;
import org.erachain.core.transaction.dto.TransferBalanceDto;
import org.erachain.core.transaction.dto.TransferRecipientDto;
import org.erachain.dapp.DApp;
import org.erachain.dapp.DAppFactory;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.erachain.core.account.Account.BALANCE_POS_OWN;

/**
 * Это простой стейкинг для активов. Проценты начисляются как плательщику в момент передачи актива в собственность
 * {"id":1012, "%":"7.5"} {"dApp":1012, "%":"7.5"}
 * По умолчанию 10%
 */
public class MoneyStaking extends EpochDAppItemJson {

    static Logger LOGGER = LoggerFactory.getLogger(MoneyStaking.class.getSimpleName());

    static public final int ID = 1012;
    static public final String NAME = "Smart Staking";
    static public final boolean DISABLED = false;

    // Формальный счёт админа - он не имеет никакой силы и не исполняет команды
    final public static PublicKeyAccount MAKER = PublicKeyAccount.makeForDApp(crypto.digest(Longs.toByteArray(ID)));

    /**
     * Число блоков между последней обработкой и текущей - чтобы часто не пересчитывать и не спамить
     */
    static final int SKIP_SECONDS = BlockChain.TEST_MODE ? BlockChain.DEMO_MODE ? 30 : 5 : 86400;

    /**
     * Коэффициент на 1 секунду для 1% в банковский год (360 дней)
     */
    static final BigDecimal STAKE_PERIOD_MULTI = BigDecimal.ONE.divide(new BigDecimal(360L * 86400L * 100L), 10, RoundingMode.HALF_DOWN);

    /**
     * Постоянный множитель доходности annualPercentage - Reward multiplier as a Percentage per year. По умолчанию 10% годовых
     * Пример: {"id":1012, "%":"2.5"} {"dApp":1012, "%":"7.5"}
     */
    static final String STAKE_MULTI_KEY = "%";

    BlockChain blockChain = Controller.getInstance().getBlockChain();

    public MoneyStaking() {
        super(ID, MAKER);
    }

    private MoneyStaking(int itemType, long itemKey, String itemDescription, String dataStr, String status) {
        super(ID, MAKER, itemType, itemKey, itemDescription, dataStr, status);
    }

    private MoneyStaking(ItemCls item, String itemDescription, JSONObject itemPars, Transaction commandTx, Block block) {
        super(ID, MAKER, item, itemDescription, itemPars, commandTx, block);
    }

    @Override
    public DApp of(String itemDescription, JSONObject jsonObject, ItemCls item, Transaction commandTx, Block block) {
        if (commandTx instanceof TransferredBalances) {
            if (commandTx instanceof HasDataString) {
                String dataStr = ((HasDataString) commandTx).getDataString();
                // TODO это в других контрактах можно использовать еще команды из самой транзакции
            }
            return new MoneyStaking(item, itemDescription, jsonObject, commandTx, block);
        }

        return new ErrorDApp("Wrong Transaction type: need 'TransferredBalances' - transfers of asset not found");
    }

    public static void setDAppFactory() {
        MoneyStaking instance = new MoneyStaking();
        DAppFactory.STOCKS.put(MAKER, instance);
        DAppFactory.DAPP_BY_ID.put(ID, instance);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isDisabled() {
        return DISABLED;
    }

    /**
     * Берем множитель для награды
     *
     * @return
     */
    protected BigDecimal stakeMulti() {
        // Если задан постоянный множитель - в параметрах у Актива
        if (itemPars.containsKey(STAKE_MULTI_KEY))
            return new BigDecimal(itemPars.get(STAKE_MULTI_KEY).toString());
        return BigDecimal.TEN;

    }

    BigDecimal getReward(TransferBalanceDto transfer, Transaction commandTx) {
        return null;
    }

    /**
     * @param stateSubPoints содердит точки состония для данного счета или пару балансов для данного актива - для откатов
     * @param account
     * @param assetKey
     * @param asset
     * @param asSender
     * @param sideAccount
     */
    // TODO нужно начисление и снятие стаскивать с Владельца - и если не хватает тут то передавать на получателя демередж
    // а если актив конечный - то надо общий баланс у владельца пересчитывать
    // короче надо делать транзакцию со счета владельца - тогда учет будет автоматом
    void makePointTransfer(List<Object[]> stateSubPoints, Account account, Long assetKey, AssetCls asset,
                           boolean asSender, Account sideAccount) {

        if (asset.getItemType() != itemType || asset.getKey() != itemKey)
            return;

        PublicKeyAccount assetMaker = asset.getMaker();
        if (account.equals(assetMaker)) {
            status += " Ignore " + (asSender ? " sender " : " recipient ") + account.getAddress() + " as Owner.";
            return;
        }

        BigDecimal balanceOwnNew = account.getBalanceForPosition(asset.getKey(), BALANCE_POS_OWN).b;

        Object[] point = (Object[]) valueGet(account.getAddress());
        // NULL тоже заносим для стирания при откате. И баланс старый
        stateSubPoints.add(new Object[]{account.getAddress(), point});

        // Накопленный, время, Депозит
        Object[] pointNew = new Object[]{BigDecimal.ZERO, blockTimestamp, balanceOwnNew};
        try {
            if (point == null) {
                // него считать еще, просто запомним ниже новую точку
                return;
            }

            if (blockTimestamp.equals(point[1])) {
                // блок тот же что был, просто запомним ниже новую точку - ничего не считаем
                pointNew[0] = point[0];
                return;
            }

            // В точке находится предыдущий баланс (всего и остаток в позиции ИМЕЮ)
            BigDecimal stake = (BigDecimal) point[2];
            int stakeSignum = stake.signum();
            if (stakeSignum == 0) {
                return;
            }

            // Был какой-то баланс в предыдущей точке - начинаем считать
            BigDecimal multi = stakeMulti();
            int multiSignum = multi.signum();
            // расчет новой ожидающей награды у получателя и обновление даты для нового начала отсчета потом
            BigDecimal pendingReward = (BigDecimal) point[0];
            Long pendingRewardTimestamp = (Long) point[1];
            BigDecimal reward = stake.multiply(new BigDecimal((blockTimestamp - pendingRewardTimestamp) / 1000)).multiply(multi)
                    .multiply(STAKE_PERIOD_MULTI);
            reward = reward.setScale(asset.getScale() + 3,
                    // так как добавили к точности 3, то полное округление без HALF_
                    multiSignum > 0 ? RoundingMode.DOWN : RoundingMode.UP);

            BigDecimal pendingRewardNew = pendingReward.add(reward);
            pointNew = new Object[]{pendingRewardNew, blockTimestamp, balanceOwnNew};

            // округлим теперь до точности актива
            reward = pendingRewardNew.setScale(asset.getScale(),
                    multiSignum > 0 ? RoundingMode.DOWN : RoundingMode.UP);
            int rewardSignum = reward.signum();
            if (rewardSignum == 0) {
                return;
            }

            BigDecimal rewardAbs;
            if (multiSignum < 0) {
                // снимем знак минуса - чтобы не влиял на Позиции Баланса
                rewardAbs = reward.negate(); // ниже используется
                // это Демерредж - надо проверить на остаток
                if (balanceOwnNew.compareTo(rewardAbs) <= 0) {
                    // сделаем перенос Демерреджа на получателя
                    if (sideAccount == null) {
                        // Поидее таких ситуаций не должно быть
                        LOGGER.warn("Recipients is Null for demerrage sender with ZERO balance - seqNo: {} demerrage: {}",
                                commandTx.viewHeightSeq(), rewardAbs.toPlainString());
                        status += " Recipients is Null for demerrage sender with ZERO balance.";
                        /// тогда продолжим расчеты ниже как обычно - без выхода тут

                    } else {
                        BigDecimal diff = balanceOwnNew.add(reward);
                        if (diff.signum() < 0) {
                            // Перенос остатков с минусом которые на счет получателя платежа
                            Object[] pointSideAccount = (Object[]) valueGet(sideAccount.getAddress());
                            // Для Отката их запомним
                            stateSubPoints.add(new Object[]{sideAccount.getAddress(), pointSideAccount});
                            status += "transfer demerrage to aSide: " + diff.abs().toString();
                            Object[] pointSideAccountNew;
                            if (pointSideAccount == null) {
                                pointSideAccountNew = new Object[]{diff, pointNew[1], BigDecimal.ZERO};
                            } else {
                                pointSideAccountNew = new Object[]{((BigDecimal) pointSideAccount[0]).add(diff), pointSideAccount[1], pointSideAccount[2]};
                            }
                            valuePut(sideAccount.getAddress(), pointSideAccountNew);
                        }

                        pointNew[0] = BigDecimal.ZERO;

                        // Сперва запомним балансы для отката
                        Fun.Tuple2<BigDecimal, BigDecimal> assetMakerBalOwn = assetMaker.getBalanceForPosition(assetKey, BALANCE_POS_OWN);
                        Fun.Tuple2<BigDecimal, BigDecimal> accountBalOwn = account.getBalanceForPosition(assetKey, BALANCE_POS_OWN);
                        stateSubPoints.add(new Object[]{assetKey,
                                assetMaker.getShortAddressBytes(), new Object[]{assetMakerBalOwn.a, assetMakerBalOwn.b},
                                account.getShortAddressBytes(), new Object[]{accountBalOwn.a, accountBalOwn.b}});

                        // теперь снимем со счета отправителя все что у него осталось
                        transfer(dcSet, block, commandTx, assetMaker, account, balanceOwnNew, assetKey, true, null, "Smart Stake demurrage");
                        status += (asSender ? " Sender " : " Recipient ") + "demurrage " + balanceOwnNew.toPlainString() + ".";
                        return; // выход - так как остатки уже перенесли
                    }
                }
            } else {
                rewardAbs = reward; // ниже используется
                // это Доход - надо проверить на перевод всей суммы - если ДА то доход тоже на получателя скинем
                if (balanceOwnNew.compareTo(BigDecimal.ZERO) == 0) {
                    // надо передать получателю весь доход
                    if (sideAccount == null) {
                        // Поидее таких ситуаций не должно быть
                        LOGGER.warn("Recipients is Null for reward sender with ZERO balance - seqNo: {} reward: {}",
                                commandTx.viewHeightSeq(), reward.toPlainString());
                        status += " Recipients is Null for reward sender with ZERO balance.";
                        /// тогда продолжим расчеты ниже как обычно - без выхода тут

                    } else {
                        // Перенос всего Дохода на счет получателя платежа
                        Object[] pointSideAccount = (Object[]) valueGet(sideAccount.getAddress());
                        // Для Отката их запомним
                        stateSubPoints.add(new Object[]{sideAccount.getAddress(), pointSideAccount});
                        status += "transfer reward to aSide: " + pointNew[0].toString();
                        Object[] pointSideAccountNew;
                        if (pointSideAccount == null) {
                            pointSideAccountNew = new Object[]{pointNew[0], pointNew[1], BigDecimal.ZERO};
                        } else {
                            pointSideAccountNew = new Object[]{((BigDecimal) pointSideAccount[0]).add((BigDecimal) pointNew[0]), pointSideAccount[1], pointSideAccount[2]};
                        }
                        valuePut(sideAccount.getAddress(), pointSideAccountNew);
                        pointNew[0] = BigDecimal.ZERO;
                        pointNew[2] = BigDecimal.ZERO;
                        return; // выход - так как остатки уже перенесли
                    }
                }
            }

            ////////////////////// Сюда дошло - обычная передача актива без крайних состояний баланса

            long lastTimeAction = (Long) point[1];
            if ((blockTimestamp - lastTimeAction) / 1000 >= SKIP_SECONDS) {
                // Выплату сделать
                // Сперва запомним балансы для отката
                Fun.Tuple2<BigDecimal, BigDecimal> assetMakerBalOwn = assetMaker.getBalanceForPosition(assetKey, BALANCE_POS_OWN);
                Fun.Tuple2<BigDecimal, BigDecimal> accountBalOwn = account.getBalanceForPosition(assetKey, BALANCE_POS_OWN);
                stateSubPoints.add(new Object[]{assetKey,
                        assetMaker.getShortAddressBytes(), new Object[]{assetMakerBalOwn.a, assetMakerBalOwn.b},
                        account.getShortAddressBytes(), new Object[]{accountBalOwn.a, accountBalOwn.b}});

                if (multiSignum > 0) {
                    // прибавим
                    transfer(dcSet, block, commandTx, assetMaker, account, rewardAbs, assetKey, false, null, "Smart Stake reward");
                    // запишем в сообщение
                    status += (asSender ? " Sender " : " Recipient ") + "reward " + rewardAbs.toPlainString() + ".";
                } else {
                    // вычтем - демерредж с отрицательным %%
                    transfer(dcSet, block, commandTx, assetMaker, account, rewardAbs, assetKey, true, null, "Smart Stake demurrage");
                    // запишем в сообщение
                    status += (asSender ? " Sender " : " Recipient ") + "demurrage " + rewardAbs.toPlainString() + ".";
                }

                // reset pending reward
                pointNew[0] = BigDecimal.ZERO;
                status += " Withdraw.";
            } else {
                status += " Pending.";
            }

            return;

        } finally {
            // STORE NEW POINT
            valuePut(account.getAddress(), pointNew);
        }
    }

    ///////// COMMANDS
    // Может несколько платежей обработать - список списков

    /**
     * Для восстановления состояния при откатах - запоминаем готовые значения предыдущие.
     * Состояние = Счет, Точка вычисления, Баланс. Тогда очень просто восстанавливать при откате.
     * Если значение Точка или Баланс = НУЛЬ - значит удалить
     *
     * @param asOrphan
     * @return
     */
    private boolean job(boolean asOrphan) {

        if (asOrphan) {
            Object[][][] statePoints = (Object[][][]) removeState(commandTx.getDBRef());
            Object[][] stateSubPoints;
            Object[] stateSubPoint;
            ItemAssetBalanceMap map = dcSet.getAssetBalanceMap();
            Long assetKey;
            byte[] shortAddress;
            Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance;
            // Разворачиваем в обратном порядке
            for (int ii = statePoints.length - 1; ii >= 0; ii--) {
                stateSubPoints = statePoints[ii];
                // Разворачиваем в обратном порядке
                for (int kk = stateSubPoints.length - 1; kk >= 0; kk--) {
                    stateSubPoint = stateSubPoints[kk];
                    if (stateSubPoint[0] instanceof String) {
                        // это просто точка состояния для счета
                        if (stateSubPoint[1] == null)
                            valuesDelete((String) stateSubPoint[0]);
                        else
                            valuePut((String) stateSubPoint[0], stateSubPoint[1]);
                    } else {
                        // это балансы для 2-х счетов так как была выплата
                        assetKey = (Long) stateSubPoint[0];
                        shortAddress = (byte[]) stateSubPoint[1];
                        balance = map.get(shortAddress, assetKey);
                        map.put(shortAddress, assetKey,
                                Account.makeBalanceOWN(balance, (Object[]) stateSubPoint[2]));
                        shortAddress = (byte[]) stateSubPoint[3];
                        balance = map.get(shortAddress, assetKey);
                        map.put(shortAddress, assetKey,
                                Account.makeBalanceOWN(balance, (Object[]) stateSubPoint[4]));

                    }
                }
            }

        } else {

            status = "";
            TransferBalanceDto[] transfers = ((TransferredBalances) commandTx).getTransfers();
            if (transfers == null || transfers.length == 0)
                return false;

            Object[][][] statePoints = new Object[transfers.length][][];
            int i = 0;
            for (TransferBalanceDto transfer : transfers) {
                if (transfer.getPosition() != BALANCE_POS_OWN)
                    continue;

                Account sender = transfer.getSender();
                AssetCls asset = transfer.getAsset();
                Long assetKey = asset.getKey();
                TransferRecipientDto[] transferRecipients = transfer.getRecipients();

                List<Object[]> stateSubPoints = new ArrayList<>();
                makePointTransfer(stateSubPoints, sender, assetKey, asset, true,
                        transferRecipients.length == 0 ? null : transferRecipients[0].getAccount());

                for (TransferRecipientDto transferRecipient : transferRecipients) {
                    if (transferRecipient.getBalancePos() != BALANCE_POS_OWN)
                        continue;

                    makePointTransfer(stateSubPoints, transferRecipient.getAccount(), assetKey, asset, false, sender);
                }

                statePoints[i++] = (Object[][]) stateSubPoints.toArray(new Object[stateSubPoints.size()][]);
            }

            // STORE STATE for ORPHAN
            putState(commandTx.getDBRef(), statePoints);

        }

        return true;

    }

    @Override
    public boolean processByTime() {
        fail("unknown command");
        return false;
    }

    @Override
    public boolean process() {
        if (block == null) {
            // Это еще неподтвержденная - нечего исполнять или не Послать
            return true;
        }
        return job(false);
    }

    @Override
    public void orphanByTime() {
    }

    @Override
    public void orphanBody() {
        job(true);
    }

    /// PARSE / TOBYTES

    public static MoneyStaking Parse(byte[] bytes, int pos, int forDeal) {

        // skip ID
        pos += 4;

        String data;
        String status;
        String itemDesc;
        if (forDeal == Transaction.FOR_DB_RECORD) {
            byte[] statusSizeBytes = Arrays.copyOfRange(bytes, pos, pos + 4);
            int statusLen = Ints.fromByteArray(statusSizeBytes);
            pos += 4;
            byte[] statusBytes = Arrays.copyOfRange(bytes, pos, pos + statusLen);
            pos += statusLen;
            status = new String(statusBytes, StandardCharsets.UTF_8);

            byte[] dataSizeBytes = Arrays.copyOfRange(bytes, pos, pos + 4);
            int dataSize = Ints.fromByteArray(dataSizeBytes);
            pos += 4;
            byte[] dataBytes = Arrays.copyOfRange(bytes, pos, pos + dataSize);
            pos += dataSize;
            data = new String(dataBytes, StandardCharsets.UTF_8);
        } else {
            data = "";
            status = "";
        }

        byte[] itemTypeBytes = Arrays.copyOfRange(bytes, pos, pos + 2);
        int itemType = Shorts.fromByteArray(itemTypeBytes);
        pos += 2;

        byte[] itemKeyBytes = Arrays.copyOfRange(bytes, pos, pos + 8);
        long itemKey = Longs.fromByteArray(itemKeyBytes);
        pos += 8;

        if (forDeal == Transaction.FOR_DB_RECORD) {
            byte[] itemDescSizeBytes = Arrays.copyOfRange(bytes, pos, pos + 4);
            int itemDescLen = Ints.fromByteArray(itemDescSizeBytes);
            pos += 4;
            byte[] itemDescBytes = Arrays.copyOfRange(bytes, pos, pos + itemDescLen);
            pos += itemDescLen;
            itemDesc = new String(itemDescBytes, StandardCharsets.UTF_8);
        } else {
            itemDesc = "";
        }

        return new MoneyStaking(itemType, itemKey, itemDesc, data, status);
    }

}
