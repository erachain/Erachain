package network.message;

import java.nio.charset.Charset;
import java.util.Arrays;

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
		int length = data.length;
		
		Transaction transaction = TransactionFactory.getInstance().parse(data, null);
		int position = transaction.getDataLength(false);
		byte[] callbackBytes = Arrays.copyOfRange(data, position, position + length);
		String callback =  new String( callbackBytes, Charset.forName("UTF-8") );

		return new TelegramMessage(transaction, callback);
	}
	
	public byte[] toBytes() 
	{
		byte[] data = new byte[0];
		
		//WRITE BLOCK
		byte[] blockBytes = this.transaction.toBytes(true, null);
		data = Bytes.concat(data, blockBytes);

		// CALLBACK
		byte[] callbackBytes = this.callback.getBytes( Charset.forName("UTF-8") );
		data = Bytes.concat(data, callbackBytes);

		//ADD CHECKSUM
		data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);
		
		return data;
	}
	

	public TelegramMessage copy() {
		try {
			byte[] data = this.toBytes();
			int position = Message.MAGIC_LENGTH + TYPE_LENGTH + 1 + MESSAGE_LENGTH + CHECKSUM_LENGTH;
			data = Arrays.copyOfRange(data, position, data.length);
			return TelegramMessage.parse(data);
		} catch (Exception e) {
			return null;
		}
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
