package org.erachain.dbs;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class DBMapSuitImpl<T, U> implements DBMapSuit<T, U> {

    protected U defaultValue;

    @Override
    public int getDefaultIndex() {
        return 0;
    }

    protected abstract void openMap();

    protected void createIndexes() {
    }

    @Override
    public U getDefaultValue() {
        return defaultValue;
    }

}
