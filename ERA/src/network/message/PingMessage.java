package network.message;

public class PingMessage extends Message{

	public PingMessage()
	{
		super(GET_PING_TYPE);	
	}
		
	public boolean isRequest()
	{
		return true;
	}

}
