package org.erachain.database.wallet;

import com.google.common.primitives.UnsignedBytes;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AccountSeedMap {

    private static final String ACCOUNT_SEEDS = "accountSeeds";

    private Set<byte[]> accountSeedsSet;

    private List<PrivateKeyAccount> privateKeyAccounts;

    public AccountSeedMap(SecureWalletDatabase secureWalletDatabase, DB database) {
        //OPEN MAP
        this.accountSeedsSet = database.createTreeSet(ACCOUNT_SEEDS)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .serializer(BTreeKeySerializer.BASIC)
                .makeOrGet();
    }

    private void loadPrivateKeyAccounts() {
        //RESET ACCOUNTS LIST
        this.privateKeyAccounts = new ArrayList<PrivateKeyAccount>();

        synchronized (this.privateKeyAccounts) {
            for (byte[] accountSeed : accountSeedsSet) {
                //CREATE ACCOUNT FROM ADDRESS
                PrivateKeyAccount privateKeyAccount = new PrivateKeyAccount(accountSeed);

                //ADD TO LIST
                this.privateKeyAccounts.add(privateKeyAccount);
            }
        }
    }

    public List<PrivateKeyAccount> getPrivateKeyAccounts() {
        if (this.privateKeyAccounts == null) {
            this.loadPrivateKeyAccounts();
        }

        return this.privateKeyAccounts;
    }


    public PrivateKeyAccount getPrivateKeyAccount(String address) {
        if (this.privateKeyAccounts == null) {
            this.loadPrivateKeyAccounts();
        }

        synchronized (this.privateKeyAccounts) {
            for (PrivateKeyAccount privateKeyAccount : this.privateKeyAccounts) {
                if (privateKeyAccount.getAddress().equals(address)) {
                    return privateKeyAccount;
                }
            }
        }

        return null;
    }

    public PrivateKeyAccount getPrivateKeyAccount(Account account) {
        if (this.privateKeyAccounts == null) {
            this.loadPrivateKeyAccounts();
        }

        synchronized (this.privateKeyAccounts) {
            for (PrivateKeyAccount privateKeyAccount : this.privateKeyAccounts) {
                if (privateKeyAccount.equals(account)) {
                    return privateKeyAccount;
                }
            }
        }

        return null;
    }

    public PublicKeyAccount getPublicKeyAccount(String address) {
        if (this.privateKeyAccounts == null) {
            this.loadPrivateKeyAccounts();
        }

        synchronized (this.privateKeyAccounts) {
            for (PrivateKeyAccount privateKeyAccount : this.privateKeyAccounts) {
                if (privateKeyAccount.getAddress().equals(address)) {
                    return new PublicKeyAccount(privateKeyAccount.getPublicKey());
                }
            }
        }

        return null;
    }

    public void add(PrivateKeyAccount account) {

        this.accountSeedsSet.add(account.getSeed());

        if (this.privateKeyAccounts == null) {
            this.loadPrivateKeyAccounts();
        } else {
            synchronized (this.privateKeyAccounts) {
                this.privateKeyAccounts.add(account);
            }
        }
    }

    public void delete(PrivateKeyAccount account) {
        this.accountSeedsSet.remove(account.getSeed());

        if (this.privateKeyAccounts == null) {
            this.loadPrivateKeyAccounts();
        }

        synchronized (this.privateKeyAccounts) {
            this.privateKeyAccounts.remove(account);
        }
    }


}
