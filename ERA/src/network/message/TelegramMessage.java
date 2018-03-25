package network.message;

import com.google.common.primitives.Bytes;

import core.transaction.Transaction;
import core.transaction.TransactionFactory;

public class TelegramMessage extends Message{

	private Transaction transaction;
	private String callback;
	
	public TelegramMessage(Transaction transaction, String callback)
	{
		super(TELEGRAM_TYPE);	
		
		this.callback = callback;
		this.transaction = transaction;
	}
	
	public Transaction getTransaction()
	{
		return this.transaction;
	}

	public String getCallback()
	{
		return this.callback;
	}
	
	public boolean isRequest()
	{
		return false;
	}

	public static TelegramMessage parse(byte[] data) throws Exception
	{
		//PARSE TRANSACTION
		Transaction transaction = TransactionFactory.getInstance().parse(data, null);
		
		return new TelegramMessage(transaction, "");
	}
	
	public byte[] toBytes() 
	{
		byte[] data = new byte[0];
		
		//WRITE BLOCK
		byte[] blockBytes = this.transaction.toBytes(true, null);
		data = Bytes.concat(data, blockBytes);
		
		//ADD CHECKSUM
		data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);
		
		return data;
	}	
	
	@Override
	public int getDataLength()
	{
		return this.transaction.getDataLength(false);
	}
	
}
