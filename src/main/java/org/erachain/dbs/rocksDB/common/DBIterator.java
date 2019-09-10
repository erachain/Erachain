package org.erachain.rocksDB.common;

import java.io.Closeable;
import java.util.Iterator;

public interface DBIterator extends Iterator<byte[]>, Closeable {

}
