package network.message;

public class GetPeersMessage extends Message{

	public GetPeersMessage()
	{
		super(GET_PEERS_TYPE);	
	}
		
	public boolean isRequest()
	{
		return true;
	}

}
