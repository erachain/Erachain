package org.erachain.core.exdata.exLink;

import org.erachain.controller.Controller;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.Transaction;
import org.json.simple.JSONObject;

public class ExLinkAuthorIssue extends ExLinkMemo {

    public ExLinkAuthorIssue(byte[] data) {
        super(data);
    }

    public ExLinkAuthorIssue(Transaction transaction, ExLinkAuthor exLinkAuthor) {
        super(ExData.LINK_AUTHOR_ISSUE_TYPE, exLinkAuthor.flags, exLinkAuthor.getValue(), transaction.getDBRef(), exLinkAuthor.memoBytes);
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

}
