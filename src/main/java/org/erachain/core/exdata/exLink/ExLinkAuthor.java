package org.erachain.core.exdata.exLink;

import org.erachain.controller.Controller;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;

public class ExLinkAuthor extends ExLinkMemo {

    public ExLinkAuthor(long parentSeqNo, String memo) {
        super(ExData.LINK_AUTHOR_TYPE, parentSeqNo, memo);
    }

    public ExLinkAuthor(byte[] data) {
        super(data);
    }

    public ExLinkAuthor(byte[] data, int position) {
        super(data, position);
    }

    public ExLinkAuthor(byte flags, int value, long ref, byte[] memoBytes) {
        super(ExData.LINK_AUTHOR_TYPE, flags, value, ref, memoBytes);
    }

    public JSONObject makeJSONforHTML(JSONObject langObj) {
        JSONObject json = super.makeJSONforHTML(langObj);
        json.put("name", Controller.getInstance().getPerson(ref).getName());
        json.put("ref", ref);

        return json;
    }

    public int isValid(DCSet dcSet) {
        int result = super.isValid(dcSet);
        if (result != Transaction.VALIDATE_OK) {
            return result;
        }

        int weight = getValue();
        if (weight > 1000 || weight < 0) {
            return Transaction.INVALID_AMOUNT;
        }

        if (!dcSet.getItemPersonMap().contains(ref))
            return Transaction.ITEM_PERSON_NOT_EXIST;

        return Transaction.VALIDATE_OK;
    }

    @Override
    public void process(Transaction transaction) {
        // EMPTY
    }

    @Override
    public void orphan(Transaction transaction) {
        // EMPTY
    }

}
