package org.erachain.core.exdata.exActions;

import com.google.common.primitives.Ints;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.exdata.ExAirDrop;
import org.erachain.core.exdata.ExPays;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Simple pay - for all same amount
 */

public abstract class ExAction {

    /////////////////
    DCSet dcSet;
    private int height;

    public String errorValue;

    public abstract long getTotalFeeBytes();

    public void setDC(DCSet dcSet) {
        if (this.dcSet == null || !this.dcSet.equals(dcSet)) {
            this.dcSet = dcSet;
        }
    }


    public List<Fun.Tuple3<Account, BigDecimal, Fun.Tuple2<Integer, String>>> getCheckedAccruals(Transaction statement) {
    }

    public List<Fun.Tuple3<Account, BigDecimal, Fun.Tuple2<Integer, String>>> precalcCheckedAccruals(int height, Account creator) {
    }

    public abstract byte[] toBytes() throws Exception;

    public abstract int length();

    public static ExAction parse(byte[] data, int pos) throws Exception {


        int actionID = Ints.fromByteArray(Arrays.copyOfRange(data, pos, pos + Integer.BYTES));
        pos += Integer.BYTES;

        switch (actionID) {
            case 0:
                return ExPays.parse(data, pos);
            case 1:
                return ExAirDrop.parse(data, pos);
        }
    }

    /**
     * Version 2 maker for BlockExplorer
     */
    public abstract JSONObject makeJSONforHTML(JSONObject langObj);

    public abstract JSONObject toJson();

    public Fun.Tuple2<Integer, String> checkValidList(DCSet dcSet, int height, AssetCls asset, Account creator) {
        return new Fun.Tuple2<>(Transaction.VALIDATE_OK, null);
    }

    public abstract int isValid(RSignNote rNote);

    public abstract void process(Transaction rNote, Block block);

    public abstract void orphan(Transaction rNote);

}
