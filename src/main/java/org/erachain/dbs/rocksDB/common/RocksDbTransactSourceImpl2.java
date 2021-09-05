package org.erachain.dbs.rocksDB.common;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.Transacted;
import org.erachain.dbs.rocksDB.RockStoreIterator;
import org.erachain.dbs.rocksDB.RockStoreIteratorFilter;
import org.erachain.dbs.rocksDB.RockStoreIteratorStart;
import org.erachain.dbs.rocksDB.exceptions.UnsupportedRocksDBOperationException;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.erachain.dbs.rocksDB.utils.ByteUtil;
import org.erachain.dbs.rocksDB.utils.FileUtil;
import org.erachain.settings.Settings;
import org.mapdb.Fun;
import org.rocksdb.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;
import static org.erachain.utils.ByteArrayUtils.areEqualMask;

/**
 * Самый низкий уровень доступа к функциям RocksDB
 */
@Slf4j
public class RocksDbTransactSourceImpl2 implements RocksDbDataSource, Transacted // implements DB<byte[], byte[]>
        //, Flusher, DbSourceInter<byte[]>
{
    protected String dataBaseName;

    // Это включает логирование данных на диск синхронизированно - защищает от утрат при КРАХЕ но чуть медленне работает
    // Если ЛОЖЬ то данные утрачиваются при КРАХЕ
    protected boolean dbSync = true;

    protected WriteOptions writeOptions = new WriteOptions().setSync(dbSync).setDisableWAL(false);
    protected ReadOptions transactReadOptions = new ReadOptions();

    ///@Getter
    public Transaction dbCore;
    @Getter
    public TransactionDB dbCoreParent;

    protected boolean alive;
    protected String parentName;
    List<IndexDB> indexes;
    RocksDbSettings settings;
    protected boolean create = false;
    protected String sizeDescriptorName = "size";
    @Getter
    protected List<ColumnFamilyHandle> columnFamilyHandles;


    protected ColumnFamilyHandle columnFamilyFieldSize;
    protected ByteableInteger byteableInteger = new ByteableInteger();


    protected ReadWriteLock resetDbLock = new ReentrantReadWriteLock();

    /*
    static {
        try {
            logger.info("load libraries");
            loadLibrary(new ArrayList<String>() {{
                add(".");
            }});
            logger.info("loaded success");
        } catch (Throwable throwable) {
            logger.error(throwable.getMessage(), throwable);
        }
    }

     */

    public RocksDbTransactSourceImpl2(String parentName, String name, List<IndexDB> indexes, RocksDbSettings settings) {
        this.dataBaseName = name;
        this.parentName = parentName;
        this.indexes = indexes;
        this.settings = settings;
        initDB();

    }

    public RocksDbTransactSourceImpl2(String name, List<IndexDB> indexes, RocksDbSettings settings) {
        this(Settings.getInstance().getDataChainPath() + ROCKS_DB_FOLDER, name, indexes, settings);
    }

    @Override
    public Path getDbPathAndFile() {
        return Paths.get(parentName, dataBaseName);
    }

    @Override
    public RocksDB getDbCore() {
        return dbCoreParent;
    }


    @Override
    public boolean isAlive() {
        return alive;
    }

    protected boolean quitIfNotAlive() {
        if (!isAlive()) {
            logger.warn("db is not alive");
        }
        return !isAlive();
    }

    @Override
    public Set<byte[]> keySet() throws RuntimeException {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        Set<byte[]> result = new TreeSet(Fun.BYTE_ARRAY_COMPARATOR);
        try (final RocksIterator iter = getIterator()) {
            for (iter.seekToFirst(); iter.isValid(); iter.next()) {
                result.add(iter.key());
            }
            return result;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public List<byte[]> values() throws RuntimeException {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        List<byte[]> result = new ArrayList<byte[]>();
        try (final RocksIterator iter = getIterator()) {
            for (iter.seekToFirst(); iter.isValid(); iter.next()) {
                result.add(iter.value());
            }
            return result;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public Set<byte[]> filterApprropriateKeys(byte[] filter) throws RuntimeException {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        Set<byte[]> result = new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);
        try (final RocksIterator iter = getIterator()) {
            for (iter.seek(filter); iter.isValid() && areEqualMask(iter.key(), filter); iter.next()) {
                result.add(iter.key());
            }
            return result;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public List<byte[]> filterApprropriateValues(byte[] filter) throws RuntimeException {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        List<byte[]> result = new ArrayList<byte[]>();
        try (final RocksIterator iter = getIterator()) {
            for (iter.seek(filter); iter.isValid() && areEqualMask(iter.key(), filter); iter.next()) {
                result.add(iter.value());
            }
            return result;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public Set<byte[]> filterApprropriateValues(byte[] filter, ColumnFamilyHandle indexDB) throws RuntimeException {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        Set<byte[]> result = new TreeSet<>();
        try (final RocksIterator iter = getIterator(indexDB)) {
            for (iter.seek(filter); iter.isValid() && areEqualMask(iter.key(), filter); iter.next()) {
                result.add(iter.value());
            }
            return result;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public Set<byte[]> filterApprropriateValues(byte[] filter, int indexDB) throws RuntimeException {
        return filterApprropriateValues(filter, columnFamilyHandles.get(indexDB));
    }

    @Override
    public String getDBName() {
        return dataBaseName;
    }

    //public void initDB() {
    //    initDB(RocksDbSettings.getSettings());
    //}

    protected void initDB() {
        resetDbLock.writeLock().lock();
        try {
            if (isAlive()) {
                return;
            }

            Preconditions.checkNotNull(dataBaseName, "no name set to the dbStore");
            try (final ColumnFamilyOptions cfOpts = new ColumnFamilyOptions().optimizeUniversalStyleCompaction()) {
                final List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
                columnFamilyHandles = new ArrayList<>();
                addIndexColumnFamilies(indexes, cfOpts, columnFamilyDescriptors);
                BlockBasedTableConfig tableCfgCf = settingsBlockBasedTable(settings);
                cfOpts.setTableFormatConfig(tableCfgCf);
                try (Options options = new Options()) {

                    if (settings.isEnableStatistics()) {
                        options.setStatistics(new Statistics());
                        options.setStatsDumpPeriodSec(60);
                    }

                    options.setCreateIfMissing(true);
                    options.setIncreaseParallelism(3);
                    options.setLevelCompactionDynamicLevelBytes(true);
                    options.setMaxOpenFiles(settings.getMaxOpenFiles());

                    options.setNumLevels(settings.getLevelNumber());
                    options.setMaxBytesForLevelMultiplier(settings.getMaxBytesForLevelMultiplier());
                    options.setMaxBytesForLevelBase(settings.getMaxBytesForLevelBase());
                    options.setMaxBackgroundCompactions(settings.getCompactThreads());
                    options.setLevel0FileNumCompactionTrigger(settings.getLevel0FileNumCompactionTrigger());
                    options.setTargetFileSizeMultiplier(settings.getTargetFileSizeMultiplier());
                    options.setTargetFileSizeBase(settings.getTargetFileSizeBase());

                    BlockBasedTableConfig tableCfg = settingsBlockBasedTable(settings);
                    options.setTableFormatConfig(tableCfg);
                    options.setAllowConcurrentMemtableWrite(true);
                    options.setMaxManifestFileSize(0);
                    options.setWalTtlSeconds(0);
                    options.setWalSizeLimitMB(0);
                    options.setLevel0FileNumCompactionTrigger(1);
                    options.setMaxBackgroundFlushes(4);
                    options.setMaxBackgroundCompactions(8);
                    options.setMaxSubcompactions(4);
                    options.setMaxWriteBufferNumber(3);
                    options.setMinWriteBufferNumberToMerge(2);

                    int dbWriteBufferSize = 512 * 1024 * 1024;
                    options.setDbWriteBufferSize(dbWriteBufferSize);
                    options.setParanoidFileChecks(false);
                    options.setCompressionType(CompressionType.NO_COMPRESSION);

                    options.setParanoidChecks(false);
                    options.setAllowMmapReads(true);
                    options.setAllowMmapWrites(true);
                    try {
                        logger.info("Opening database");
                        final Path dbPath = getDbPathAndFile();
                        if (!Files.isSymbolicLink(dbPath.getParent())) {
                            Files.createDirectories(dbPath.getParent());
                        }

                        try (final DBOptions dbOptions = new DBOptions()) {
                            TransactionDBOptions transactionDbOptions = new TransactionDBOptions();
                            try {

                                // MAKE DATABASE
                                // USE transactions
                                dbCoreParent = TransactionDB.open(options, transactionDbOptions, dbPath.toString());

                                columnFamilyHandles.add(dbCoreParent.getDefaultColumnFamily());
                                int indexID = 0;
                                for (ColumnFamilyDescriptor columnFamilyDescriptor : columnFamilyDescriptors) {
                                    ColumnFamilyHandle columnFamilyHandle = dbCoreParent.createColumnFamily(columnFamilyDescriptor);
                                    columnFamilyHandles.add(columnFamilyHandle);
                                    if (indexes != null && indexes.size() > indexID)
                                        indexes.get(indexID++).setColumnFamilyHandle(columnFamilyHandle);
                                }
                                create = true;
                                logger.info("database created");
                            } catch (RocksDBException e) {
                                dbOptions.setCreateIfMissing(true);
                                dbOptions.setCreateMissingColumnFamilies(true);
                                dbOptions.setIncreaseParallelism(3);
                                dbOptions.setMaxOpenFiles(settings.getMaxOpenFiles());
                                dbOptions.setMaxBackgroundCompactions(settings.getCompactThreads());
                                dbOptions.setAllowConcurrentMemtableWrite(true);
                                dbOptions.setMaxManifestFileSize(0);
                                dbOptions.setWalTtlSeconds(0);
                                dbOptions.setWalSizeLimitMB(0);
                                dbOptions.setMaxBackgroundFlushes(4);
                                dbOptions.setMaxBackgroundCompactions(8);
                                dbOptions.setMaxSubcompactions(4);
                                dbOptions.setDbWriteBufferSize(dbWriteBufferSize);

                                dbOptions.setParanoidChecks(false);
                                dbOptions.setAllowMmapReads(true);
                                dbOptions.setAllowMmapWrites(true);

                                columnFamilyDescriptors.clear();
                                columnFamilyDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOpts));
                                addIndexColumnFamilies(indexes, cfOpts, columnFamilyDescriptors);

                                // MAKE DATABASE
                                // USE transactions
                                dbCoreParent = TransactionDB.open(dbOptions, transactionDbOptions,
                                        dbPath.toString(), columnFamilyDescriptors, columnFamilyHandles);

                                logger.info("database opened");
                            }

                            if (indexes != null) {
                                if (!indexes.isEmpty()) {
                                    for (int i = 0; i < indexes.size(); i++) {
                                        indexes.get(i).setColumnFamilyHandle(columnFamilyHandles.get(i));
                                    }

                                }

                                // INIT SIZE INDEX - только если заданы индексы вторичные вообще
                                columnFamilyFieldSize = columnFamilyHandles.get(columnFamilyHandles.size() - 1);

                                if (create) {
                                    // Нужно для того чтобы в базе даже у транзакционных был Размер уже
                                    dbCore.put(columnFamilyFieldSize, SIZE_BYTE_KEY, new byte[]{0, 0, 0, 0});
                                }
                            }

                            alive = true;

                            dbCore = dbCoreParent.beginTransaction(writeOptions);

                        } catch (RocksDBException e) {
                            logger.error(e.getMessage(), e);
                            throw new RuntimeException("Failed to initialize database", e);
                        }

                    } catch (IOException ioe) {
                        logger.error(ioe.getMessage(), ioe);
                        throw new RuntimeException("Failed to initialize database", ioe);
                    }

                    logger.info("RocksDbDataSource.initDB(): " + dataBaseName);
                }
            }
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

    public static TransactionDB initDB(Path dbPath, Options createOptions, DBOptions openOptions,
                                       TransactionDBOptions transactionDbOptions,
                                       List<ColumnFamilyDescriptor> columnFamilyDescriptors,
                                       List<ColumnFamilyHandle> columnFamilyHandles) {

        TransactionDB dbCore;
        try {
            dbCore = TransactionDB.open(createOptions, transactionDbOptions, dbPath.toString());
            logger.info("database CREATED");

        } catch (RocksDBException e) {
            try {
                dbCore = TransactionDB.open(openOptions, transactionDbOptions,
                        dbPath.toString(), columnFamilyDescriptors, columnFamilyHandles);

                logger.info("database opened");

            } catch (RocksDBException ex) {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException("Failed to initialize database", ex);
            }
        } finally {
        }
        return dbCore;
    }

    private BlockBasedTableConfig settingsBlockBasedTable(RocksDbSettings settings) {
        BlockBasedTableConfig tableCfg = new BlockBasedTableConfig();
        tableCfg.setBlockCache(new LRUCache(128 << 20));
        tableCfg.setBlockSize(settings.getBlockSize());
//        tableCfg.setBlockSize(1024);
        tableCfg.setBlockCacheSize(settings.getBlockCacheSize());
//        tableCfg.setBlockCacheSize(2<<20);
        tableCfg.setCacheIndexAndFilterBlocks(true);
        tableCfg.setPinL0FilterAndIndexBlocksInCache(true);
        tableCfg.setFilter(new BloomFilter(10, false));
        return tableCfg;
    }

    private void addIndexColumnFamilies(List<IndexDB> indexes, ColumnFamilyOptions cfOpts, List<ColumnFamilyDescriptor> columnFamilyDescriptors) {
        if (indexes != null) {
            for (IndexDB index : indexes) {
                columnFamilyDescriptors.add(new ColumnFamilyDescriptor(index.getNameIndex().getBytes(StandardCharsets.UTF_8), cfOpts));
            }
            columnFamilyDescriptors.add(new ColumnFamilyDescriptor(sizeDescriptorName.getBytes(StandardCharsets.UTF_8), cfOpts));
        }
    }

    @Override
    public void put(byte[] key, byte[] value) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbCore.put(key, value);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void put(ColumnFamilyHandle columnFamilyHandle, byte[] key, byte[] value) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbCore.put(columnFamilyHandle, key, value);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void put(byte[] key, byte[] value, WriteOptions writeOptions) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbCore.put(key, value);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    /**
     * if a value is found in block-cache
     */
    final StringBuilder inCache = new StringBuilder();
    DBOptions optionsDBcont = new DBOptions().setAllowMmapReads(true);
    ReadOptions optionsReadDBcont = new ReadOptions(false, false);
    byte[] containsBuff = new byte[0];

    @Override
    public boolean contains(byte[] key) {
        if (quitIfNotAlive()) {
            return false;
        }
        resetDbLock.readLock().lock();
        try {
            // быстрая проверка - потенциально он может содержаться в базе?
            if (!dbCoreParent.keyMayExist(key, inCache)) return false;
            // теперь ищем по настоящему - без получения данных
            return dbCoreParent.get(optionsReadDBcont, key, containsBuff) != RocksDB.NOT_FOUND;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
        return false;
    }

    @Override
    public boolean contains(ColumnFamilyHandle columnFamilyHandle, byte[] key) {
        if (quitIfNotAlive()) {
            return false;
        }
        resetDbLock.readLock().lock();
        try {
            // быстрая проверка - потенциально он может содержаться в базе?
            if (!dbCoreParent.keyMayExist(columnFamilyHandle, key, inCache)) return false;
            // теперь ищем по настоящему - без получения данных
            return dbCoreParent.get(optionsReadDBcont, key, containsBuff) != RocksDB.NOT_FOUND;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
        return false;
    }

    @Override
    public byte[] get(byte[] key) {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        try {
            return dbCore.get(transactReadOptions, key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
        return null;
    }

    @Override
    public byte[] get(ReadOptions readOptions, byte[] key) {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        try {
            return dbCore.get(readOptions, key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
        return null;
    }

    @Override
    public byte[] get(ColumnFamilyHandle columnFamilyHandle, byte[] key) {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        try {
            return dbCore.get(columnFamilyHandle, transactReadOptions, key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
        return null;
    }

    @Override
    public byte[] get(ColumnFamilyHandle columnFamilyHandle, ReadOptions readOptions, byte[] key) {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        try {
            return dbCore.get(columnFamilyHandle, readOptions, key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
        return null;
    }

    @Override
    public void delete(byte[] key) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbCore.delete(key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void deleteValue(byte[] key) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbCore.delete(key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void delete(ColumnFamilyHandle columnFamilyHandle, byte[] key) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbCore.delete(columnFamilyHandle, key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void deleteValue(ColumnFamilyHandle columnFamilyHandle, byte[] key) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbCore.delete(columnFamilyHandle, key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void delete(byte[] key, WriteOptions writeOptions) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbCore.delete(key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void deleteValue(byte[] key, WriteOptions writeOptions) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbCore.delete(key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void deleteRange(byte[] keyFrom, byte[] keyToExclude) {
        throw new UnsupportedRocksDBOperationException();
    }

    @Override
    public void deleteRange(ColumnFamilyHandle columnFamilyHandle, byte[] keyFrom, byte[] keyToExclude) {
        throw new UnsupportedRocksDBOperationException();
    }

    @Override
    public RockStoreIterator iterator(boolean descending, boolean isIndex) {
        return new RockStoreIterator(getIterator(), descending, false);
    }

    @Override
    public RockStoreIterator indexIterator(boolean descending, ColumnFamilyHandle columnFamilyHandle, boolean isIndex) {
        return new RockStoreIterator(getIterator(columnFamilyHandle), descending, true);
    }

    @Override
    public RockStoreIterator indexIteratorFilter(boolean descending, byte[] filter, boolean isIndex) {
        return new RockStoreIteratorFilter(getIterator(), descending, true, filter);
    }

    @Override
    public RockStoreIterator indexIteratorFilter(boolean descending, byte[] start, byte[] stop, boolean isIndex) {
        return new RockStoreIteratorStart(getIterator(), descending, isIndex, start, stop);
    }

    @Override
    public RockStoreIterator indexIteratorFilter(boolean descending, ColumnFamilyHandle columnFamilyHandle, byte[] filter, boolean isIndex) {
        return new RockStoreIteratorFilter(getIterator(columnFamilyHandle), descending, isIndex, filter);
    }

    @Override
    public RockStoreIterator indexIteratorFilter(boolean descending, ColumnFamilyHandle columnFamilyHandle, byte[] start, byte[] stop, boolean isIndex) {
        return new RockStoreIteratorStart(getIterator(columnFamilyHandle), descending, isIndex, start, stop);
    }

    @Override
    public RockStoreIterator indexIterator(boolean descending, int indexDB, boolean isIndex) {
        return new RockStoreIterator(getIterator(columnFamilyHandles.get(indexDB)), descending, true);
    }

    @Override
    public void write(WriteBatch batch) {
    }

    private void updateByBatchInner(Map<byte[], byte[]> rows) throws Exception {
        if (quitIfNotAlive()) {
            return;
        }
        try (WriteBatch batch = new WriteBatch()) {
            for (Entry<byte[], byte[]> entry : rows.entrySet()) {
                if (entry.getValue() == null) {
                    batch.delete(entry.getKey());
                } else {
                    batch.put(entry.getKey(), entry.getValue());
                }
            }
            dbCoreParent.write(new WriteOptions(), batch);
        }
    }

    private void updateByBatchInner(Map<byte[], byte[]> rows, WriteOptions options)
            throws Exception {
        if (quitIfNotAlive()) {
            return;
        }
        try (WriteBatch batch = new WriteBatch()) {
            for (Entry<byte[], byte[]> entry : rows.entrySet()) {
                if (entry.getValue() == null) {
                    batch.delete(entry.getKey());
                } else {
                    batch.put(entry.getKey(), entry.getValue());
                }
            }
            dbCoreParent.write(options, batch);
        }
    }

    @Override
    public void updateByBatch(Map<byte[], byte[]> rows) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            updateByBatchInner(rows);
        } catch (Exception e) {
            try {
                updateByBatchInner(rows);
            } catch (Exception e1) {
                throw new RuntimeException(e);
            }
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void updateByBatch(Map<byte[], byte[]> rows, WriteOptions writeOptions) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            updateByBatchInner(rows, writeOptions);
        } catch (Exception e) {
            try {
                updateByBatchInner(rows);
            } catch (Exception e1) {
                throw new RuntimeException(e);
            }
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    /**
     * Unsorted!
     *
     * @param key
     * @param limit
     * @return
     */
    @Override
    public Map<byte[], byte[]> getNext(byte[] key, long limit) {
        if (quitIfNotAlive()) {
            return null;
        }
        if (limit <= 0) {
            return Collections.emptyMap();
        }
        resetDbLock.readLock().lock();
        try (final RocksIterator iter = getIterator()) {
            Map<byte[], byte[]> result = new HashMap<>();
            long i = 0;
            for (iter.seek(key); iter.isValid() && i < limit; iter.next(), i++) {
                result.put(iter.key(), iter.value());
            }
            return result;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public List<byte[]> getLatestValues(long limit) {
        if (quitIfNotAlive()) {
            return null;
        }
        if (limit <= 0) {
            return new ArrayList<>();
        }
        resetDbLock.readLock().lock();
        try (final RocksIterator iter = getIterator()) {
            List<byte[]> result = new ArrayList<>();
            long i = 0;
            for (iter.seekToLast(); iter.isValid() && i < limit; iter.prev(), i++) {
                result.add(iter.value());
            }
            return result;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public List<byte[]> getValuesPrevious(byte[] key, long limit) {
        if (quitIfNotAlive()) {
            return null;
        }
        if (limit <= 0) {
            return new ArrayList<>();
        }
        resetDbLock.readLock().lock();
        try (final RocksIterator iter = getIterator()) {
            List<byte[]> result = new ArrayList<>();
            long i = 0;
            byte[] data = get(key);
            if (Objects.nonNull(data)) {
                result.add(data);
                i++;
            }
            for (iter.seekForPrev(key); iter.isValid() && i < limit; iter.prev(), i++) {
                result.add(iter.value());
            }
            return result;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public List<byte[]> getValuesNext(byte[] key, long limit) {
        if (quitIfNotAlive()) {
            return null;
        }
        if (limit <= 0) {
            return new ArrayList<>();
        }
        resetDbLock.readLock().lock();
        try (final RocksIterator iter = getIterator()) {
            List<byte[]> result = new ArrayList<>();
            long i = 0;
            for (iter.seek(key); iter.isValid() && i < limit; iter.next(), i++) {
                result.add(iter.value());
            }
            return result;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public Set<byte[]> getKeysNext(byte[] key, long limit) {
        if (quitIfNotAlive()) {
            return null;
        }
        if (limit <= 0) {
            return new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);
        }
        resetDbLock.readLock().lock();
        try (final RocksIterator iter = getIterator()) {
            Set<byte[]> result = new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);
            long i = 0;
            for (iter.seek(key); iter.isValid() && i < limit; iter.next(), i++) {
                result.add(iter.key());
            }
            return result;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public Set<byte[]> getKeysNext(byte[] key, long limit, ColumnFamilyHandle columnFamilyHandle) {
        if (quitIfNotAlive()) {
            return null;
        }
        if (limit <= 0) {
            return new TreeSet(Fun.BYTE_ARRAY_COMPARATOR);
        }
        resetDbLock.readLock().lock();
        try (final RocksIterator iter = getIterator(columnFamilyHandle)) {
            Set<byte[]> result = new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);
            long i = 0;
            for (iter.seek(key); iter.isValid() && i < limit; iter.next(), i++) {
                result.add(iter.value());
            }
            return result;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    /**
     * Unsorted!
     *
     * @param key
     * @param limit
     * @param precision
     * @return
     */
    @Override
    public Map<byte[], byte[]> getPrevious(byte[] key, long limit, int precision) {
        if (quitIfNotAlive()) {
            return null;
        }
        if (limit <= 0 || key.length < precision) {
            return Collections.emptyMap();
        }
        resetDbLock.readLock().lock();
        try (final RocksIterator iterator = getIterator()) {
            Map<byte[], byte[]> result = new HashMap<>();
            long i = 0;
            for (iterator.seekToFirst(); iterator.isValid() && i++ < limit; iterator.next()) {
                if (iterator.key().length >= precision) {
                    if (ByteUtil.less(ByteUtil.parseBytes(key, 0, precision),
                            ByteUtil.parseBytes(iterator.key(), 0, precision))) {
                        break;
                    }
                    result.put(iterator.key(), iterator.value());
                }
            }
            return result;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void backup(String dir) throws RocksDBException {
        Checkpoint cp = Checkpoint.create(dbCoreParent);
        cp.createCheckpoint(dir + getDBName());
    }

    @Override
    public boolean deleteDbBakPath(String dir) {
        return FileUtil.deleteDir(new File(dir + getDBName()));
    }

    byte[] sizeBytes = new byte[4];

    @Override
    public int size() {
        sizeBytes = get(columnFamilyFieldSize, optionsReadDBcont, SIZE_BYTE_KEY);
        return Ints.fromBytes(sizeBytes[0], sizeBytes[1], sizeBytes[2], sizeBytes[3]);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public void flush(Map<byte[], byte[]> rows) {
        updateByBatch(rows, writeOptions);

    }

    @Override
    public void flush() throws RocksDBException {
        FlushOptions flushOptions = new FlushOptions();
        dbCoreParent.flush(flushOptions);
    }

    @Override
    public RocksIterator getIterator() {
        if (quitIfNotAlive()) {
            return null;
        }

        return dbCore.getIterator(transactReadOptions, dbCoreParent.getDefaultColumnFamily());
    }

    @Override
    public RocksIterator getIterator(ColumnFamilyHandle indexDB) {
        if (quitIfNotAlive()) {
            return null;
        }

        return dbCore.getIterator(transactReadOptions, indexDB);
    }

    @Override
    public void commit() {
        // сольем старый и начнем новый
        resetDbLock.writeLock().lock();
        try {
            dbCore.commit();
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            dbCore.close();
            dbCore = dbCoreParent.beginTransaction(writeOptions);
            resetDbLock.writeLock().unlock();
        }
    }

    @Override
    public void rollback() {
        resetDbLock.writeLock().lock();
        try {
            dbCore.rollback();
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            dbCore.close();
            dbCore = dbCoreParent.beginTransaction(writeOptions);
            resetDbLock.writeLock().unlock();
        }
    }

    @Override
    public void clearCache() {
    }

    @Override
    public void close() {
        resetDbLock.writeLock().lock();
        try {
            if (!isAlive()) {
                return;
            }
            alive = false;
            dbCore.commit();
            dbCore.close();
            writeOptions.dispose();
            dbCoreParent.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

}