package network.message;

import com.google.common.primitives.Bytes;

import core.transaction.Transaction;
import core.transaction.TransactionFactory;

public class TransactionMessage extends Message{

	private Transaction transaction;
	
	public TransactionMessage(Transaction transaction)
	{
		super(TRANSACTION_TYPE);	
		
		this.transaction = transaction;
	}
	
	public Transaction getTransaction()
	{
		return this.transaction;
	}
	
	public boolean isRequest()
	{
		return false;
	}

	public static TransactionMessage parse(byte[] data) throws Exception
	{
		//PARSE TRANSACTION
		Transaction transaction = TransactionFactory.getInstance().parse(data, null);
		
		return new TransactionMessage(transaction);
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
