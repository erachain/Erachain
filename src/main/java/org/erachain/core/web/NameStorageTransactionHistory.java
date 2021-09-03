package org.erachain.core.web;

import org.apache.commons.lang3.StringUtils;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.ArbitraryTransaction;
import org.erachain.utils.DateTimeFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Deprecated
public class NameStorageTransactionHistory {

    private final List<NamestorageKeyValueHistory> keyvalueList;
    private final ArbitraryTransaction tx;

    public NameStorageTransactionHistory(ArbitraryTransaction tx) {
        keyvalueList = new ArrayList<>();
        this.tx = tx;
    }

    public ArbitraryTransaction getTx() {
        return tx;
    }

    public List<NamestorageKeyValueHistory> getKeyValueHistoryList() {
        return Collections.unmodifiableList(keyvalueList);
    }

    public void addEntry(NamestorageKeyValueHistory entry) {
        if (!keyvalueList.contains(entry)) {
            keyvalueList.add(entry);
        }
    }

    public String getCreationTime() {
        return DateTimeFormat.timestamptoString(tx.getTimestamp());
    }

    public String getSignature() {
        return Base58.encode(tx.getSignature());
    }

    public String getAllKeys() {
        String result = "";

        for (NamestorageKeyValueHistory namestorageKeyValueHistory : keyvalueList) {
            result += namestorageKeyValueHistory.getKey() + ", ";
        }

        return StringUtils.removeEnd(result, ", ");

    }

}
