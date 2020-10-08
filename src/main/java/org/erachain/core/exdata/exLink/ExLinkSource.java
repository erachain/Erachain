package org.erachain.core.exdata.exLink;

import org.erachain.controller.Controller;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;

public class ExLinkSource extends ExLinkMemo {

    public ExLinkSource(long parentSeqNo, String memo) {
        super(ExData.LINK_SOURCE_TYPE, parentSeqNo, memo);
    }

    public ExLinkSource(byte[] data, int position) {
        super(data, position);
    }

    public ExLinkSource(byte type, byte flags, int value, long ref, byte[] memoBytes) {
        super(ExData.LINK_SOURCE_TYPE, flags, value, ref, memoBytes);
    }

    public JSONObject makeJSONforHTML() {
        JSONObject json = super.makeJSONforHTML();
        json.put("title", Controller.getInstance().getTransaction(ref).getTitle());
        json.put("ref", Transaction.viewDBRef(ref));

        return json;
    }

    public JSONObject toJson() {
        JSONObject json = super.toJson(false);
        json.put("ref", Transaction.viewDBRef(ref));

        return json;
    }


    public int isValid(DCSet dcSet) {

        int result = super.isValid(dcSet);
        if (result != Transaction.VALIDATE_OK) {
            return result;
        }

        if (!dcSet.getItemPersonMap().contains(ref))
            return Transaction.ITEM_PERSON_NOT_EXIST;

        return Transaction.VALIDATE_OK;
    }

    public void process(Transaction transaction) {
        // создадим связь в базе - как источник / пользователи + потребители + получатели +
        transaction.getDCSet().getExLinksMap().put(transaction.getDBRef(), new ExLink(ExData.LINK_SOURCE_TYPE, ref));
    }

    public void orphan(Transaction transaction) {
        transaction.getDCSet().getExLinksMap().remove(transaction.getDBRef());
    }

}
