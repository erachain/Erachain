package org.erachain.core.exdata.exActions;

import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import java.util.List;

/**
 * Simple pay - for all same amount
 */

public abstract class ExAction<R> {

    public static final int FILTERED_ACCRUALS_TYPE = 0;
    public static final int LIST_PAYOUTS_TYPE = 1;

    int type;
    R results;
    /////////////////
    DCSet dcSet;
    private int height;

    public String errorValue;

    ExAction(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public R getResults() {
        return results;
    }

    public abstract long getTotalFeeBytes();

    public void setDC(DCSet dcSet) {
        if (this.dcSet == null || !this.dcSet.equals(dcSet)) {
            this.dcSet = dcSet;
        }
    }

    public abstract byte[] getDBdata();

    public abstract byte[] toBytes() throws Exception;

    public abstract int length();

    public abstract int getLengthDBData();

    public abstract int parseDBData(byte[] dbData, int position);

    public static ExAction parse(int type, byte[] data, int pos) throws Exception {

        switch (type) {
            case FILTERED_ACCRUALS_TYPE:
                return ExPays.parse(data, pos);
            case LIST_PAYOUTS_TYPE:
                return ExAirDrop.parse(data, pos);
        }

        throw new Exception("Invalid ExAction type: " + type);

    }

    public static Fun.Tuple2<ExAction, String> parseJSON(JSONObject json) {

        try {
            int type = (Integer) json.get("type");
            switch (type) {
                case FILTERED_ACCRUALS_TYPE:
                    return ExPays.parseJSON_local(json);
                case LIST_PAYOUTS_TYPE:
                    return ExAirDrop.parseJSON_local(json);
            }
            return new Fun.Tuple2<>(null, "Invalid ExAction type: " + type);
        } catch (Exception e) {
            return new Fun.Tuple2<>(null, e.getMessage());
        }

    }

    /**
     * Version 2 maker for BlockExplorer
     */
    public abstract JSONObject makeJSONforHTML(JSONObject langObj);

    public abstract JSONObject toJson();

    public abstract String getInfoHTML();

    /**
     * make calculations and validate it if need
     *
     * @param andValidate
     */
    public abstract int preProcessAndValidate(int height, Account creator, boolean andValidate);

    public int makeResults(Transaction transaction) {
        setDC(transaction.getDCSet());
        return preProcessAndValidate(transaction.getBlockHeight(), transaction.getCreator(), false);
    }

    public abstract void updateItemsKeys(List listTags);

    public abstract int isValid(RSignNote rNote);

    public abstract void process(Transaction rNote, Block block);

    public abstract void orphan(Transaction rNote);

}
