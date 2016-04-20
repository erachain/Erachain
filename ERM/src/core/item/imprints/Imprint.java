package core.item.imprints;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
//import org.apache.log4j.Logger;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import core.account.Account;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.transaction.Transaction;

public class Imprint extends ImprintCls {
	
	private static final int TYPE_ID = ImprintCls.IMPRINT;
	private static final int CUTTED_REFERENCE_LENGTH = 20;

	public Imprint(Account creator, String name, String description)
	{
		super(TYPE_ID, creator, name, description);
		this.reference = Bytes.ensureCapacity(Base58.decode(name), Transaction.REFERENCE_LENGTH, 0);

	}
	public Imprint(byte[] typeBytes, Account creator, String name, String description)
	{
		super(typeBytes, creator, name, description);
		this.reference = Bytes.ensureCapacity(Base58.decode(name), Transaction.REFERENCE_LENGTH, 0);
	}

	//GETTERS/SETTERS
	public String getItemSubType() { return "cutted58"; }

	public static String hashNameToBase58(String name_total)
	{
		byte[] digest = Crypto.getInstance().digest(name_total.getBytes());
		digest = Arrays.copyOfRange(digest, 0, Imprint.CUTTED_REFERENCE_LENGTH);
		return Base58.encode(digest);
	}
	public byte[] getCuttedReference()
	{
		return Arrays.copyOfRange(this.reference, 0, CUTTED_REFERENCE_LENGTH);
	}

	//PARSE
	// includeReference - TRUE only for store in local DB
	public static Imprint parse(byte[] data, boolean includeReference) throws Exception
	{	

		// READ TYPE
		byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
		int position = TYPE_LENGTH;
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		Account creator = new Account(Base58.encode(creatorBytes));
		position += CREATOR_LENGTH;
		
		//READ NAME
		//byte[] nameLengthBytes = Arrays.copyOfRange(data, position, position + NAME_SIZE_LENGTH);
		//int nameLength = Ints.fromByteArray(nameLengthBytes);
		//position += NAME_SIZE_LENGTH;
		int nameLength = Byte.toUnsignedInt(data[position]);
		position ++;
		
		if(nameLength < CUTTED_REFERENCE_LENGTH || nameLength > CUTTED_REFERENCE_LENGTH * 2)
		{
			throw new Exception("Invalid name length");
		}
		
		byte[] nameBytes = Arrays.copyOfRange(data, position, position + nameLength);
		String name = new String(nameBytes, StandardCharsets.UTF_8);
		position += nameLength;
				
		//READ DESCRIPTION
		byte[] descriptionLengthBytes = Arrays.copyOfRange(data, position, position + DESCRIPTION_SIZE_LENGTH);
		int descriptionLength = Ints.fromByteArray(descriptionLengthBytes);
		position += DESCRIPTION_SIZE_LENGTH;
		
		if(descriptionLength < 0 || descriptionLength > 4000)
		{
			throw new Exception("Invalid description length");
		}
		
		byte[] descriptionBytes = Arrays.copyOfRange(data, position, position + descriptionLength);
		String description = new String(descriptionBytes, StandardCharsets.UTF_8);
		position += descriptionLength;
		
		byte[] reference = null;
		if (includeReference)
		{
			//READ REFERENCE
			reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
			position += REFERENCE_LENGTH;
		}
		
		//RETURN
		Imprint imprint = new Imprint(typeBytes, creator, name, description);
		if (includeReference)
		{
			imprint.setReference(reference);
		}

		return imprint;
	}
	
	@Override
	public int getDataLength(boolean includeReference) 
	{
		return BASE_LENGTH
				+ this.name.getBytes().length // it is Base58 - not UTF
				+ this.description.getBytes(StandardCharsets.UTF_8).length
				+ (includeReference? REFERENCE_LENGTH: 0);
	}	

	
}
