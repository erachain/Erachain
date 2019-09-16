package org.erachain.dbs;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public interface DBMapSuit<T, U> extends DBTabSuitCommon<T, U> {

    void getMap();
    void createIndexes();

    U getDefaultValue();

}
