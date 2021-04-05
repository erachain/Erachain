package org.erachain.core.transaction;

import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.item.statuses.StatusFactory;

import java.util.Arrays;

//import java.math.BigInteger;

public class GenesisIssueStatusRecord extends GenesisIssueItemRecord {

    private static final byte TYPE_ID = (byte) GENESIS_ISSUE_STATUS_TRANSACTION;
    private static final String NAME_ID = "GENESIS Issue Status";

    public GenesisIssueStatusRecord(StatusCls status) {
        super(TYPE_ID, NAME_ID, status);

        //this.generateSignature();

    }

    //GETTERS/SETTERS
    //public static String getName() { return "Genesis Issue Status"; }

    @SuppressWarnings("unchecked")

    //PARSE CONVERT
    public static Transaction Parse(byte[] data) throws Exception {
        //CHECK IF WE MATCH BLOCK LENGTH
        if (data.length < BASE_LENGTH) {
            throw new Exception("Data does not match block length");
        }

        // READ TYPE
        int position = SIMPLE_TYPE_LENGTH;

        //READ STATUS
        // read without reference
        StatusCls status = StatusFactory.getInstance().parse(Transaction.FOR_NETWORK, Arrays.copyOfRange(data, position, data.length), false);
        //position += status.getDataLength(false);

        return new GenesisIssueStatusRecord(status);
    }

}
