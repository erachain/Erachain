package core.block;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import at.AT_Block;
import at.AT_Controller;
import at.AT_Exception;
import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;
import datachain.DCSet;
import datachain.TransactionFinalMap;
import datachain.TransactionFinalMapSigns;
import datachain.TransactionMap;
import ntp.NTP;
import utils.Converter;


public class Block {

    public static final int VERSION_LENGTH = 4;
    //public static final int TIMESTAMP_LENGTH = 8;
    public static final int GENERATING_BALANCE_LENGTH = 4;
    public static final int CREATOR_LENGTH = Crypto.HASH_LENGTH;
    public static final int SIGNATURE_LENGTH = Crypto.SIGNATURE_LENGTH;
    public static final int REFERENCE_LENGTH = SIGNATURE_LENGTH;
    public static final int TRANSACTIONS_HASH_LENGTH = Crypto.HASH_LENGTH;
    public static final int AT_BYTES_LENGTH = 4;
    private static final int TRANSACTIONS_COUNT_LENGTH = 4;
    private static final int TRANSACTION_SIZE_LENGTH = 4;
    private static final int BASE_LENGTH = VERSION_LENGTH + REFERENCE_LENGTH + CREATOR_LENGTH
            //+ GENERATING_BALANCE_LENGTH
            + TRANSACTIONS_HASH_LENGTH + SIGNATURE_LENGTH + TRANSACTIONS_COUNT_LENGTH;
    public static final int MAX_TRANSACTION_BYTES = BlockChain.MAX_BLOCK_BYTES - BASE_LENGTH;
    //private static final int AT_FEES_LENGTH = 8;
    //private static final int AT_LENGTH = AT_FEES_LENGTH + AT_BYTES_LENGTH;
    private static final int AT_LENGTH = 0 + AT_BYTES_LENGTH;
    static Logger LOGGER = Logger.getLogger(Block.class.getName());
    /// HEAD of BLOCK ///
    // FACE
    protected int version;
    protected PublicKeyAccount creator;
    protected byte[] signature;
    protected int transactionCount;
    //protected long timestamp;
    protected byte[] transactionsHash;
    // LINK
    protected byte[] reference;
    protected Tuple3<Tuple5<Integer, byte[], byte[], Integer, byte[]>, byte[], Tuple3<Integer, Long, Long>> parentBlockHead;
    // MIND - that calculated on DB
    protected int heightBlock;
    //protected int creatorPreviousHeightBlock;
    protected int forgingValue;
    protected long winValue;
    /// END of HEAD ///
    protected long target;
    // BODY
    protected List<Transaction> transactions;
    protected byte[] rawTransactions = null;
    //protected Long atFees;
    protected byte[] atBytes;

    public Block(int version, byte[] reference, PublicKeyAccount creator, byte[] transactionsHash, byte[] atBytes) {
        this.version = version;
        this.reference = reference;
        this.creator = creator;

        this.transactionsHash = transactionsHash;

        this.transactionCount = 0;
        this.atBytes = atBytes;

        //this.setGeneratingBalance(dcSet);
        //BlockChain.getTarget();

    }

    // VERSION 2 AND 3 BLOCKS, WITH AT AND MESSAGE
    public Block(int version, byte[] reference, PublicKeyAccount creator, byte[] transactionsHash, byte[] atBytes, byte[] signature) {
        this(version, reference, creator, transactionsHash, atBytes);
        this.signature = signature;
    }


    //GETTERS/SETTERS

    public Block(int version, byte[] reference, PublicKeyAccount generator, Tuple2<List<Transaction>, Integer> transactionsItem,
                 byte[] atBytes) {
        // TODO Auto-generated constructor stub
        this.version = version;
        this.reference = reference;
        this.creator = generator;
        this.transactions = transactionsItem.a;
        this.transactionsHash = makeTransactionsHash(this.creator.getPublicKey(), transactions, this.atBytes);
        this.transactionCount = transactionsItem.b;
        this.atBytes = atBytes;

    }

    public static byte[] makeTransactionsHash(byte[] creator, List<Transaction> transactions, byte[] atBytes) {

        byte[] data = new byte[0];

        if (transactions == null || transactions.isEmpty()) {
            data = Bytes.concat(data, creator);

        } else {

            //MAKE TRANSACTIONS HASH
            for (Transaction transaction : transactions) {
                data = Bytes.concat(data, transaction.getSignature());
                transaction = null;
            }
            transactions = null;

        }

        if (atBytes != null)
            data = Bytes.concat(data, atBytes);


        return Crypto.getInstance().digest(data);

    }

    public static Block parse(byte[] data, boolean forDB) throws Exception {
        if (data.length == 0) {
            return null;
        }

        //CHECK IF WE HAVE MINIMUM BLOCK LENGTH
        if (data.length < BASE_LENGTH) {
            throw new Exception("Data is less then minimum block length");
        }

        int position = 0;

        //READ VERSION
        byte[] versionBytes = Arrays.copyOfRange(data, position, position + VERSION_LENGTH);
        int version = Ints.fromByteArray(versionBytes);
        position += VERSION_LENGTH;

		/*
		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);
		position += TIMESTAMP_LENGTH;
		 */

        //READ REFERENCE
        byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
        position += REFERENCE_LENGTH;

        //READ GENERATOR
        byte[] generatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
        PublicKeyAccount generator = new PublicKeyAccount(generatorBytes);
        position += CREATOR_LENGTH;

        int generatingBalance = 0;
        if (false && forDB) {
            //READ GENERATING BALANCE
            byte[] generatingBalanceBytes = Arrays.copyOfRange(data, position, position + GENERATING_BALANCE_LENGTH);
            generatingBalance = Ints.fromByteArray(generatingBalanceBytes);
            if (generatingBalance < 0) {
                LOGGER.error("block.generatingBalance < 0:" + generatingBalance);
            }
            position += GENERATING_BALANCE_LENGTH;
        }

        //READ TRANSACTION SIGNATURE
        byte[] transactionsHash = Arrays.copyOfRange(data, position, position + TRANSACTIONS_HASH_LENGTH);
        position += TRANSACTIONS_HASH_LENGTH;

        //READ GENERATOR SIGNATURE
        byte[] signature = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
        position += SIGNATURE_LENGTH;

        //CREATE BLOCK
        Block block;
        if (version > 1) {
            //ADD ATs BYTES
            byte[] atBytesCountBytes = Arrays.copyOfRange(data, position, position + AT_BYTES_LENGTH);
            int atBytesCount = Ints.fromByteArray(atBytesCountBytes);
            position += AT_BYTES_LENGTH;

            byte[] atBytes = Arrays.copyOfRange(data, position, position + atBytesCount);
            position += atBytesCount;

            //byte[] atFees = Arrays.copyOfRange( data , position , position + 8 );
            //position += 8;

            //long atFeesL = Longs.fromByteArray(atFees);

            block = new Block(version, reference, generator, transactionsHash, atBytes, signature); //, atFeesL);
        } else {
            // GENESIS BLOCK version = 0
            block = new Block(version, reference, generator, transactionsHash, new byte[0], signature);
        }

        //if (forDB)
        //	block.setGeneratingBalance(generatingBalance);

        //READ TRANSACTIONS COUNT
        byte[] transactionCountBytes = Arrays.copyOfRange(data, position, position + TRANSACTIONS_COUNT_LENGTH);
        int transactionCount = Ints.fromByteArray(transactionCountBytes);
        position += TRANSACTIONS_COUNT_LENGTH;

        //SET TRANSACTIONDATA

        block.setTransactionData(transactionCount, Arrays.copyOfRange(data, position, data.length));


        //SET TRANSACTIONS SIGNATURE
        // transaction only in raw here - block.makeTransactionsHash();

        return block;
    }

    public int getVersion() {
        return version;
    }

    public byte[] getSignature() {
        return this.signature;
    }

    public void setHeight(int height) {
        this.heightBlock = height;
    }

    public void setHeadMind(int height, int forgingValue, long winValue, long previousTarget) {
        this.heightBlock = height;
        this.forgingValue = forgingValue;
        this.winValue = winValue;
        this.target = BlockChain.calcTarget(this.heightBlock, previousTarget, this.winValue);

    }

    public int getHeight(DCSet db) {

        if (this instanceof GenesisBlock
                || Arrays.equals(this.signature,
                Controller.getInstance().getBlockChain().getGenesisBlock().getSignature()))
            return 1;

        if (heightBlock < 1) {
            Integer item = db.getBlockSignsMap().get(this.signature);
            if (item == null) {
                heightBlock = -1;
            } else {
                heightBlock = item;
            }
        }

        return heightBlock;

    }

    public long getTarget() {
        return this.target;
    }

    public void loadParentHead(DCSet db) {
        if (parentBlockHead == null) {
            Integer heightParentBlock = db.getBlockSignsMap().get(this.reference);
            if (heightParentBlock == null) {
                this.heightBlock = -1;
                this.parentBlockHead = null;
            } else {
                this.heightBlock = heightParentBlock + 1;
                this.parentBlockHead = db.getBlocksHeadsMap().get(heightParentBlock);
            }
        }
    }

    public Tuple3<Tuple5<Integer, byte[], byte[], Integer, byte[]>, byte[], Tuple3<Integer, Long, Long>> getParentHead() {
        return this.parentBlockHead;
    }

    public Block getParent(DCSet dcSet) {
        try {
            int parentHeight = dcSet.getBlockSignsMap().get(this.reference);
            //assert (parentHeight, this.heightBlock - 1);
            return dcSet.getBlockMap().get(parentHeight);
        } catch (Exception e) {
            // TODO Auto-generated catch block
           return null;
        }
    }

    public void loadHeadMind(DCSet dcSet) {
        Tuple3<Integer, Long, Long> headMind = dcSet.getBlocksHeadsMap().get(this.heightBlock).c;
        this.forgingValue = headMind.a;
        this.winValue = headMind.b;
        this.target = headMind.c;

    }

	/*
	// NEED CALCULATE BEFORE add in BlockMap
	public void calcHeadMind(DCSet dcSet)
	{
		
		if (this.version == 0) {
			this.heightBlock = 1;
			this.forgingValue = BlockChain.GENESIS_WIN_VALUE;
			this.winValue = BlockChain.GENESIS_WIN_VALUE;
			this.target = BlockChain.GENESIS_WIN_VALUE;

		} else { 
			this.parentBlockHead = this.getParent(dcSet);
			this.heightBlock = this.parentBlockHead.getHeight(dcSet) + 1;
			//Tuple2<Integer, Integer> forgingPoint = this.creator.getForgingData(dcSet, this.heightBlock);
			//this.creatorPreviousHeightBlock = forgingPoint.a;
			this.forgingValue = this.creator.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).intValue();
			this.winValue = calcWinValue(dcSet);
			this.target = BlockChain.calcTarget(heightBlock, parentBlockHead.getTarget(), this.winValue);
		}
	}
	*/

    public Tuple5<Integer, byte[], byte[], Integer, byte[]> getHeadFace() {

        return new Tuple5<Integer, byte[], byte[], Integer, byte[]>(
                this.version, this.creator.getPublicKey(), this.signature, this.transactionCount, this.transactionsHash);
    }

    public Tuple3<Integer, Long, Long> getHeadMind() {
        return new Tuple3<Integer, Long, Long>(
                this.forgingValue, this.winValue, this.target);
    }

    public Block getChild(DCSet db) {
        return db.getBlockMap().get(this.getHeight(db) + 1);
    }

    public int compareWin(Block block) {
        long myWin = this.winValue;
        long itWin = block.winValue;

        if (myWin < itWin) return -1;
        if (myWin > itWin) return 1;

        BigInteger myBI = new BigInteger(this.signature);
        BigInteger itBI = new BigInteger(block.signature);
        return myBI.compareTo(itBI);

    }

    public int getHeightByParent(DCSet db) {

        if (this.version == 0 // ||this instanceof GenesisBlock
                || Arrays.equals(this.signature,
                Controller.getInstance().getBlockChain().getGenesisBlock().getSignature()))
            return 1;


        this.loadParentHead(db);
        return this.heightBlock;

    }

    public long getTimestamp(DCSet db) {

        int height = getHeightByParent(db);

        BlockChain blockChain = Controller.getInstance().getBlockChain();

        return blockChain.getTimestamp(height);
    }

	/*
	private void setGeneratingBalance(int generatingBalance)
	{
		this.forgingValue = generatingBalance;
	}
	 */


	/*
	// CALCULATE and SET
	public void setCalcGeneratingBalance(DCSet dcSet)
	{
		this.forgingValue = this.creator.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).intValue();
	}
	*/

    // balance on creator account when making this block
    public int getForgingValue() {
        return this.forgingValue;
    }

    public long getWinValue() {
        return this.winValue;
    }

    public byte[] getReference() {
        return this.reference;
    }

    public PublicKeyAccount getCreator() {
        return this.creator;
    }

    public BigDecimal getBonusFee() {

        // NOT GIFT for MISSed forger
        long cut1 = this.target << 1;
        // TODO - off START POINT
        if (//this.heightBlock > 140000 &&
                this.winValue >= cut1) {
            return BigDecimal.ZERO;
        }

        int inDay30 = BlockChain.BLOCKS_PER_DAY * 30;

        BigDecimal bonusFee = BlockChain.MIN_FEE_IN_BLOCK;

        if(this.heightBlock < BlockChain.ALL_BALANCES_OK_TO) {
            if (this.heightBlock < inDay30 << 1)
                ;
            else if (this.heightBlock < inDay30 << 2) // 120 days = 4 mounth
                bonusFee = bonusFee.divide(new BigDecimal(2), 8, BigDecimal.ROUND_DOWN).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
            else if (this.heightBlock < inDay30 << 3) // 16 mounth
                bonusFee = bonusFee.divide(new BigDecimal(3), 8, BigDecimal.ROUND_DOWN).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
            else if (this.heightBlock < inDay30 << 4) //  64 mounth
                bonusFee = bonusFee.divide(new BigDecimal(4), 8, BigDecimal.ROUND_DOWN).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
            else
                bonusFee = bonusFee.divide(new BigDecimal(2), 8, BigDecimal.ROUND_DOWN).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
        } else {
            if (this.heightBlock < inDay30 << 1)
                ;
            else if (this.heightBlock < inDay30 << 2) // 120 days = 4 mounth
                bonusFee = bonusFee.divide(new BigDecimal(2), 8, BigDecimal.ROUND_DOWN).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
            else if (this.heightBlock < inDay30 << 3) // 16 mounth
                bonusFee = bonusFee.divide(new BigDecimal(4), 8, BigDecimal.ROUND_DOWN).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
            else if (this.heightBlock < inDay30 << 4) //  64 mounth
                bonusFee = bonusFee.divide(new BigDecimal(8), 8, BigDecimal.ROUND_DOWN).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
            else
                bonusFee = bonusFee.divide(new BigDecimal(16), 8, BigDecimal.ROUND_DOWN).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
        }

        return bonusFee;
    }

    public BigDecimal getTotalFee(DCSet db) {
        BigDecimal fee = this.getFeeByProcess(db);
        return fee.add(getBonusFee());
    }

	/*
	public BigDecimal getATfee()
	{
		return BigDecimal.valueOf(this.atFees, BlockChain.AMOUNT_DEDAULT_SCALE);
	}
	 */

    public BigDecimal getTotalFee() {
        return getTotalFee(DCSet.getInstance());
    }

    public BigDecimal getFeeByProcess(DCSet db) {
        //BigDecimal fee = BigDecimal.ZERO;
        int fee = 0;

        for (Transaction transaction : this.getTransactions()) {
            //fee = fee.add(transaction.getFee());
            fee += transaction.getForgedFee();
        }

        // TODO calculate AT FEE
        // fee = fee.add(BigDecimal.valueOf(this.atFees, BlockChain.AMOUNT_DEDAULT_SCALE));

        return BigDecimal.valueOf(fee, BlockChain.AMOUNT_DEDAULT_SCALE);

    }

    public void setTransactionData(int transactionCount, byte[] rawTransactions) {

        this.transactionCount = transactionCount;
        this.rawTransactions = rawTransactions;
    }

    public int getTransactionCount() {
        return this.transactionCount;
    }

    public synchronized List<Transaction> getTransactions() {
        if (this.transactions == null) {
            //LOAD TRANSACTIONS
            this.transactions = new ArrayList<Transaction>();

            try {
                int position = 0;
                for (int i = 0; i < transactionCount; i++) {
                    //GET TRANSACTION SIZE
                    byte[] transactionLengthBytes = Arrays.copyOfRange(this.rawTransactions, position, position + TRANSACTION_SIZE_LENGTH);
                    int transactionLength = Ints.fromByteArray(transactionLengthBytes);
                    position += TRANSACTION_SIZE_LENGTH;

                    //PARSE TRANSACTION
                    byte[] transactionBytes = Arrays.copyOfRange(this.rawTransactions, position, position + transactionLength);
                    Transaction transaction = TransactionFactory.getInstance().parse(transactionBytes, Transaction.FOR_NETWORK);
                    ///transaction.setBlock(this, i + 1);

                    //ADD TO TRANSACTIONS
                    this.transactions.add(transaction);

                    //ADD TO POSITION
                    position += transactionLength;
                }
            } catch (Exception e) {
                //FAILED TO LOAD TRANSACTIONS
            }
        }

        return this.transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.setTransactions(transactions, transactions.size());
    }

    public void setTransactions(List<Transaction> transactions, int count) {
        this.transactions = transactions;
        this.transactionCount = count;
        //this.atBytes = null;
        if (this.transactionsHash == null)
            this.transactionsHash = makeTransactionsHash(this.creator.getPublicKey(), transactions, null);
    }
	/*
	public int getTransactionIndex(byte[] signature)
	{

		int i = 0;

		for(Transaction transaction: this.getTransactions())
		{
			if(Arrays.equals(transaction.getSignature(), signature))
			{
				return i;
			}
			i++;
		}

		return -1;
	}
	 */

    public void setATBytes(byte[] atBytes) {
        this.atBytes = atBytes;
    }

    public int getTransactionSeq(byte[] signature) {
        int seq = 1;
        for (Transaction transaction : this.getTransactions()) {
            if (Arrays.equals(transaction.getSignature(), signature)) {
                return seq;
            }
            seq++;
        }

        return -1;
    }

    public Transaction getTransaction(byte[] signature) {

        for (Transaction transaction : this.getTransactions()) {
            if (Arrays.equals(transaction.getSignature(), signature)) {
                return transaction;
            }
        }

        return null;
    }

    public Transaction getTransaction(int index) {

        if (index < this.transactionCount)
            return getTransactions().get(index);
        else
            return null;
    }

    public byte[] getBlockATs() {
        return this.atBytes;
    }

    public byte[] getTransactionsHash() {
        return this.transactionsHash;
    }

    //PARSE/CONVERT

    public void makeTransactionsHash() {
        this.transactionsHash = makeTransactionsHash(this.creator.getPublicKey(), this.getTransactions(), this.atBytes);
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJson() {
        JSONObject block = new JSONObject();

        block.put("version", this.version);
        block.put("reference", Base58.encode(this.reference));
        block.put("timestamp", this.getTimestamp(DCSet.getInstance()));
        block.put("generatingBalance", this.forgingValue);
        block.put("winValue", this.getWinValue());
        block.put("target", this.getTarget());
        ///block.put("winValueTargeted", this.calcWinValueTargeted(DCSet.getInstance()));
        block.put("creator", this.creator.getAddress());
        block.put("fee", this.getTotalFee().toPlainString());
        block.put("transactionsHash", Base58.encode(this.transactionsHash));
        block.put("signature", Base58.encode(this.signature));
        block.put("signature", Base58.encode(this.getSignature()));
        block.put("height", this.getHeight(DCSet.getInstance()));

        //CREATE TRANSACTIONS
        JSONArray transactionsArray = new JSONArray();

        for (Transaction transaction : this.getTransactions()) {
            transactionsArray.add(transaction.toJson());
        }

        //ADD TRANSACTIONS TO BLOCK
        block.put("transactions", transactionsArray);

        //ADD AT BYTES
        if (atBytes != null) {
            block.put("blockATs", Converter.toHex(atBytes));
            //block.put("atFees", this.atFees);
        }

        //RETURN
        return block;
    }

    public byte[] toBytes(boolean withSign, boolean forDB) {
        byte[] data = new byte[0];

        //WRITE VERSION
        byte[] versionBytes = Ints.toByteArray(this.version);
        versionBytes = Bytes.ensureCapacity(versionBytes, VERSION_LENGTH, 0);
        data = Bytes.concat(data, versionBytes);

        //WRITE REFERENCE
        byte[] referenceBytes = Bytes.ensureCapacity(this.reference, REFERENCE_LENGTH, 0);
        data = Bytes.concat(data, referenceBytes);

        //WRITE GENERATOR
        byte[] generatorBytes = Bytes.ensureCapacity(this.creator.getPublicKey(), CREATOR_LENGTH, 0);
        data = Bytes.concat(data, generatorBytes);

        if (false && forDB) {
            //WRITE GENERATING BALANCE
            byte[] generatingBalanceBytes = Ints.toByteArray(this.forgingValue);
            generatingBalanceBytes = Bytes.ensureCapacity(generatingBalanceBytes, GENERATING_BALANCE_LENGTH, 0);
            data = Bytes.concat(data, generatingBalanceBytes);
        }

        //WRITE TRANSACTIONS HASH
        data = Bytes.concat(data, this.transactionsHash);

        if (!withSign) {
            // make HEAD data for signature
            return data;
        }

        //WRITE GENERATOR SIGNATURE
        data = Bytes.concat(data, this.signature);

        //ADD ATs BYTES
        if (this.version > 1) {
            if (atBytes != null) {
                byte[] atBytesCount = Ints.toByteArray(atBytes.length);
                data = Bytes.concat(data, atBytesCount);

                data = Bytes.concat(data, atBytes);

                //byte[] atByteFees = Longs.toByteArray(atFees);
                //data = Bytes.concat(data,atByteFees);
            } else {
                byte[] atBytesCount = Ints.toByteArray(0);
                data = Bytes.concat(data, atBytesCount);

                //byte[] atByteFees = Longs.toByteArray(0L);
                //data = Bytes.concat(data,atByteFees);
            }
        }

        //WRITE TRANSACTION COUNT
        byte[] transactionCountBytes = Ints.toByteArray(this.getTransactionCount());
        transactionCountBytes = Bytes.ensureCapacity(transactionCountBytes, TRANSACTIONS_COUNT_LENGTH, 0);
        data = Bytes.concat(data, transactionCountBytes);

        for (Transaction transaction : this.getTransactions()) {
            //WRITE TRANSACTION LENGTH
            int transactionLength = transaction.getDataLength(Transaction.FOR_NETWORK, true);
            byte[] transactionLengthBytes = Ints.toByteArray(transactionLength);
            transactionLengthBytes = Bytes.ensureCapacity(transactionLengthBytes, TRANSACTION_SIZE_LENGTH, 0);
            data = Bytes.concat(data, transactionLengthBytes);

            //WRITE TRANSACTION
            data = Bytes.concat(data, transaction.toBytes(Transaction.FOR_NETWORK, true));
        }

        return data;
    }

    public byte[] toBytesForSign() {
        byte[] data = new byte[0];

        //WRITE VERSION
        byte[] versionBytes = Ints.toByteArray(this.version);
        versionBytes = Bytes.ensureCapacity(versionBytes, VERSION_LENGTH, 0);
        data = Bytes.concat(data, versionBytes);

        //WRITE REFERENCE
        byte[] referenceBytes = Bytes.ensureCapacity(this.reference, REFERENCE_LENGTH, 0);
        data = Bytes.concat(data, referenceBytes);

        data = Bytes.concat(data, this.transactionsHash);

        return data;
    }

    public void sign(PrivateKeyAccount account) {
        byte[] data = toBytesForSign();
        this.signature = Crypto.getInstance().sign(account, data);
    }

    public int getDataLength(boolean forDB) {

        int length = BASE_LENGTH; // + (forDB?GENERATING_BALANCE_LENGTH:0);

        if (this.version > 1) {
            length += AT_LENGTH;
            if (this.atBytes != null) {
                length += atBytes.length;
            }
        }

        for (Transaction transaction : this.getTransactions()) {
            length += TRANSACTION_SIZE_LENGTH + transaction.getDataLength(Transaction.FOR_NETWORK, true);
        }

        return length;
    }

    public byte[] getProofHash() {
        //newSig = sha256(prevSig || pubKey)
        byte[] data = Bytes.concat(this.reference, creator.getPublicKey());

        return Crypto.getInstance().digest(data);
    }

	/*
	public static int getPreviousForgingHeightForIncomes(DBSet dcSet, Account creator, int height) {

		// IF BLOCK in the MAP
		int previousForgingHeight = creator.getForgingData(dcSet, height);
		if (previousForgingHeight == -1) {
			// IF BLOCK not inserted in MAP
			previousForgingHeight = creator.getLastForgingData(dcSet);
			if (previousForgingHeight == -1) {
				// if it is first payment to this account
				return height;
			}
		}

		return previousForgingHeight;

	}
	 */


    //VALIDATE

    public boolean isSignatureValid() {

        if (this.version == 0) {
            // genesis block
            GenesisBlock gb = (GenesisBlock) this;
            return gb.isSignatureValid();
        }
        //VALIDATE BLOCK SIGNATURE
        byte[] data = this.toBytesForSign();

        if (!Crypto.getInstance().verify(this.creator.getPublicKey(), this.signature, data)) {
            LOGGER.error("Block signature not valid"
                    + ", Creator:" + this.creator.getAddress()
                    + ", SIGN: " + Base58.encode(this.signature));
            return false;
        }

        return true;
    }

    // canonical definition of block version release schedule
    public int getNextBlockVersion(DCSet db) {

        return 1;

		/*
		int height = getHeight(db);

		if(height < Transaction.getAT_BLOCK_HEIGHT_RELEASE())
		{
			return 1;
		}
		else if(getTimestamp() < Transaction.getPOWFIX_RELEASE())
		{
			return 2;
		}
		else
		{
			return 3;
		}
		 */
    }

    public long calcWinValue(DCSet dcSet) {

        if (this.version == 0 || this.creator == null) {
            // GENESIS
            this.winValue = BlockChain.GENESIS_WIN_VALUE;
            return this.winValue;
        }

        this.winValue = BlockChain.calcWinValue(dcSet, this.creator, this.heightBlock, this.forgingValue);
        return this.winValue;
    }

    public int calcWinValueTargeted() {

        if (this.version == 0 || this.creator == null) {
            // GENESIS - getBlockChain = null
            return BlockChain.BASE_TARGET;
        }

        return BlockChain.calcWinValueTargeted(this.winValue, this.target);
    }

    public boolean isValid(DCSet dcSet, boolean andProcess) {

        int height = this.getHeightByParent(dcSet);
        Controller cnt = Controller.getInstance();

        // for DEBUG
        if (height == 65431
                || height == 86549) {
            height = this.getHeightByParent(dcSet);
        }

		/*
		// FOR PROBE START !!!
		if(height > 1000)
		{
			LOGGER.error("*** Block[" + this.getHeightByParent(db) + "] is PROBE");
			return false;
		}
		 */

        //CHECK IF PARENT EXISTS
        if (height < 2 || this.reference == null) {
            LOGGER.debug("*** Block[" + height + "].reference invalid");
            return false;
        }

        byte[] lastSignature = dcSet.getBlockMap().getLastBlockSignature();
        if (!Arrays.equals(lastSignature, this.reference)) {
            LOGGER.debug("*** Block[" + height + "].reference from fork");
            return false;
        }

        // TODO - show it to USER
        long blockTime = this.getTimestamp(dcSet);
        long thisTimestamp = NTP.getTime();
        //LOGGER.debug("*** Block[" + height + "] " + new Timestamp(myTime));

        if (blockTime + (BlockChain.WIN_BLOCK_BROADCAST_WAIT_MS >> 2) > thisTimestamp) {
            LOGGER.debug("*** Block[" + height + ":" + Base58.encode(this.signature).substring(0, 10) + "].timestamp invalid >NTP.getTime(): "
                    + " \n " + " diff sec: " + (blockTime - thisTimestamp) / 1000);
            return false;
        }

        //CHECK IF VERSION IS CORRECT
        if (this.version != 1) //this.getParent(dcSet).getNextBlockVersion(dcSet))
        {
            LOGGER.debug("*** Block[" + height + "].version invalid");
            return false;
        }
        if (this.version < 2 && this.atBytes != null && this.atBytes.length > 0) // || this.atFees != 0))
        {
            LOGGER.debug("*** Block[" + height + "].version AT invalid");
            return false;
        }

		/*
		// STOP IF SO RAPIDLY
		int previousForgingHeight = Block.getPreviousForgingHeightForCalcWin(dcSet, this.getCreator(), height);
		if (previousForgingHeight < 1 || !cnt.isTestNet() && isSoRapidly(dcSet, height, this.getCreator(),
				//cnt.getBlockChain().getLastBlocksForTarget(dcSet)
				previousForgingHeight
				) > 0) {
			LOGGER.debug("*** Block[" + height + "] REPEATED WIN invalid");
			return false;
		}
		 */

        // TEST STRONG of win Value
        //int base = BlockChain.getMinTarget(height);
        ///int targetedWinValue = this.calcWinValueTargeted(dcSet);

        this.forgingValue = creator.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).intValue();
        this.heightBlock = height;

        this.winValue = BlockChain.calcWinValue(dcSet, this.creator, this.heightBlock, this.forgingValue);
        if (!cnt.isTestNet() && this.winValue < 1) {
            LOGGER.debug("*** Block[" + height + "] WIN_VALUE not in BASE RULES " + this.winValue);
            return false;
        }

        long currentTarget = this.parentBlockHead.c.c;
        int targetedWinValue = BlockChain.calcWinValueTargetedBase(dcSet, height, this.winValue, currentTarget);
        if (!cnt.isTestNet() && targetedWinValue < 1) {
            //targetedWinValue = this.calcWinValueTargeted(dcSet);
            LOGGER.debug("*** Block[" + height + "] targeted WIN_VALUE < MINIMAL TARGET " + targetedWinValue + " < " + currentTarget);
            return false;
        }
        this.target = BlockChain.calcTarget(this.heightBlock, currentTarget, this.winValue);

        if (this.atBytes != null && this.atBytes.length > 0) {
            try {

                AT_Block atBlock = AT_Controller.validateATs(this.getBlockATs(), dcSet.getBlockMap().last().getHeight(dcSet) + 1, dcSet);
                //this.atFees = atBlock.getTotalFees();
            } catch (NoSuchAlgorithmException | AT_Exception e) {
                LOGGER.error(e.getMessage(), e);
                return false;
            }
        }

        long timerStart = System.currentTimeMillis();

        if (andProcess) {
            //ADD TO DB
            //LOGGER.debug("getBlockMap() [" + dcSet.getBlockMap().size() + "]");
            dcSet.getBlockMap().add(this);
            this.heightBlock = dcSet.getBlockSignsMap().getHeight(this.signature);
            LOGGER.debug("getBlockMap().set timer: " + (System.currentTimeMillis() - timerStart) + " [" + this.heightBlock + "]");


        }
        //CHECK TRANSACTIONS

        if (this.transactions == null || this.transactionCount == 0) {
            // empty transactions
        } else {
            int seq = 1;
            byte[] blockSignature = this.getSignature();
            byte[] transactionSignature;
            this.getTransactions();
            boolean isPrimarySet = !dcSet.isFork();

            long timerProcess = 0;
            long timerRefsMap_set = 0;
            long timerUnconfirmedMap_delete = 0;
            long timerFinalMap_set = 0;
            long timerTransFinalMapSinds_set = 0;

            byte[] transactionsSignatures = new byte[0];

            long timestampEnd = this.getTimestamp(dcSet) + BlockChain.GENERATING_MIN_BLOCK_TIME_MS;
            // because time filter used by parent block timestamp on core.BlockGenerator.run()
            //long timestampBeg = this.getParent(dcSet).getTimestamp(dcSet);

            DCSet validatingDC;

            if (andProcess) {
                validatingDC = dcSet;
            } else {
                validatingDC = dcSet.fork();
            }

            //DBSet dbSet = Controller.getInstance().getDBSet();
            TransactionMap unconfirmedMap = validatingDC.getTransactionMap();
            TransactionFinalMap finalMap = validatingDC.getTransactionFinalMap();
            TransactionFinalMapSigns transFinalMapSinds = validatingDC.getTransactionFinalMapSigns();

            int seqNo = 0;
            for (Transaction transaction : this.transactions) {
                if (cnt.isOnStopping())
                    return false;

                seqNo++;

                if (!transaction.isWiped()) {

                    //CHECK IF NOT GENESIS TRANSACTION
                    if (transaction.getCreator() == null) {
                        // ALL GENESIS transaction
                        LOGGER.debug("*** Block[" + height
                                + "].Tx[" + seqNo + " : " ///this.getTransactionSeq(transaction.getSignature()) + " : "
                                + transaction.viewFullTypeName() + "]"
                                + "creator is Null!"
                        );
                        return false;
                    }

                    if (!transaction.isSignatureValid(validatingDC)) {
                        //
                        LOGGER.debug("*** Block[" + height
                                + "].Tx[" + this.getTransactionSeq(transaction.getSignature()) + " : "
                                + transaction.viewFullTypeName() + "]"
                                + "signature not valid!"
                                + " " + Base58.encode(transaction.getSignature()));
                        return false;
                    }

                    transaction.setBlock(this, validatingDC, Transaction.FOR_NETWORK, height, seqNo);

                    //CHECK IF VALID
                    if (transaction.isValid(Transaction.FOR_NETWORK, 0l) != Transaction.VALIDATE_OK) {
                        LOGGER.debug("*** Block[" + height
                                + "].Tx[" + this.getTransactionSeq(transaction.getSignature()) + " : "
                                + transaction.viewFullTypeName() + "]"
                                + "invalid code: " + transaction.isValid(Transaction.FOR_NETWORK, 0l)
                                + " " + Base58.encode(transaction.getSignature()));
                        return false;
                    }

                    //CHECK TIMESTAMP AND DEADLINE
                    long transactionTimestamp = transaction.getTimestamp() - (BlockChain.GENERATING_MIN_BLOCK_TIME_MS - BlockChain.WIN_BLOCK_BROADCAST_WAIT_MS);
                    if (transactionTimestamp > timestampEnd
                            //|| transaction.getDeadline() <= timestampBeg
                            && height > 105999
                            ) {
                        LOGGER.debug("*** Block[" + height + "].TX.timestamp invalid "
                                + Base58.encode(transaction.getSignature()));
                        return false;
                    }

                    timerStart = System.currentTimeMillis();
                    try {
                        transaction.process(this, Transaction.FOR_NETWORK);
                    } catch (Exception e) {
                        if (cnt.isOnStopping())
                            return false;

                        LOGGER.error("*** Block[" + height + "].TX.process ERROR", e);
                        return false;
                    }
                    timerProcess += System.currentTimeMillis() - timerStart;

                } else {

                    transaction.setBlock(this, validatingDC, Transaction.FOR_NETWORK, height, seqNo);

                    //UPDATE REFERENCE OF SENDER
                    if (transaction.isReferenced())
                        // IT IS REFERENCED RECORD?
                        transaction.getCreator().setLastTimestamp(transaction.getTimestamp(), validatingDC);
                }

                transactionSignature = transaction.getSignature();

                if (andProcess) {

                    //SET PARENT
                    ///LOGGER.debug("[" + seq + "] try refsMap.set" );
                    if (isPrimarySet) {
                        //REMOVE FROM UNCONFIRMED DATABASE
                        ///LOGGER.debug("[" + seq + "] try unconfirmedMap delete" );
                        timerStart = System.currentTimeMillis();
                        unconfirmedMap.delete(transactionSignature);
                        timerUnconfirmedMap_delete += System.currentTimeMillis() - timerStart;
                    }

                    Tuple2<Integer, Integer> key = new Tuple2<Integer, Integer>(this.heightBlock, seq);

                    if (cnt.isOnStopping())
                        return false;

                    ///LOGGER.debug("[" + seq + "] try finalMap.set" );
                    timerStart = System.currentTimeMillis();
                    finalMap.set(key, transaction);
                    timerFinalMap_set += System.currentTimeMillis() - timerStart;
                    //LOGGER.debug("[" + seq + "] try transFinalMapSinds.set" );
                    timerStart = System.currentTimeMillis();
                    transFinalMapSinds.set(transactionSignature, key);
                    List<byte[]> signatures = transaction.getSignatures();
                    if (signatures != null) {
                        for (byte[] itemSignature : signatures) {
                            transFinalMapSinds.set(itemSignature, key);
                        }
                    }
                    timerTransFinalMapSinds_set += System.currentTimeMillis() - timerStart;

                } else {
                    // for some TRANSACTIONs need add to FINAM MAP etc.
                    // R_SertifyPubKeys - in same BLOCK with IssuePersonRecord

                    Tuple2<Integer, Integer> key = new Tuple2<Integer, Integer>(this.heightBlock, seq);

                    finalMap.set(key, transaction);
                    transFinalMapSinds.set(transactionSignature, key);
                    List<byte[]> signatures = transaction.getSignatures();
                    if (signatures != null) {
                        for (byte[] itemSignature : signatures) {
                            transFinalMapSinds.set(itemSignature, key);
                        }
                    }
                }

                seq++;

                transactionsSignatures = Bytes.concat(transactionsSignatures, transactionSignature);
            }

            if (validatingDC.isFork()) {
                validatingDC.close();
            }

            transactionsSignatures = Crypto.getInstance().digest(transactionsSignatures);
            if (!Arrays.equals(this.transactionsHash, transactionsSignatures)) {
                LOGGER.debug("*** Block[" + height + "].digest(transactionsSignatures) invalid");
                return false;
            }
            
            // for DEBUG
            if (!validatingDC.isFork() && height == 86549) {
                return false;
            }

        }

        //BLOCK IS VALID
        if (andProcess) {
            try {
                this.process_after(cnt, dcSet);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return false;
            }
        }

        return true;
    }

    //PROCESS/ORPHAN
    public void feeProcess(DCSet dcSet, boolean asOrphan) {
        //REMOVE FEE
        BigDecimal blockTotalFee = this.getTotalFee(dcSet);
        BigDecimal bonusFee = this.getBonusFee();
        BigDecimal emittedFee;
        
        if (BlockChain.ROBINHOOD_USE) {
            // find rich account
            String rich = Account.getRichWithForks(dcSet, Transaction.FEE_KEY);

            if (!rich.equals(this.creator.getAddress())) {
                emittedFee = bonusFee.divide(new BigDecimal(2));
                
                Account richAccount = new Account(rich);
                //richAccount.setBalance(Transaction.FEE_KEY, richAccount.getBalance(dcSet, Transaction.FEE_KEY).add(bonus_fee), dcSet);
                richAccount.changeBalance(dcSet, !asOrphan, Transaction.FEE_KEY, emittedFee, true);
            } else {
                emittedFee = BigDecimal.ZERO;
            }
            
        } else {
            emittedFee = bonusFee;
        }

        // SUBSTRACT from EMISSION (with minus)
        GenesisBlock.CREATOR.changeBalance(dcSet, !asOrphan, Transaction.FEE_KEY, emittedFee, true);

        //LOGGER.debug("<<< core.block.Block.orphan(DBSet) #3");

        //UPDATE GENERATOR BALANCE WITH FEE
        this.creator.changeBalance(dcSet, asOrphan, Transaction.FEE_KEY, blockTotalFee, true);

    }
    
    // TODO - make it trownable
    public void process_after(Controller cnt, DCSet dcSet) throws Exception {

        //PROCESS FEE
        feeProcess(dcSet, false);
        
		/*
		if (!dcSet.isFork()) {
			int lastHeight = dcSet.getBlockMap().getLastBlock().getHeight(dcSet);
			LOGGER.error("*** core.block.Block.process(DBSet)[" + (this.getParentHeight(dcSet) + 1)
					+ "] SET new last Height: " + lastHeight
					+ " getHeightMap().getHeight: " + this.height_process);
		}
		 */

        if (heightBlock % BlockChain.MAX_ORPHAN == 0) {
            cnt.blockchainSyncStatusUpdate(heightBlock);
        }

    }

    // TODO - make it trownable
    public void process(DCSet dcSet) throws Exception {

        Controller cnt = Controller.getInstance();
        if (cnt.isOnStopping())
            throw new Exception("on stoping");

        long start = System.currentTimeMillis();

        //ADD TO DB
        long timerStart = System.currentTimeMillis();

        if (dcSet.getBlockMap().add(this))
            throw new Exception("block already exist!!");

        LOGGER.debug("getBlockMap().set timer: " + (System.currentTimeMillis() - timerStart));

        this.heightBlock = dcSet.getBlockSignsMap().getHeight(this.signature);
        
        // for DEBUG
        if (this.heightBlock == 65431
                || this.heightBlock == 86549) {
            this.heightBlock = dcSet.getBlockSignsMap().getHeight(this.signature);
        }

        //PROCESS TRANSACTIONS
        int seq = 1;
        byte[] blockSignature = this.getSignature();
        byte[] transactionSignature;
        this.getTransactions();
        //DBSet dbSet = Controller.getInstance().getDBSet();
        TransactionMap unconfirmedMap = dcSet.getTransactionMap();
        TransactionFinalMap finalMap = dcSet.getTransactionFinalMap();
        TransactionFinalMapSigns transFinalMapSinds = dcSet.getTransactionFinalMapSigns();

        long timerProcess = 0;
        long timerRefsMap_set = 0;
        long timerUnconfirmedMap_delete = 0;
        long timerFinalMap_set = 0;
        long timerTransFinalMapSinds_set = 0;

        int seqNo = 0;
        for (Transaction transaction : this.transactions) {

            if (cnt.isOnStopping())
                throw new Exception("on stoping");

            //LOGGER.debug("[" + seq + "] record is process" );

            // NEED set DC for WIPED too
            transaction.setBlock(this, dcSet, Transaction.FOR_NETWORK, this.heightBlock, ++seqNo);

            //PROCESS
            if (!transaction.isWiped()) {
                timerStart = System.currentTimeMillis();
                transaction.process(this, Transaction.FOR_NETWORK);
                timerProcess += System.currentTimeMillis() - timerStart;
            } else {
                //UPDATE REFERENCE OF SENDER
                if (transaction.isReferenced())
                    // IT IS REFERENCED RECORD?
                    transaction.getCreator().setLastTimestamp(transaction.getTimestamp(), dcSet);
            }

            transactionSignature = transaction.getSignature();

            //SET PARENT
            ///LOGGER.debug("[" + seq + "] try refsMap.set" );

            //REMOVE FROM UNCONFIRMED DATABASE
            ///LOGGER.debug("[" + seq + "] try unconfirmedMap delete" );
            timerStart = System.currentTimeMillis();
            unconfirmedMap.delete(transactionSignature);
            timerUnconfirmedMap_delete += System.currentTimeMillis() - timerStart;

            Tuple2<Integer, Integer> key = new Tuple2<Integer, Integer>(this.heightBlock, seq);

            if (cnt.isOnStopping())
                throw new Exception("on stoping");

            ///LOGGER.debug("[" + seq + "] try finalMap.set" );
            timerStart = System.currentTimeMillis();
            finalMap.set(key, transaction);
            timerFinalMap_set += System.currentTimeMillis() - timerStart;
            //LOGGER.debug("[" + seq + "] try transFinalMapSinds.set" );
            timerStart = System.currentTimeMillis();
            transFinalMapSinds.set(transactionSignature, key);
            List<byte[]> signatures = transaction.getSignatures();
            if (signatures != null) {
                for (byte[] itemSignature : signatures) {
                    transFinalMapSinds.set(itemSignature, key);
                }
            }
            timerTransFinalMapSinds_set += System.currentTimeMillis() - timerStart;

            seq++;

        }

        LOGGER.debug("timerProcess: " + timerProcess + "  timerRefsMap_set: " + timerRefsMap_set
                + "  timerUnconfirmedMap_delete: " + timerUnconfirmedMap_delete + "  timerFinalMap_set:" + timerFinalMap_set
                + "  timerTransFinalMapSinds_set: " + timerTransFinalMapSinds_set);

        this.process_after(cnt, dcSet);

        long tickets = System.currentTimeMillis() - start;
        LOGGER.debug("[" + this.heightBlock + "] processing time: " + tickets * 0.001
                + " for records:" + this.getTransactionCount() + " millsec/record:" + tickets / (this.getTransactionCount() + 1));

    }

    public void orphan(DCSet dcSet) throws Exception {

        Controller cnt = Controller.getInstance();
        if (cnt.isOnStopping())
            throw new Exception("on stoping");

        //LOGGER.debug("<<< core.block.Block.orphan(DBSet) #0");
        int height = this.getHeight(dcSet);
        if (height == 1) {
            // GENESIS BLOCK cannot be orphanED
            return;
        }

        long start = System.currentTimeMillis();

        //ORPHAN TRANSACTIONS
        //LOGGER.debug("<<< core.block.Block.orphan(DBSet) #2 ORPHAN TRANSACTIONS");
        this.orphanTransactions(dcSet, height);

        //LOGGER.debug("<<< core.block.Block.orphan(DBSet) #2f FEE");

        //REMOVE FEE
        feeProcess(dcSet, true);

        //DELETE BLOCK FROM DB
        dcSet.getBlockMap().remove(this.signature, this.reference);

        //LOGGER.debug("<<< core.block.Block.orphan(DBSet) #4");

        long tickets = System.currentTimeMillis() - start;
        LOGGER.debug("[" + this.heightBlock + "] orphaning time: " + (System.currentTimeMillis() - start) * 0.001
                + " for records:" + this.getTransactionCount() + " millsec/record:" + tickets / (this.getTransactionCount() + 1));

        this.heightBlock = -1;
        //this.parentBlock = null;
        byte[] lastSignature = dcSet.getBlockMap().getLastBlockSignature();
        if (!Arrays.equals(lastSignature, this.reference)) {
            LOGGER.debug("[" + this.heightBlock + "] orphaning time: " + (System.currentTimeMillis() - start) * 0.001
                    + "  ERROR ");

        }


    }

    private void orphanTransactions(DCSet dcSet, int height) throws Exception {

        Controller cnt = Controller.getInstance();
        //DBSet dbSet = Controller.getInstance().getDBSet();

        TransactionMap unconfirmedMap = dcSet.getTransactionMap();
        TransactionFinalMap finalMap = dcSet.getTransactionFinalMap();
        TransactionFinalMapSigns transFinalMapSinds = dcSet.getTransactionFinalMapSigns();

        this.getTransactions();
        //ORPHAN ALL TRANSACTIONS IN DB BACK TO FRONT
        int seqNo = 0;
        for (int i = this.transactionCount - 1; i >= 0; i--) {
            if (cnt.isOnStopping())
                throw new Exception("on stoping");

            Transaction transaction = transactions.get(i);
            //LOGGER.debug("<<< core.block.Block.orphanTransactions\n" + transaction.toJson());

            transaction.setBlock(this, dcSet, Transaction.FOR_NETWORK, this.heightBlock, ++seqNo);

            if (!transaction.isWiped()) {
                transaction.orphan(Transaction.FOR_NETWORK);
            } else {
                // IT IS REFERENCED RECORD?
                if (transaction.isReferenced()) {
                    //UPDATE REFERENCE OF SENDER
                    transaction.getCreator().removeLastTimestamp(dcSet);
                }
            }

            //ADD ORPHANED TRANASCTIONS BACK TO DATABASE
            unconfirmedMap.add(transaction);

            Tuple2<Integer, Integer> key = new Tuple2<Integer, Integer>(height, i);
            finalMap.delete(key);
            transFinalMapSinds.delete(transaction.getSignature());
            List<byte[]> signatures = transaction.getSignatures();
            if (signatures != null) {
                for (byte[] itemSignature : signatures) {
                    transFinalMapSinds.delete(itemSignature);
                }
            }

        }
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject instanceof Block) {
            Block otherBlock = (Block) otherObject;

            return Arrays.equals(this.getSignature(), otherBlock.getSignature());
        }

        return false;
    }

    public String toString(DCSet dcSet) {
        
        return this.forgingValue != 0? " GB: " + this.forgingValue : "" //this.calcWinValueTargeted()
                + " recs: " + this.transactionCount
                + " H: " + this.getHeightByParent(dcSet)
                + " C: " + this.getCreator().getPersonAsString();
    }

}
