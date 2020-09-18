package org.erachain.core.exdata.exLink;

import org.erachain.core.exdata.ExData;

public class ExLinkComment extends ExLink {
    public ExLinkComment(long parentSeqNo, byte likeValue) {
        super(ExData.LINK_COMMENT_TYPE, likeValue, parentSeqNo);
    }

    public ExLinkComment(byte[] data) {
        super(data);
    }

    public ExLinkComment(byte[] type, long refLink) {
        super(type, refLink);
    }

}