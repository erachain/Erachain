package org.erachain.core.exdata.exLink;

public class ExLinkLike extends ExLink {
    public ExLinkLike(long parentSeqNo, byte likeValue) {
        super(LIKE_TYPE, likeValue, parentSeqNo);
    }

    public ExLinkLike(byte[] data) {
        super(data);
    }

}