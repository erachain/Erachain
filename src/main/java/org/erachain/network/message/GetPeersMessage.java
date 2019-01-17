package org.erachain.network.message;

public class GetPeersMessage extends Message {

    public GetPeersMessage() {
        super(GET_PEERS_TYPE);
    }

    public boolean isRequest() {
        return true;
    }

    public int getDataLength() {
        return 0;
    }

}
