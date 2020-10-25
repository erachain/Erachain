package org.erachain.core.exdata.exLink;

import org.erachain.controller.Controller;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.Transaction;
import org.json.simple.JSONObject;

public class ExLinkSourceUse extends ExLinkMemo {

    public ExLinkSourceUse(byte[] data) {
        super(data);
    }

    public ExLinkSourceUse(Transaction transaction, ExLinkSource exLinkSource) {
        super(ExData.LINK_SOURCE_USE_TYPE, exLinkSource.flags, exLinkSource.getValue(), transaction.getDBRef(), exLinkSource.memoBytes);
    }

    public JSONObject makeJSONforHTML(JSONObject langObj) {
        JSONObject json = super.makeJSONforHTML(langObj);
        json.put("name", Controller.getInstance().getTransaction(ref).toStringShortAsCreator());
        json.put("weight", getValue());

        return json;
    }

    public JSONObject toJson() {
        JSONObject json = super.toJson(false);
        json.put("weight", getValue());

        return json;
    }
}
