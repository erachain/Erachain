package org.erachain.database.telegram;
// 30/03 ++

import org.erachain.core.account.PublicKeyAccount;
import org.erachain.datachain.IDB;
import org.erachain.settings.Settings;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;

//import org.mapdb.Serializer;

public class TelegramSet implements IDB {

    private static final String VERSION = "version";
    private DB database;
    private int uses;
    private AllTelegramsMap telegramsMap;
    File dbFile;

    public TelegramSet() {

        dbFile = new File(Settings.getInstance().getTelegramDir(), "telegram.dat");
        dbFile.getParentFile().mkdirs();

        // DELETE TRANSACTIONS
        // File transactionFile = new
        // File(Settings.getInstance().getWalletDir(), "wallet.dat.t");
        // transactionFile.delete();

        this.database = DBMaker.newFileDB(dbFile)
                // .cacheSize(2048)
                // .cacheDisable()
                .checksumEnable()
                .mmapFileEnableIfSupported()
                .transactionDisable() // DISABLE TRANSACTIONS
                .make();

        uses = 0;

        this.telegramsMap = new AllTelegramsMap(this, this.database);

    }

    public boolean exists() {
        return dbFile.exists();
    }

    public int getVersion() {
        this.uses++;
        int u = this.database.getAtomicInteger(VERSION).intValue();
        this.uses--;
        return u;
    }

    public void setVersion(int version) {
        this.uses++;
        this.database.getAtomicInteger(VERSION).set(version);
        this.uses--;
    }

    public void addUses() {
        this.uses++;

    }

    @Override
    public void outUses() {
        this.uses--;
    }

    @Override
    public boolean isBusy() {
        if (this.uses > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void openDBSet() {

    }

    public AllTelegramsMap getTelegramsMap() {
        return this.telegramsMap;
    }

    public void delete(PublicKeyAccount account) {
        this.uses++;
        this.telegramsMap.deleteFromAccount(account);
        this.uses--;

    }

    @Override
    public void commit() {
        this.uses++;
        this.database.commit();
        this.uses--;

    }

    @Override
    public void close() {
        if (this.database != null) {
            if (!this.database.isClosed()) {
                this.uses++;
                // this.database.rollback();
                this.database.commit();
                this.database.close();
                this.uses--;

            }
        }
    }
}