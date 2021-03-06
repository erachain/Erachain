package org.erachain.core.web;

import com.google.common.base.Charsets;
import org.erachain.controller.Controller;
import org.erachain.core.transaction.ArbitraryTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.utils.GZIP;
import org.erachain.utils.StorageUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.*;

@Deprecated
public class WebNameStorageHistoryHelper {


    public static List<NameStorageTransactionHistory> getHistory(String name, int maxStorageTx) {
        List<NameStorageTransactionHistory> results = new ArrayList<>();

        OrphanNameStorageHelperMap orphanNameStorageHelperMap = DCSet.getInstance().getOrphanNameStorageHelperMap();

        List<byte[]> orphanMap = orphanNameStorageHelperMap.get(name);

        orphanMap = orphanMap == null ? new ArrayList<byte[]>() : orphanMap;

        List<byte[]> list = new ArrayList<>(orphanMap);
        Collections.reverse(list);

        for (int i = 0; i < list.size() && i < maxStorageTx; i++) {

            byte[] bs = list.get(i);

            Transaction transaction = Controller.getInstance().getTransaction(bs);
            if (transaction != null) {
                results.add(getKeyHistory((ArbitraryTransaction) transaction));
            } else {
                maxStorageTx++;
            }

        }


        return results;
    }

    @SuppressWarnings("unchecked")
    public static NameStorageTransactionHistory getKeyHistory(ArbitraryTransaction tx) {


//		MAKE SURE OTHER VALUE IN INNER JSON DON'T MAKE PROBLEMS!
        String jsonString = new String(tx.getData(), Charsets.UTF_8);

        jsonString = GZIP.webDecompress(jsonString);

        JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonString);

        NameStorageTransactionHistory result = new NameStorageTransactionHistory(tx);
        Set<String> keySet = jsonObject.keySet();

        OrphanNameStorageMap orphanNameStorageMap = DCSet.getInstance()
                .getOrphanNameStorageMap();

        Map<String, String> map = orphanNameStorageMap.get(tx.getSignature());

        Set<String> orphanKeySet = map.keySet();

        // initializing tempnamestorage with old values
        NameStorageMap tempNameStorageMap = new NameStorageMap(null);
        String name = (String) jsonObject.get("name");
        for (String key : orphanKeySet) {
            tempNameStorageMap.add(name, key, map.get(key));
        }
        // changing in temp namestorage
        StorageUtils.addTxChangesToStorage(jsonObject, name,
                tempNameStorageMap, null);

        for (String mainkey : keySet) {

            String innerJson = (String) jsonObject.get(mainkey);

            JSONObject innerJsonObject = (JSONObject) JSONValue
                    .parse(innerJson);

            if (innerJsonObject != null) {
                Set<String> innerKeyset = innerJsonObject.keySet();

                for (String innerkey : innerKeyset) {
                    result.addEntry(new NamestorageKeyValueHistory(map.get(innerkey),
                            (String) innerJsonObject.get(innerkey),
                            tempNameStorageMap.getOpt(name, innerkey), mainkey,
                            innerkey, name));
                }
            }

        }

        return result;

    }

}
