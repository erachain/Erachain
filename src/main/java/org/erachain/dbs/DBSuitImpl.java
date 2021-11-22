package org.erachain.dbs;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class DBSuitImpl<T, U> implements DBSuit<T, U> {

    protected DBTab cover;

    protected boolean sizeEnable;

    @Override
    public boolean isSizeEnable() {
        return sizeEnable;
    }

    @Override
    public int getDefaultIndex() {
        return 0;
    }

    protected void createIndexes() {
    }

    @Override
    public U getDefaultValue(T key) {
        if (cover != null)
            return (U) cover.getDefaultValue(key);

        return null;
    }

    @Override
    public void close() {
        cover = null;
    }

}
