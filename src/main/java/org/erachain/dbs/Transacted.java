package org.erachain.dbs;

public interface Transacted {

    void commit();

    void rollback();
}
