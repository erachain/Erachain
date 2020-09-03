package org.erachain.core.exdata.exLink;

public class exLinkReply extends ExLink {
    public exLinkReply(long parentSeqNo) {
        super(REPLY_TYPE, parentSeqNo);
    }
}