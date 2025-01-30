/*
 *  Copyright (c) 2012 Jan Kotek
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.mapdb;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MapDB abstraction over raw storage (file, disk partition, memory etc...).
 * <p/>
 * Implementations needs to be thread safe (especially
 'ensureAvailable') operation.
 * However updates do not have to be atomic, it is clients responsibility
 * to ensure two threads are not writing/reading into the same location.
 *
 * @author Jan Kotek
 */
public abstract class Volume {


//    protected static final Logger LOG = Logger.getLogger(Volume.class.getName());
//
//    //uncomment to get stack trace on Volume leak warning
//    final private Throwable constructorStackTrace = new AssertionError();
//
//    @Override
//    protected void finalize(){
//            if(!closed){
//                LOG.log(Level.WARNING, "Open Volume was GCed, possible file handle leak."
//                        ,constructorStackTrace
//                );
//            }
//
//    }

    protected boolean closed = false;

    /**
     * Check space allocated by Volume is bigger or equal to given offset.
     * So it is safe to write into smaller offsets.
     *
     * @throws IOError if Volume can not be expanded beyond given offset
     * @param offset
     */
    public void ensureAvailable(final long offset){
        if(!tryAvailable(offset))
            throw new IOError(new IOException("no free space to expand Volume"));
    }


    abstract public boolean tryAvailable(final long offset);

    public abstract void truncate(long size);


    abstract public void putLong(final long offset, final long value);
    abstract public void putInt(long offset, int value);
    abstract public void putByte(final long offset, final byte value);

    abstract public void putData(final long offset, final byte[] src, int srcPos, int srcSize);
    abstract public void putData(final long offset, final ByteBuffer buf);

    abstract public long getLong(final long offset);
    abstract public int getInt(long offset);
    abstract public byte getByte(final long offset);



    abstract public DataInput getDataInput(final long offset, final int size);

    abstract public void close();

    abstract public void sync();

    public abstract boolean isEmpty();

    public abstract void deleteFile();

    public abstract boolean isSliced();


    public void putUnsignedShort(final long offset, final int value){
        putByte(offset, (byte) (value>>8));
        putByte(offset+1, (byte) (value));
    }

    public int getUnsignedShort(long offset) {
        return (( (getByte(offset) & 0xff) << 8) |
                ( (getByte(offset+1) & 0xff)));
    }

    public int getUnsignedByte(long offset) {
        return getByte(offset) & 0xff;
    }

    public void putUnsignedByte(long offset, int b) {
        putByte(offset, (byte)(b & 0xff));
    }

    /**
     * Reads a long from the indicated position
     */
    public long getSixLong(long pos) {
        return
                ((long) (getByte(pos + 0) & 0xff) << 40) |
                        ((long) (getByte(pos + 1) & 0xff) << 32) |
                        ((long) (getByte(pos + 2) & 0xff) << 24) |
                        ((long) (getByte(pos + 3) & 0xff) << 16) |
                        ((long) (getByte(pos + 4) & 0xff) << 8) |
                        ((long) (getByte(pos + 5) & 0xff) << 0);
    }

    /**
     * Writes a long to the indicated position
     */
    public void putSixLong(long pos, long value) {
        assert(value>=0 && (value>>>6*8)==0): "value does not fit";
        //TODO read/write as integer+short, might be faster
        putByte(pos + 0, (byte) (0xff & (value >> 40)));
        putByte(pos + 1, (byte) (0xff & (value >> 32)));
        putByte(pos + 2, (byte) (0xff & (value >> 24)));
        putByte(pos + 3, (byte) (0xff & (value >> 16)));
        putByte(pos + 4, (byte) (0xff & (value >> 8)));
        putByte(pos + 5, (byte) (0xff & (value >> 0)));

    }

    /**
     * Writes packed long at given position and returns number of bytes used.
     */
    public int putPackedLong(long pos, long value) {
        assert(value>=0):"negative value";

        int ret = 0;

        while ((value & ~0x7FL) != 0) {
            putUnsignedByte(pos+(ret++), (((int) value & 0x7F) | 0x80));
            value >>>= 7;
        }
        putUnsignedByte(pos + (ret++), (byte) value);
        return ret;
    }



    /** returns underlying file if it exists */
    abstract public File getFile();

    public long getPackedLong(long pos){
        //TODO unrolled version?
        long result = 0;
        for (int offset = 0; offset < 64; offset += 7) {
            long b = getUnsignedByte(pos++);
            result |= (b & 0x7F) << offset;
            if ((b & 0x80) == 0) {
                return result;
            }
        }
        throw new AssertionError("Malformed long.");
    }


    /**
     * Factory which creates two/three volumes used by each MapDB Storage Engine
     */
    public static interface Factory {
        Volume createIndexVolume();
        Volume createPhysVolume();
        Volume createTransLogVolume();
    }

    public static Volume volumeForFile(File f, boolean useRandomAccessFile, boolean readOnly, long sizeLimit, int chunkShift, int sizeIncrement) {
        return volumeForFile(f, useRandomAccessFile, readOnly, sizeLimit, chunkShift,sizeIncrement, false);
    }
    public static Volume volumeForFile(File f, boolean useRandomAccessFile, boolean readOnly, long sizeLimit, int chunkShift,
                                       int sizeIncrement, boolean asyncWriteEnabled) {
        return volumeForFile(f, useRandomAccessFile, readOnly, sizeLimit, chunkShift,sizeIncrement, asyncWriteEnabled,false);
    }
    public static Volume volumeForFile(File f, boolean useRandomAccessFile, boolean readOnly, long sizeLimit, int chunkShift,
                                       int sizeIncrement, boolean asyncWriteEnabled, boolean cleanerHackDisable) {
        return useRandomAccessFile ?
                new FileChannelVol(f, readOnly,sizeLimit, chunkShift, sizeIncrement):
                new MappedFileVol(f, readOnly,sizeLimit,chunkShift, sizeIncrement,asyncWriteEnabled, cleanerHackDisable);
    }


    public static Factory fileFactory(final File indexFile, final int rafMode, final boolean readOnly, final long sizeLimit,
                                      final int chunkShift, final int sizeIncrement){
                return fileFactory(
                                indexFile,
                                rafMode, readOnly, sizeLimit, chunkShift, sizeIncrement,
                                new File(indexFile.getPath() + StoreDirect.DATA_FILE_EXT),
                                new File(indexFile.getPath() + StoreWAL.TRANS_LOG_FILE_EXT));
    }

    public static Factory fileFactory(final File indexFile,
                                      final int rafMode,
                                      final boolean readOnly,
                                      final long sizeLimit,
                                      final int chunkShift,
                                      final int sizeIncrement,

                                      final File physFile,
                                      final File transLogFile) {
        return fileFactory(
            indexFile,
            rafMode,
            readOnly,
            sizeLimit,
            chunkShift,
            sizeIncrement,
            physFile,
            transLogFile,
            false, false
        );
    }


    public static Factory fileFactory(final File indexFile,
                                      final int rafMode,
                                      final boolean readOnly,
                                      final long sizeLimit,
                                      final int chunkShift,
                                      final int sizeIncrement,

                                      final File physFile,
                                      final File transLogFile,
                                      final boolean asyncWriteEnabled) {
        return fileFactory(
                indexFile,
                rafMode,
                readOnly,
                sizeLimit,
                chunkShift,
                sizeIncrement,
                physFile,
                transLogFile,
                false, false
        );
    }
        public static Factory fileFactory(final File indexFile,
                                      final int rafMode,
                                      final boolean readOnly,
                                      final long sizeLimit,
                                      final int chunkShift,
                                      final int sizeIncrement,

                                      final File physFile,
                                      final File transLogFile,
                                      final boolean asyncWriteEnabled,
                                      final boolean cleanerHackDisable) {
        return new Factory() {
            @Override
            public Volume createIndexVolume() {
                return volumeForFile(indexFile, rafMode>1, readOnly, sizeLimit, chunkShift, sizeIncrement, asyncWriteEnabled,cleanerHackDisable);
            }

            @Override
            public Volume createPhysVolume() {
                return volumeForFile(physFile, rafMode>0, readOnly, sizeLimit, chunkShift, sizeIncrement,asyncWriteEnabled,cleanerHackDisable);
            }

            @Override
            public Volume createTransLogVolume() {
                return volumeForFile(transLogFile, rafMode>0, readOnly, sizeLimit,chunkShift, sizeIncrement,asyncWriteEnabled,cleanerHackDisable);
            }
        };
    }


    public static Factory memoryFactory(final boolean useDirectBuffer, final long sizeLimit, final int chunkShift) {
        return new Factory() {

            @Override public synchronized  Volume createIndexVolume() {
                return new MemoryVol(useDirectBuffer, sizeLimit, chunkShift);
            }

            @Override public synchronized Volume createPhysVolume() {
                return new MemoryVol(useDirectBuffer, sizeLimit, chunkShift);
            }

            @Override public synchronized Volume createTransLogVolume() {
                return new MemoryVol(useDirectBuffer, sizeLimit, chunkShift);
            }
        };
    }


    /**
     * Abstract Volume over bunch of ByteBuffers
     * It leaves ByteBufferVol details (allocation, disposal) on subclasses.
     * Most methods are final for better performance (JIT compiler can inline those).
     */
    abstract static public class ByteBufferVol extends Volume{


        protected final ReentrantLock growLock = new ReentrantLock(CC.FAIR_LOCKS);

        protected final long sizeLimit;
        protected final boolean hasLimit;
        protected final int chunkShift;
        protected final int chunkSizeModMask;
        protected final int chunkSize;

        protected volatile ByteBuffer[] chunks = new ByteBuffer[0];
        protected final boolean readOnly;

        /**
         * if Async Write is enabled, do not use unmap hack see
         * https://github.com/jankotek/MapDB/issues/442
         */
        protected final boolean asyncWriteEnabled;
        protected boolean cleanerHackDisabled;

        protected ByteBufferVol(boolean readOnly, long sizeLimit, int chunkShift) {
            this(readOnly, sizeLimit, chunkShift, false);
        }

        protected ByteBufferVol(boolean readOnly, long sizeLimit, int chunkShift, boolean asyncWriteEnabled) {
            this.readOnly = readOnly;
            this.sizeLimit = sizeLimit;
            this.chunkShift = chunkShift;
            this.chunkSize = 1<< chunkShift;
            this.chunkSizeModMask = chunkSize -1;

            this.hasLimit = sizeLimit>0;
            this.asyncWriteEnabled = asyncWriteEnabled;
            this.cleanerHackDisabled = false;
        }

        @Override
        public final boolean tryAvailable(long offset) {
            if (hasLimit && offset > sizeLimit) return false;

            int chunkPos = (int) (offset >>> chunkShift);

            //check for most common case, this is already mapped
            if (chunkPos < chunks.length){
                return true;
            }

            growLock.lock();
            try{
                //check second time
                if(chunkPos< chunks.length)
                    return true;

                int oldSize = chunks.length;
                ByteBuffer[] chunks2 = chunks;

                chunks2 = Arrays.copyOf(chunks2, Math.max(chunkPos+1, chunks2.length + chunks2.length/1000));

                for(int pos=oldSize;pos<chunks2.length;pos++) {
                    chunks2[pos]=makeNewBuffer(1L*chunkSize*pos);
                }


                chunks = chunks2;
            }finally{
                growLock.unlock();
            }
            return true;
        }

        protected abstract ByteBuffer makeNewBuffer(long offset);

        @Override public final void putLong(final long offset, final long value) {
            chunks[(int)(offset >>> chunkShift)].putLong((int) (offset & chunkSizeModMask), value);
        }

        @Override public final void putInt(final long offset, final int value) {
            chunks[(int)(offset >>> chunkShift)].putInt((int) (offset & chunkSizeModMask), value);
        }


        @Override public final void putByte(final long offset, final byte value) {
            chunks[(int)(offset >>> chunkShift)].put((int) (offset & chunkSizeModMask), value);
        }



        @Override public void putData(final long offset, final byte[] src, int srcPos, int srcSize){
            final ByteBuffer b1 = chunks[(int)(offset >>> chunkShift)].duplicate();
            final int bufPos = (int) (offset&chunkSizeModMask);

            b1.position(bufPos);
            b1.put(src, srcPos, srcSize);
        }

        @Override public final void putData(final long offset, final ByteBuffer buf) {
            final ByteBuffer b1 = chunks[(int)(offset >>> chunkShift)].duplicate();
            final int bufPos = (int) (offset&chunkSizeModMask);
            //no overlap, so just write the value
            b1.position(bufPos);
            b1.put(buf);
        }

        @Override final public long getLong(long offset) {
            return chunks[(int)(offset >>> chunkShift)].getLong((int) (offset&chunkSizeModMask));
        }

        @Override final public int getInt(long offset) {
            return chunks[(int)(offset >>> chunkShift)].getInt((int) (offset&chunkSizeModMask));
        }


        @Override public final byte getByte(long offset) {
            return chunks[(int)(offset >>> chunkShift)].get((int) (offset&chunkSizeModMask));
        }


        @Override
        public final DataInput2 getDataInput(long offset, int size) {
            return new DataInput2(chunks[(int)(offset >>> chunkShift)], (int) (offset&chunkSizeModMask));
        }

        @Override
        public boolean isEmpty() {
            return chunks.length==0;
        }

        @Override
        public boolean isSliced(){
            return true;
        }



        /**
         * Hack to unmap MappedByteBuffer.
         * Unmap is necessary on Windows, otherwise file is locked until JVM exits or BB is GCed.
         * There is no public JVM API to unmap buffer, so this tries to use SUN proprietary API for unmap.
         * Any error is silently ignored (for example SUN API does not exist on Android).
         */
        protected void unmap(MappedByteBuffer b){
            try{
                if(unmapHackSupported && !asyncWriteEnabled){

                    // need to dispose old direct buffer, see bug
                    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4724038
                    Method cleanerMethod = b.getClass().getMethod("cleaner", new Class[0]);
                    cleanerMethod.setAccessible(true);
                    if(cleanerMethod!=null){
                        Object cleaner = cleanerMethod.invoke(b);
                        if(cleaner!=null){
                            Method clearMethod = cleaner.getClass().getMethod("clean", new Class[0]);
                            if(clearMethod!=null) {
                                clearMethod.invoke(cleaner);
                            }
                        }else{
                            //cleaner is null, try fallback method for readonly buffers
                            Method attMethod = b.getClass().getMethod("attachment", new Class[0]);
                            attMethod.setAccessible(true);
                            Object att = attMethod.invoke(b);
                            if(att instanceof MappedByteBuffer)
                                unmap((MappedByteBuffer) att);
                        }
                    }
                }
            }catch(Exception e){
                unmapHackSupported = false;
                //TODO exception handling
                //Utils.LOG.log(Level.WARNING, "ByteBufferVol Unmap failed", e);
            }
        }

        private static boolean unmapHackSupported = true;
        static{
            try{
                unmapHackSupported =
                        SerializerPojo.classForName("sun.nio.ch.DirectBuffer")!=null;
            }catch(Exception e){
                unmapHackSupported = false;
            }
        }

        // Workaround for https://github.com/jankotek/MapDB/issues/326
        // File locking after .close() on Windows.
        private static boolean windowsWorkaround = System.getProperty("os.name").toLowerCase().startsWith("win");

    }

    public static final class MappedFileVol extends ByteBufferVol {

        protected final File file;
        protected final FileChannel fileChannel;
        protected final FileChannel.MapMode mapMode;
        protected final RandomAccessFile raf;

        public MappedFileVol(File file, boolean readOnly, long sizeLimit, int chunkShift, int sizeIncrement) {
            this(file,readOnly,sizeLimit,chunkShift,sizeIncrement,false,false);
        }


        public MappedFileVol(File file, boolean readOnly, long sizeLimit, int chunkShift,
                             int sizeIncrement, boolean asyncWriteEnabled) {
            this(file,readOnly,sizeLimit,chunkShift,sizeIncrement,asyncWriteEnabled,false);
        }
        public MappedFileVol(File file, boolean readOnly, long sizeLimit, int chunkShift, int sizeIncrement,
                             boolean asyncWriteEnabled, boolean cleanerHackDisable) {
            super(readOnly, sizeLimit, chunkShift, asyncWriteEnabled);
            this.file = file;
            this.mapMode = readOnly? FileChannel.MapMode.READ_ONLY: FileChannel.MapMode.READ_WRITE;
            this.cleanerHackDisabled = cleanerHackDisable;
            try {
                FileChannelVol.checkFolder(file,readOnly);
                this.raf = new RandomAccessFile(file, readOnly?"r":"rw");
                this.fileChannel = raf.getChannel();

                final long fileSize = fileChannel.size();
                if(fileSize>0){
                    //map existing data
                    int chunksSize = (int) ((Fun.roundUp(fileSize,chunkSize)>>> chunkShift));
                    chunks = new ByteBuffer[chunksSize];
                    for(int i=0;i<chunks.length;i++){
                        chunks[i] = makeNewBuffer(1L*i*chunkSize);
                    }
                }else{
                    chunks = new ByteBuffer[0];
                }
            } catch (IOException e) {
                throw new IOError(e);
            }
        }

        @Override
        public void close() {
            growLock.lock();
            try{
                closed = true;
                fileChannel.close();
                raf.close();
                //TODO not sure if no sync causes problems while unlocking files
                //however if it is here, it causes slow commits, sync is called on write-ahead-log just before it is deleted and closed
//                if(!readOnly)
//                    sync();

                if(!cleanerHackDisabled) {
                    for (ByteBuffer b : chunks) {
                        if (b != null && (b instanceof MappedByteBuffer)) {
                            unmap((MappedByteBuffer) b);
                        }
                    }
                }

                chunks = null;

            } catch (IOException e) {
                throw new IOError(e);
            }finally{
                growLock.unlock();
            }

        }

        @Override
        public void sync() {
            if(readOnly) return;
            growLock.lock();
            try{
                for(ByteBuffer b: chunks){
                    if(b!=null && (b instanceof MappedByteBuffer)){
                        MappedByteBuffer bb = ((MappedByteBuffer) b);
                        bb.force();
                    }
                }

            }finally{
                growLock.unlock();
            }

        }

        @Override
        protected ByteBuffer makeNewBuffer(long offset) {
            try {
                assert((offset&chunkSizeModMask)==0);
                assert(offset>=0);

                //write to end of file, to make sure space is allocated, see #442
                if(!readOnly) {
                    // get file size
                    long fileSize = fileChannel.size();
                    //and get last byte in mapped offset
                    long lastMappedOffset = Fun.roundUp(offset+1,chunkSize);

                    //expand file size, so no file is expanded by writing into mmaped ByteBuffer
                    if(Fun.roundUp(fileSize, chunkSize)<lastMappedOffset) {
                        //first write to last offset to expand file
                        //possibly better performance if file is expanded only once
                        ByteBuffer b = ByteBuffer.allocate(1);
                        while (b.remaining()>0) {
                            fileChannel.write(b, lastMappedOffset-1);
                        }

                        //now zero out all newly allocated bytes
                        b = ByteBuffer.allocate(1024);
                        for(;fileSize+1024<lastMappedOffset;fileSize+=1024){
                            //write to all data between current size and the end
                            while (b.remaining()>0) {
                                fileChannel.write(b, fileSize+b.position());
                            }
                            b.rewind();
                        }
                    }
                }

                ByteBuffer ret = fileChannel.map(mapMode,offset,chunkSize);
                return ret;
            } catch (IOException e) {
                throw new IOError(e);
            }
        }


        @Override
        public void deleteFile() {
            file.delete();
        }

        @Override
        public File getFile() {
            return file;
        }


        @Override
        public void truncate(long size) {
            final int maxSize = 1+(int) (size >>> chunkShift);
            if(maxSize==chunks.length)
                return;
            if(maxSize>chunks.length) {
                ensureAvailable(size);
                return;
            }
            growLock.lock();
            try{
                if(maxSize>=chunks.length)
                    return;
                ByteBuffer[] old = chunks;
                chunks = Arrays.copyOf(chunks,maxSize);

                //unmap remaining buffers
                for(int i=maxSize;i<old.length;i++){
                    if(!cleanerHackDisabled) {
                        unmap((MappedByteBuffer) old[i]);
                    }
                    old[i] = null;
                }

                if (ByteBufferVol.windowsWorkaround) {
                    for(int i=0;i<maxSize;i++){
                        if(!cleanerHackDisabled) {
                            unmap((MappedByteBuffer) old[i]);
                        }
                        old[i] = null;
                    }
                }

                try {
                    fileChannel.truncate(1L * chunkSize *maxSize);
                } catch (IOException e) {
                    throw new IOError(e);
                }

                if (ByteBufferVol.windowsWorkaround) {
                    for(int pos=0;pos<maxSize;pos++) {
                        chunks[pos]=makeNewBuffer(1L*chunkSize*pos);
                    }
                }

            }finally {
                growLock.unlock();
            }
        }

    }

    public static final class MemoryVol extends ByteBufferVol {
        protected final boolean useDirectBuffer;

        @Override
        public String toString() {
            return super.toString()+",direct="+useDirectBuffer;
        }

        public MemoryVol(final boolean useDirectBuffer, final long sizeLimit, final int chunkShift) {
            super(false,sizeLimit, chunkShift);
            this.useDirectBuffer = useDirectBuffer;
        }

        @Override
        protected ByteBuffer makeNewBuffer(long offset) {
            return useDirectBuffer?
                    ByteBuffer.allocateDirect(chunkSize):
                    ByteBuffer.allocate(chunkSize);
        }


        @Override
        public void truncate(long size) {
            final int maxSize = 1+(int) (size >>> chunkShift);
            if(maxSize==chunks.length)
                return;
            if(maxSize>chunks.length) {
                ensureAvailable(size);
                return;
            }
            growLock.lock();
            try{
                if(maxSize>=chunks.length)
                    return;
                ByteBuffer[] old = chunks;
                chunks = Arrays.copyOf(chunks,maxSize);

                //unmap remaining buffers
                for(int i=maxSize;i<old.length;i++){
                    if(!cleanerHackDisabled && old[i] instanceof  MappedByteBuffer)
                        unmap((MappedByteBuffer) old[i]);
                    old[i] = null;
                }

            }finally {
                growLock.unlock();
            }
        }

        @Override public void close() {
            growLock.lock();
            try{
                closed = true;
                if(!cleanerHackDisabled) {
                    for (ByteBuffer b : chunks) {
                        if (b != null && (b instanceof MappedByteBuffer)) {
                            unmap((MappedByteBuffer) b);
                        }
                    }
                }
                chunks = null;
            }finally{
                growLock.unlock();
            }
        }

        @Override public void sync() {}

        @Override public void deleteFile() {}

        @Override
        public File getFile() {
            return null;
        }
    }


    /**
     * Volume which uses FileChannel.
     * Uses global lock and does not use mapped memory.
     */
    public static final class FileChannelVol extends Volume {

        protected final File file;
        protected final int chunkSize;
        protected RandomAccessFile raf;
        protected FileChannel channel;
        protected final boolean readOnly;
        protected final long sizeLimit;
        protected final boolean hasLimit;

        protected volatile long size;
        protected final Object growLock = new Object();

        public FileChannelVol(File file, boolean readOnly, long sizeLimit, int chunkShift, int sizeIncrement){
            this.file = file;
            this.readOnly = readOnly;
            this.sizeLimit = sizeLimit;
            this.hasLimit = sizeLimit>0;
            this.chunkSize = 1<<chunkShift;
            try {
                checkFolder(file,readOnly);
                if(readOnly && !file.exists()){
                    raf = null;
                    channel = null;
                    size = 0;
                }else {
                    raf = new RandomAccessFile(file, readOnly ? "r" : "rw");
                    channel = raf.getChannel();
                    size = channel.size();
                }
            } catch (IOException e) {
                throw new IOError(e);
            }
        }

        protected static void checkFolder(File file, boolean readOnly) throws IOException {
            File parent = file.getParentFile();
            if(parent == null) {
                parent = file.getCanonicalFile().getParentFile();
            }
            if (parent == null) {
                throw new IOException("Parent folder could not be determined for: "+file);
            }
            if(!parent.exists() || !parent.isDirectory())
                throw new IOException("Parent folder does not exist: "+file);
            if(!parent.canRead())
                throw new IOException("Parent folder is not readable: "+file);
            if(!readOnly && !parent.canWrite())
                throw new IOException("Parent folder is not writable: "+file);
        }

        @Override
        public boolean tryAvailable(long offset) {
            if(hasLimit && offset>sizeLimit) return false;
            if(offset% chunkSize !=0)
                offset += chunkSize - offset% chunkSize; //round up to multiply of chunk size

            if(offset>size)synchronized (growLock){
                try {
                    channel.truncate(offset);
                    size = offset;
                } catch (IOException e) {
                    throw new IOError(e);
                }
            }
            return true;
        }

        @Override
        public void truncate(long size) {
            synchronized (growLock){
                try {
                    this.size = size;
                    channel.truncate(size);
                } catch (IOException e) {
                    throw new IOError(e);
                }
            }

        }

        protected void writeFully(long offset, ByteBuffer buf) throws IOException {
            int remaining = buf.limit()-buf.position();
            while(remaining>0){
                int write = channel.write(buf, offset);
                if(write<0) throw new EOFException();
                remaining-=write;
            }
        }

        @Override
        public final void putSixLong(long offset, long value) {
            assert(value>=0 && (value>>>6*8)==0): "value does not fit";

            try{

                ByteBuffer buf = ByteBuffer.allocate(6);
                buf.put(0, (byte) (0xff & (value >> 40)));
                buf.put(1, (byte) (0xff & (value >> 32)));
                buf.put(2, (byte) (0xff & (value >> 24)));
                buf.put(3, (byte) (0xff & (value >> 16)));
                buf.put(4, (byte) (0xff & (value >> 8)));
                buf.put(5, (byte) (0xff & (value >> 0)));

                writeFully(offset, buf);
            }catch(IOException e){
                throw new IOError(e);
            }
        }

        @Override
        public void putLong(long offset, long value) {
            try{
                ByteBuffer buf = ByteBuffer.allocate(8);
                buf.putLong(0, value);
                writeFully(offset, buf);
            }catch(IOException e){
                throw new IOError(e);
            }
        }

        @Override
        public void putInt(long offset, int value) {
            try{
                ByteBuffer buf = ByteBuffer.allocate(4);
                buf.putInt(0, value);
                writeFully(offset, buf);
            }catch(IOException e){
                throw new IOError(e);
            }
        }

        @Override
        public void putByte(long offset, byte value) {
            try{
                ByteBuffer buf = ByteBuffer.allocate(1);
                buf.put(0, value);
                writeFully(offset, buf);
            }catch(IOException e){
                throw new IOError(e);
            }
        }

        @Override
        public void putData(long offset, byte[] src, int srcPos, int srcSize) {
            try{
                ByteBuffer buf = ByteBuffer.wrap(src,srcPos, srcSize);
                writeFully(offset, buf);
            }catch(IOException e){
                throw new IOError(e);
            }
        }

        @Override
        public void putData(long offset, ByteBuffer buf) {
            try{
                writeFully(offset,buf);
            }catch(IOException e){
                throw new IOError(e);
            }
        }

        protected void readFully(long offset, ByteBuffer buf) throws IOException {
            int remaining = buf.limit()-buf.position();
            while(remaining>0){
                int read = channel.read(buf, offset);
                if(read<0) throw new EOFException();
                remaining-=read;
            }
        }

        @Override
        public final long getSixLong(long offset) {
            try{
                ByteBuffer buf = ByteBuffer.allocate(6);
                readFully(offset,buf);
                return ((long) (buf.get(0) & 0xff) << 40) |
                        ((long) (buf.get(1) & 0xff) << 32) |
                        ((long) (buf.get(2) & 0xff) << 24) |
                        ((long) (buf.get(3) & 0xff) << 16) |
                        ((long) (buf.get(4) & 0xff) << 8) |
                        ((long) (buf.get(5) & 0xff) << 0);

            }catch(IOException e){
                throw new IOError(e);
            }
        }


        @Override
        public long getLong(long offset) {
            try{
                ByteBuffer buf = ByteBuffer.allocate(8);
                readFully(offset,buf);
                return buf.getLong(0);
            }catch(IOException e){
                throw new IOError(e);
            }
        }

        @Override
        public int getInt(long offset) {
            try{
                ByteBuffer buf = ByteBuffer.allocate(4);
                readFully(offset,buf);
                return buf.getInt(0);
            }catch(IOException e){
                throw new IOError(e);
            }

        }

        @Override
        public byte getByte(long offset) {
            try{
                ByteBuffer buf = ByteBuffer.allocate(1);
                readFully(offset,buf);
                return buf.get(0);
            }catch(IOException e){
                throw new IOError(e);
            }
        }

        @Override
        public DataInput2 getDataInput(long offset, int size) {
            try{
                ByteBuffer buf = ByteBuffer.allocate(size);
                readFully(offset,buf);
                return new DataInput2(buf,0);
            }catch(IOException e){
                throw new IOError(e);
            }
        }

        @Override
        public void close() {
            try{
                closed = true;
                if(channel!=null)
                    channel.close();
                channel = null;
                if (raf != null)
                    raf.close();
                raf = null;
            }catch(IOException e){
                throw new IOError(e);
            }
        }

        @Override
        public void sync() {
            try{
                if(channel!=null)
                    channel.force(true);
            }catch(IOException e){
                throw new IOError(e);
            }
        }

        @Override
        public boolean isEmpty() {
            try {
                return channel==null || channel.size()==0;
            } catch (IOException e) {
                throw new IOError(e);
            }
        }

        @Override
        public void deleteFile() {
            file.delete();
        }

        @Override
        public boolean isSliced() {
            return false;
        }

        @Override
        public File getFile() {
            return file;
        }
    }

    /** transfer data from one volume to second. Second volume will be expanded if needed*/
    public static void volumeTransfer(long size, Volume from, Volume to){
        int bufSize = 1024*64;

        for(long offset=0;offset<size;offset+=bufSize){
            int bb = (int) Math.min(bufSize, size-offset);
            DataInput2 input = (DataInput2) from.getDataInput(offset, bb);
            ByteBuffer buf = input.buf.duplicate();
            buf.position(input.pos);
            buf.limit(input.pos+bb);
            to.ensureAvailable(offset+bb);
            to.putData(offset,buf);
        }
    }
}

