package org.erachain.core.exdata.exLink;

import org.erachain.core.exdata.ExData;

public class ExLinkAppendix extends ExLink {

    public ExLinkAppendix(long parentSeqNo) {
        super(ExData.LINK_APPENDIX_TYPE, parentSeqNo);
    }

    public ExLinkAppendix(byte[] data) {
        super(data);
    }

    public ExLinkAppendix(byte[] type, long refLink) {
        super(type, refLink);
    }

}