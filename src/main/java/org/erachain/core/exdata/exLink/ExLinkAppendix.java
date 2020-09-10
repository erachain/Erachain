package org.erachain.core.exdata.exLink;

import org.erachain.core.transaction.Transaction;

public class ExLinkAppendix extends ExLink {

    public ExLinkAppendix(long parentSeqNo) {
        super(APPENDIX_TYPE, parentSeqNo);
    }

    public ExLinkAppendix(byte[] data) {
        super(data);
    }

    public ExLinkAppendix(byte[] type, long refLink) {
        super(type, refLink);
    }

    @Override
    public void process(Transaction transaction) {
        transaction.getDCSet().getVouchRecordMap().put(transaction.getDBRef(), this);
    }

    @Override
    public void orphan(Transaction transaction) {
        transaction.getDCSet().getVouchRecordMap().remove(transaction.getDBRef());
    }


}