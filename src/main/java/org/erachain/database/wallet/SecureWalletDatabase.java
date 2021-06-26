package org.erachain.database.wallet;

import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.settings.Settings;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

public class SecureWalletDatabase {

    private static final String SEED = "seed";
    private static final String NONCE = "nonce";

    private DB database;

    private AccountSeedMap accountSeedMap;

    public SecureWalletDatabase(String password) {
        //OPEN WALLET
        Settings.SECURE_WALLET_FILE.getParentFile().mkdirs();

        //DELETE TRANSACTIONS
        //File transactionFile = new File(Settings.getInstance().getWalletDir(), "wallet.s.dat.t");
        //transactionFile.delete();

        // тут база данных еще пустая или закрыта выше по .lock()
        this.database = DBMaker.newFileDB(Settings.SECURE_WALLET_FILE)
                .encryptionEnable(password)
                // убрал .closeOnJvmShutdown() it closing not by my code and rise errors! closed before my closing
                .cacheSize(1 << 11)
                .checksumEnable()
                .mmapFileEnableIfSupported()
                .make();

        this.accountSeedMap = new AccountSeedMap(this, this.database);
    }

   // public static boolean exists() {
   //     return SECURE_WALLET_FILE.exists();
   // }

    public AccountSeedMap getAccountSeedMap() {
        return this.accountSeedMap;
    }


    public byte[] getSeed() {
        return (byte[]) this.database.getAtomicVar(SEED).get();
    }

    public void setSeed(byte[] seed) {
        this.database.createAtomicVar(SEED, seed, Serializer.BYTE_ARRAY);
    }

    public int getNonce() {
        return this.database.getAtomicInteger(NONCE).intValue();
    }

    public void setNonce(int nonce) {
        this.database.getAtomicInteger(NONCE).set(nonce);
    }

    private int getAndIncrementNonce() {
        return this.database.getAtomicInteger(NONCE).getAndIncrement();
    }

    public synchronized int addPrivateKey(PrivateKeyAccount privateKey) {

        int nonce = getAndIncrementNonce();

        // ADD TO DATABASE
        getAccountSeedMap().add(privateKey);

        return nonce;
    }

    public void commit() {
        this.database.commit();
    }

    public void close() {
        if (this.database != null) {
            if (!this.database.isClosed()) {
                this.database.commit();
                this.database.close();
            }
        }
    }
}

