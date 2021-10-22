package org.erachain;

import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.wallet.Wallet;
import org.erachain.database.wallet.DWSet;
import org.erachain.database.wallet.SecureWalletDatabase;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
@Ignore
public class WalletTests {

    byte[] releaserReference = null;

    boolean asPack = false;
    long FEE_KEY = AssetCls.FEE_KEY;
    long VOTE_KEY = AssetCls.ERA_KEY;
    byte FEE_POWER = (byte) 1;
    byte[] noteReference = new byte[64];
    long timestamp = NTP.getTime();
    String password = "test";
    Wallet wallet;
    boolean create;
    //CREATE EMPTY MEMORY DATABASE
    private DWSet database;
    private SecureWalletDatabase secureDatabase;
    private GenesisBlock gb;

    // INIT NOTES
    private void init() {

        database = DWSet.reCreateDB(DCSet.getInstance(), false, false);
        secureDatabase = new SecureWalletDatabase(password);

        wallet = new Wallet(DCSet.getInstance(), false, false);
        create = wallet.create(secureDatabase, Crypto.getInstance()
                .digest(password.getBytes()), 10, false, false);
    }

    @Test
    public void createWallet() throws Exception {
        //CREATE DATABASE
        //CREATE WALLET
        init();

        //CHECK CREATE
        assertEquals(true, create);

        //CHECK VERSION
        assertEquals(1, wallet.getVersion());

        //CHECK ADDRESSES
        assertEquals(10, wallet.getAccounts().size());

        //CHECK PRIVATE KEYS
        assertEquals(10, wallet.getprivateKeyAccounts().size());

        //CHECK LAST BLOCKS
        assertNotNull(wallet.getLastBlocks(50));

        //CHECK LAST TRANSACTIONS
        assertNotNull(wallet.getLastTransactions(100));
    }

    @Test
    public void lockUnlock() {

        //CREATE DATABASE
        //CREATE WALLET
        init();

        //CHECK UNLOCKED
        assertEquals(true, wallet.isUnlocked());

        //LOCK
        wallet.lock();

        //CHECK LOCKED
        assertEquals(false, wallet.isUnlocked());

        //CHECK ACCOUNTS
        assertEquals(null, wallet.getprivateKeyAccounts());

        //UNLOCK
        wallet.unlock(secureDatabase);

        //CHECK UNLOCKED
        assertEquals(true, wallet.isUnlocked());

        //CHECK ACCOUNTS
        assertEquals(10, wallet.getprivateKeyAccounts().size());
    }

}
