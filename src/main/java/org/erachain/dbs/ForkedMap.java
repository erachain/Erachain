package org.erachain.dbs;

public interface ForkedMap {

    DBTab getParent();

    void writeToParent();
}
