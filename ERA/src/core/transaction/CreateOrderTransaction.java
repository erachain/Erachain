package core.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.crypto.Base58;
import core.item.assets.AssetCls;
import core.item.assets.Order;
import datachain.DCSet;

public class CreateOrderTransaction extends Transaction {
	private static final byte TYPE_ID = (byte) Transaction.CREATE_ORDER_TRANSACTION;
	private static final String NAME_ID = "Create Order";
	private static final int AMOUNT_LENGTH = TransactionAmount.AMOUNT_LENGTH;
	private static final int HAVE_LENGTH = 8;
	private static final int WANT_LENGTH = 8;
	// private static final int PRICE_LENGTH = 12;
	private static final int BASE_LENGTH = Transaction.BASE_LENGTH + HAVE_LENGTH + WANT_LENGTH + 2 * AMOUNT_LENGTH;

	private Order order;

	public static final byte[][] VALID_REC = new byte[][] {
		Base58.decode("5XMmLXACUPu74absaKQwVSnzf91ppvYcMK8mBqQ18dALQxvVrB46atw2bfv4xXXq7ZXrM1iELKyW5jMiLgf8uHKf"),
		Base58.decode(
				"4fWbpHBsEzyG9paXH5oJswn3YMhvxw6fRssk6qZmB7jxQ72sRXJunEQhi9bnTwg2cUjwGCZy54u4ZseLRM7xh2x6")
	};

	public CreateOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, long have, long want,
			BigDecimal amountHave, BigDecimal amountWant, byte feePow, long timestamp, Long reference) {
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);
		this.order = new Order(null, creator, have, want, amountHave, amountWant, timestamp);

	}

	public CreateOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, long have, long want,
			BigDecimal amountHave, BigDecimal amountWant, byte feePow, long timestamp, Long reference,
			byte[] signature) {
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);
		this.signature = signature;
		this.order = new Order(new BigInteger(signature), creator, have, want, amountHave, amountWant, timestamp);
		// this.calcFee();

	}

	public CreateOrderTransaction(PublicKeyAccount creator, long have, long want, BigDecimal amountHave,
			BigDecimal amountWant, byte feePow, long timestamp, Long reference, byte[] signature) {
		this(new byte[] { TYPE_ID, 0, 0, 0 }, creator, have, want, amountHave, amountWant, feePow, timestamp, reference,
				signature);
	}

	public CreateOrderTransaction(PublicKeyAccount creator, long have, long want, BigDecimal amountHave,
			BigDecimal amountWant, byte feePow, long timestamp, Long reference) {
		this(new byte[] { TYPE_ID, 0, 0, 0 }, creator, have, want, amountHave, amountWant, feePow, timestamp,
				reference);
	}

	// GETTERS/SETTERS
	// public static String getName() { return "Create Order"; }

	/*
	 * public void makeOrder() { if (this.order == null) this.order = new
	 * Order(new BigInteger(this.signature), this.creator, this.have, this.want,
	 * this.amount, this.price, this.timestamp); }
	 */

	@Override
	public void setDC(DCSet dcSet, boolean asPack) {
		super.setDC(dcSet, asPack);

		this.order.setDC(dcSet);

	}

	@Override
	public BigDecimal getAmount() {
		return this.order.getAmountHave();
	}

	@Override
	public long getKey() {
		return this.order.getHave();
	}

	public Order getOrder() {
		return this.order;
	}

	@Override
	public boolean hasPublicText() {
		return false;
	}

	// @Override
	@Override
	public void sign(PrivateKeyAccount creator, boolean asPack) {
		super.sign(creator, asPack);
		// in IMPRINT reference already setted before sign
		this.order.setId(this.signature);
	}

	// PARSE CONVERT

	public static Transaction Parse(byte[] data, Long releaserReference) throws Exception {
		boolean asPack = releaserReference != null;

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

		// READ HAVE
		byte[] haveBytes = Arrays.copyOfRange(data, position, position + HAVE_LENGTH);
		long have = Longs.fromByteArray(haveBytes);
		position += HAVE_LENGTH;

		// READ WANT
		byte[] wantBytes = Arrays.copyOfRange(data, position, position + WANT_LENGTH);
		long want = Longs.fromByteArray(wantBytes);
		position += WANT_LENGTH;

		// READ AMOUNT HAVE
		byte[] amountHaveBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amountHave = new BigDecimal(new BigInteger(amountHaveBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
		position += AMOUNT_LENGTH;

		// READ AMOUNT WANT
		byte[] amountWantBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amountWant = new BigDecimal(new BigInteger(amountWantBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
		position += AMOUNT_LENGTH;

		return new CreateOrderTransaction(typeBytes, creator, have, want, amountHave, amountWant, feePow, timestamp,
				reference, signatureBytes);
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() {
		// GET BASE
		JSONObject transaction = this.getJsonBase();

		// ADD CREATOR/ORDER
		transaction.put("creator", this.creator.getAddress());

		JSONObject order = new JSONObject();
		order.put("have", this.order.getHave());
		order.put("want", this.order.getWant());
		order.put("amountHave", this.order.getAmountHave().toPlainString());
		order.put("amountWant", this.order.getAmountWant().toPlainString());
		order.put("price", this.order.getPriceCalc().toPlainString());

		transaction.put("order", order);

		return transaction;
	}

	// @Override
	@Override
	public byte[] toBytes(boolean withSign, Long releaserReference) {
		byte[] data = super.toBytes(withSign, releaserReference);

		// WRITE HAVE
		byte[] haveBytes = Longs.toByteArray(this.order.getHave());
		haveBytes = Bytes.ensureCapacity(haveBytes, HAVE_LENGTH, 0);
		data = Bytes.concat(data, haveBytes);

		// WRITE WANT
		byte[] wantBytes = Longs.toByteArray(this.order.getWant());
		wantBytes = Bytes.ensureCapacity(wantBytes, WANT_LENGTH, 0);
		data = Bytes.concat(data, wantBytes);

		// WRITE AMOUNT HAVE
		byte[] amountHaveBytes = this.order.getAmountHave().unscaledValue().toByteArray();
		byte[] fill_H = new byte[AMOUNT_LENGTH - amountHaveBytes.length];
		amountHaveBytes = Bytes.concat(fill_H, amountHaveBytes);
		data = Bytes.concat(data, amountHaveBytes);

		// WRITE AMOUNT WANT
		byte[] amountWantBytes = this.order.getAmountWant().unscaledValue().toByteArray();
		byte[] fill_W = new byte[AMOUNT_LENGTH - amountWantBytes.length];
		amountWantBytes = Bytes.concat(fill_W, amountWantBytes);
		data = Bytes.concat(data, amountWantBytes);

		return data;
	}

	@Override
	public int getDataLength(boolean asPack) {
		return BASE_LENGTH;
	}

	// VALIDATE

	@Override
	public int isValid(DCSet db, Long releaserReference) {

		for (byte[] valid_item : VALID_REC) {
			if (Arrays.equals(this.signature, valid_item)) {
				return VALIDATE_OK;
			}
		}

		int height = this.getBlockHeightByParentOrLast(db);

		// CHECK IF ASSETS NOT THE SAME
		long have = this.order.getHave();
		long want = this.order.getWant();

		if (have == RIGHTS_KEY && !BlockChain.DEVELOP_USE
				// && want != FEE_KEY
				) {
			// have ERA
			if (height > BlockChain.FREEZE_FROM
					&& BlockChain.FOUNDATION_ADDRESSES.contains(this.creator.getAddress())) {
				// LOCK ERA sell
				return INVALID_CREATOR;
			}
		}

		if (have == want) {
			return HAVE_EQUALS_WANT;
		}

		// CHECK IF AMOUNT POSITIVE
		BigDecimal amountHave = this.order.getAmountHave();
		BigDecimal amountWant = this.order.getAmountWant();
		if (amountHave.compareTo(BigDecimal.ZERO) <= 0 || amountWant.compareTo(BigDecimal.ZERO) <= 0) {
			return NEGATIVE_AMOUNT;
		}

		// CHECK IF WANT EXISTS
		AssetCls haveAsset = this.order.getHaveAsset(db);
		if (haveAsset == null) {
			// WANT DOES NOT EXIST
			return ITEM_ASSET_NOT_EXIST;
		}

		// CHECK IF SENDER HAS ENOUGH ASSET BALANCE
		if (FEE_KEY == have) {
			if (this.creator.getBalance(db, FEE_KEY).a.b.compareTo(amountHave.add(this.fee)) == -1) {
				return NO_BALANCE;
			}
		} else {

			// CHECK IF SENDER HAS ENOUGH FEE BALANCE
			if (this.creator.getBalance(db, FEE_KEY).a.b.compareTo(this.fee) == -1) {
				return NOT_ENOUGH_FEE;
			}

			// if asset is unlimited and me is creator of this asset
			boolean unLimited = haveAsset.getQuantity().equals(0l)
					&& haveAsset.getOwner().getAddress().equals(this.creator.getAddress());

			if (!unLimited) {

				BigDecimal forSale = this.creator.getForSale(db, have, height);

				if (forSale.compareTo(amountHave) < 0) {
					return NO_BALANCE;
				}
			}

			if (height > BlockChain.FREEZE_FROM && BlockChain.LOCKED__ADDRESSES.get(this.creator.getAddress()) != null)
				return INVALID_CREATOR;

		}

		// CHECK IF HAVE IS NOT DIVISBLE
		if (!haveAsset.isDivisible()) {
			// CHECK IF AMOUNT DOES NOT HAVE ANY DECIMALS
			if (amountHave.stripTrailingZeros().scale() > 0) {
				// AMOUNT HAS DECIMALS
				return INVALID_AMOUNT;
			}
		}

		// CHECK IF WANT EXISTS
		AssetCls wantAsset = this.order.getWantAsset(db);
		if (wantAsset == null) {
			// WANT DOES NOT EXIST
			return ITEM_ASSET_NOT_EXIST;
		}

		//
		Long maxWant = wantAsset.getQuantity();
		if (maxWant > 0 && new BigDecimal(maxWant).compareTo(amountWant) < 0)
			return INVALID_QUANTITY;

		// CHECK IF WANT IS NOT DIVISIBLE
		if (!wantAsset.isDivisible()) {
			// CHECK IF AMOUNT DOES NOT HAVE ANY DECIMALS
			if (amountWant.stripTrailingZeros().scale() > 0) {
				// AMOUNT HAS DECIMALS
				return INVALID_RETURN;
			}
		}

		// for PARSE and toBYTES need only AMOUNT_LENGTH bytes
		// and SCALE
		byte[] amountBytes = amountHave.unscaledValue().toByteArray();
		if (amountBytes.length > AMOUNT_LENGTH) {
			return AMOUNT_LENGHT_SO_LONG;
		}
		if (amountHave.scale() != haveAsset.getScale()) {
			return AMOUNT_SCALE_WRONG;
		}
		amountBytes = amountWant.unscaledValue().toByteArray();
		if (amountBytes.length > AMOUNT_LENGTH) {
			return AMOUNT_LENGHT_SO_LONG;
		}
		if (amountWant.scale() != wantAsset.getScale()) {
			return AMOUNT_SCALE_WRONG;
		}

		return super.isValid(db, releaserReference);
	}

	// PROCESS/ORPHAN

	// @Override
	@Override
	public void process(Block block, boolean asPack) {
		// UPDATE CREATOR
		super.process(block, asPack);

		// PROCESS ORDER
		// NEED COPY for check block.isValid() and not modify ORDER for
		// block.process
		Order orderThis = this.order.copy();
		orderThis.setDC(this.dcSet);
		orderThis.process(this);
	}

	// @Override
	@Override
	public void orphan(boolean asPack) {
		// UPDATE CREATOR
		super.orphan(asPack);

		// ORPHAN ORDER
		Order orderThis = this.order.copy();
		orderThis.setDC(this.dcSet);
		orderThis.orphan();
	}

	@Override
	public HashSet<Account> getInvolvedAccounts() {
		HashSet<Account> accounts = new HashSet<>();
		accounts.add(this.creator);
		return accounts;
	}

	@Override
	public HashSet<Account> getRecipientAccounts() {
		return new HashSet<>();
	}

	@Override
	public boolean isInvolved(Account account) {
		String address = account.getAddress();

		if (address.equals(this.creator.getAddress())) {
			return true;
		}

		return false;
	}

	@Override
	public BigDecimal getAmount(Account account) {
		if (account.getAddress().equals(this.creator.getAddress())) {
			return this.order.getAmountHave();
		}

		return BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
	}

	public Map<String, Map<Long, BigDecimal>> getAssetAmount() {
		Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();

		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.fee);
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), this.order.getHave(),
				this.order.getAmountHave());

		return assetAmount;
	}

	@Override
	public int calcBaseFee() {
		return 5 * calcCommonFee();
	}
}
