package org.erachain.network.message;

public class GetHWeightMessage extends Message {

    public GetHWeightMessage() {
        super(GET_HWEIGHT_TYPE);
    }

    public boolean isRequest() {
        return true;
    }

    public int getDataLength() {
        return 0;
    }
}
