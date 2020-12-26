package org.erachain.core.exdata.exLink;

import org.erachain.controller.Controller;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;

public class ExLinkSource extends ExLinkMemo {

    public ExLinkSource(long parentSeqNo, String memo) {
        super(ExData.LINK_SOURCE_TYPE, parentSeqNo, memo);
    }

    public ExLinkSource(byte[] data) {
        super(data);
    }

    public ExLinkSource(byte[] data, int position) {
        super(data, position);
    }

    public ExLinkSource(byte flags, int value, long ref, byte[] memoBytes) {
        super(ExData.LINK_SOURCE_TYPE, flags, value, ref, memoBytes);
    }

    public JSONObject makeJSONforHTML(JSONObject langObj) {
        JSONObject json = super.makeJSONforHTML(langObj);
        Transaction source = Controller.getInstance().getTransaction(ref);
        json.put("name", Lang.getInstance().translateFromLangObj(source.viewFullTypeName(), langObj)
                + ": " + source.getTitle() + (source.getCreator() == null ? "" : " - " + source.getCreator().getPersonAsString()));
        json.put("weight", getValue());

        return json;
    }

    public JSONObject toJson() {
        JSONObject json = super.toJson(false);
        json.put("weight", getValue());

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

        if (!dcSet.getTransactionFinalMap().contains(ref))
            return Transaction.TRANSACTION_DOES_NOT_EXIST;

        return Transaction.VALIDATE_OK;
    }

    /*
    @Override
    public void process(Transaction transaction) {
        super.process(transaction);
        transaction.getDCSet().getExLinksMap().put(ref, new ExLinkSourceUse(transaction, this));
    }

    @Override
    public void orphan(Transaction transaction) {
        super.orphan(transaction);
        transaction.getDCSet().getExLinksMap().remove(ref, transaction.getDBRef());
    }
     */

}
