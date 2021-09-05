package org.erachain.database.wallet;

import com.google.common.primitives.UnsignedBytes;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DCUMapImpl;
import org.erachain.utils.ObserverMessage;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import java.math.BigDecimal;
import java.util.*;

/**
 * UNCONFIRMED balances for accounts in owner wallet only
 */
public class AccountMap extends DCUMapImpl<String, Integer> {

    private static final String ADDRESS_ASSETS = "address_assets";
    private static final String ADDRESSES = "addresses";
    private static final String ADDRESSES_NO = "addresses_number";

    /**
     * address + itemKey -> balance
     */
    private Map<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> assetsBalanceMap;
    private Set<byte[]> publicKeys;

    List<Account> accounts;
    List<PublicKeyAccount> accountPubKeys;


    public AccountMap(DWSet dWSet, DB database) {
        super(dWSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.RESET_ALL_ACCOUNT_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.LIST_ALL_ACCOUNT_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.ADD_ACCOUNT_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.REMOVE_ACCOUNT_TYPE);
        }
    }

    @Override
    public void openMap() {
        this.publicKeys = database.createTreeSet(ADDRESSES)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .serializer(BTreeKeySerializer.BASIC)
                .makeOrGet();

        this.assetsBalanceMap = database.getTreeMap(ADDRESS_ASSETS);
        map = database.getTreeMap(ADDRESSES_NO);

        accounts = getAccounts();
        accountPubKeys = getPublicKeyAccounts();
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<String, Integer>();
    }

    public List<Account> getAccounts() {

        if (accounts != null)
            return accounts;

        List<Account> accounts = new ArrayList<Account>();

        synchronized (this.publicKeys) {

            for (byte[] publickKey : this.publicKeys) {
                accounts.add(new PublicKeyAccount(publickKey));
            }
        }

        return accounts;
    }

    public List<PublicKeyAccount> getPublicKeyAccounts() {

        if (accountPubKeys != null)
            return accountPubKeys;

        List<PublicKeyAccount> accounts = new ArrayList<PublicKeyAccount>();

        synchronized (this.publicKeys) {

            for (byte[] publickKey : this.publicKeys) {
                accounts.add(new PublicKeyAccount(publickKey));
            }
        }

        return accounts;
    }

    // collect account + assets in wallet
    public List<Tuple2<Account, Long>> getAccountsAssets() {

        List<Tuple2<Account, Long>> account_assets = new ArrayList<Tuple2<Account, Long>>();

        Collection<Tuple2<String, Long>> keys = this.assetsBalanceMap.keySet();

        for (Tuple2<String, Long> key : keys) {
            account_assets.add(new Tuple2<Account, Long>(new Account(key.a), key.b));
        }

        return account_assets;
    }

    // collect address + assets in wallet
    public Set<Tuple2<String, Long>> getAddressesAssets() {

        Set<Tuple2<String, Long>> keys = this.assetsBalanceMap.keySet();

        return keys;
    }

    public boolean exists(String address) {
        if (address == null)
            return false;

        for (Account account : this.accounts) {
            if (account.equals(address)) return true;
        }
        return false;
    }

    public boolean exists(Account account) {
        if (account == null)
            return false;

        for (Account myAaccount : this.accounts) {
            if (myAaccount.equals(account))
                return true;
        }
        return false;
    }

    public Account getAccount(String address) {
        return getPublicKeyAccount(address);
    }

    public PublicKeyAccount getPublicKeyAccount(String address) {

        for (PublicKeyAccount account : this.accountPubKeys) {
            if (account.equals(address)) {
                return account;
            }
        }

        return null;
    }

    private Tuple3<BigDecimal, BigDecimal, BigDecimal> getBalanceNull() {
        return new Tuple3<BigDecimal, BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    // change BALANCE - add or subtract amount by KEY + AMOUNT = TYPE
    public Tuple3<BigDecimal, BigDecimal, BigDecimal> changeBalance(String address, boolean subtract, long key, BigDecimal amount, boolean isBackward, boolean isDirect) {

        int actionType = Account.balancePosition(key, amount, isBackward, isDirect);
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
    public void add(PublicKeyAccount pubKeyAccount, Integer number) {

        synchronized (this.publicKeys) {
            if (!this.publicKeys.contains(pubKeyAccount.getPublicKey())) {
                this.publicKeys.add(pubKeyAccount.getPublicKey());
                this.accountPubKeys.add(pubKeyAccount);
                this.accounts.add(pubKeyAccount);

                if (number < 0 && Controller.getInstance().doesWalletExists()) {
                    number = Controller.getInstance().getWallet().getAccountNonce();
                }

                // USE NOTIFY
                super.put(pubKeyAccount.getAddress(), number);

            }
        }
    }

    public Integer getAccountNo(String account) {
        return map.get(account);
    }

    // delete all assets for this account
    public void delete(PublicKeyAccount accountPublicKey) {

        Map<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> keys = ((BTreeMap) this.assetsBalanceMap).subMap(
                //BTreeMap keys = ((BTreeMap) this.assetsBalanceMap).subMap(
                Fun.t2(accountPublicKey.getAddress(), null),
                Fun.t2(accountPublicKey.getAddress(), Long.MAX_VALUE));

        /*
		if(this.publickKeys == null)
		{
			this.loadPublickKeys();
		}
		*/

        synchronized (this.publicKeys) {
            //DELETE NAMES
            for (Tuple2<String, Long> key : keys.keySet()) {

                this.assetsBalanceMap.remove(key);
            }

            this.publicKeys.remove(accountPublicKey.getPublicKey());
            this.publicKeys.remove(accountPublicKey);
            this.accountPubKeys.remove((Account) accountPublicKey);

            // USE NOTIFY
            super.delete(accountPublicKey.getAddress());

        }
    }

    public void clear() {
        synchronized (this.publicKeys) {
            this.publicKeys.clear();
            this.accountPubKeys.clear();
            this.accounts.clear();

            this.assetsBalanceMap.clear();

            this.map.clear();
        }

    }

}
