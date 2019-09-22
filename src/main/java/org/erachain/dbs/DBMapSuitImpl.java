package org.erachain.dbs;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class DBMapSuitImpl<T, U> implements DBMapSuit<T, U> {

    @Override
    public int getDefaultIndex() {
        return 0;
    }

    protected abstract void getMap();

    protected void createIndexes() {
    }

    protected U getDefaultValue() { return null; }

    @Override
    public void close() {}

    @Override
    public void commit() {}

    @Override
    public void rollback() {}
}
