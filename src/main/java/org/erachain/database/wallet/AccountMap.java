package org.erachain.database.wallet;

import com.google.common.primitives.UnsignedBytes;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.database.DBMap;
import org.erachain.utils.ObserverMessage;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import java.math.BigDecimal;
import java.util.*;

// UNCONFIRMED balances for accounts in owner wallet only
public class AccountMap extends DBMap <String, Integer> {

    private static final String ADDRESS_ASSETS = "address_assets";
    private static final String ADDRESSES = "addresses";
    private static final String ADDRESSES_NO = "addresses_nomer";

    private Map<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> assetsBalanceMap;
    private Set<byte[]> publickKeys;

    public AccountMap(DWSet dWSet, DB database) {
        super(dWSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_ALL_ACCOUNT_TYPE);
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_ALL_ACCOUNT_TYPE);
            this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_ACCOUNT_TYPE);
            this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_ACCOUNT_TYPE);
        }

    }

    @Override
    protected void createIndexes(DB database) {
    }

    @Override
    protected void getMap(DB database) {
        this.publickKeys = database.createTreeSet(ADDRESSES)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .serializer(BTreeKeySerializer.BASIC)
                .makeOrGet();

        this.assetsBalanceMap = database.getTreeMap(ADDRESS_ASSETS);
        map = database.getTreeMap(ADDRESSES_NO);
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<String, Integer>();
    }

    @Override
    protected Integer getDefaultValue() {
        return null;
    }


    public List<Account> getAccounts() {

        List<Account> accounts = new ArrayList<Account>();

        synchronized (this.publickKeys) {

            for (byte[] publickKey : this.publickKeys) {
                accounts.add(new PublicKeyAccount(publickKey));
            }
        }

        return accounts;
    }

    public List<PublicKeyAccount> getPublicKeyAccounts() {
        List<PublicKeyAccount> accounts = new ArrayList<PublicKeyAccount>();

        synchronized (this.publickKeys) {


            for (byte[] publickKey : this.publickKeys) {
                accounts.add(new PublicKeyAccount(publickKey));
            }
        }

        return accounts;
    }

    // collect account + assets in wallet
    public List<Tuple2<Account, Long>> getAccountsAssets() {

        List<Tuple2<Account, Long>> account_assets = new ArrayList<Tuple2<Account, Long>>();

        Collection<Tuple2<String, Long>> keys = this.assetsBalanceMap.keySet();

        //for(PublicKeyAccount publickKey: this.publickKeys)
        for (Tuple2<String, Long> key : keys) {
            account_assets.add(new Tuple2<Account, Long>(new Account(key.a), key.b));
        }

        return account_assets;
    }

    // collect address + assets in wallet
    public Set<Tuple2<String, Long>> getAddressessAssets() {

        Set<Tuple2<String, Long>> keys = this.assetsBalanceMap.keySet();

        return keys;
    }

    public boolean exists(String address) {
        //return this.assetsBalanceMap.containsKey(address);
        for (byte[] publickKey : this.publickKeys) {
            PublicKeyAccount account = new PublicKeyAccount(publickKey);
            if (account.getAddress().equals(address)) return true;
        }
        //return this.publickKeys.containsKey(address);
        return false;
    }

    public Account getAccount(String address) {
        return getPublicKeyAccount(address);
    }

    public PublicKeyAccount getPublicKeyAccount(String address) {

        synchronized (this.publickKeys) {
            for (byte[] publickKeyBytes : this.publickKeys) {
                PublicKeyAccount publickKey = new PublicKeyAccount(publickKeyBytes);
                if (publickKey.getAddress().equals(address)) {
                    return publickKey;
                }
            }
        }

        return null;
    }

    private Tuple3<BigDecimal, BigDecimal, BigDecimal> getBalanceNull() {
        return new Tuple3<BigDecimal, BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    // change BALANCE - add or subtract amount by KEY + AMOUNT = TYPE
    public Tuple3<BigDecimal, BigDecimal, BigDecimal> changeBalance(String address, boolean subtract, long key, BigDecimal amount) {

        int actionType = Account.actionType(key, amount);
        long absKey;
        if (key > 0) {
            absKey = key;
        } else {
            absKey = -key;
        }

        Tuple2<String, Long> k = new Tuple2<String, Long>(address, absKey);

        if (!this.assetsBalanceMap.containsKey(k))
            return getBalanceNull();

        Tuple3<BigDecimal, BigDecimal, BigDecimal> balance = this.assetsBalanceMap.get(k);

        if (actionType == TransactionAmount.ACTION_SEND) {
            // OWN + property
            balance = new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
                    subtract ? balance.a.subtract(amount) : balance.a.add(amount),
                    balance.b, balance.c
            );
        } else if (actionType == TransactionAmount.ACTION_DEBT) {
            // DEBT + CREDIT
            balance = new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
                    balance.a,
                    subtract ? balance.b.subtract(amount) : balance.b.add(amount),
                    balance.c
            );
        } else if (actionType == TransactionAmount.ACTION_HOLD) {
            // HOLD + STOCK
            balance = new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
                    balance.a, balance.b,
                    subtract ? balance.c.subtract(amount) : balance.c.add(amount)
            );
        } else if (actionType == TransactionAmount.ACTION_SPEND) {
            // TODO - SPEND + PRODUCE
            balance = new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
                    balance.a, balance.b,
                    subtract ? balance.c.subtract(amount) : balance.c.add(amount)
            );
        }

        this.assetsBalanceMap.put(k, balance);
        return balance;
    }

    private Tuple3<BigDecimal, BigDecimal, BigDecimal> getBalance(String address, Long key) {

        if (key < 0) {
            key = -key;
        }

        Tuple2<String, Long> k = new Tuple2<String, Long>(address, key);

        if (!this.assetsBalanceMap.containsKey(k))
            return getBalanceNull();

        return this.assetsBalanceMap.get(k);
    }

    public Tuple3<BigDecimal, BigDecimal, BigDecimal> getBalance(Account account, Long key) {
        return getBalance(account.getAddress(), key);
    }

    // ADD AN PUBLIC KEY ACCOUNT in wallet
    public void add(PublicKeyAccount account, Integer number) {

        synchronized (this.publickKeys) {
            if (!this.publickKeys.contains(account.getPublicKey())) {
                this.publickKeys.add(account.getPublicKey());
                if (number < 0) {
                    number = Controller.getInstance().wallet.getAccountNonce();
                }

                // USE NOTIFY
                super.set(account.getAddress(), number);

            }
        }
    }

    public Integer getAccountNo(String account) {

        return map.get(account);
    }

    // delete all assets for this account
    public void delete(PublicKeyAccount account) {

        Map<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> keys = ((BTreeMap) this.assetsBalanceMap).subMap(
                //BTreeMap keys = ((BTreeMap) this.assetsBalanceMap).subMap(
                Fun.t2(account.getAddress(), null),
                Fun.t2(account.getAddress(), Fun.HI()));

        /*
		if(this.publickKeys == null)
		{
			this.loadPublickKeys();
		}
		*/

        synchronized (this.publickKeys) {
            //DELETE NAMES
            for (Tuple2<String, Long> key : keys.keySet()) {

                this.assetsBalanceMap.remove(key);
            }

            this.publickKeys.remove(account.getPublicKey());

            // USE NOTIFY
            super.delete(account.getAddress());

        }
    }

    public void reset() {
        synchronized (this.publickKeys) {
            this.publickKeys.clear();
            this.assetsBalanceMap.clear();

            this.map.clear();
        }

    }

}
