package org.erachain.core.exdata.exLink;

public class ExLinkReply extends ExLink {

    public ExLinkReply(long parentSeqNo) {
        super(REPLY_TYPE, parentSeqNo);
    }

    public ExLinkReply(byte[] data) {
        super(data);
    }

}