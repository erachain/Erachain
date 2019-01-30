package org.erachain.core.account;

import com.google.common.primitives.Bytes;
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
import org.erachain.datachain.OrderMap;
import org.erachain.datachain.ReferenceMap;
import org.erachain.lang.Lang;
import org.erachain.utils.NameUtils;
import org.erachain.utils.NameUtils.NameResult;
import org.erachain.utils.NumberAsString;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

//import org.erachain.core.crypto.Base64;

//04/01 +-

/**
 * обработка ключей и криптографии
 */
public class Account {

    public static final int ADDRESS_LENGTH = 25;
    // private static final long ERA_KEY = Transaction.RIGHTS_KEY;
    private static final long FEE_KEY = Transaction.FEE_KEY;
    // public static final long ALIVE_KEY = StatusCls.ALIVE_KEY;
    // public static String EMPTY_PUBLICK_ADDRESS = new PublicKeyAccount(new
    // byte[PublicKeyAccount.PUBLIC_KEY_LENGTH]).getAddress();

    protected String address;
    protected byte[] bytes;
    protected byte[] shortBytes;
    // private long generatingBalance; //used for forging balance
    Tuple4<Long, Integer, Integer, Integer> personDuration;


    protected Account() {
        // this.generatingBalance = 0l;
    }

    public Account(String address) {

        // ///test address
        assert (Base58.decode(address) instanceof byte[]);
        this.bytes = Base58.decode(address);
        this.shortBytes = Arrays.copyOfRange(this.bytes, 1, this.bytes.length - 4);
        this.address = address;
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

        boolean isBase58 = false;
        try {
            Base58.decode(address);
            isBase58 = true;
        } catch (Exception e) {
            if (PublicKeyAccount.isValidPublicKey(address)) {
                // MAY BE IT BASE.32 +
                return new Tuple2<Account, String>(new PublicKeyAccount(address), null);
            }
        }

        if (isBase58) {
            // ORDINARY RECIPIENT
            if (Crypto.getInstance().isValidAddress(address)) {
                return new Tuple2<Account, String>(new Account(address), null);
            } else if (PublicKeyAccount.isValidPublicKey(address)) {
                return new Tuple2<Account, String>(new PublicKeyAccount(address), null);
            } else {
                return new Tuple2<Account, String>(null, "Wrong Address or PublickKey");
            }
        } else {
            // IT IS NAME - resolve ADDRESS
            Pair<Account, NameResult> result = NameUtils.nameToAdress(address);

            if (result.getB() == NameResult.OK) {
                return new Tuple2<Account, String>(result.getA(), null);
            } else {
                return new Tuple2<Account, String>(null, "The name is not registered");
            }
        }

    }

    // make TYPE of transactionAmount by signs of KEY and AMOUNT
    public static int actionType(long key, BigDecimal amount) {
        if (key == 0l || amount == null || amount.signum() == 0)
            return 0;

        int type;
        int amount_sign = amount.signum();
        if (key > 0) {
            if (amount_sign > 0) {
                // SEND
                type = TransactionAmount.ACTION_SEND;
            } else {
                // HOLD in STOCK
                type = TransactionAmount.ACTION_HOLD;
            }
        } else {
            if (amount_sign > 0) {
                // give CREDIT or BORROW CREDIT
                type = TransactionAmount.ACTION_DEBT;
            } else {
                // PRODUCE or SPEND
                type = TransactionAmount.ACTION_SPEND;
            }
        }

        return type;

    }

    public static String getDetails(String toValue, AssetCls asset) {

        String out = "";

        if (toValue.isEmpty()) {
            return out;
        }

        boolean statusBad = Controller.getInstance().getStatus() != Controller.STATUS_OK;

        Account account = null;

        // CHECK IF RECIPIENT IS VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(toValue)) {
            Pair<Account, NameResult> nameToAdress = NameUtils.nameToAdress(toValue);

            if (nameToAdress.getB() == NameResult.OK) {
                account = nameToAdress.getA();
                return (statusBad ? "??? " : "") + account.toString(asset.getKey());
            } else {
                return (statusBad ? "??? " : "") + nameToAdress.getB().getShortStatusMessage();
            }
        } else {
            account = new Account(toValue);

            if (account.getBalanceUSE(asset.getKey()).compareTo(BigDecimal.ZERO) == 0
                    && account.getBalanceUSE(Transaction.FEE_KEY).compareTo(BigDecimal.ZERO) == 0) {
                return Lang.getInstance().translate("Warning!") + " " + (statusBad ? "???" : "")
                        + account.toString(asset.getKey());
            } else {
                return (statusBad ? "???" : "") + account.toString(asset.getKey());
            }
        }

    }

    public static Map<String, BigDecimal> getKeyBalancesWithForks(DCSet dcSet, long key,
                                                                  Map<String, BigDecimal> values) {
        ItemAssetBalanceMap map = dcSet.getAssetBalanceMap();
        Iterator<Tuple2<String, Long>> iterator = map.getIterator(0, true);
        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> ballance;

        Tuple2<String, Long> iteratorKey;
        while (iterator.hasNext()) {
            iteratorKey = iterator.next();
            if (iteratorKey.b == key) {
                ballance = map.get(iteratorKey);
                values.put(iteratorKey.a, ballance.a.b);
            }
        }

        DCSet dcParent = dcSet.getParent();
        if (dcParent != null) {
            values = getKeyBalancesWithForks(dcParent, key, values);
        }

        return values;

    }

    public static Map<String, BigDecimal> getKeyOrdersWithForks(DCSet dcSet, long key, Map<String, BigDecimal> values) {

        OrderMap map = dcSet.getOrderMap();
        Iterator<Long> iterator = map.getIterator(0, true);
        Order order;
        while (iterator.hasNext()) {
            order = map.get(iterator.next());
            if (order.getHave() == key) {
                String address = order.getCreator().getAddress();
                values.put(address, values.get(address).add(order.getAmountHave()));
            }
        }

        DCSet dcParent = dcSet.getParent();
        if (dcParent != null) {
            values = getKeyOrdersWithForks(dcParent, key, values);
        }

        return values;

    }

    // top balance + orders values
    public static String getRichWithForks(DCSet dcSet, long key) {

        Map<String, BigDecimal> values = new TreeMap<String, BigDecimal>();

        values = getKeyBalancesWithForks(dcSet, key, values);

        // add ORDER values
        values = getKeyOrdersWithForks(dcSet, key, values);

        // search richest address
        String rich = null;
        BigDecimal maxValue = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : values.entrySet()) {
            BigDecimal value = entry.getValue();
            if (value.compareTo(maxValue) > 0) {
                maxValue = value;
                rich = entry.getKey();
            }
        }

        return rich;

    }

    /*
     * public BigDecimal getBalance(long key) { if (key < 0) key = -key; return
     * this.getBalance(key, DBSet.getInstance()); } public BigDecimal
     * getBalance(long key, DBSet db) { int type = 1; // OWN if (key < 0) { type
     * = 2; // RENT key = -key; } Tuple3<BigDecimal, BigDecimal, BigDecimal>
     * balance = db.getAssetBalanceMap().get(getAddress(), key);
     *
     * if (type == 1) return balance.a; else if (type == 2) return balance.b;
     * else return balance.c; }
     *
     * public Integer setConfirmedPersonStatus(long personKey, long statusKey,
     * int end_date, DBSet db) { return
     * db.getPersonStatusMap().addItem(personKey, statusKey, end_date); }
     */

    // SET
    /*
     * public void setConfirmedBalance(BigDecimal amount) {
     * this.setConfirmedBalance(amount, DBSet.getInstance()); } public void
     * setConfirmedBalance(BigDecimal amount, DBSet db) { //UPDATE BALANCE IN DB
     * db.getAssetBalanceMap().set(getAddress(), Transaction.FEE_KEY, amount); }
     * // public void setBalance(long key, BigDecimal balance) {
     * this.setBalance(key, balance, DBSet.getInstance()); }
     *
     * // TODO in_OWN in_RENT on_HOLD public void setBalance(long key,
     * BigDecimal balance, DBSet db) {
     *
     * int type = 1; if (key < 0) { key = -key; type = 2; }
     *
     * Tuple3<BigDecimal, BigDecimal, BigDecimal> value =
     * db.getAssetBalanceMap().get(getAddress(), key); //UPDATE BALANCE IN DB if
     * (type == 1) { value = new Tuple3<BigDecimal, BigDecimal,
     * BigDecimal>(balance, value.b, value.c); } else { // SET RENT balance
     * value = new Tuple3<BigDecimal, BigDecimal, BigDecimal>(value.a, balance,
     * value.c); } db.getAssetBalanceMap().set(getAddress(), key, value); }
     */

    public String getAddress() {
        return address;
    }

    public byte[] getAddressBytes() {
        return bytes;
    }

    public byte[] getShortAddressBytes() {
        return this.shortBytes;
    }

    // BALANCE
    public Tuple3<BigDecimal, BigDecimal, BigDecimal> getUnconfirmedBalance(long key) {
        return Controller.getInstance().getUnconfirmedBalance(this, key);
    }

    /*
     * public BigDecimal getConfirmedBalance() { return
     * this.getConfirmedBalance(DBSet.getInstance()); } public BigDecimal
     * getConfirmedBalance(DBSet db) { return
     * db.getAssetBalanceMap().get(getAddress(), Transaction.FEE_KEY); }
     */
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

    /*
     * public void setBalance3(long key, Tuple3<BigDecimal, BigDecimal,
     * BigDecimal> balance, DBSet db) { if (key < 0) key = -key;
     *
     * db.getAssetBalanceMap().set(getAddress(), key, balance); }
     *
     * public void addBalanceOWN(long key, BigDecimal value, DBSet db) {
     * Tuple3<BigDecimal, BigDecimal, BigDecimal> balance =
     * this.getBalance3(key, db); Tuple3<BigDecimal, BigDecimal, BigDecimal>
     * balance_new = new Tuple3<BigDecimal, BigDecimal,
     * BigDecimal>(balance.a.add(value), balance.b, balance.c);
     *
     * this.setBalance3(key, balance_new, db); }
     */

    // STATUS
    /*
     * public void setConfirmedPersonStatus(long personKey, long statusKey,
     * Integer days) { this.setConfirmedPersonStatus(personKey, statusKey, days,
     * DBSet.getInstance()); }
     *
     * public void setConfirmedPersonStatus(long personKey, long statusKey,
     * Integer days, DBSet db) { //UPDATE PRIMARY TIME IN DB
     * db.getPersonStatusMap().set(personKey, statusKey, days); }
     */

    public Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> getBalance(
            long key) {
        return this.getBalance(DCSet.getInstance(), key);
    }

    public BigDecimal getForSale(DCSet dcSet, long key, int height, boolean withCredit) {
        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance = this
                .getBalance(dcSet, key);
        BigDecimal ownVol = balance.a.b;

        if (!BlockChain.DEVELOP_USE && key == Transaction.RIGHTS_KEY && height > BlockChain.FREEZE_FROM) {
            int[][] item = BlockChain.FREEZED_BALANCES.get(this.address);
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

    /*
     * private void updateGeneratingBalance(DBSet db) { //CHECK IF WE NEED TO
     * RECALCULATE if(this.lastBlockSignature == null) { this.lastBlockSignature
     * = db.getBlocksHeadMap().getLastBlockSignature();
     * calculateGeneratingBalance(db); } else { //CHECK IF WE NEED TO
     * RECALCULATE if(!Arrays.equals(this.lastBlockSignature,
     * db.getBlocksHeadMap().getLastBlockSignature())) { this.lastBlockSignature =
     * db.getBlocksHeadMap().getLastBlockSignature(); calculateGeneratingBalance(db);
     * } } }
     *
     * // take current balance public void calculateGeneratingBalance(DBSet db)
     * { long balance = this.getConfirmedBalance(ERA_KEY,
     * db).setScale(0).longValue(); this.generatingBalance = balance; }
     *
     * // balance FOR generation public void
     * calculateGeneratingBalance_old(DBSet db) { //CONFIRMED BALANCE + ALL
     * NEGATIVE AMOUNTS IN LAST 9 BLOCKS - for ERA_KEY only BigDecimal balance =
     * this.getConfirmedBalance(ERA_KEY, db);
     *
     * Block block = db.getBlocksHeadMap().getLastBlock();
     *
     * int penalty_koeff = 1000000; int balance_penalty = penalty_koeff;
     *
     * // icreator X 10 // not resolve first 100 blocks for(int i=1;
     * i<GenesisBlock.GENERATING_RETARGET * 10 && block != null &&
     * block.getHeight(db) > 100; i++) { for(Transaction transaction:
     * block.getTransactions()) { if(transaction.isInvolved(this) & transaction
     * instanceof TransactionAmount) { TransactionAmount ta =
     * (TransactionAmount)transaction;
     *
     * if(ta.getKey() == ERA_KEY &
     * transaction.getAmount(this).compareTo(BigDecimal.ZERO) == 1) { balance =
     * balance.subtract(transaction.getAmount(this)); } } }
     * LinkedHashMap<Tuple2<Integer,Integer>,AT_Transaction> atTxs =
     * db.getATTransactionMap().getATTransactions(block.getHeight(db));
     * Iterator<AT_Transaction> iter = atTxs.values().iterator(); while (
     * iter.hasNext() ) { AT_Transaction key = iter.next(); if (
     * key.getRecipient().equals( this.getAddress() ) ) { balance =
     * balance.subtract( BigDecimal.valueOf(key.getAmount()) ); } }
     *
     * // icreator X 0.9 for each block generated if (balance_penalty > 0.1 *
     * penalty_koeff && block.getCreator().getAddress().equals(this.address)) {
     * balance_penalty *= Settings.GENERATE_CONTINUOUS_PENALTY * 0.001; } else {
     * // reset balance_penalty = penalty_koeff; } block = block.getParent(db);
     * }
     *
     * //DO NOT GO BELOW 0 if(balance.compareTo(BigDecimal.ZERO) == -1) {
     * balance = BigDecimal.ZERO; }
     *
     * // use penalty this.generatingBalance = balance.multiply(new
     * BigDecimal(balance_penalty / penalty_koeff));
     *
     * }
     */

    // REFERENCE

    public Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> getBalance(
            DCSet db, long key) {
        if (key < 0)
            key = -key;

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance = db
                .getAssetBalanceMap().get(getAddress(), key);
        if (BlockChain.DEVELOP_USE) {
            if (key == 1)
                return new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>(
                        new Tuple2<BigDecimal, BigDecimal>(balance.a.a, balance.a.b.add(BigDecimal.valueOf(1000))),
                        balance.b, balance.c, balance.d, balance.e);
            else if (key == 2)
                return new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>(
                        new Tuple2<BigDecimal, BigDecimal>(balance.a.a, balance.a.b.add(BigDecimal.TEN)), balance.b,
                        balance.c, balance.d, balance.e);
        }
        return balance;

    }

    public Tuple2<BigDecimal, BigDecimal> getBalance(DCSet db, long key, int actionType) {
        if (key < 0)
            key = -key;

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance = db
                .getAssetBalanceMap().get(getAddress(), key);

        if (actionType == TransactionAmount.ACTION_SEND) {
            if (BlockChain.DEVELOP_USE) {
                if (key == 1)
                    return new Tuple2<BigDecimal, BigDecimal>(balance.a.a, balance.a.b.add(BigDecimal.valueOf(1000)));
                else if (key == 2)
                    return new Tuple2<BigDecimal, BigDecimal>(balance.a.a, balance.a.b.add(BigDecimal.TEN));
            }

            return balance.a;
        } else if (actionType == TransactionAmount.ACTION_DEBT)
            return balance.b;
        else if (actionType == TransactionAmount.ACTION_HOLD)
            return balance.c;
        else if (actionType == TransactionAmount.ACTION_SPEND)
            return balance.d;
        else
            return balance.e;

    }

    /*
     * public void setLastReference(Long timestamp) {
     * this.setLastReference(timestamp, DBSet.getInstance()); }
     */

    // change BALANCE - add or subtract amount by KEY + AMOUNT = TYPE
    public Tuple3<BigDecimal, BigDecimal, BigDecimal> changeBalance(DCSet db, boolean substract, long key,
                                                                    BigDecimal amount_in, boolean notUpdateIncomed) {

        int actionType = actionType(key, amount_in);

        BigDecimal amount = amount_in.abs();
        long absKey;
        if (key > 0) {
            absKey = key;
        } else {
            absKey = -key;
        }

        // for DEBUG
        /*
        if (false
                && this.equals("77HyuCsr8u7f6znj2Lq8gXjK6DCG7osehs") && absKey == 1 && !db.isFork()
                && (actionType == TransactionAmount.ACTION_SEND || actionType == TransactionAmount.ACTION_DEBT)
                && true) {
            ;
        }
        */

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance = db
                .getAssetBalanceMap().get(getAddress(), absKey);

        boolean updateIncomed = !notUpdateIncomed;

        if (actionType == TransactionAmount.ACTION_SEND) {
            // OWN + property
            balance = new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>(
                    substract ? new Tuple2<BigDecimal, BigDecimal>(
                            updateIncomed ? balance.a.a.subtract(amount) : balance.a.a, balance.a.b.subtract(amount))
                            : new Tuple2<BigDecimal, BigDecimal>(updateIncomed ? balance.a.a.add(amount) : balance.a.a,
                            balance.a.b.add(amount)),
                    balance.b, balance.c, balance.d, balance.e);
        } else if (actionType == TransactionAmount.ACTION_DEBT) {
            // DEBT + CREDIT
            balance = new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>(
                    balance.a,
                    substract ? new Tuple2<BigDecimal, BigDecimal>(
                            updateIncomed ? balance.b.a.subtract(amount) : balance.b.a, balance.b.b.subtract(amount))
                            : new Tuple2<BigDecimal, BigDecimal>(updateIncomed ? balance.b.a.add(amount) : balance.b.a,
                            balance.b.b.add(amount)),
                    balance.c, balance.d, balance.e);
        } else if (actionType == TransactionAmount.ACTION_HOLD) {
            // HOLD + STOCK
            balance = new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>(
                    balance.a, balance.b,
                    substract ? new Tuple2<BigDecimal, BigDecimal>(
                            updateIncomed ? balance.c.a.subtract(amount) : balance.c.a, balance.c.b.subtract(amount))
                            : new Tuple2<BigDecimal, BigDecimal>(updateIncomed ? balance.c.a.add(amount) : balance.c.a,
                            balance.c.b.add(amount)),
                    balance.d, balance.e);
        } else if (actionType == TransactionAmount.ACTION_SPEND) {
            // TODO - SPEND + PRODUCE
            balance = new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>(
                    balance.a, balance.b, balance.c,
                    substract ? new Tuple2<BigDecimal, BigDecimal>(
                            updateIncomed ? balance.d.a.subtract(amount) : balance.d.a, balance.d.b.subtract(amount))
                            : new Tuple2<BigDecimal, BigDecimal>(updateIncomed ? balance.d.a.add(amount) : balance.d.a,
                            balance.d.b.add(amount)),
                    balance.e);
        }

        db.getAssetBalanceMap().set(getAddress(), absKey, balance);

        ////////////// DEBUG TOTAL COMPU
        // несотыковка из-за ордеров на бирже
        if (false && absKey == 2l && this.equals("73EotEbxvAo39tyugJSyL5nbcuMWs4aUpS")) {
            Collection<Tuple2<String, Long>> addrs = db.getAssetBalanceMap().getKeys();
            BigDecimal total = BigDecimal.ZERO;
            for (Tuple2<String, Long> addr : addrs) {
                if (addr.b == 2l) {
                    Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> ball =
                            db.getAssetBalanceMap().get(addr);

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

                        int actionType = ((TransactionAmount)transaction).getActionType();
                        if (actionType == TransactionAmount.ACTION_SEND) {
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

    public Long getLastTimestamp() {
        return this.getLastTimestamp(DCSet.getInstance());
    }

    public Long getLastTimestamp(DCSet dcSet) {
        return dcSet.getReferenceMap().getLast(this.getAddress());
    }

    public void setLastTimestamp(Long timestamp, DCSet dcSet) {
        byte[] key = Base58.decode(this.getAddress());
        ReferenceMap map = dcSet.getReferenceMap();

        // GET CURRENT REFERENCE
        Long reference = map.get(key);

        // MAKE KEY for this TIMESTAMP
        byte[] keyTimestamp = Bytes.concat(key, Longs.toByteArray(timestamp));

        // set NEW LAST TIMESTAMP as REFERENCE
        map.set(keyTimestamp, reference);

        // SET NEW REFERENCE
        map.set(key, timestamp);
    }

    public void removeLastTimestamp(DCSet dcSet) {
        byte[] key = Base58.decode(this.getAddress());
        ReferenceMap map = dcSet.getReferenceMap();

        // GET LAST TIMESTAMP
        Long timestamp = map.get(key);

        // MAKE KEY for this TIMESTAMP
        byte[] keyTimestamp = Bytes.concat(key, Longs.toByteArray(timestamp));

        // GET REFERENCE
        Long reference = map.get(keyTimestamp);

        // DELETE TIMESTAMP - REFERENCE
        map.delete(keyTimestamp);
        // SET OLD REFERENCE
        map.set(key, reference);
    }

    // TOSTRING
    public String personChar(Tuple2<Integer, PersonCls> personRes) {
        if (personRes == null)
            return "";

        PersonCls person = personRes.b;
        if (!person.isAlive(0l))
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

        long result = this.getBalanceUSE(FEE_KEY).unscaledValue().longValue();
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
                // + NumberAsString.formatAsString(this.getBalanceUSE(FEE_KEY))
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

        return (statusBad ? "??? " : "") + NumberAsString.formatAsString(this.getBalanceUSE(key)) + " {"
                //+ NumberAsString.formatAsString(this.getBalanceUSE(FEE_KEY))
                + viewFEEbalance()
                + "}" + " " + addressStr + "" + personStr;
    }

    //////////
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
        // more effective VS Base58 or Base64
        return new BigInteger(shortBytes).hashCode();
    }

    // EQUALS
    @Override
    public boolean equals(Object b) {
        if (b instanceof Account) {
            return this.address.equals(((Account) b).getAddress());
        } else if (b instanceof String) {
            return this.address.equals(b);
        } else if (b instanceof byte[]) {
            byte[] bs = (byte[]) b;
            if (bytes.length == ADDRESS_LENGTH) {
                return Arrays.equals(this.bytes, bs);
            } else {
                return Arrays.equals(this.shortBytes, bs);
            }
        }

        return false;
    }

    public Tuple4<Long, Integer, Integer, Integer> getPersonDuration(DCSet db) {
        if (this.personDuration == null) {
            this.personDuration =  db.getAddressPersonMap().getItem(address);

        }
        return this.personDuration;
    }

    public boolean isPerson(DCSet dcSet, int forHeight) {

        // IF DURATION ADDRESS to PERSON IS ENDED
        Tuple4<Long, Integer, Integer, Integer> addressDuration = this.getPersonDuration(dcSet);
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

        // IF PERSON ALIVE
        Long personKey = addressDuration.a;
        // TODO by deth day if
        /*
         * //Tuple5<Long, Long, byte[], Integer, Integer> personDuration =
         * db.getPersonStatusMap().getItem(personKey, ALIVE_KEY); // TEST TIME
         * and EXPIRE TIME for ALIVE person Long end_date = personDuration.b; if
         * (end_date == null ) return true; // permanent active if (end_date <
         * current_time + 86400000l ) return false; // - 1 day
         */

        return true;

    }

    public boolean isPerson() {
        return isPerson(DCSet.getInstance(), Controller.getInstance().getMyHeight());
    }

    /*
     * public void setForgingData(DBSet db, int height, int prevHeight) {
     * db.getAddressForging().set(this.address, height, prevHeight); }
     */

    public Tuple2<Integer, PersonCls> getPerson(DCSet dcSet, int forHeight) {

        // IF DURATION ADDRESS to PERSON IS ENDED
        Tuple4<Long, Integer, Integer, Integer> addressDuration = this.getPersonDuration(dcSet);
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

        // IF PERSON is ALIVE
        // TODO by DEATH day
        /*
         * Tuple5<Long, Long, byte[], Integer, Integer> personDuration =
         * db.getPersonStatusMap().getItem(personKey, ALIVE_KEY); // TEST TIME
         * and EXPIRE TIME for ALIVE person if (personDuration == null) return
         * new Tuple2<Integer, PersonCls>(-2, person); Long end_date =
         * personDuration.b; if (end_date == null ) // permanent active return
         * new Tuple2<Integer, PersonCls>(0, person); else if (end_date <
         * current_time + 86400000l ) // ALIVE expired return new Tuple2<Integer,
         * PersonCls>(-1, person);
         */

        return new Tuple2<Integer, PersonCls>(1, person);

    }

    public Tuple2<Integer, PersonCls> getPerson() {
        return getPerson(DCSet.getInstance(), Controller.getInstance().getMyHeight());
    }

    // previous forging block or changed ERA volume
    public Tuple2<Integer, Integer> getForgingData(DCSet db, int height) {
        return db.getAddressForging().get(this.address, height);
    }
    /*
     * public void setLastForgingData(DCSet db, int height) { getAddressForging
     * = this.getBal USE db.getAddressForging().setLast(this.address, height,
     * forgingBalance); }
     */

    public void setForgingData(DCSet db, int height, int forgingBalance) {
        db.getAddressForging().set(this.address, height, forgingBalance);
    }

    public void delForgingData(DCSet db, int height) {
        db.getAddressForging().delete(this.address, height);
    }

    public Tuple2<Integer, Integer> getLastForgingData(DCSet db) {
        return db.getAddressForging().getLast(this.address);
    }

    public Tuple2<String, String> getName() {

        return Controller.getInstance().wallet.database.getAccountsPropertisMap().get(this.getAddress());

    }

    public int getAccountNo() {
        return Controller.getInstance().wallet.database.getAccountMap().getAccountNo(this.address);
    }

}
