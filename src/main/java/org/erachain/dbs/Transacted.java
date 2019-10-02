package org.erachain.dbs;

public interface Transacted {

    int parentSize();

    void commit();

    void rollback();
}
