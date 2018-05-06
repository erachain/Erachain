package core.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.BlockChain;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.crypto.Base58;

/*

## typeBytes
0 - record type
1 - record version
2 - property 1
3 = property 2

## version 0
typeBytes[2] = -128 if NO AMOUNT
typeBytes[3] = -128 if NO DATA

## version 1
typeBytes[1] (version) = 1 - if backward - CONFISCATE CREDIT

## version 2
typeBytes[1] - version

#### PROPERTY 1
typeBytes[2].0 = -128 if NO AMOUNT
typeBytes[2].1 = -64 if backward - CONFISCATE CREDIT

#### PROPERTY 2
typeBytes[3].0 = -128 if NO DATA

## version 3

#### PROPERTY 1
typeBytes[2].0 = -128 if NO AMOUNT - check sign
typeBytes[2].1 = 64 if backward (CONFISCATE CREDIT, ...)

#### PROPERTY 2
typeBytes[3].0 = -128 if NO DATA - check sign = '10000000' = Integer.toBinaryString(128) - assertEquals((byte)128, (byte)-128);
typeBytes[3].3-7 = point accuracy: -16..16 = BYTE - 16

 */

public class R_Send extends TransactionAmount {

	private static final byte TYPE_ID = (byte) Transaction.SEND_ASSET_TRANSACTION;
	private static final String NAME_ID = "Send";
	//private static int position;

	public static final int NO_DATA_MASK = 128; // 0x10000000

	protected String head;
	protected byte[] data;
	protected byte[] encrypted;
	protected byte[] isText;

	protected static final int BASE_LENGTH = IS_TEXT_LENGTH + ENCRYPTED_LENGTH + DATA_SIZE_LENGTH;
	public static final int MAX_DATA_VIEW = 64;

	public R_Send(byte[] typeBytes, PublicKeyAccount creator, byte feePow, Account recipient, long key,
			BigDecimal amount, String head, byte[] data, byte[] isText, byte[] encrypted, long timestamp,
			Long reference) {
		super(typeBytes, NAME_ID, creator, feePow, recipient, amount, key, timestamp, reference);

		this.head = head;
		if (head == null)
			this.head = "";

		if (data == null || data.length == 0) {
			// set version byte
			typeBytes[3] = (byte) (typeBytes[3] | NO_DATA_MASK);
		} else {
			this.data = data;
			this.encrypted = encrypted;
			this.isText = isText;
		}
	}

	public R_Send(byte[] typeBytes, PublicKeyAccount creator, byte feePow, Account recipient, long key,
			BigDecimal amount, String head, byte[] data, byte[] isText, byte[] encrypted, long timestamp,
			Long reference, byte[] signature) {
		this(typeBytes, creator, feePow, recipient, key, amount, head, data, isText, encrypted, timestamp, reference);
		this.signature = signature;
		// this.calcFee();
	}

	// as pack
	public R_Send(byte[] typeBytes, PublicKeyAccount creator, Account recipient, long key, BigDecimal amount,
			String head, byte[] data, byte[] isText, byte[] encrypted, Long reference, byte[] signature) {
		this(typeBytes, creator, (byte) 0, recipient, key, amount, head, data, isText, encrypted, 0l, reference);
		this.signature = signature;
	}

	// FOR BACKWARDS - CONFISCATE CREDIT
	public R_Send(byte version, byte property1, byte property2, PublicKeyAccount creator, byte feePow,
			Account recipient, long key, BigDecimal amount, String head, byte[] data, byte[] isText, byte[] encrypted,
			long timestamp, Long reference) {
		this(new byte[] { TYPE_ID, version, property1, property2 }, creator, feePow, recipient, key, amount, head, data,
				isText, encrypted, timestamp, reference);
	}

	public R_Send(PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, String head,
			byte[] data, byte[] isText, byte[] encrypted, long timestamp, Long reference) {
		this(new byte[] { TYPE_ID, 0, 0, 0 }, creator, feePow, recipient, key, amount, head, data, isText, encrypted,
				timestamp, reference);
	}

	public R_Send(PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, String head,
			byte[] data, byte[] isText, byte[] encrypted, long timestamp, Long reference, byte[] signature) {
		this(new byte[] { TYPE_ID, 0, 0, 0 }, creator, feePow, recipient, key, amount, head, data, isText, encrypted,
				timestamp, reference, signature);
	}

	// as pack
	public R_Send(PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, String head, byte[] data,
			byte[] isText, byte[] encrypted, Long reference) {
		this(new byte[] { TYPE_ID, 0, 0, 0 }, creator, (byte) 0, recipient, key, amount, head, data, isText, encrypted,
				0l, reference);
	}

	////////////////////////// SHOR -text DATA
	public R_Send(byte[] typeBytes, PublicKeyAccount creator, byte feePow, Account recipient, long key,
			BigDecimal amount, long timestamp, Long reference) {
		super(typeBytes, NAME_ID, creator, feePow, recipient, amount, key, timestamp, reference);
		// typeBytes[3] = (byte)(typeBytes[3] & (byte)-128);
		this.head = "";
		typeBytes[3] = (byte) (typeBytes[3] | (byte) -128);

	}

	public R_Send(byte[] typeBytes, PublicKeyAccount creator, byte feePow, Account recipient, long key,
			BigDecimal amount, long timestamp, Long reference, byte[] signature) {
		this(typeBytes, creator, feePow, recipient, key, amount, timestamp, reference);
		this.signature = signature;
		// this.calcFee();
	}

	// as pack
	public R_Send(byte[] typeBytes, PublicKeyAccount creator, Account recipient, long key, BigDecimal amount) {
		this(typeBytes, creator, (byte) 0, recipient, key, amount, 0l, null);
	}

	public R_Send(PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, long timestamp,
			Long reference) {
		this(new byte[] { TYPE_ID, 0, 0, 0 }, creator, feePow, recipient, key, amount, timestamp, reference);
	}

	public R_Send(PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, long timestamp,
			Long reference, byte[] signature) {
		this(new byte[] { TYPE_ID, 0, 0, 0 }, creator, feePow, recipient, key, amount, timestamp, reference,
				signature);
	}

	// as pack
	public R_Send(PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, Long reference) {
		this(new byte[] { TYPE_ID, 0, 0, 0 }, creator, (byte) 0, recipient, key, amount, 0l, reference);
	}

	// GETTERS/SETTERS

	// public static String getName() { return "Send"; }

	public String getHead() {
		return this.head;
	}

	public byte[] getData() {
		return this.data;
	}

	public String viewData() {

		if (data == null)
			return "";
		if (this.isText()) {

			if (this.isEncrypted()) {
				return "encrypted";
			} else {
				if (this.data.length > MAX_DATA_VIEW << 4) {
					return new String(data, Charset.forName("UTF-8")); // "{{" +
					// new
					// String(Arrays.copyOfRange(data,
					// 0,
					// MAX_DATA_VIEW),
					// Charset.forName("UTF-8"))
					// +
					// "...}}";
				}
				return new String(this.data, Charset.forName("UTF-8"));
			}
		} else {
			if (this.data.length > MAX_DATA_VIEW) {
				return "{{" + Base58.encode(Arrays.copyOfRange(data, 0, MAX_DATA_VIEW)) + "...}}";
			}
			return "{{" + Base58.encode(data) + "}}";
		}
	}

	public byte[] getEncrypted() {

		byte[] enc = new byte[1];
		enc[0] = (isEncrypted()) ? (byte) 1 : (byte) 0;
		return enc;
	}

	public boolean isText() {
		if (data == null || data.length == 0)
			return false;
		return (Arrays.equals(this.isText, new byte[1])) ? false : true;
	}

	public boolean isEncrypted() {
		if (data == null || data.length == 0)
			return false;
		return (Arrays.equals(this.encrypted, new byte[1])) ? false : true;
	}

	@Override
	public boolean hasPublicText() {
		if (head.length() > 2)
			return true;

		if (data == null || data.length == 0)
			return false;
		if (!Arrays.equals(this.encrypted, new byte[1]))
			return false;

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() {
		// GET BASE
		JSONObject transaction = this.getJsonBase();

		if (head.length() > 0)
			transaction.put("head", this.head);

		if (data != null && data.length > 0) {

			// ADD CREATOR/SERVICE/DATA
			if (this.isText() && !this.isEncrypted()) {
				transaction.put("data", new String(this.data, Charset.forName("UTF-8")));
			} else {
				transaction.put("data", Base58.encode(this.data));
			}
			transaction.put("encrypted", this.isEncrypted());
			transaction.put("isText", this.isText());
		}

		return transaction;
	}

	// PARSE/CONVERT

	public static Transaction Parse(byte[] data, Long releaserReference) throws Exception {

		boolean asPack = releaserReference != null;

		//
		// CHECK IF WE MATCH BLOCK LENGTH
		if (data.length < BASE_LENGTH_AS_PACK | !asPack & data.length < BASE_LENGTH) {
			throw new Exception("Data does not match block length " + data.length);
		}

		// READ TYPE
		byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
		int position = TYPE_LENGTH;

		long timestamp = 0;
		if (!asPack) {
			// READ TIMESTAMP
			byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
			timestamp = Longs.fromByteArray(timestampBytes);
			position += TIMESTAMP_LENGTH;
		}

		Long reference = null;
		if (!asPack) {
			// READ REFERENCE
			byte[] referenceBytes = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
			reference = Longs.fromByteArray(referenceBytes);
			position += REFERENCE_LENGTH;
		} else {
			reference = releaserReference;
		}

		// READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;

		byte feePow = 0;
		if (!asPack) {
			// READ FEE POWER
			byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
			feePow = feePowBytes[0];
			position += 1;
		}

		// READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		position += SIGNATURE_LENGTH;

		// READ RECIPIENT
		byte[] recipientBytes = Arrays.copyOfRange(data, position, position + RECIPIENT_LENGTH);
		Account recipient = new Account(Base58.encode(recipientBytes));
		position += RECIPIENT_LENGTH;

		long key = 0;
		BigDecimal amount = null;
		if (typeBytes[2] >= 0) {
			// IF here is AMOUNT

			// READ KEY
			byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
			key = Longs.fromByteArray(keyBytes);
			position += KEY_LENGTH;

			// READ AMOUNT
			byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
			amount = new BigDecimal(new BigInteger(amountBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
			position += AMOUNT_LENGTH;

			// CHECK ACCURACY of AMOUNT
			if (typeBytes[3] != -1) {
				// not use old FLAG from vers 2 
				int accuracy = typeBytes[3] & SCALE_MASK;
				if (accuracy > 0) {
					if (accuracy > TransactionAmount.SCALE_MASK_HALF + 1) {
						accuracy -= TransactionAmount.SCALE_MASK + 1;
					}
	
					// RESCALE AMOUNT
					amount = amount.scaleByPowerOfTen(-accuracy);
				}
			}

		}

		// HEAD LEN
		int headLen = Byte.toUnsignedInt(data[position]);
		position++;
		// HEAD
		byte[] headBytes = Arrays.copyOfRange(data, position, position + headLen);
		String head = new String(headBytes, StandardCharsets.UTF_8);
		position += headLen;

		// DATA +++
		byte[] arbitraryData = null;
		byte[] encryptedByte = null;
		byte[] isTextByte = null;
		if (typeBytes[3] >= 0) {
			// IF here is DATA

			// READ DATA SIZE
			byte[] dataSizeBytes = Arrays.copyOfRange(data, position, position + DATA_SIZE_LENGTH);
			int dataSize = Ints.fromByteArray(dataSizeBytes);
			position += DATA_SIZE_LENGTH;

			// READ DATA
			arbitraryData = Arrays.copyOfRange(data, position, position + dataSize);
			position += dataSize;

			encryptedByte = Arrays.copyOfRange(data, position, position + ENCRYPTED_LENGTH);
			position += ENCRYPTED_LENGTH;

			isTextByte = Arrays.copyOfRange(data, position, position + IS_TEXT_LENGTH);
			position += IS_TEXT_LENGTH;
		}

		if (!asPack) {
			return new R_Send(typeBytes, creator, feePow, recipient, key, amount, head, arbitraryData, isTextByte,
					encryptedByte, timestamp, reference, signatureBytes);
		} else {
			return new R_Send(typeBytes, creator, recipient, key, amount, head, arbitraryData, isTextByte,
					encryptedByte, reference, signatureBytes);
		}

	}

	@Override
	public byte[] toBytes(boolean withSign, Long releaserReference) {

		byte[] data = super.toBytes(withSign, releaserReference);

		// WRITE HEAD
		byte[] headBytes = this.head.getBytes(StandardCharsets.UTF_8);
		// HEAD SIZE
		data = Bytes.concat(data, new byte[] { (byte) headBytes.length });
		// HEAD
		data = Bytes.concat(data, headBytes);

		if (this.data != null) {
			// WRITE DATA SIZE
			byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
			data = Bytes.concat(data, dataSizeBytes);

			// WRITE DATA
			data = Bytes.concat(data, this.data);

			// WRITE ENCRYPTED
			data = Bytes.concat(data, this.encrypted);

			// WRITE ISTEXT
			data = Bytes.concat(data, this.isText);
		}

		return data;
	}

	@Override
	public int getDataLength(boolean asPack) {

		int dataLen = super.getDataLength(asPack) + 1 + head.getBytes(StandardCharsets.UTF_8).length;

		if (this.typeBytes[3] >= 0)
			return dataLen + BASE_LENGTH + this.data.length;
		else
			return dataLen;
	}

	// @Override
	@Override
	public int isValid(Long releaserReference, long flags) {

		if (head.getBytes(StandardCharsets.UTF_8).length > 256)
			return INVALID_HEAD_LENGTH;

		if (this.data != null) {
			// CHECK DATA SIZE
			if (data.length > Integer.MAX_VALUE) {
				return INVALID_DATA_LENGTH;
			}
		}

		return super.isValid(releaserReference, flags);
	}

}