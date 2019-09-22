package org.erachain.dbs.rocksDB.common;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.erachain.dbs.rocksDB.utils.ByteUtil;
import org.erachain.dbs.rocksDB.utils.FileUtil;
import org.mapdb.Fun;
import org.rocksdb.RocksDB;
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

import static org.rocksdb.RocksDB.loadLibrary;

@Slf4j
@NoArgsConstructor
public class RocksDbDataSourceImpl implements DbSourceInter<byte[]> {
    private String dataBaseName;
    @Getter
    public RocksDB database;
    private boolean alive;
    private String parentName;

    private ColumnFamilyHandle columnFamilyFieldSize;
    private ByteableInteger byteableInteger = new ByteableInteger();


    private ReadWriteLock resetDbLock = new ReentrantReadWriteLock();

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

    private boolean create = false;
    protected String sizeDescriptorName = "size";

    @Getter
    private List<ColumnFamilyHandle> columnFamilyHandles;

    private RocksDbSettings settings;


    public RocksDbDataSourceImpl(String parentName, String name) {
        this.dataBaseName = name;
        this.parentName = parentName;
    }

    public Path getDbPath() {
        return Paths.get(parentName, dataBaseName);
    }


    public boolean isAlive() {
        return alive;
    }

    @Override
    public void closeDB() {
        resetDbLock.writeLock().lock();
        try {
            if (!isAlive()) {
                return;
            }
            database.close();
            alive = false;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

    private boolean quitIfNotAlive() {
        if (!isAlive()) {
            logger.warn("db is not alive");
        }
        return !isAlive();
    }

    @Override
    public Set<byte[]> allKeys() throws RuntimeException {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        Set<byte[]> result = new TreeSet(Fun.BYTE_ARRAY_COMPARATOR);
        try (final RocksIterator iter = database.newIterator()) {
            for (iter.seekToFirst(); iter.isValid(); iter.next()) {
                result.add(iter.key());
            }
            return result;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public List<byte[]> allValues() throws RuntimeException {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        List<byte[]> result = new ArrayList<byte[]>();
        try (final RocksIterator iter = database.newIterator()) {
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

        return database.newIterator(database.getDefaultColumnFamily());
    }

    @Override
    public RocksIterator getIterator(ColumnFamilyHandle indexDB) {
        if (quitIfNotAlive()) {
            return null;
        }

        return database.newIterator(indexDB);
    }

    @Override
    public Set<byte[]> filterApprropriateKeys(byte[] filter) throws RuntimeException {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        Set<byte[]> result = new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);
        try (final RocksIterator iter = database.newIterator(database.getDefaultColumnFamily())) {
            for (iter.seek(filter); iter.isValid() && new String(iter.key()).startsWith(new String(filter)); iter.next()) {
                result.add(iter.key());
            }
            return result;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    public List<byte[]> filterApprropriateValues(byte[] filter) throws RuntimeException {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        List<byte[]> result = new ArrayList<byte[]>();
        try (final RocksIterator iter = database.newIterator(database.getDefaultColumnFamily())) {
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
        try (final RocksIterator iter = database.newIterator(indexDB)) {
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

    @Override
    public void setDBName(String name) {
    }

    public void initDB(List<IndexDB> indexes) {
        initDB(RocksDbSettings.getSettings(), indexes);
    }


    public void initDB(RocksDbSettings settings, List<IndexDB> indexes) {
        resetDbLock.writeLock().lock();
        this.settings = settings;
        try {
            if (isAlive()) {
                return;
            }

            Preconditions.checkNotNull(dataBaseName, "no name set to the dbStore");
            try (final ColumnFamilyOptions cfOpts = new ColumnFamilyOptions().optimizeUniversalStyleCompaction()) {
                final List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
                columnFamilyHandles = new ArrayList<>();
                addIndexColumnFamilies(indexes, cfOpts, columnFamilyDescriptors);
//                BlockBasedTableConfig tableCfgCf = settingsBlockBasedTable(settings);
//                cfOpts.setTableFormatConfig(tableCfgCf);
                try (Options options = new Options()) {

//                    if (settings.isEnableStatistics()) {
//                        options.setStatistics(new Statistics());
//                        options.setStatsDumpPeriodSec(60);
//                    }
                    options.setCreateIfMissing(true);
//                    options.setIncreaseParallelism(3);
//                    options.setLevelCompactionDynamicLevelBytes(true);
//                    options.setMaxOpenFiles(settings.getMaxOpenFiles());

//                    options.setNumLevels(settings.getLevelNumber());
//                    options.setMaxBytesForLevelMultiplier(settings.getMaxBytesForLevelMultiplier());
//                    options.setMaxBytesForLevelBase(settings.getMaxBytesForLevelBase());
//                    options.setMaxBackgroundCompactions(settings.getCompactThreads());
//                    options.setLevel0FileNumCompactionTrigger(settings.getLevel0FileNumCompactionTrigger());
//                    options.setTargetFileSizeMultiplier(settings.getTargetFileSizeMultiplier());
//                    options.setTargetFileSizeBase(settings.getTargetFileSizeBase());

//                    BlockBasedTableConfig tableCfg = settingsBlockBasedTable(settings);
//                    options.setTableFormatConfig(tableCfg);
//                    options.setAllowConcurrentMemtableWrite(true);
//                    options.setMaxManifestFileSize(0);
//                    options.setWalTtlSeconds(0);
//                    options.setWalSizeLimitMB(0);
//                    options.setLevel0FileNumCompactionTrigger(1);
//                    options.setMaxBackgroundFlushes(4);
//                    options.setMaxBackgroundCompactions(8);
//                    options.setMaxSubcompactions(4);
//                    options.setMaxWriteBufferNumber(3);
//                    options.setMinWriteBufferNumberToMerge(2);

                    int dbWriteBufferSize = 512 * 1024 * 1024;
                    options.setDbWriteBufferSize(dbWriteBufferSize);
                    options.setParanoidFileChecks(false);
                    options.setCompressionType(CompressionType.NO_COMPRESSION);
                    options.setParanoidChecks(false);
                    options.setAllowMmapReads(true);
                    options.setAllowMmapWrites(true);
                    try {
                        logger.info("Opening database");
                        final Path dbPath = getDbPath();
                        if (!Files.isSymbolicLink(dbPath.getParent())) {
                            Files.createDirectories(dbPath.getParent());
                        }

                        try (final DBOptions dbOptions = new DBOptions()) {
                            TransactionDBOptions transactionDbOptions = new TransactionDBOptions();
                            try {
                                database = TransactionDB.open(options, transactionDbOptions, dbPath.toString());
                                columnFamilyHandles.add(database.getDefaultColumnFamily());
                                int indexID = 0;
                                for (ColumnFamilyDescriptor columnFamilyDescriptor : columnFamilyDescriptors) {
                                    ColumnFamilyHandle columnFamilyHandle = database.createColumnFamily(columnFamilyDescriptor);
                                    columnFamilyHandles.add(columnFamilyHandle);
                                    indexes.get(indexID++).setColumnFamilyHandle(columnFamilyHandle);
                                }
                                create = true;
                            } catch (RocksDBException e) {
                                dbOptions.setCreateIfMissing(true);
                                dbOptions.setCreateMissingColumnFamilies(true);
//                                dbOptions.setIncreaseParallelism(3);
//                                dbOptions.setMaxOpenFiles(settings.getMaxOpenFiles());
//                                dbOptions.setMaxBackgroundCompactions(settings.getCompactThreads());
//                                dbOptions.setAllowConcurrentMemtableWrite(true);
//                                dbOptions.setMaxManifestFileSize(0);
//                                dbOptions.setWalTtlSeconds(0);
//                                dbOptions.setWalSizeLimitMB(0);
//                                dbOptions.setMaxBackgroundFlushes(4);
//                                dbOptions.setMaxBackgroundCompactions(8);
//                                dbOptions.setMaxSubcompactions(4);
                                dbOptions.setDbWriteBufferSize(dbWriteBufferSize);
                                dbOptions.setParanoidChecks(false);
                                dbOptions.setAllowMmapReads(true);
                                dbOptions.setAllowMmapWrites(true);
                                columnFamilyDescriptors.clear();
                                columnFamilyDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOpts));
                                addIndexColumnFamilies(indexes, cfOpts, columnFamilyDescriptors);
                                database = TransactionDB.open(dbOptions, dbPath.toString(), columnFamilyDescriptors, columnFamilyHandles);
                                for (int i = 0; i < indexes.size(); i++) {
                                    indexes.get(i).setColumnFamilyHandle(columnFamilyHandles.get(i));
                                }

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
                    if (create)
                        putData(columnFamilyFieldSize, new byte[]{0}, new byte[]{0, 0, 0, 0});

                    logger.info("RocksDbDataSource.initDB(): " + dataBaseName);
                }
            }
        } finally {
            resetDbLock.writeLock().unlock();
        }
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
        }
        columnFamilyDescriptors.add(new ColumnFamilyDescriptor(sizeDescriptorName.getBytes(StandardCharsets.UTF_8), cfOpts));
    }

    @Override
    public void putData(byte[] key, byte[] value) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            database.put(key, value);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    public void putData(ColumnFamilyHandle columnFamilyHandle, byte[] key, byte[] value) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            database.put(columnFamilyHandle, key, value);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void putData(byte[] key, byte[] value, WriteOptionsWrapper optionsWrapper) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            database.put(optionsWrapper.getRocks(), key, value);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public byte[] getData(byte[] key) {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        try {
            return database.get(key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
        return null;
    }

    public byte[] getData(ColumnFamilyHandle columnFamilyHandle, byte[] key) {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        try {
            return database.get(columnFamilyHandle, key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
        return null;
    }

    @Override
    public void deleteData(byte[] key) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            database.delete(key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    public void deleteData(ColumnFamilyHandle columnFamilyHandle, byte[] key) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            database.delete(columnFamilyHandle, key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void deleteData(byte[] key, WriteOptionsWrapper optionsWrapper) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            database.delete(optionsWrapper.getRocks(), key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void flush() throws RocksDBException {
        FlushOptions flushOptions = new FlushOptions();
        database.flush(flushOptions);
    }

    public RockStoreIterator iterator(boolean descending) {
        return new RockStoreIterator(database.newIterator(), descending, false);
    }

    public RockStoreIterator indexIterator(boolean descending, ColumnFamilyHandle columnFamilyHandle) {
        return new RockStoreIterator(database.newIterator(columnFamilyHandle), descending, true);
    }
    public RockStoreIterator indexIterator(boolean descending, int indexDB) {
        return new RockStoreIterator(database.newIterator(columnFamilyHandles.get(indexDB)), descending, true);
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
            database.write(new WriteOptions(), batch);
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
            database.write(options, batch);
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
    public void updateByBatch(Map<byte[], byte[]> rows, WriteOptionsWrapper optionsWrapper) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            updateByBatchInner(rows, optionsWrapper.getRocks());
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
    public Map<byte[], byte[]> getNext(byte[] key, long limit) {
        if (quitIfNotAlive()) {
            return null;
        }
        if (limit <= 0) {
            return Collections.emptyMap();
        }
        resetDbLock.readLock().lock();
        try (RocksIterator iter = database.newIterator()) {
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

    public List<byte[]> getLatestValues(long limit) {
        if (quitIfNotAlive()) {
            return null;
        }
        if (limit <= 0) {
            return new ArrayList<>();
        }
        resetDbLock.readLock().lock();
        try (RocksIterator iter = database.newIterator()) {
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

    public List<byte[]> getValuesPrevious(byte[] key, long limit) {
        if (quitIfNotAlive()) {
            return null;
        }
        if (limit <= 0) {
            return new ArrayList<>();
        }
        resetDbLock.readLock().lock();
        try (RocksIterator iter = database.newIterator()) {
            List<byte[]> result = new ArrayList<>();
            long i = 0;
            byte[] data = getData(key);
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

    public List<byte[]> getValuesNext(byte[] key, long limit) {
        if (quitIfNotAlive()) {
            return null;
        }
        if (limit <= 0) {
            return new ArrayList<>();
        }
        resetDbLock.readLock().lock();
        try (RocksIterator iter = database.newIterator()) {
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

    public Set<byte[]> getKeysNext(byte[] key, long limit) {
        if (quitIfNotAlive()) {
            return null;
        }
        if (limit <= 0) {
            return new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);
        }
        resetDbLock.readLock().lock();
        try (RocksIterator iter = database.newIterator()) {
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

    public Set<byte[]> getKeysNext(byte[] key, long limit, ColumnFamilyHandle columnFamilyHandle) {
        if (quitIfNotAlive()) {
            return null;
        }
        if (limit <= 0) {
            return new TreeSet(Fun.BYTE_ARRAY_COMPARATOR);
        }
        resetDbLock.readLock().lock();
        try (RocksIterator iter = database.newIterator(columnFamilyHandle)) {
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
    public Map<byte[], byte[]> getPrevious(byte[] key, long limit, int precision) {
        if (quitIfNotAlive()) {
            return null;
        }
        if (limit <= 0 || key.length < precision) {
            return Collections.emptyMap();
        }
        resetDbLock.readLock().lock();
        try (RocksIterator iterator = database.newIterator()) {
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

    public void backup(String dir) throws RocksDBException {
        Checkpoint cp = Checkpoint.create(database);
        cp.createCheckpoint(dir + getDBName());
    }

    public boolean deleteDbBakPath(String dir) {
        return FileUtil.deleteDir(new File(dir + getDBName()));
    }

    public int size() {
        byte[] sizeBytes = getData(columnFamilyFieldSize, new byte[]{0});
        return byteableInteger.receiveObjectFromBytes(sizeBytes);
    }

}