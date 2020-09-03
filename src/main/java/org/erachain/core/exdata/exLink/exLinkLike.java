package org.erachain.core.exdata.exLink;

public class exLinkLike extends ExLink {
    public exLinkLike(long parentSeqNo, byte likeValue) {
        super(LIKE_TYPE, parentSeqNo, likeValue);
    }
}