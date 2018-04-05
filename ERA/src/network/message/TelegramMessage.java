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
	
	public TelegramMessage(Transaction transaction)
	{
		super(TELEGRAM_TYPE);
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

	public static TelegramMessage parse(byte[] data) throws Exception
	{
		//PARSE TRANSACTION
		int length = data.length;
		
		Transaction transaction = TransactionFactory.getInstance().parse(data, null);

		return new TelegramMessage(transaction);
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
		
		return telegram;

	}
	
	@Override
	public int getDataLength()
	{
		return this.transaction.getDataLength(false);
	}
	
}
