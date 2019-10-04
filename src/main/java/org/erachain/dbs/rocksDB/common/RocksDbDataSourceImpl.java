package org.erachain.dbs.rocksDB.common;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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

/**
 * Самый низкий уровень доступа к функциям RocksDB
 */
@Slf4j
public abstract class RocksDbDataSourceImpl implements RocksDbDataSource
        // DB<byte[], byte[]>, Flusher, DbSourceInter<byte[]>
{
    protected String dataBaseName;
    public static final byte[] SIZE_BYTE_KEY = new byte[]{0};

    //Глеб * эта переменная позаимствована из проекта "tron" нужна для создания каких-то настроек
    // Это включает логирование данных на диск синхронизированно - защищает от утрат при КРАХЕ но чуть медленне работает
    // Если ЛОЖЬ то данные утрачиваются при КРАХЕ
    //protected boolean dbSync = true;

    protected WriteOptions writeOptions;

    @Getter
    //public RocksDB database;
    // база данных - сама
    public RocksDB dbCore;
    // некий объект работы с базой данных - как сама так и транзакция
    public RocksDbCom table;
    //public OptimisticTransactionDB database;
    Options options;

    protected boolean alive;
    protected String pathName;
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

    public RocksDbDataSourceImpl(TransactionDB dbCore, RocksDbCom table,
                                 String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings,
                                 WriteOptions writeOptions) {
        this.dataBaseName = name;
        this.pathName = pathName;
        this.indexes = indexes;
        this.settings = settings;
        this.writeOptions = writeOptions;
        this.dbCore = dbCore;
        this.table = table;
    }

    public RocksDbDataSourceImpl(String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings,
                                 WriteOptions writeOptions) {
        this.dataBaseName = name;
        this.pathName = pathName;
        this.indexes = indexes;
        this.settings = settings;
        this.writeOptions = writeOptions;
    }

    public RocksDbDataSourceImpl(String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings) {
        this(pathName, name, indexes, settings, new WriteOptions().setSync(true).setDisableWAL(false));
    }

    public RocksDbDataSourceImpl(String name, List<IndexDB> indexes, RocksDbSettings settings) {
        this(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER, name, indexes, settings);
    }

    abstract protected void createDB(Options options, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException;

    abstract protected void openDB(DBOptions dbOptions, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException;

    public void initDB() {
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
                            try {

                                // MAKE DATABASE
                                // USE transactions
                                createDB(options, columnFamilyDescriptors);

                                // MAKE COLUMNS FAMILY
                                columnFamilyHandles.add(dbCore.getDefaultColumnFamily());
                                for (ColumnFamilyDescriptor columnFamilyDescriptor : columnFamilyDescriptors) {
                                    ColumnFamilyHandle columnFamilyHandle = dbCore.createColumnFamily(columnFamilyDescriptor);
                                    columnFamilyHandles.add(columnFamilyHandle);
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
                                openDB(dbOptions, columnFamilyDescriptors);

                                if (indexes != null && !indexes.isEmpty()) {
                                    for (int i = 0; i < indexes.size(); i++) {
                                        indexes.get(i).setColumnFamilyHandle(columnFamilyHandles.get(i));
                                    }
                                }

                                logger.info("database opened");
                            }

                        } catch (RocksDBException e) {
                            logger.error(e.getMessage(), e);
                            throw new RuntimeException("Failed to initialize database", e);
                        }
                        alive = true;
                    } catch (IOException ioe) {
                        logger.error(ioe.getMessage(), ioe);
                        throw new RuntimeException("Failed to initialize database", ioe);
                    }

                    columnFamilyFieldSize = columnFamilyHandles.get(columnFamilyHandles.size() - 1);

                    // Нужно для того чтобы в базе дае у транзакционных был Размер уже
                    try {
                        dbCore.put(columnFamilyFieldSize, SIZE_BYTE_KEY, new byte[]{0, 0, 0, 0});
                    } catch (RocksDBException edb) {

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
                throw new RuntimeException("Failed to initialize database", e);
            }
        } finally {
        }
        return dbCore;
    }

    public void afterOpenTable() {
        if (create) {
            // уже есть выше put(columnFamilyFieldSize, SIZE_BYTE_KEY, new byte[]{0, 0, 0, 0});
        }
    }

    @Override
    public Path getDbPathAndFile() {
        return Paths.get(pathName, dataBaseName);
    }


    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public void close() {
        resetDbLock.writeLock().lock();
        try {
            if (!isAlive()) {
                return;
            }
            alive = false;
            dbCore.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.writeLock().unlock();
        }
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
        try (final RocksIterator iter = table.getIterator()) {
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
        try (final RocksIterator iter = table.getIterator()) {
            for (iter.seekToFirst(); iter.isValid(); iter.next()) {
                result.add(iter.value());
            }
            return result;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public RocksIterator getIterator() {
        if (quitIfNotAlive()) {
            return null;
        }

        return table.getIterator();
    }

    @Override
    public RocksIterator getIterator(ColumnFamilyHandle indexDB) {
        if (quitIfNotAlive()) {
            return null;
        }

        return table.getIterator(indexDB);
    }

    @Override
    public Set<byte[]> filterApprropriateKeys(byte[] filter) throws RuntimeException {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        Set<byte[]> result = new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);
        try (final RocksIterator iter = table.getIterator()) {
            for (iter.seek(filter); iter.isValid() && new String(iter.key()).startsWith(new String(filter)); iter.next()) {
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
        try (final RocksIterator iter = table.getIterator()) {
            for (iter.seek(filter); iter.isValid() && new String(iter.key()).startsWith(new String(filter)); iter.next()) {
                result.add(iter.value());
            }
            return result;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public List<byte[]> filterApprropriateValues(byte[] filter, ColumnFamilyHandle indexDB) throws RuntimeException {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        List<byte[]> result = new ArrayList<byte[]>();
        try (final RocksIterator iter = table.getIterator(indexDB)) {
            for (iter.seek(filter); iter.isValid() && new String(iter.key()).startsWith(new String(filter)); iter.next()) {
                result.add(iter.value());
            }
            return result;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public List<byte[]> filterApprropriateValues(byte[] filter, int indexDB) throws RuntimeException {
        return filterApprropriateValues(filter, columnFamilyHandles.get(indexDB));
    }

    @Override
    public String getDBName() {
        return dataBaseName;
    }

    private BlockBasedTableConfig settingsBlockBasedTable(RocksDbSettings settings) {
        BlockBasedTableConfig tableCfg = new BlockBasedTableConfig();
        tableCfg.setBlockCache(new LRUCache(128 << 20));
        tableCfg.setBlockSize(settings.getBlockSize());
        tableCfg.setBlockSize(1024);
        tableCfg.setBlockCacheSize(settings.getBlockCacheSize());
        tableCfg.setBlockCacheSize(64 << 20);
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
            table.put(key, value);
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
            table.put(columnFamilyHandle, key, value);
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
            table.put(key, value, writeOptions);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public boolean contains(byte[] key) {
        if (quitIfNotAlive()) {
            return false;
        }
        resetDbLock.readLock().lock();
        try {
            return table.contains(key);
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
            return table.contains(columnFamilyHandle, key);
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
            return table.get(key);
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
            return table.get(columnFamilyHandle, key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
        return null;
    }

    @Override
    public void remove(byte[] key) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            table.remove(key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void remove(ColumnFamilyHandle columnFamilyHandle, byte[] key) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            table.remove(columnFamilyHandle, key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void remove(byte[] key, WriteOptions writeOptions) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            table.remove(key, writeOptions);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public RockStoreIterator iterator(boolean descending) {
        return new RockStoreIterator(table.getIterator(), descending, false);
    }

    @Override
    public RockStoreIterator indexIterator(boolean descending, ColumnFamilyHandle columnFamilyHandle) {
        return new RockStoreIterator(table.getIterator(columnFamilyHandle), descending, true);
    }

    @Override
    public RockStoreIteratorFilter indexIteratorFilter(boolean descending, byte[] filter) {
        return new RockStoreIteratorFilter(table.getIterator(), descending, true, filter);
    }
    @Override
    public RockStoreIteratorFilter indexIteratorFilter(boolean descending, ColumnFamilyHandle columnFamilyHandle, byte[] filter) {
        return new RockStoreIteratorFilter(table.getIterator(columnFamilyHandle), descending, true, filter);
    }

    @Override
    public RockStoreIterator indexIterator(boolean descending, int indexDB) {
        return new RockStoreIterator(table.getIterator(columnFamilyHandles.get(indexDB)), descending, true);
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
            dbCore.write(new WriteOptions(), batch);
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
            dbCore.write(options, batch);
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
        try (RocksIterator iter = table.getIterator()) {
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
        try (RocksIterator iter = table.getIterator()) {
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
        try (RocksIterator iter = table.getIterator()) {
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
        try (RocksIterator iter = table.getIterator()) {
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
        try (RocksIterator iter = table.getIterator()) {
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
        try (RocksIterator iter = table.getIterator(columnFamilyHandle)) {
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
        try (RocksIterator iterator = table.getIterator()) {
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
        Checkpoint cp = Checkpoint.create(dbCore);
        cp.createCheckpoint(dir + getDBName());
    }

    @Override
    public boolean deleteDbBakPath(String dir) {
        return FileUtil.deleteDir(new File(dir + getDBName()));
    }

    @Override
    public int size() {
        byte[] sizeBytes = get(columnFamilyFieldSize, SIZE_BYTE_KEY);
        return byteableInteger.receiveObjectFromBytes(sizeBytes);
    }

    @Override
    public void setSize(int size) {
        put(columnFamilyFieldSize, SIZE_BYTE_KEY, Ints.toByteArray(size));
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
        dbCore.flush(flushOptions);
    }

}