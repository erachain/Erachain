package network.message;

import com.google.common.primitives.Bytes;

public class GetBlockMessage extends Message{

	private byte[] signature;
	
	private static final int GET_BLOCK_LENGTH = core.block.Block.SIGNATURE_LENGTH;
	
	public GetBlockMessage(byte[] signature)
	{
		super(GET_BLOCK_TYPE);	
		
		this.signature = signature;
	}
	
	public byte[] getSignature()
	{
		return this.signature;
	}
	
	public boolean isRequest()
	{
		return true;
	}

	public static GetBlockMessage parse(byte[] data) throws Exception 
	{
		//CHECK IF DATA MATCHES LENGTH
		if(data.length != GET_BLOCK_LENGTH)
		{
			throw new Exception("Data does not match length");
		}
				
		return new GetBlockMessage(data);
	}
	
	@Override
	public byte[] toBytes()
	{
		byte[] data = new byte[0];
		
		//WRITE SIGNATURE
		data = Bytes.concat(data, this.signature);
		
		//ADD CHECKSUM
		data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);
		
		return data;
	}
	
	@Override
	public int getDataLength()
	{
		return GET_BLOCK_LENGTH;
	}

}
