package org.erachain.dbs.rocksDB.utils;

import org.erachain.settings.Settings;

import java.io.File;

public class ConstantsRocksDB {
    public static String ROCKS_DB_FOLDER = Settings.getInstance().getDataDir() + File.separator + "RocksDB";
    public static String ROCKS_DB_WALLET_FOLDER = "./RocksDBWallet";
    public static int SIZE_ADDRESS = 20;

}
