package org.erachain.core.exdata.exLink;

import org.erachain.core.exdata.ExData;

public class ExLinkReply extends ExLink {

    public ExLinkReply(long parentSeqNo) {
        super(ExData.LINK_REPLY_COMMENT_TYPE, parentSeqNo);
    }

    public ExLinkReply(byte[] data) {
        super(data);
    }

    public ExLinkReply(byte[] type, long refLink) {
        super(type, refLink);
    }

}