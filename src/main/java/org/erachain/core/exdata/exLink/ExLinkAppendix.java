package org.erachain.core.exdata.exLink;

public class ExLinkAppendix extends ExLink {

    public ExLinkAppendix(long parentSeqNo) {
        super(APPENDIX_TYPE, parentSeqNo);
    }

    public ExLinkAppendix(byte[] data) {
        super(data);
    }

}