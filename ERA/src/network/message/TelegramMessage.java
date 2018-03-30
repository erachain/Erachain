package network.message;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;

import core.crypto.Base58;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;
import datachain.DCSet;

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
	
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {

		//DCSet localDCSet = DCSet.getInstance();
		JSONObject telegram = new JSONObject();

		telegram.put("transaction", transaction.toJson());
		telegram.put("callback", callback);
		
		return telegram;

	}
	
	@Override
	public int getDataLength()
	{
		return this.transaction.getDataLength(false);
	}
	
}
