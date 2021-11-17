package org.erachain.core.account;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.erachain.datachain.OrderMapImpl;
import org.erachain.datachain.ReferenceMapImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.lang.Lang;
import org.erachain.utils.NumberAsString;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;
import org.mapdb.Fun.Tuple5;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;


/**
 * обработка ключей и криптографии
 */
public class Account {

    public static final int ADDRESS_SHORT_LENGTH = 20;
    public static final int ADDRESS_LENGTH = 25;

    public static final int BALANCE_POS_OWN = 1;
    public static final int BALANCE_POS_DEBT = 2;
    public static final int BALANCE_POS_HOLD = 3;
    public static final int BALANCE_POS_SPEND = 4;
    public static final int BALANCE_POS_PLEDGE = 5;
    public static final int BALANCE_POS_6 = 6;

    public static final int BALANCE_SIDE_DEBIT = 1;
    public static final int BALANCE_SIDE_LEFT = 2;
    public static final int BALANCE_SIDE_CREDIT = 3;

    public static final int FEE_BALANCE_SIDE_REFERAL_AND_GIFTS = 1; // это чисто с рефералки накапало
    public static final int FEE_BALANCE_SIDE_FORGED = 2; // всего нафоржили
    public static final int FEE_BALANCE_SIDE_TOTAL_EARNED = 3; // всего заработали - доход со всего
    public static final int FEE_BALANCE_SIDE_SPEND = 4; // всего на комиссии
    public static final int FEE_BALANCE_SIDE_DIFFERENCE = 5; // разница между всем доходом и расходами

    protected String address;
    protected byte[] bytes;
    protected byte[] shortBytes;
    // нельзя тут запминать так как при откате данные не будут очищены Tuple4<Long, Integer, Integer, Integer> personDuration;
    Tuple2<Integer, PersonCls> person;
    int viewBalancePosition = 0;

    public Account(String address) {
        this.bytes = Base58.decode(address);
        this.shortBytes = Arrays.copyOfRange(this.bytes, 1, this.bytes.length - 4);
        this.address = address;
    }

    public Account(byte[] addressBytes) {
        if (addressBytes.length == ADDRESS_SHORT_LENGTH) {
            // AS SHORT BYTES
            this.shortBytes = addressBytes;
            this.bytes = Crypto.getInstance().getAddressFromShortBytes(addressBytes);
        } else if (addressBytes.length == ADDRESS_LENGTH) {
            // AS FULL 25 byres
            this.bytes = addressBytes;
            this.shortBytes = Arrays.copyOfRange(addressBytes, 1, this.bytes.length - 4);

        } else {
            assert(addressBytes.length == ADDRESS_LENGTH);
        }

        /// make on demand this.address = Base58.encode(bytes);
    }

    public static byte[] makeShortBytes(String address) {
        return Arrays.copyOfRange(Base58.decode(address), 1, ADDRESS_LENGTH - 4);

    }
    public static Account makeAccountFromShort(byte[] addressShort) {

        String address = Crypto.getInstance().getAddressFromShort(addressShort);
        return new Account(address);
    }

    public static Account makeAccountFromShort(BigInteger addressShort) {

        String address = Crypto.getInstance().getAddressFromShort(addressShort.toByteArray());
        return new Account(address);
    }

    public static Tuple2<Account, String> tryMakeAccount(String address) {

        if (address == null || address.length() < ADDRESS_LENGTH)
            return new Tuple2<Account, String>(null, "Wrong Address or PublicKey");

        if (address.startsWith("+")) {
            if (PublicKeyAccount.isValidPublicKey(address)) {
                // MAY BE IT BASE.32 +
                return new Tuple2<Account, String>(new PublicKeyAccount(address), null);
            } else {
                return new Tuple2<Account, String>(null, "Wrong Address or PublicKey");
            }
        }

        boolean isBase58 = !Base58.isExtraSymbols(address);

        if (isBase58) {
            // ORDINARY RECIPIENT
            if (Crypto.getInstance().isValidAddress(address)) {
                return new Tuple2<Account, String>(new Account(address), null);
            } else if (PublicKeyAccount.isValidPublicKey(address)) {
                return new Tuple2<Account, String>(new PublicKeyAccount(address), null);
            } else {
                return new Tuple2<Account, String>(null, "Wrong Address or PublicKey");
            }
        } else {
            return new Tuple2<Account, String>(null, "The name is not registered");
        }

    }

    public static String balancePositionName(int position) {
        switch (position) {
            case BALANCE_POS_OWN:
                return "I Own";
            case BALANCE_POS_DEBT:
                return "I Debt";
            case BALANCE_POS_HOLD:
                return "I Hold";
            case BALANCE_POS_SPEND:
                return "I Spend";
            case BALANCE_POS_PLEDGE:
                return "I Pledge";
        }

        return null;

    }

    public static String balanceSideName(int side) {
        switch (side) {
            case BALANCE_SIDE_DEBIT:
                return "Total Debit";
            case BALANCE_SIDE_LEFT:
                return "Left # остаток";
            case BALANCE_SIDE_CREDIT:
                return "Total Credit";
        }

        return null;

    }

    public static String balanceCOMPUPositionName(int position) {
        switch (position) {
            case BALANCE_POS_OWN:
                return "I Own";
            case BALANCE_POS_DEBT:
                return "I Debt";
            case BALANCE_POS_HOLD:
                return "I Earn";
            case BALANCE_POS_SPEND:
                return "I Spend";
            case BALANCE_POS_PLEDGE:
                return "I Pledge";
        }
        return null;
    }

    public static String balanceCOMPUSideName(int statsSide) {
        switch (statsSide) {
            case FEE_BALANCE_SIDE_REFERAL_AND_GIFTS:
                return "Referal & Gift";
            case FEE_BALANCE_SIDE_TOTAL_EARNED:
                return "Total Earn";
            case FEE_BALANCE_SIDE_FORGED:
                return "Forged";
            case FEE_BALANCE_SIDE_SPEND:
                return "Spend # Потрачено";
            case FEE_BALANCE_SIDE_DIFFERENCE:
                return "Difference";
        }
        return null;

    }

    public static int balanceCOMPUStatsSide(int position, int side) {
        switch (position) {
            case BALANCE_POS_HOLD:
                switch (side) {
                    case BALANCE_SIDE_DEBIT:
                        return FEE_BALANCE_SIDE_REFERAL_AND_GIFTS;
                    case BALANCE_SIDE_LEFT:
                        return FEE_BALANCE_SIDE_TOTAL_EARNED;
                    case BALANCE_SIDE_CREDIT:
                        return FEE_BALANCE_SIDE_FORGED;
                }
            case BALANCE_POS_SPEND:
                switch (side) {
                    case BALANCE_SIDE_LEFT:
                        return FEE_BALANCE_SIDE_SPEND;
                    case BALANCE_SIDE_CREDIT:
                        return FEE_BALANCE_SIDE_DIFFERENCE;
                }
        }
        return -1;

    }

    /**
     * Make TYPE of transactionAmount by signs of KEY and AMOUNT
     *
     * @param key
     * @param amount
     * @param isBackward
     * @param isDirect   если задано то номера балансов только 4-ре по минусам - без учета сложной схемы с isBackward
     * @return
     */
    public static int balancePosition(long key, BigDecimal amount, boolean isBackward, boolean isDirect) {
        if (key == 0L || amount == null || amount.signum() == 0)
            return 0;

        int type;
        int amount_sign = amount.signum();
        if (key > 0) {
            if (amount_sign > 0) {
                // OWN SEND or PLEDGE
                type = !isDirect && isBackward ? BALANCE_POS_PLEDGE : BALANCE_POS_OWN;
            } else {
                // HOLD in STOCK or PLEDGE
                type = isDirect || isBackward ? BALANCE_POS_HOLD : BALANCE_POS_6;
            }
        } else {
            if (amount_sign > 0) {
                // give CREDIT or BORROW CREDIT
                type = BALANCE_POS_DEBT;
            } else {
                // SPEND or backward PLEDGE
                type = !isDirect && isBackward ? BALANCE_POS_6 : BALANCE_POS_SPEND;
            }
        }

        return type;

    }

    /**
     * Sign asset + sign amount
     *
     * @param balancePos
     * @return
     */
    public static Tuple2<Integer, Integer> getSignsForBalancePos(int balancePos) {

        switch (balancePos) {
            case BALANCE_POS_OWN:
            case BALANCE_POS_PLEDGE:
                return new Tuple2(1, 1);
            case BALANCE_POS_HOLD:
            case BALANCE_POS_6:
                return new Tuple2(1, -1);
            case BALANCE_POS_DEBT:
                return new Tuple2(-1, 1);
            case BALANCE_POS_SPEND:
                return new Tuple2(-1, -1);
        }

        return null;

    }

    public static String getDetailsForEncrypt(String address, long itemKey, boolean forEncrypt, boolean okAsMess) {

        if (address.isEmpty()) {
            return "";
        }

        // CHECK IF RECIPIENT IS VALID ADDRESS
        if (Crypto.getInstance().isValidAddress(address)) {
            if (forEncrypt && null == Controller.getInstance().getPublicKeyByAddress(address)) {
                return "address is unknown - can't encrypt for it, please use public key instead";
            }
            if (itemKey > 0) {
                Account account = new Account(address);
                if (account.isPerson()) {
                    return account.getPerson().b.toString() + " " + account.getBalance(itemKey).a.b.toPlainString();
                }
                return " + " + account.getBalance(itemKey).a.b.toPlainString();
            }
            return okAsMess ? "address is OK" : "";
        } else {
            // Base58 string len = 33-34 for ADDRESS and 40-44 for PubKey
            if (PublicKeyAccount.isValidPublicKey(address)) {
                if (itemKey > 0) {
                    Account account = new PublicKeyAccount(address);
                    if (account.isPerson()) {
                        return account.getPerson().b.toString() + " " + account.getBalance(itemKey).a.b.toPlainString();
                    }
                    return " + " + account.getBalance(itemKey).a.b.toPlainString();
                }
                return okAsMess ? "public key is OK" : "";
            } else {
                return "address or public key is invalid";
            }
        }

    }

    public static String getDetails(String address, long assetKey) {

        String out = "";

        if (address.isEmpty()) {
            return out;
        }

        boolean statusBad = Controller.getInstance().getStatus() != Controller.STATUS_OK;

        Account account = null;

        // CHECK IF RECIPIENT IS VALID ADDRESS
        if (Crypto.getInstance().isValidAddress(address)) {
            account = new Account(address);
        } else {
            if (PublicKeyAccount.isValidPublicKey(address)) {
                account = new PublicKeyAccount(address);
            } else {
                return (statusBad ? "??? " : "") + "ERROR";
            }
        }

        if (account.getBalanceUSE(assetKey).compareTo(BigDecimal.ZERO) == 0
                && account.getBalanceUSE(Transaction.FEE_KEY).compareTo(BigDecimal.ZERO) == 0) {
            return Lang.T("Warning!") + " " + (statusBad ? "???" : "")
                    + account.toString(assetKey);
        } else {
            return (statusBad ? "???" : "") + account.toString(assetKey);
        }

    }

    public static String getDetails(String address, AssetCls asset) {
        return getDetails(address, asset.getKey());
    }


    public static Map<byte[], BigDecimal> getKeyBalancesWithForks(DCSet dcSet, long key,
                                                                  Map<byte[], BigDecimal> values) {
        ItemAssetBalanceMap map = dcSet.getAssetBalanceMap();

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> ballance;

        // здесь нужен протокольный итератор! Берем TIMESTAMP_INDEX
        for (byte[] mapKey : map.keySet()) {
            if (ItemAssetBalanceMap.getAssetKeyFromKey(mapKey) == key) {
                ballance = map.get(mapKey);
                values.put(ItemAssetBalanceMap.getShortAccountFromKey(mapKey), ballance.a.b);
            }
        }

        DCSet dcParent = dcSet.getParent();
        if (dcParent != null) {
            values = getKeyBalancesWithForks(dcParent, key, values);
        }

        return values;

    }

    public static Map<byte[], BigDecimal> getKeyOrdersWithForks(DCSet dcSet, long key, Map<byte[], BigDecimal> values) {

        OrderMapImpl map = dcSet.getOrderMap();
        Order order;
        try (IteratorCloseable<Long> iterator = map.getIndexIterator(0, true)) {
            while (iterator.hasNext()) {
                order = map.get(iterator.next());
                if (order.getHaveAssetKey() == key) {
                    byte[] address = order.getCreator().getShortAddressBytes();
                    values.put(address, values.get(address).add(order.getAmountHave()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        DCSet dcParent = dcSet.getParent();
        if (dcParent != null) {
            values = getKeyOrdersWithForks(dcParent, key, values);
        }

        return values;

    }

    // top balance + orders values
    public static byte[] getRichWithForks(DCSet dcSet, long key) {

        Map<byte[], BigDecimal> values = new TreeMap<byte[], BigDecimal>();

        values = getKeyBalancesWithForks(dcSet, key, values);

        // add ORDER values
        values = getKeyOrdersWithForks(dcSet, key, values);

        // search richest address
        byte[] rich = null;
        BigDecimal maxValue = BigDecimal.ZERO;
        for (Map.Entry<byte[], BigDecimal> entry : values.entrySet()) {
            BigDecimal value = entry.getValue();
            if (value.compareTo(maxValue) > 0) {
                maxValue = value;
                rich = entry.getKey();
            }
        }

        return rich;

    }


    public String getAddress() {
        if (address == null) {
            this.address = Base58.encode(bytes);
        }
        return address;
    }

    public void setViewBalancePosition(int viewBalancePosition) {
        this.viewBalancePosition = viewBalancePosition;
    }

    public byte[] getAddressBytes() {
        return bytes;
    }

    public byte[] getShortAddressBytes() {
        return this.shortBytes;
    }

    // BALANCE
    public Tuple3<BigDecimal, BigDecimal, BigDecimal> getUnconfirmedBalance(long key) {
        return Controller.getInstance().getWalletUnconfirmedBalance(this, key);
    }

    public BigDecimal getBalanceUSE(long key) {
        return this.getBalanceUSE(key, DCSet.getInstance());
    }

    /**
     *
     * @param key asset key (long)
     * @param db database Set
     * @return (BigDecimal) balance.a + balance.b
     */
    public BigDecimal getBalanceUSE(long key, DCSet db) {
        if (key < 0)
            key = -key;
        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance = this
                .getBalance(db, key);

        return balance.a.b.add(balance.b.b);
    }

    /**
     * позиция баланса предустанавливается - нужно для Сравнителей - utils.AccountBalanceComparator#compare
     * @param key
     * @return
     */
    public Tuple2<BigDecimal, BigDecimal> getBalanceInSettedPosition(long key) {
        return getBalanceForPosition(DCSet.getInstance(), key, this.viewBalancePosition);
    }

    /**
     * в заданной позиции баланс взять
     *
     * @param key
     * @param action
     * @return
     */
    public Tuple2<BigDecimal, BigDecimal> getBalanceForPosition(long key, int action) {
        return getBalanceForPosition(DCSet.getInstance(), key, action);
    }

    public Tuple2<BigDecimal, BigDecimal> getBalanceForPosition(DCSet dcSet, long key, int action) {
        switch (action) {
            case BALANCE_POS_OWN:
                return this.getBalance(dcSet, key).a;
            case BALANCE_POS_DEBT:
            case TransactionAmount.ACTION_REPAY_DEBT:
                return this.getBalance(dcSet, key).b;
            case BALANCE_POS_HOLD:
                return this.getBalance(dcSet, key).c;
            case BALANCE_POS_SPEND:
                return this.getBalance(dcSet, key).d;
            case BALANCE_POS_PLEDGE:
                return this.getBalance(dcSet, key).e;
            case BALANCE_POS_6:
                return new Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        return null;
    }

    public static BigDecimal balanceInPositionAndSide(Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance,
                                                      int position, int side) {
        switch (position) {
            case BALANCE_POS_OWN:
                switch (side) {
                    case BALANCE_SIDE_CREDIT:
                        return balance.a.a;
                    case BALANCE_SIDE_LEFT:
                        return balance.a.b;
                    case BALANCE_SIDE_DEBIT:
                        return balance.a.a.subtract(balance.a.b);
                }
            case BALANCE_POS_DEBT:
                switch (side) {
                    case BALANCE_SIDE_CREDIT:
                        return balance.b.a;
                    case BALANCE_SIDE_LEFT:
                        return balance.b.b;
                    case BALANCE_SIDE_DEBIT:
                        return balance.b.a.subtract(balance.b.b);
                }
            case BALANCE_POS_HOLD:
                switch (side) {
                    case BALANCE_SIDE_CREDIT:
                        return balance.c.a;
                    case BALANCE_SIDE_LEFT:
                        return balance.c.b;
                    case BALANCE_SIDE_DEBIT:
                        return balance.c.a.subtract(balance.c.b);
                }
            case BALANCE_POS_SPEND:
                switch (side) {
                    case BALANCE_SIDE_CREDIT:
                        return balance.d.a;
                    case BALANCE_SIDE_LEFT:
                        return balance.d.b;
                    case BALANCE_SIDE_DEBIT:
                        return balance.d.a.subtract(balance.d.b);
                }
            case BALANCE_POS_PLEDGE:
                switch (side) {
                    case BALANCE_SIDE_CREDIT:
                        return balance.e.a;
                    case BALANCE_SIDE_LEFT:
                        return balance.e.b;
                    case BALANCE_SIDE_DEBIT:
                        return balance.e.a.subtract(balance.e.b);
                }
        }

        return null;
    }

    static public Tuple2<BigDecimal, BigDecimal> getBalanceForPosition(Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance,
                                                                       int position) {
        switch (position) {
            case BALANCE_POS_OWN:
                return balance.a;
            case BALANCE_POS_DEBT:
                return balance.b;
            case BALANCE_POS_HOLD:
                return balance.c;
            case BALANCE_POS_SPEND:
                return balance.d;
            case BALANCE_POS_PLEDGE:
                return balance.e;
        }

        return null;
    }


    public Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> getBalance(
            long key) {
        return this.getBalance(DCSet.getInstance(), key);
    }

    public BigDecimal getForSale(DCSet dcSet, long key, int height, boolean withCredit) {
        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance = this
                .getBalance(dcSet, key);
        BigDecimal ownVol = balance.a.b;

        if (!BlockChain.ERA_COMPU_ALL_UP && key == Transaction.RIGHTS_KEY && height > BlockChain.FREEZE_FROM) {
            int[][] item = BlockChain.FREEZED_BALANCES.get(this.getAddress());
            if (item != null) {
                if (item[0][0] < 0) {
                    return BigDecimal.ZERO;
                }

                // int height = dcSet.getBlocksHeadMap().size();
                BigDecimal freeze = BigDecimal.ZERO;
                for (int[] point : item) {
                    if (height < point[0]) {
                        freeze = new BigDecimal(point[1]);
                        break;
                    }
                }
                ownVol = ownVol.subtract(freeze);
            }
        }

        BigDecimal inDebt = balance.b.b;
        if (inDebt.signum() < 0 && withCredit) {
            ownVol = ownVol.add(inDebt);
        }
        return ownVol;
    }

    /**
     * с учетом данных в долг средств или выданных
     *
     * @param dcSet
     * @return
     */
    public BigDecimal getForFee(DCSet dcSet) {
        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance = this
                .getBalance(dcSet, AssetCls.FEE_KEY);
        BigDecimal ownVol = balance.a.b;
        BigDecimal inDebt = balance.b.b;

        return ownVol.add(inDebt);
    }


    // Добавляем величины для тестовых режимов
    public static BigDecimal addDEVAmount(long key, byte[] shortAddressBytes) {
        if (BlockChain.ERA_COMPU_ALL_UP) {
            if (key == AssetCls.ERA_KEY)
                return BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL / 1000 * (5000 + shortAddressBytes[10]) / 5000);
            else if (key == AssetCls.FEE_KEY)
                return new BigDecimal("100.0");

            if (BlockChain.isNovaAsset(key)) {
                return new BigDecimal("1000.0");
            }
        }

        return BigDecimal.ZERO;

    }

    public BigDecimal addDEVAmount(long key) {
        return addDEVAmount(key, shortBytes);

    }

    public Tuple2<BigDecimal, BigDecimal> balAaddDEVAmount(long key, Tuple2<BigDecimal, BigDecimal> balA) {
        BigDecimal addAmount = addDEVAmount(key, shortBytes);
        if (addAmount.signum() == 0)
            return balA;

        return new Tuple2<>(balA.a, balA.b.add(addAmount));

    }

    public Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
        balanceAddDEVAmount(long key, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
                            balance) {
        BigDecimal addAmount = addDEVAmount(key, this.getShortAddressBytes());
        if (addAmount.signum() == 0)
            return balance;

        return new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>(
                    new Tuple2<BigDecimal, BigDecimal>(balance.a.a, balance.a.b.add(addAmount)),
                    balance.b, balance.c, balance.d, balance.e);
    }

    public Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> getBalance(
            DCSet db, long key) {
        if (key < 0)
            key = -key;

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
                balance = db.getAssetBalanceMap().get(getShortAddressBytes(), key);
        if (BlockChain.ERA_COMPU_ALL_UP) {
            return balanceAddDEVAmount(key, balance);
        }
        return balance;

    }

    public Tuple2<BigDecimal, BigDecimal> getBalance(DCSet db, long key, int actionType) {
        if (key < 0)
            key = -key;

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance = db
                .getAssetBalanceMap().get(getShortAddressBytes(), key);

        if (actionType == BALANCE_POS_OWN) {
            if (BlockChain.ERA_COMPU_ALL_UP) {
                return new Tuple2<BigDecimal, BigDecimal>(balance.a.a, balance.a.b.add(addDEVAmount(key, this.getShortAddressBytes())));
            }

            return balance.a;

        } else if (actionType == BALANCE_POS_DEBT)
            return balance.b;
        else if (actionType == BALANCE_POS_HOLD)
            return balance.c;
        else if (actionType == BALANCE_POS_SPEND)
            return balance.d;
        else
            return balance.e;

    }

    public void changeCOMPUStatsBalances(DCSet dcSet, boolean substract, BigDecimal amount, int side) {
        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
                balance = dcSet.getAssetBalanceMap().get(getShortAddressBytes(), Transaction.FEE_KEY);

        if (side == FEE_BALANCE_SIDE_REFERAL_AND_GIFTS) {
            // учтем Всего рефералка награды разные
            // это Баланс 3-й (ХРАНЮ) сторона 1
            // одновременно увеличим Бонусы и Всего заработал
            balance = new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>(
                    balance.a, balance.b,
                    substract ? new Tuple2<BigDecimal, BigDecimal>(balance.c.a.subtract(amount), balance.c.b.subtract(amount))
                            : new Tuple2<BigDecimal, BigDecimal>(balance.c.a.add(amount), balance.c.b.add(amount)),
                    balance.d,
                    balance.e);
        } else if (side == FEE_BALANCE_SIDE_FORGED) {
            // учтем что Всего нафоржили - это как разница между Бонусы и всего Заработали
            // поэтому увеличим только Остаток
            // это Баланс 3-й (ХРАНЮ) сторона 2
            balance = new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>(
                    balance.a, balance.b,
                    substract ? new Tuple2<BigDecimal, BigDecimal>(balance.c.a, balance.c.b.subtract(amount))
                            : new Tuple2<BigDecimal, BigDecimal>(balance.c.a, balance.c.b.add(amount)),
                    balance.d,
                    balance.e);
        } else if (false) {
            // не трогаем
            // это Баланс 4-й (ПОТРАТИЛ) сторона 1
            balance = new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>(
                    balance.a, balance.b, balance.c,
                    substract ? new Tuple2<BigDecimal, BigDecimal>(balance.d.a.subtract(amount), balance.d.b)
                            : new Tuple2<BigDecimal, BigDecimal>(balance.d.a.add(amount), balance.d.b),
                    balance.e);
        } else if (side == FEE_BALANCE_SIDE_SPEND) {
            // учтем что Всего потратили
            // это Баланс 4-й (ПОТРАТИЛ) сторона 2
            balance = new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>(
                    balance.a, balance.b, balance.c,
                    substract ? new Tuple2<BigDecimal, BigDecimal>(balance.d.a, balance.d.b.subtract(amount))
                            : new Tuple2<BigDecimal, BigDecimal>(balance.d.a, balance.d.b.add(amount)),
                    balance.e);
        } else {
            return;
        }

        dcSet.getAssetBalanceMap().put(getShortAddressBytes(), Transaction.FEE_KEY, balance);

    }

    public BigDecimal getCOMPUStatsBalances(Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance, int side) {

        switch (side) {
            case FEE_BALANCE_SIDE_REFERAL_AND_GIFTS:
                return balance.c.a;
            case FEE_BALANCE_SIDE_TOTAL_EARNED:
                return balance.c.b;
            case FEE_BALANCE_SIDE_FORGED:
                // как разница
                return balance.c.b.subtract(balance.c.a);
            case FEE_BALANCE_SIDE_SPEND:
                return balance.d.b;
            case FEE_BALANCE_SIDE_DIFFERENCE:
                // все разница приход / расход
                return balance.c.b.subtract(balance.d.b);
        }

        return null;
    }

    /**
     * change BALANCE - add or subtract amount by KEY + AMOUNT = TYPE
     *
     * @param db
     * @param subtract
     * @param isBackward
     * @param key
     * @param amount_in
     * @param isDirect
     * @param isNotSender
     * @param notUpdateIncomed
     * @param mirrorToPos      - all POS except DEBT - for mirror change balance in that POS
     * @return
     */
    public Tuple3<BigDecimal, BigDecimal, BigDecimal> changeBalance(
            DCSet db, boolean subtract, boolean isBackward, long key,
            BigDecimal amount_in, boolean isDirect, boolean isNotSender, boolean notUpdateIncomed,
            int mirrorToPos) {

        int balancePosition = balancePosition(key, amount_in, isBackward, isDirect);

        ItemAssetBalanceMap map = db.getAssetBalanceMap();

        BigDecimal amount = amount_in.abs();
        long absKey;
        if (key > 0) {
            absKey = key;
        } else {
            absKey = -key;
        }

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance =
                map.get(getShortAddressBytes(), absKey);

        boolean updateIncomed = !notUpdateIncomed;

        if (mirrorToPos == balancePosition || mirrorToPos == BALANCE_POS_DEBT) {
            mirrorToPos = 0;
        }
        Tuple2<BigDecimal, BigDecimal> balance1 = balance.a;
        Tuple2<BigDecimal, BigDecimal> balance2 = balance.b;
        Tuple2<BigDecimal, BigDecimal> balance3 = balance.c;
        Tuple2<BigDecimal, BigDecimal> balance4 = balance.d;
        Tuple2<BigDecimal, BigDecimal> balance5 = balance.e;
        // обработает зеркальное изменение заданной позиции
        switch (mirrorToPos) {
            case BALANCE_POS_OWN:
                balance1 = !subtract ? new Tuple2<BigDecimal, BigDecimal>(
                        updateIncomed ? balance1.a.subtract(amount) : balance1.a, balance1.b.subtract(amount))
                        : new Tuple2<BigDecimal, BigDecimal>(
                        updateIncomed ? balance1.a.add(amount) : balance1.a, balance1.b.add(amount));
                break;
            case BALANCE_POS_DEBT:
                // тут ничего нельзя делать так как еще есть отдельные записи совместного баланса
                break;
            case BALANCE_POS_HOLD:
                balance3 = !subtract ? new Tuple2<BigDecimal, BigDecimal>(
                        updateIncomed ? balance3.a.subtract(amount) : balance3.a, balance3.b.subtract(amount))
                        : new Tuple2<BigDecimal, BigDecimal>(
                        updateIncomed ? balance3.a.add(amount) : balance3.a, balance3.b.add(amount));
                break;
            case BALANCE_POS_SPEND:
                balance4 = !subtract ? new Tuple2<BigDecimal, BigDecimal>(
                        updateIncomed ? balance4.a.subtract(amount) : balance4.a, balance4.b.subtract(amount))
                        : new Tuple2<BigDecimal, BigDecimal>(
                        updateIncomed ? balance4.a.add(amount) : balance4.a, balance4.b.add(amount));
                break;
            case BALANCE_POS_PLEDGE:
                balance5 = !subtract ? new Tuple2<BigDecimal, BigDecimal>(
                        updateIncomed ? balance5.a.subtract(amount) : balance5.a,
                        balance5.b.subtract(amount))
                        : new Tuple2<BigDecimal, BigDecimal>(
                        updateIncomed ? balance5.a.add(amount) : balance5.a,
                        balance5.b.add(amount));
                break;

        }

        switch (balancePosition) {
            case BALANCE_POS_OWN:
                // OWN + property
                //if (isBackward) amount = amount.negate();
                balance = new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>(
                        subtract ? new Tuple2<BigDecimal, BigDecimal>(
                                updateIncomed ? balance1.a.subtract(amount) : balance1.a, balance1.b.subtract(amount))
                                : new Tuple2<BigDecimal, BigDecimal>(
                                updateIncomed ? balance1.a.add(amount) : balance1.a, balance1.b.add(amount)),
                        balance2, balance3, balance4, balance5);
                break;

            case BALANCE_POS_DEBT:
                // DEBT + CREDIT
                balance = new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>(
                        balance1,
                        subtract ? new Tuple2<BigDecimal, BigDecimal>(
                                updateIncomed ? balance2.a.subtract(amount) : balance2.a, balance2.b.subtract(amount))
                                : new Tuple2<BigDecimal, BigDecimal>(
                                updateIncomed ? balance2.a.add(amount) : balance2.a, balance2.b.add(amount)),
                        balance3, balance4, balance5);
                break;

            case BALANCE_POS_HOLD:
                // HOLD + STOCK 🕐 🕝

                balance = new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>(
                        balance1, balance2,
                        subtract ? new Tuple2<BigDecimal, BigDecimal>(
                                updateIncomed ? balance3.a.subtract(amount) : balance3.a, balance3.b.subtract(amount))
                                : new Tuple2<BigDecimal, BigDecimal>(
                                updateIncomed ? balance3.a.add(amount) : balance3.a, balance3.b.add(amount)),
                        balance4, balance5);
                break;

            case BALANCE_POS_SPEND:

                Tuple2<BigDecimal, BigDecimal> ownBalance = balance.a;

                if (!isNotSender) {
                    // у создателя транзакции так же баланс ИМЕЮ уменьшаем - просто вычитаем - для учета вывода из оборота
                    if (subtract) {
                        ownBalance = new Tuple2<BigDecimal, BigDecimal>(ownBalance.a, ownBalance.b.subtract(amount));
                    } else {
                        ownBalance = new Tuple2<BigDecimal, BigDecimal>(ownBalance.a, ownBalance.b.add(amount));
                    }
                }

                balance = new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>(
                        ownBalance, balance2, balance3,
                        subtract ? new Tuple2<BigDecimal, BigDecimal>(
                                updateIncomed ? balance4.a.subtract(amount) : balance4.a, balance4.b.subtract(amount))
                                : new Tuple2<BigDecimal, BigDecimal>(
                                updateIncomed ? balance4.a.add(amount) : balance4.a, balance4.b.add(amount)),
                        balance5);
                break;

            case BALANCE_POS_PLEDGE:
                // DEX PLEDGE

                balance = new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>(
                        balance1, balance2,
                        balance3, balance4,
                        subtract ? new Tuple2<BigDecimal, BigDecimal>(
                                updateIncomed ? balance5.a.subtract(amount) : balance5.a, balance5.b.subtract(amount))
                                : new Tuple2<BigDecimal, BigDecimal>(
                                updateIncomed ? balance5.a.add(amount) : balance5.a, balance5.b.add(amount))
                );
                break;

        }

        map.put(getShortAddressBytes(), absKey, balance);

        ////////////// DEBUG TOTAL COMPU
        // несотыковка из-за ордеров на бирже
        if (false && absKey == 2l && this.equals("73EotEbxvAo39tyugJSyL5nbcuMWs4aUpS")) {
            Collection<byte[]> addrs = db.getAssetBalanceMap().keySet();
            BigDecimal total = BigDecimal.ZERO;
            for (byte[] mapKey : addrs) {
                if (ItemAssetBalanceMap.getAssetKeyFromKey(mapKey) == 2l) {
                    Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> ball =
                            map.get(mapKey);

                    total = total.add(ball.a.b);
                }
            }
            if (total.signum() != 0) {
                Long error = null;
                error++;
            }
        }

        return new Tuple3<BigDecimal, BigDecimal, BigDecimal>(balance.a.b, balance.b.b, balance.c.b);
    }

    public Tuple3<BigDecimal, BigDecimal, BigDecimal> changeBalance(
            DCSet db, boolean subtract, boolean isBackward, long key,
            BigDecimal amount_in, boolean isDirect, boolean isNotSender, boolean notUpdateIncomed) {
        return changeBalance(db, subtract, isBackward, key,
                amount_in, isDirect, isNotSender, notUpdateIncomed, 0);
    }

    public Tuple3<BigDecimal, BigDecimal, BigDecimal> getConfBalance3(int confirmations, long key) {
        return this.getConfBalance3(confirmations, key, DCSet.getInstance());
    }

    public Tuple3<BigDecimal, BigDecimal, BigDecimal> getConfBalance3(int confirmations, long key, DCSet db) {
        // CHECK IF UNCONFIRMED BALANCE
        if (confirmations <= 0) {
            return this.getUnconfirmedBalance(key);
        }

        // IF 1 CONFIRMATION
        if (confirmations == 1) {
            Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance = this
                    .getBalance(db, key);
            return new Tuple3<BigDecimal, BigDecimal, BigDecimal>(balance.a.b, balance.b.b, balance.c.b);
        }

        // GO TO PARENT BLOCK 10
        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance = this
                .getBalance(db, key);
        BigDecimal own = balance.a.b;
        BigDecimal rent = balance.b.b;
        BigDecimal hold = balance.c.b;

        Block block = db.getBlockMap().last();

        for (int i = 1; i < confirmations && block != null && block.getVersion() > 0; i++) {
            for (Transaction transaction : block.getTransactions()) {

                transaction.setDC(db); // need for Involved

                if (transaction.isInvolved(this)) {
                    if (transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {

                        int balancePosition = ((TransactionAmount) transaction).balancePosition();
                        if (balancePosition == BALANCE_POS_OWN) {
                            own = own.subtract(transaction.getAmount(this));
                        } else {
                            rent = own.subtract(transaction.getAmount(this));
                        }
                    }

                }
            }

            block = block.getParent(db);
        }

        // RETURN
        return new Tuple3<BigDecimal, BigDecimal, BigDecimal>(own, rent, hold);
    }

    public static BigDecimal totalForAddresses(DCSet dcSet, Set<String> addresses, Long assetKey, int pos) {

        BigDecimal eraBalanceA = BigDecimal.ZERO;
        for (String address : addresses) {

            Account account = new Account(address);
            Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance
                    = account.getBalance(dcSet, assetKey);

            switch (pos) {
                case 1:
                    eraBalanceA = eraBalanceA.add(balance.a.b);
                    break;
                case 2:
                    eraBalanceA = eraBalanceA.add(balance.b.b);
                    break;
                case 3:
                    eraBalanceA = eraBalanceA.add(balance.c.b);
                    break;
                case 4:
                    eraBalanceA = eraBalanceA.add(balance.d.b);
                    break;
                case 5:
                    eraBalanceA = eraBalanceA.add(balance.e.b);
                    break;
            }
        }

        return eraBalanceA;

    }

    public long[] getLastTimestamp() {
        if (BlockChain.CHECK_DOUBLE_SPEND_DEEP < 0)
            return null;
        return this.getLastTimestamp(DCSet.getInstance());
    }

    /**
     * account.address -> LAST[TX.timestamp + TX.dbRef]
     *
     * @param dcSet
     * @return
     */
    public long[] getLastTimestamp(DCSet dcSet) {
        if (BlockChain.CHECK_DOUBLE_SPEND_DEEP < 0)
            return null;
        return dcSet.getReferenceMap().get(shortBytes);
    }

    /**
     * @param currentPoint [timestamp, dbRef]
     * @param dcSet        DCSet
     */
    public void setLastTimestamp(long[] currentPoint, DCSet dcSet) {

        if (BlockChain.CHECK_DOUBLE_SPEND_DEEP < 0)
            return;

        ReferenceMapImpl map = dcSet.getReferenceMap();

        if (BlockChain.NOT_STORE_REFFS_HISTORY) {
            // SET NEW REFERENCE
            map.put(shortBytes, currentPoint);
            return;
        }

        // GET CURRENT REFERENCE
        long[] reference = map.get(shortBytes);

        // MAKE KEY for this TIMESTAMP
        byte[] keyCurrentPoint = Bytes.concat(shortBytes, Longs.toByteArray(currentPoint[0]));

        if (reference != null) {
            // set NEW LAST TIMESTAMP as REFERENCE
            map.put(keyCurrentPoint, reference);
        }

        // SET NEW REFERENCE
        map.put(shortBytes, currentPoint);

    }

    public void removeLastTimestamp(DCSet dcSet) {

        if (BlockChain.CHECK_DOUBLE_SPEND_DEEP < 0) {
            return;
        }

        ReferenceMapImpl map = dcSet.getReferenceMap();

        if (BlockChain.NOT_STORE_REFFS_HISTORY) {
            map.delete(shortBytes);
            return;
        }

        // GET LAST TIMESTAMP
        long[] lastPoint = map.get(shortBytes);

        if (lastPoint == null)
            return;

        // MAKE KEY for this TIMESTAMP
        byte[] keyPrevPoint = Bytes.concat(shortBytes, Longs.toByteArray(lastPoint[0]));

        // GET REFERENCE
        // DELETE TIMESTAMP - REFERENCE
        long[] reference = map.remove(keyPrevPoint);
        if (reference == null) {
            map.delete(shortBytes);
        } else {
            // PUT OLD REFERENCE
            map.put(shortBytes, reference);
        }
    }

    public void removeLastTimestamp(DCSet dcSet, long timestamp) {

        if (BlockChain.CHECK_DOUBLE_SPEND_DEEP < 0) {
            return;
        }

        ReferenceMapImpl map = dcSet.getReferenceMap();

        if (BlockChain.NOT_STORE_REFFS_HISTORY) {
            map.delete(shortBytes);
            return;
        }

        // MAKE KEY for this TIMESTAMP
        byte[] keyPrevPoint = Bytes.concat(shortBytes, Longs.toByteArray(timestamp));

        // GET REFERENCE
        // DELETE TIMESTAMP - REFERENCE
        long[] reference = map.remove(keyPrevPoint);
        if (reference == null) {
            map.delete(shortBytes);
        } else {
            // PUT OLD REFERENCE
            map.put(shortBytes, reference);
        }
    }

    // TOSTRING
    public String personChar(Tuple2<Integer, PersonCls> personRes) {
        if (personRes == null)
            return "";

        PersonCls person = personRes.b;
        if (!person.isAlive(0L))
            return "☗"; // "☗"; ☻

        int key = personRes.a;
        if (key == -1)
            return "-"; // "☺";
        else if (key == 1)
            return "♥"; // "♥"; //"☺"; //"☑"; 9829
        else
            return "";

    }

    public String viewFEEbalance() {

        long result = this.getBalanceUSE(Transaction.FEE_KEY).unscaledValue().longValue();
        result /= BlockChain.FEE_PER_BYTE;
        result >>= 8;

        if (result > 1000)
            return "+4";
        else if (result > 100)
            return "+3";
        else if (result > 10)
            return "+2";
        else if (result > 1)
            return "+1";
        else
            return "0";

    }

    @Override
    public String toString() {
        Tuple2<Integer, PersonCls> personRes = this.getPerson();
        String personStr;
        String addressStr;
        if (personRes == null) {
            personStr = "";
            addressStr = this.getAddress();
        } else {
            personStr = personChar(personRes) + personRes.b.getShort();
            addressStr = this.getAddress().substring(1, 8);
        }

        return " {"
                + viewFEEbalance()
                + "}" + " " + addressStr + "" + personStr;
    }

    public String toString(long key) {
        Tuple2<Integer, PersonCls> personRes = this.getPerson();
        String personStr;
        String addressStr;
        if (personRes == null) {
            personStr = "";
            addressStr = GenesisBlock.CREATOR.equals(this) ? "GENESIS" : this.getAddress();
        } else {
            personStr = personChar(personRes) + personRes.b.getShort();
            addressStr = this.getAddress().substring(1, 8);
        }

        boolean statusBad = Controller.getInstance().getStatus() != Controller.STATUS_OK;
        Tuple2<BigDecimal, BigDecimal> balance = this.getBalanceInSettedPosition(key);

        return (statusBad ? "??? " : "")
                + (balance == null? "" : NumberAsString.formatAsString(balance.b) + " ")
                + (key == Transaction.FEE_KEY?" " : "{" + viewFEEbalance() + "} ")
                + addressStr + "" + personStr;
    }


    public String viewPerson() {
        Tuple2<Integer, PersonCls> personRes = this.getPerson();
        if (personRes == null) {
            if (this.getAddress() != null) {
                return this.getAddress();

            } else {
                return "";
            }
        } else {
            String personStr = personChar(personRes) + personRes.b.toString();
            return personStr;
        }

    }

    public String getPersonAsString() {
        Tuple2<Integer, PersonCls> personRes = this.getPerson();
        if (personRes == null) {
            return GenesisBlock.CREATOR.equals(this) ? "GENESIS" : this.getAddress();
        } else {
            String personStr = personChar(personRes) + personRes.b.getShort();
            String addressStr = this.getAddress().substring(1, 7);
            return addressStr + "" + personStr;
        }
    }

    public String getPersonAsString(int cutAddress) {
        Tuple2<Integer, PersonCls> personRes = this.getPerson();
        if (personRes == null) {
            return GenesisBlock.CREATOR.equals(this) ? "GENESIS" : this.getAddress().substring(0, cutAddress) + "..";
        } else {
            String personStr = personChar(personRes) + personRes.b.getShort();
            String addressStr = this.getAddress().substring(1, 5);
            return addressStr + "" + personStr;
        }
    }

    public String getPersonOrShortAddress(int max) {
        Tuple2<Integer, PersonCls> personRes = this.getPerson();
        if (personRes == null) {
            return GenesisBlock.CREATOR.equals(this) ? "GENESIS" : this.getAddress().substring(0, max) + "~";
        } else {
            return "[" + personRes.b.getKey() + "]" + personRes.b.getName();
        }
    }

    public String getPersonAsString_01(boolean shrt) {
        Tuple2<Integer, PersonCls> personRes = this.getPerson();
        if (personRes == null) {
            return "";
        } else {
            return shrt ? personRes.b.getShort() : personRes.b.getName();
        }
    }

    @Override
    public int hashCode() {
        return Ints.fromByteArray(shortBytes);
    }

    public long hashCodeLong() {
        return Longs.fromByteArray(shortBytes);
    }

    // EQUALS
    @Override
    public boolean equals(Object b) {
        if (b instanceof Account) {
            return Arrays.equals(this.shortBytes, ((Account) b).getShortAddressBytes());
        } else if (b instanceof String) {
            return this.getAddress().equals(b);
        } else if (b instanceof byte[]) {
            byte[] bs = (byte[]) b;
            if (bs.length == ADDRESS_LENGTH) {
                return Arrays.equals(this.bytes, bs);
            } else {
                return Arrays.equals(this.shortBytes, bs);
            }
        }

        return false;
    }


    public Tuple4<Long, Integer, Integer, Integer> getPersonDuration(DCSet db) {
        return db.getAddressPersonMap().getItem(shortBytes);
    }

    public boolean isPerson(DCSet dcSet, int forHeight, Tuple4<Long, Integer, Integer, Integer> addressDuration) {

        // IF DURATION ADDRESS to PERSON IS ENDED
        if (addressDuration == null)
            return false;

        // TEST TIME and EXPIRE TIME
        long current_time = Controller.getInstance().getBlockChain().getTimestamp(forHeight);

        // TEST TIME and EXPIRE TIME for PERSONALIZE address
        int days = addressDuration.b;
        if (days < 0)
            return false;
        if (days * (long) 86400000 < current_time)
            return false;

        return true;

    }

    public boolean isPerson(DCSet dcSet, int forHeight) {

        // IF DURATION ADDRESS to PERSON IS ENDED
        Tuple4<Long, Integer, Integer, Integer> addressDuration =
                this.getPersonDuration(dcSet);
        if (addressDuration == null)
            return false;

        return isPerson(dcSet, forHeight, addressDuration);
    }

    public boolean isPerson() {
        return isPerson(DCSet.getInstance(), Controller.getInstance().getMyHeight());
    }


    /**
     * Обновляет данные о персоне даже если они уже были записаны
     *
     * @param dcSet
     * @param forHeight
     * @return
     */
    public Tuple2<Integer, PersonCls> getPerson(DCSet dcSet, int forHeight, Tuple4<Long, Integer, Integer, Integer> addressDuration) {

        // IF DURATION ADDRESS to PERSON IS ENDED
        if (addressDuration == null)
            return null;

        // TEST TIME and EXPIRE TIME
        long current_time = Controller.getInstance().getBlockChain().getTimestamp(forHeight);

        // get person
        Long personKey = addressDuration.a;
        PersonCls person = (PersonCls) Controller.getInstance().getItem(dcSet, ItemCls.PERSON_TYPE, personKey);

        // TEST ADDRESS is ACTIVE?
        int days = addressDuration.b;
        // TODO x 1000 ?
        if (days < 0 || days * (long) 86400000 < current_time)
            return new Tuple2<Integer, PersonCls>(-1, person);

        return new Tuple2<Integer, PersonCls>(1, person);

    }

    public Tuple2<Integer, PersonCls> getPerson(DCSet dcSet, int forHeight) {

        // IF DURATION ADDRESS to PERSON IS ENDED
        Tuple4<Long, Integer, Integer, Integer> addressDuration = this.getPersonDuration(dcSet);
        if (addressDuration == null)
            return null;

        return getPerson(dcSet, forHeight, addressDuration);
    }

    /**
     * берет данные из переменной локальной если там что-то было
     *
     * @return
     */
    public Tuple2<Integer, PersonCls> getPerson() {
        if (person == null) {
            person = getPerson(DCSet.getInstance(), Controller.getInstance().getMyHeight());
        }
        return person;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("address", getAddress());
        Tuple2<Integer, PersonCls> personRes = this.getPerson();
        if (personRes != null) {
            JSONObject personJson = new JSONObject();
            personJson.put("key", personRes.b.getKey());
            personJson.put("name", personRes.b.getName());
            personJson.put("birthday", personRes.b.getBirthdayStr());
            json.put("person", personJson);
        }
        return json;
    }

    public void toJsonPersonInfo(Map json, String keyName) {

        json.put(keyName, getAddress());

        Tuple2<Integer, PersonCls> personRes = this.getPerson();

        if (personRes == null)
            return;

        json.put(keyName + "_key", personRes.b.getKey());
        json.put(keyName + "_name", personRes.b.viewName());
        json.put(keyName + "_birthday", personRes.b.getBirthdayStr());
        json.put(keyName + "_image", personRes.b.getImageURL());

    }

    // previous forging block or changed ERA volume
    public Tuple3<Integer, Integer, Integer> getForgingData(DCSet db, int height) {
        return db.getAddressForging().get(getAddress(), height);
    }

    public void setForgingData(DCSet db, int height, int forgingBalance) {
        db.getAddressForging().putAndProcess(getAddress(), height, forgingBalance);
    }

    public void delForgingData(DCSet db, int height) {
        db.getAddressForging().deleteAndProcess(getAddress(), height);
    }

    public Tuple3<Integer, Integer, Integer> getLastForgingData(DCSet db) {
        return db.getAddressForging().getLast(getAddress());
    }

    public static Tuple3<String, String, String> getFromFavorites(String address) {
        return Controller.getInstance().getWallet().dwSet.getFavoriteAccountsMap().get(address);

    }

    public Tuple3<String, String, String> getFromFavorites() {
        return getFromFavorites(getAddress());
    }

    public Integer getAccountNo() {
        return Controller.getInstance().getWallet().dwSet.getAccountMap().getAccountNo(getAddress());
    }

}
