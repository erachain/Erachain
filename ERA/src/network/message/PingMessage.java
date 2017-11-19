package network.message;

public class PingMessage extends Message{

	public PingMessage()
	{
		super(PING_TYPE);	
	}
		
	public boolean isRequest()
	{
		return true;
	}

}
