package org.erachain.core.exdata.exLink;

import org.erachain.api.ApiErrorFactory;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.Transaction;
import org.json.simple.JSONObject;

public class ExLinkAppendix extends ExLink {

    public ExLinkAppendix(long parentSeqNo) {
        super(ExData.LINK_APPENDIX_TYPE, parentSeqNo);
    }

    public static ExLinkAppendix of(JSONObject jsonObject) {
        Object linkToRefObj = jsonObject.get("linkTo");
        if (linkToRefObj == null)
            return null;
        else {
            Long linkToRef = Transaction.parseDBRef(linkToRefObj);
            if (linkToRef == null) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_BLOCK_TRANS_SEQ_ERROR);
            }
            return new ExLinkAppendix(linkToRef);
        }
    }

    public static ExLinkAppendix of(Object linkToRefObj) {
        if (linkToRefObj == null)
            return null;
        else {
            Long linkToRef = Transaction.parseDBRef(linkToRefObj);
            if (linkToRef == null) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_BLOCK_TRANS_SEQ_ERROR);
            }
            return new ExLinkAppendix(linkToRef);
        }
    }

    public ExLinkAppendix(byte[] data) {
        super(data);
    }

    public ExLinkAppendix(byte[] type, long refLink) {
        super(type, refLink);
    }

}