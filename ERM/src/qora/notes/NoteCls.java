package qora.notes;

//import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import controller.Controller;
import database.DBSet;
import qora.account.Account;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import qora.transaction.Transaction;

public abstract class NoteCls {

	public static final int NOTE = 1;
	public static final int SAMPLE = 2;
	public static final int PAPER = 3;

	protected static final int TYPE_LENGTH = 2;
	protected static final int CREATOR_LENGTH = Account.ADDRESS_LENGTH;
	protected static final int NAME_SIZE_LENGTH = 1;
	protected static final int DESCRIPTION_SIZE_LENGTH = 4;
	protected static final int REFERENCE_LENGTH = Transaction.SIGNATURE_LENGTH;
	protected static final int BASE_LENGTH = TYPE_LENGTH + CREATOR_LENGTH + NAME_SIZE_LENGTH + DESCRIPTION_SIZE_LENGTH;
	
	protected byte[] typeBytes;
	protected Account creator;
	protected String name;
	protected String description;
	protected long key = -1;
	protected byte[] reference = null; // this is signature of issued record
	
	public NoteCls(byte[] typeBytes, Account creator, String name, String description)
	{
		this.typeBytes = typeBytes;
		this.creator = creator;
		this.name = name;
		this.description = description;
	}
	public NoteCls(int type, Account creator, String name, String description)
	{
		this(new byte[TYPE_LENGTH], creator, name, description);
		this.typeBytes[0] = (byte)type;
	}

	//GETTERS/SETTERS
	
	public byte[] getType()
	{
		return this.typeBytes;
	}

	public Account getCreator() {
		return this.creator;
	}
	
	public String getName() {
		return this.name;
	}
	public long getKey() {
		return this.getKey(DBSet.getInstance());
	}
	public long getKey(DBSet db) {
		// TODO if ophran ?
		if (this.key <0) this.key = db.getIssueNoteMap().get(this.reference);
		return this.key;
	}
	
	public String getDescription() {
		return this.description;
	}
		
	public byte[] getReference() {
		return this.reference;
	}
	public void setReference(byte[] reference) {
		this.reference = reference;
	}
		
	public boolean isConfirmed() {
		return DBSet.getInstance().getIssueNoteMap().contains(this.reference);
	}
	
	public byte[] toBytes(boolean includeReference)
	{

		byte[] data = new byte[0];
		
		//WRITE TYPE
		data = Bytes.concat(data, this.typeBytes);

		//WRITE CREATOR
		try
		{
			data = Bytes.concat(data, Base58.decode(this.creator.getAddress()));
		}
		catch(Exception e)
		{
			//DECODE EXCEPTION
		}
		
		//WRITE NAME SIZE
		byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
		data = Bytes.concat(data, new byte[]{(byte)nameBytes.length});
		
		//WRITE NAME
		data = Bytes.concat(data, nameBytes);
		
		//WRITE DESCRIPTION SIZE
		byte[] descriptionBytes = this.description.getBytes(StandardCharsets.UTF_8);
		int descriptionLength = descriptionBytes.length;
		byte[] descriptionLengthBytes = Ints.toByteArray(descriptionLength);
		//Logger.getGlobal().info("descriptionLengthBytes.len : " + descriptionLengthBytes.length);
		data = Bytes.concat(data, descriptionLengthBytes);
				
		//WRITE DESCRIPTION
		data = Bytes.concat(data, descriptionBytes);
		
		if(includeReference)
		{
			//WRITE REFERENCE
			data = Bytes.concat(data, this.reference);
		}
		else
		{
			//WRITE EMPTY REFERENCE
			// data = Bytes.concat(data, new byte[64]);
		}
		
		return data;
	}

	public int getDataLength(boolean includeReference) 
	{
		return BASE_LENGTH
				+ this.name.getBytes(StandardCharsets.UTF_8).length
				+ this.description.getBytes(StandardCharsets.UTF_8).length
				+ (includeReference? REFERENCE_LENGTH: 0);
	}	
	
	//OTHER
	
	public String toString()
	{		
		return "(" + this.key + ":" + this.typeBytes.toString() + ") " + this.name;
	}
	
	public String getShort()
	{
		return "(" + this.key + ":" + this.typeBytes.toString() + ") " + this.name.substring(0, Math.min(this.name.length(), 4));
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		
		JSONObject noteJSON = new JSONObject();

		// ADD DATA
		noteJSON.put("type0", Byte.toUnsignedInt(this.typeBytes[0]));
		noteJSON.put("type1", Byte.toUnsignedInt(this.typeBytes[1]));
		noteJSON.put("key", this.key);
		noteJSON.put("name", this.name);
		noteJSON.put("description", this.description);
		noteJSON.put("creator", this.creator.getAddress());
		noteJSON.put("isConfirmed", this.isConfirmed());
		noteJSON.put("reference", Base58.encode(this.reference));
		
		Transaction txReference = Controller.getInstance().getTransaction(this.reference);
		if(txReference != null)
		{
			noteJSON.put("timestamp", txReference.getTimestamp());
		}
		
		return noteJSON;
	}
}
