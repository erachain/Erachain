package org.erachain.core.exdata.exLink;

import org.erachain.controller.Controller;
import org.erachain.core.exdata.ExData;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;

public class ExLinkAuthor extends ExLinkMemo {

    public ExLinkAuthor(byte[] data) {
        super(data);
    }

    public ExLinkAuthor(long parentSeqNo, int value, String memo) {
        super(ExData.LINK_AUTHOR_TYPE, parentSeqNo, value, memo);
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
        json.put("share", getValue());
        json.put("ref", ref);

        return json;
    }

    public JSONObject toJson() {
        JSONObject json = super.toJson(false);
        json.put("share", getValue());
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

    // ТУТ Персоны ане ссылки на Зпаись - надо переделывать
    @Override
    public void process(Transaction transaction) {
        super.process(transaction); // ADD PERSON as KEY

        // ADD issue TX as KEY
        DCSet dcSet = transaction.getDCSet();
        ItemCls person = dcSet.getItemPersonMap().get(ref);
        Transaction issueTX = person.getIssueTransaction(dcSet);
        dcSet.getExLinksMap().put(new ExLinkAuthor(flags, getValue(), issueTX.getDBRef(), memoBytes), transaction.getDBRef());
    }

    @Override
    public void orphan(Transaction transaction) {
        super.orphan(transaction); // REMOVE PERSON as KEY

        // REMOVE issue TX as KEY
        DCSet dcSet = transaction.getDCSet();
        ItemCls person = dcSet.getItemPersonMap().get(ref);
        Transaction issueTX = person.getIssueTransaction(dcSet);
        transaction.getDCSet().getExLinksMap().remove(issueTX.getDBRef(), type, transaction.getDBRef());
    }

}
