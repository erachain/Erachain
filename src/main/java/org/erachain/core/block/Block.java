package org.erachain.core.block;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.at.AT_Block;
import org.erachain.at.AT_Controller;
import org.erachain.at.AT_Exception;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.R_Calculated;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionFactory;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMap;
import org.erachain.datachain.TransactionFinalMapSigns;
import org.erachain.datachain.TransactionMap;
import org.erachain.ntp.NTP;
import org.erachain.utils.Converter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * обработка блоков - все что с ними связано. Без базы данных - сухие данные в вакууме
 */
public class Block {

    static private HashMap totalCOMPUtest = new HashMap();

    public static final int VERSION_LENGTH = 4;
    public static final int TIMESTAMP_LENGTH = 8;
    public static final int GENERATING_BALANCE_LENGTH = 4;
    public static final int CREATOR_LENGTH = Crypto.HASH_LENGTH;
    public static final int SIGNATURE_LENGTH = Crypto.SIGNATURE_LENGTH;
    public static final int REFERENCE_LENGTH = SIGNATURE_LENGTH;
    public static final int TRANSACTIONS_HASH_LENGTH = Crypto.HASH_LENGTH;
    public static final int AT_BYTES_LENGTH = 4;
    public static final int TRANSACTIONS_COUNT_LENGTH = 4;
    public static final int TRANSACTION_SIZE_LENGTH = 4;
    public static final int HEIGHT_LENGTH = 4;
    public static final int WIN_VALUE_LENGTH = 8;
    public static final int FEE_LENGTH = 8;

    public static final int BASE_LENGTH = VERSION_LENGTH + REFERENCE_LENGTH + CREATOR_LENGTH
            //+ GENERATING_BALANCE_LENGTH
            + TRANSACTIONS_HASH_LENGTH + SIGNATURE_LENGTH + TRANSACTIONS_COUNT_LENGTH;
    public static final int MAX_TRANSACTION_BYTES = BlockChain.MAX_BLOCK_BYTES - BASE_LENGTH;
    //private static final int AT_FEES_LENGTH = 8;
    //private static final int AT_LENGTH = AT_FEES_LENGTH + AT_BYTES_LENGTH;
    private static final int AT_LENGTH = 0 + AT_BYTES_LENGTH;
    static Logger LOGGER = LoggerFactory.getLogger(Block.class.getName());
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
    protected BlockHead parentBlockHead;
    // MIND - that calculated on DB
    public final int heightBlock;
    //protected int creatorPreviousHeightBlock;
    protected int forgingValue;
    protected long winValue;
    /// END of HEAD ///
    protected long target;
    protected long totalFee;
    protected long emittedFee;
    public Block.BlockHead blockHead;
    public List<R_Calculated> txCalculated;

    // BODY
    protected List<Transaction> transactions;
    protected byte[] rawTransactions = null;
    //protected Long atFees;
    protected byte[] atBytes;

    /////////////////////////////////////// BLOCK HEAD //////////////////////////////
    public static class BlockHead {

        public static final int BASE_LENGTH = VERSION_LENGTH + REFERENCE_LENGTH + CREATOR_LENGTH
                + TRANSACTIONS_COUNT_LENGTH + TRANSACTIONS_HASH_LENGTH + SIGNATURE_LENGTH
                + HEIGHT_LENGTH + GENERATING_BALANCE_LENGTH + WIN_VALUE_LENGTH + WIN_VALUE_LENGTH
                + FEE_LENGTH + FEE_LENGTH;

        public final int version;
        public final byte[] reference;
        public final PublicKeyAccount creator;
        public final int transactionsCount;
        public final byte[] transactionsHash;
        public final byte[] signature;

        // MIND - that calculated on DB
        public final int heightBlock;
        public final int forgingValue;
        public final long winValue;
        public final long target;
        public final long totalFee;
        public final long emittedFee;

        public BlockHead(int version, byte[] reference, PublicKeyAccount creator, int transactionCount,
                         byte[] transactionsHash, byte[] signature,
                         int heightBlock, int forgingValue, long winValue, long target,
                         long totalFee, long emittedFee) {
            this.version = version;
            this.creator = creator;
            this.signature = signature;
            this.transactionsCount = transactionCount;
            this.transactionsHash = transactionsHash;
            this.reference = reference;

            this.heightBlock = heightBlock;
            this.forgingValue = forgingValue;
            this.winValue = winValue;
            this.target = target;
            this.totalFee = totalFee;
            this.emittedFee = emittedFee;
        }

        public BlockHead(Block block, int heightBlock, int forgingValue, long winValue, long target,
                         long totalFee, long emittedFee) {
            this.version = block.version;
            this.reference = block.reference;
            this.creator = block.creator;
            this.transactionsCount = block.transactionCount;
            this.transactionsHash = block.transactionsHash;
            this.signature = block.signature;

            this.heightBlock = heightBlock;
            this.forgingValue = forgingValue;
            this.winValue = winValue;
            this.target = target;
            this.totalFee = totalFee;
            this.emittedFee = emittedFee;
        }

        public BlockHead(Block block, long totalFee, long emittedFee) {
            this.version = block.version;
            this.reference = block.reference;
            this.creator = block.creator;
            this.transactionsCount = block.transactionCount;
            this.transactionsHash = block.transactionsHash;
            this.signature = block.signature;

            this.heightBlock = block.heightBlock;
            this.forgingValue = block.forgingValue;
            this.winValue = block.winValue;
            this.target = block.target;
            this.totalFee = totalFee;
            this.emittedFee = emittedFee;
        }

        public BlockHead(Block block) {
            this.version = block.version;
            this.reference = block.reference;
            this.creator = block.creator;
            this.transactionsCount = block.transactionCount;
            this.transactionsHash = block.transactionsHash;
            this.signature = block.signature;

            this.heightBlock = block.heightBlock;
            this.forgingValue = block.forgingValue;
            this.winValue = block.winValue;
            this.target = block.target;
            this.totalFee = block.totalFee;
            this.emittedFee = block.emittedFee;
        }

        public long getTimestamp() {
            BlockChain blockChain = Controller.getInstance().getBlockChain();
            return blockChain.getTimestamp(this.heightBlock);
        }

        public byte[] toBytes() {
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

            //WRITE TRANSACTION COUNT
            byte[] transactionCountBytes = Ints.toByteArray(this.transactionsCount);
            transactionCountBytes = Bytes.ensureCapacity(transactionCountBytes, TRANSACTIONS_COUNT_LENGTH, 0);
            data = Bytes.concat(data, transactionCountBytes);

            //WRITE TRANSACTIONS HASH
            data = Bytes.concat(data, this.transactionsHash);

            //WRITE SIGNATURE
            data = Bytes.concat(data, this.signature);

            //WRITE HEIGHT
            byte[] heightBytes = Ints.toByteArray(this.heightBlock);
            heightBytes = Bytes.ensureCapacity(heightBytes, HEIGHT_LENGTH, 0);
            data = Bytes.concat(data, heightBytes);

            //WRITE GENERATING BALANCE
            byte[] generatingBalanceBytes = Ints.toByteArray(this.forgingValue);
            generatingBalanceBytes = Bytes.ensureCapacity(generatingBalanceBytes, GENERATING_BALANCE_LENGTH, 0);
            data = Bytes.concat(data, generatingBalanceBytes);

            //WRITE WIN VALUE
            byte[] winValueBytes = Longs.toByteArray(this.winValue);
            winValueBytes = Bytes.ensureCapacity(winValueBytes, WIN_VALUE_LENGTH, 0);
            data = Bytes.concat(data, winValueBytes);

            //WRITE TARGET
            byte[] targetBytes = Longs.toByteArray(this.target);
            targetBytes = Bytes.ensureCapacity(targetBytes, WIN_VALUE_LENGTH, 0);
            data = Bytes.concat(data, targetBytes);

            //WRITE TOTAL FEE
            byte[] totalFeeBytes = Longs.toByteArray(this.totalFee);
            totalFeeBytes = Bytes.ensureCapacity(totalFeeBytes, FEE_LENGTH, 0);
            data = Bytes.concat(data, totalFeeBytes);

            //WRITE EMITTED FEE
            byte[] emittedFeeBytes = Longs.toByteArray(this.emittedFee);
            emittedFeeBytes = Bytes.ensureCapacity(emittedFeeBytes, FEE_LENGTH, 0);
            data = Bytes.concat(data, emittedFeeBytes);

            return data;
        }

        public static BlockHead parse(byte[] data) throws Exception {

            if (data.length == 0) {
                return null;
            }

            //CHECK IF WE HAVE MINIMUM BLOCK LENGTH
            if (data.length < BlockHead.BASE_LENGTH) {
                throw new Exception("Data is less then minimum blockHead length");
            }

            int position = 0;

            //READ VERSION
            byte[] versionBytes = Arrays.copyOfRange(data, position, position + VERSION_LENGTH);
            int version = Ints.fromByteArray(versionBytes);
            position += VERSION_LENGTH;

            //READ REFERENCE
            byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
            position += REFERENCE_LENGTH;

            //READ GENERATOR
            byte[] generatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
            PublicKeyAccount creator = new PublicKeyAccount(generatorBytes);
            position += CREATOR_LENGTH;

            //READ TRANSACTIONS COUNT
            byte[] transactionCountBytes = Arrays.copyOfRange(data, position, position + TRANSACTIONS_COUNT_LENGTH);
            int transactionCount = Ints.fromByteArray(transactionCountBytes);
            position += TRANSACTIONS_COUNT_LENGTH;

            //READ TRANSACTION HASH
            byte[] transactionsHash = Arrays.copyOfRange(data, position, position + TRANSACTIONS_HASH_LENGTH);
            position += TRANSACTIONS_HASH_LENGTH;

            //READ SIGNATURE
            byte[] signature = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
            position += SIGNATURE_LENGTH;

            //////////////////////
            //READ HEIGHT
            byte[] heightBytes = Arrays.copyOfRange(data, position, position + HEIGHT_LENGTH);
            int height = Ints.fromByteArray(heightBytes);
            position += HEIGHT_LENGTH;

            //READ GENERATING BALANCE
            byte[] generatingBalanceBytes = Arrays.copyOfRange(data, position, position + GENERATING_BALANCE_LENGTH);
            int forgingValue = Ints.fromByteArray(generatingBalanceBytes);
            position += GENERATING_BALANCE_LENGTH;

            //READ WIN VALUE
            byte[] winValueBytes = Arrays.copyOfRange(data, position, position + WIN_VALUE_LENGTH);
            long winValue = Longs.fromByteArray(winValueBytes);
            position += WIN_VALUE_LENGTH;

            //READ TARGET
            byte[] targetBytes = Arrays.copyOfRange(data, position, position + WIN_VALUE_LENGTH);
            long target = Longs.fromByteArray(targetBytes);
            position += WIN_VALUE_LENGTH;

            //READ TOTAL FEE
            byte[] totalFeeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
            long totalFee = Longs.fromByteArray(totalFeeBytes);
            position += FEE_LENGTH;

            //READ EMITTED FEE
            byte[] emittedFeeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
            long emittedFee = Longs.fromByteArray(emittedFeeBytes);
            position += FEE_LENGTH;

            return new BlockHead(version, reference, creator, transactionCount, transactionsHash, signature,
                    height, forgingValue, winValue, target, totalFee, emittedFee);

        }

        @SuppressWarnings("unchecked")
        public JSONObject toJson() {
            JSONObject head = new JSONObject();

            DCSet dcSet = DCSet.getInstance();
            head.put("version", this.version);
            head.put("reference", Base58.encode(this.reference));
            head.put("timestamp", this.getTimestamp());
            head.put("forgingValue", this.forgingValue);
            head.put("winValue", this.winValue);
            head.put("target", this.target);
            head.put("creator", this.creator.getAddress());
            head.put("fee", this.totalFee);
            head.put("emittedFee", this.emittedFee);
            head.put("transactionsCount", this.transactionsCount);
            head.put("transactionsHash", Base58.encode(this.transactionsHash));
            head.put("signature", Base58.encode(this.signature));
            head.put("height", this.heightBlock);

            return head;
        }


    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    public Block(int version, byte[] reference, PublicKeyAccount creator, int heightBlock,
                 int transactionCount, byte[] transactionsHash, byte[] atBytes,
                 int forgingValue, long winValue, long target) {
        this.version = version;
        this.reference = reference;
        this.creator = creator;
        this.heightBlock = heightBlock;

        this.transactionCount = transactionCount;
        this.transactionsHash = transactionsHash;

        this.atBytes = atBytes;

        this.forgingValue = forgingValue;
        this.winValue = winValue;
        this.target = target;

    }

    public Block(int version, byte[] reference, PublicKeyAccount creator) {
        this.version = version;
        this.reference = reference;
        this.creator = creator;
        this.heightBlock = 1;

        this.forgingValue = BlockChain.GENERAL_ERA_BALANCE;
        this.winValue = BlockChain.GENERAL_ERA_BALANCE;
        this.target = BlockChain.GENERAL_ERA_BALANCE;

    }


    // VERSION 2 AND 3 BLOCKS, WITH AT AND MESSAGE
    public Block(int version, byte[] reference, PublicKeyAccount creator, int heightBlock,
                 byte[] transactionsHash, byte[] atBytes, byte[] signature) {
        this.version = version;
        this.reference = reference;
        this.creator = creator;
        this.heightBlock = heightBlock;

        this.transactionCount = transactionCount;
        this.transactionsHash = transactionsHash;

        this.signature = signature;

    }

    public Block(int version, byte[] reference, PublicKeyAccount generator, int heightBlock,
                 Tuple2<List<Transaction>, Integer> transactionsItem,
                 byte[] atBytes,
                 int forgingValue, long winValue, long target) {
        // TODO Auto-generated constructor stub
        this.version = version;
        this.reference = reference;
        this.creator = generator;
        this.heightBlock = heightBlock;

        this.transactions = transactionsItem.a;
        this.transactionsHash = makeTransactionsHash(this.creator.getPublicKey(), transactions, this.atBytes);
        this.transactionCount = transactionsItem.b;
        this.atBytes = atBytes;

        this.forgingValue = forgingValue;
        this.winValue = winValue;
        this.target = target;

    }

    //GETTERS/SETTERS


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

    public static Block parse(byte[] data, int useHeight) throws Exception {
        if (data.length == 0) {
            return null;
        }

        //CHECK IF WE HAVE MINIMUM BLOCK LENGTH
        if (data.length < (useHeight <= 0? BASE_LENGTH + HEIGHT_LENGTH: BASE_LENGTH)
        ) {
            throw new Exception("Data is less then minimum block length - " + data.length + " useHeight:" + useHeight);
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

        int height;
        if (useHeight > 0) {
            height = useHeight;
        } else {
            //READ HEIGHT
            byte[] heightBytes = Arrays.copyOfRange(data, position, position + HEIGHT_LENGTH);
            height = Ints.fromByteArray(heightBytes);
            position += HEIGHT_LENGTH;
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

            block = new Block(version, reference, generator, height, transactionsHash, atBytes, signature); //, atFeesL);
        } else {
            // GENESIS BLOCK version = 0
            block = new Block(version, reference, generator, height, transactionsHash, new byte[0], signature);
        }

        //if (forDB)
        //	block.setGeneratingBalance(generatingBalance);

        //READ TRANSACTIONS COUNT
        byte[] transactionCountBytes = Arrays.copyOfRange(data, position, position + TRANSACTIONS_COUNT_LENGTH);
        int transactionCount = Ints.fromByteArray(transactionCountBytes);
        if (transactionCount <0 || transactionCount > 20000) {
            throw new Exception("Block parse - transactionCount error for useHeight[" + useHeight + "] with height:" + height);
        }
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

    public int getHeight() {
        return this.heightBlock;
    }

    /*
    public void setHeight(int height) {
        this.heightBlock = height;
    }
    */

    /*
    public void setHeadMind(int height, int forgingValue, long winValue, long previousTarget) {
        this.heightBlock = height;
        this.forgingValue = forgingValue;
        this.winValue = winValue;
        this.target = BlockChain.calcTarget(this.heightBlock, previousTarget, this.winValue);

    }
    */

    /*
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
    */

    public long getTarget() {
        return this.target;
    }

    public Block.BlockHead getParentHead() {
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
        this.blockHead = dcSet.getBlocksHeadsMap().get(this.heightBlock);
        this.forgingValue = blockHead.forgingValue;
        this.winValue = blockHead.winValue;
        this.target = blockHead.target;
        this.totalFee = blockHead.totalFee;
        this.emittedFee = blockHead.emittedFee;
        //this.transactionCount = blockHead.transactionsCount;
        //this.version = blockHead.version;
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

    public Block getChild(DCSet db) {
        return db.getBlockMap().get(this.getHeight() + 1);
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

    /*
    public int getHeightByParent(DCSet db) {

        if (this.version == 0 // ||this instanceof GenesisBlock
                || Arrays.equals(this.signature,
                Controller.getInstance().getBlockChain().getGenesisBlock().getSignature()))
            return 1;


        this.loadParentHead(db);
        return this.heightBlock;

    }
    */


    public static long getTimestamp(int height) {
        BlockChain blockChain = Controller.getInstance().getBlockChain();
        return blockChain.getTimestamp(height);
    }

    public long getTimestamp() {

        //int height = getHeight();

        BlockChain blockChain = Controller.getInstance().getBlockChain();

        return blockChain.getTimestamp(this.heightBlock);
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

        if (this.heightBlock == 1) {
            return BigDecimal.ZERO;
        }

        int inDay30 = BlockChain.BLOCKS_PER_DAY * 30;

        BigDecimal bonusFee; // = BlockChain.MIN_FEE_IN_BLOCK;

        if(true || this.heightBlock < BlockChain.VERS_4_12) {
            bonusFee = BlockChain.MIN_FEE_IN_BLOCK_4_10;
            if (this.heightBlock < inDay30 << 1)
                return BigDecimal.valueOf(55000, BlockChain.FEE_SCALE); // need SCALE for .unscaled()
            else if (this.heightBlock < inDay30 << 2) // 120 days = 4 mounth
                return BigDecimal.valueOf(50000, BlockChain.FEE_SCALE); // need SCALE for .unscaled()
            else if (this.heightBlock < inDay30 << 3) // 16 mounth - 72000
                return BigDecimal.valueOf(45000, BlockChain.FEE_SCALE); // need SCALE for .unscaled()
            else if (false && this.heightBlock < inDay30 << 4) //  64 mounth
                return BigDecimal.valueOf(40000, BlockChain.FEE_SCALE); // need SCALE for .unscaled()
            else if (false && this.heightBlock < inDay30 << 6) //  256 mounth
                return BigDecimal.valueOf(35000, BlockChain.FEE_SCALE); // need SCALE for .unscaled()
            else
                return BigDecimal.valueOf(30000, BlockChain.FEE_SCALE); // need SCALE for .unscaled()
        } else {
            bonusFee = BlockChain.MIN_FEE_IN_BLOCK;
            if (this.heightBlock < inDay30 << 1)
                ;
            else if (this.heightBlock < inDay30 << 2) // 120 days = 4 mounth
                bonusFee = bonusFee.divide(new BigDecimal(2), 8, BigDecimal.ROUND_DOWN).setScale(BlockChain.FEE_SCALE);
            else if (this.heightBlock < inDay30 << 3) // 16 mounth
                bonusFee = bonusFee.divide(new BigDecimal(4), 8, BigDecimal.ROUND_DOWN).setScale(BlockChain.FEE_SCALE);
            else
                bonusFee = bonusFee.divide(new BigDecimal(8), 8, BigDecimal.ROUND_DOWN).setScale(BlockChain.FEE_SCALE);
        }

        return bonusFee;
    }

    public BigDecimal getTotalFee(DCSet db) {
        BigDecimal fee = this.getFeeByProcess(db);
        return fee.add(this.getBonusFee());
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

            int position = 0;
            for (int i = 0; i < transactionCount; i++) {
                //GET TRANSACTION SIZE
                try {
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
                } catch (Exception e) {
                    //FAILED TO LOAD TRANSACTIONS
                    //throw new Exception(
                    LOGGER.error("block getTransactions error: " + e.getMessage(), e);
                    Long error = null;
                    ++error;

                }
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

        DCSet dcSet = DCSet.getInstance();
        block.put("version", this.version);
        block.put("reference", Base58.encode(this.reference));
        block.put("timestamp", this.getTimestamp());
        block.put("generatingBalance", this.forgingValue);
        block.put("winValue", this.getWinValue());
        block.put("target", this.getTarget());
        ///block.put("winValueTargeted", this.calcWinValueTargeted(DCSet.getInstance()));
        block.put("creator", this.creator.getAddress());
        block.put("fee", this.getTotalFee().toPlainString());
        block.put("transactionsHash", Base58.encode(this.transactionsHash));
        block.put("signature", Base58.encode(this.signature));
        block.put("height", this.getHeight());

        //CREATE TRANSACTIONS
        JSONArray transactionsArray = new JSONArray();

        for (Transaction transaction : this.getTransactions()) {
            transaction.setDC(dcSet);
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

        if (forDB) {
            //WRITE HEIGHT
            byte[] heightBytes = Ints.toByteArray(this.heightBlock);
            heightBytes = Bytes.ensureCapacity(heightBytes, HEIGHT_LENGTH, 0);
            data = Bytes.concat(data, heightBytes);
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

    public int getDataLength(boolean withHeight) {

        int length = BASE_LENGTH;
        if (withHeight)
            length += HEIGHT_LENGTH;

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

        LOGGER.debug("*** Block[" + this.heightBlock + "] try Validate");

        Controller cnt = Controller.getInstance();

        // for DEBUG
        /*
        if (this.heightBlock == 60624) {
            int rrr = 0;
        }
        */

        //CHECK IF PARENT EXISTS
        if (this.heightBlock < 2 || this.reference == null) {
            LOGGER.debug("*** Block[" + this.heightBlock + "].reference invalid");
            return false;
        }
        ///this.heightBlock = height;

        byte[] lastSignature = dcSet.getBlockMap().getLastBlockSignature();
        if (!Arrays.equals(lastSignature, this.reference)) {
            LOGGER.debug("*** Block[" + this.heightBlock + "].reference from fork");
            return false;
        }

        // TODO - show it to USER
        long blockTime = this.getTimestamp();
        long thisTimestamp = NTP.getTime();
        //LOGGER.debug("*** Block[" + height + "] " + new Timestamp(myTime));

        if (blockTime + (BlockChain.WIN_BLOCK_BROADCAST_WAIT_MS >> 2) > thisTimestamp) {
            LOGGER.debug("*** Block[" + this.heightBlock + ":" + Base58.encode(this.signature).substring(0, 10) + "].timestamp invalid >NTP.getTime(): "
                    + " \n " + " diff sec: " + (blockTime - thisTimestamp) / 1000);
            return false;
        }

        //CHECK IF VERSION IS CORRECT
        if (this.version != 1) //this.getParent(dcSet).getNextBlockVersion(dcSet))
        {
            LOGGER.debug("*** Block[" + this.heightBlock + "].version invalid");
            return false;
        }
        if (this.version < 2 && this.atBytes != null && this.atBytes.length > 0) // || this.atFees != 0))
        {
            LOGGER.debug("*** Block[" + this.heightBlock + "].version AT invalid");
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

        this.winValue = BlockChain.calcWinValue(dcSet, this.creator, this.heightBlock, this.forgingValue);
        if (!cnt.isTestNet() && this.winValue < 1) {
            LOGGER.debug("*** Block[" + this.heightBlock + "] WIN_VALUE not in BASE RULES " + this.winValue);
            return false;
        }

        this.parentBlockHead = dcSet.getBlocksHeadsMap().get(this.heightBlock - 1);

        long currentTarget = this.parentBlockHead.target;
        int targetedWinValue = BlockChain.calcWinValueTargetedBase(dcSet, this.heightBlock, this.winValue, currentTarget);
        if (!cnt.isTestNet() && targetedWinValue < 1) {
            //targetedWinValue = this.calcWinValueTargeted(dcSet);
            LOGGER.debug("*** Block[" + this.heightBlock + "] targeted WIN_VALUE < MINIMAL TARGET " + targetedWinValue + " < " + currentTarget);
            return false;
        }
        this.target = BlockChain.calcTarget(this.heightBlock, currentTarget, this.winValue);
        if (this.target == 0) {
            BlockChain.calcTarget(this.heightBlock, currentTarget, this.winValue);
            LOGGER.debug("*** Block[" + this.heightBlock + "] TARGET = 0");
            return false;
        }

        if (this.atBytes != null && this.atBytes.length > 0) {
            try {

                AT_Block atBlock = AT_Controller.validateATs(this.getBlockATs(), dcSet.getBlockMap().last().getHeight() + 1, dcSet);
                //this.atFees = atBlock.getTotalFees();
            } catch (NoSuchAlgorithmException | AT_Exception e) {
                LOGGER.error(e.getMessage(), e);
                return false;
            }
        }

        if (dcSet.getBlockSignsMap().contains(signature)) {
            LOGGER.debug("*** Block[" + Base58.encode(signature) + "] already exist");
            return false;
        }

        long timerStart = System.currentTimeMillis();

        //CHECK TRANSACTIONS

        if (this.transactionCount == 0) {
            // empty transactions
        } else {
            int seq = 1;
            byte[] blockSignature = this.getSignature();
            byte[] transactionSignature;
            byte[] transactionsSignatures = new byte[0];

            this.getTransactions();

            boolean isPrimarySet = !dcSet.isFork();

            long timerProcess = 0;
            long timerRefsMap_set = 0;
            long timerUnconfirmedMap_delete = 0;
            long timerFinalMap_set = 0;
            long timerTransFinalMapSinds_set = 0;

            long timestampEnd = this.getTimestamp()
                    + BlockChain.GENERATING_MIN_BLOCK_TIME_MS + 1;
            // because time filter used by parent block timestamp on core.BlockGenerator.run()
            //long timestampBeg = this.getParent(dcSet).getTimestamp(dcSet);

            DCSet validatingDC;

            if (andProcess) {
                validatingDC = dcSet;
                this.txCalculated = new ArrayList<R_Calculated>();
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

                seqNo++; /// (!!)

                if (!transaction.isWiped()) {

                    //CHECK IF NOT GENESIS TRANSACTION
                    if (transaction.getCreator() == null) {
                        // ALL GENESIS transaction
                        LOGGER.debug("*** Block[" + this.heightBlock
                                + "].Tx[" + seqNo + " : " ///this.getTransactionSeq(transaction.getSignature()) + " : "
                                + transaction.viewFullTypeName() + "]"
                                + "creator is Null!"
                        );
                        return false;
                    }

                    if (!transaction.isSignatureValid(validatingDC)) {
                        //
                        LOGGER.debug("*** Block[" + this.heightBlock
                                + "].Tx[" + seqNo + "=" + this.getTransactionSeq(transaction.getSignature()) + " : "
                                + transaction.viewFullTypeName() + "]"
                                + "signature not valid!"
                                + " " + Base58.encode(transaction.getSignature()));
                        return false;
                    }

                    transaction.setBlock(this, validatingDC, Transaction.FOR_NETWORK, seqNo);

                    //CHECK IF VALID
                    if (transaction.isValid(Transaction.FOR_NETWORK, 0l) != Transaction.VALIDATE_OK) {
                        LOGGER.debug("*** Block[" + this.heightBlock
                                + "].Tx[" + this.getTransactionSeq(transaction.getSignature()) + " : "
                                + transaction.viewFullTypeName() + "]"
                                + "invalid code: " + transaction.isValid(Transaction.FOR_NETWORK, 0l)
                                + " " + Base58.encode(transaction.getSignature()));
                        return false;
                    }

                    //CHECK TIMESTAMP AND DEADLINE
                    if (transaction.getTimestamp() > timestampEnd
                            //|| transaction.getDeadline() <= timestampBeg
                            && this.heightBlock > 105999
                            ) {
                        LOGGER.debug("*** Block[" + this.heightBlock + "].TX.timestamp invalid "
                                + Base58.encode(transaction.getSignature()));
                        return false;
                    }

                    timerStart = System.currentTimeMillis();
                    try {
                        transaction.process(this, Transaction.FOR_NETWORK);
                    } catch (Exception e) {
                        if (cnt.isOnStopping())
                            return false;

                        LOGGER.error("*** Block[" + this.heightBlock + "].TX.process ERROR", e);
                        return false;
                    }
                    timerProcess += System.currentTimeMillis() - timerStart;

                } else {

                    transaction.setBlock(this, validatingDC, Transaction.FOR_NETWORK, seqNo);

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

                    Long key = Transaction.makeDBRef(this.heightBlock, seq);

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

                    Long key = Transaction.makeDBRef(this.heightBlock, seq);

                    finalMap.set(key, transaction);
                    transFinalMapSinds.set(transactionSignature, key);
                    List<byte[]> signatures = transaction.getSignatures();
                    if (signatures != null) {
                        for (byte[] itemSignature : signatures) {
                            transFinalMapSinds.set(itemSignature, key);
                        }
                    }
                }

                transactionsSignatures = Bytes.concat(transactionsSignatures, transactionSignature);
            }

            if (validatingDC.isFork()) {
                validatingDC.close();
            }

            transactionsSignatures = Crypto.getInstance().digest(transactionsSignatures);
            if (!Arrays.equals(this.transactionsHash, transactionsSignatures)) {
                LOGGER.debug("*** Block[" + this.heightBlock + "].digest(transactionsSignatures) invalid");
                return false;
            }

            long tickets = System.currentTimeMillis() - timerStart;
            LOGGER.debug("[" + this.heightBlock + "] processing time: " + tickets * 0.001
                    + " for org.erachain.records:" + this.transactionCount + " millsec/record:" + tickets / this.transactionCount);

        }

        //BLOCK IS VALID
        if (andProcess) {
            try {
                this.process_after(cnt, dcSet);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return false;
            }

            timerStart = System.currentTimeMillis();
            dcSet.getBlockMap().add(this);
            LOGGER.debug("BlockMap add timer: " + (System.currentTimeMillis() - timerStart) + " [" + this.heightBlock + "]");

        }

        return true;
    }

    //PROCESS/ORPHAN
    public void feeProcess(DCSet dcSet, boolean asOrphan) {
        //REMOVE FEE

        long emittedFee;
        if (blockHead == null) {
            this.blockHead = new BlockHead(this, this.getTotalFee(dcSet).unscaledValue().longValue(),
                    this.getBonusFee().unscaledValue().longValue());
        } else {

        }

        if (BlockChain.ROBINHOOD_USE) {
            // find rich account
            String rich = Account.getRichWithForks(dcSet, Transaction.FEE_KEY);

            if (!rich.equals(this.creator.getAddress())) {
                emittedFee = this.blockHead.totalFee>>1;
                
                Account richAccount = new Account(rich);
                richAccount.changeBalance(dcSet, !asOrphan, Transaction.FEE_KEY,
                        new BigDecimal(emittedFee).movePointLeft(BlockChain.AMOUNT_DEDAULT_SCALE), true);
            } else {
                emittedFee = this.blockHead.emittedFee;
            }
            
        } else {
            emittedFee = this.blockHead.emittedFee;
        }

        //UPDATE GENERATOR BALANCE WITH FEE
        if (this.blockHead.totalFee != 0) {
            BigDecimal totalFee = new BigDecimal(this.blockHead.totalFee).movePointLeft(BlockChain.AMOUNT_DEDAULT_SCALE);
            this.creator.changeBalance(dcSet, asOrphan, Transaction.FEE_KEY,
                    totalFee, true);

            // MAKE CALCULATED TRANSACTIONS
            if (!dcSet.isFork() && !asOrphan) {
                if (this.txCalculated == null)
                    this.txCalculated = new ArrayList<R_Calculated>();

                this.txCalculated.add(new R_Calculated(this.creator, Transaction.FEE_KEY,
                        totalFee, "forging", Transaction.makeDBRef(this.heightBlock, 0)));
            }
        }

        if (emittedFee != 0) {
            // SUBSTRACT from EMISSION (with minus)
            GenesisBlock.CREATOR.changeBalance(dcSet, !asOrphan, Transaction.FEE_KEY,
                    new BigDecimal(emittedFee).movePointLeft(BlockChain.AMOUNT_DEDAULT_SCALE), true);
        }

        //LOGGER.debug("<<< core.block.Block.orphan(DBSet) #3");

    }

    public void setCOMPUbals(DCSet dcSet, int height) {

        // TEST COMPU ORPHANs
        HashMap bals = new HashMap();
        Collection<Tuple2<String, Long>> keys = dcSet.getAssetBalanceMap().getKeys();
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal totalNeg = BigDecimal.ZERO;
        for (Tuple2<String, Long> key : keys) {
            if (key.b == 2l) {
                Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> ball = dcSet
                        .getAssetBalanceMap().get(key);

                bals.put(key.a, ball.a.b);
            }
        }
        totalCOMPUtest.put(height, bals);
    }


    public void compareCOMPUbals(DCSet dcSet, int heightParent, String mess) {

        HashMap parentBalanses = (HashMap) totalCOMPUtest.get(heightParent);
        if (parentBalanses != null) {
            Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> ball;
            BigDecimal ballParent;
            Collection<Tuple2<String, Long>> keys = dcSet.getAssetBalanceMap().getKeys();
            boolean error = false;
            for (Tuple2<String, Long> key : keys) {
                if (key.b == 2l) {
                    ball = dcSet.getAssetBalanceMap().get(key);

                    ballParent = (BigDecimal) parentBalanses.get(key.a);
                    if (ballParent != null && ballParent.compareTo(ball.a.b) != 0
                            ||  ballParent == null && ball.a.b.signum() != 0) {
                        LOGGER.error(" WRONG COMPU orphan " + mess + " [" + (heightParent + 1) + "] for ADDR :" + key.a
                                + " balParent : " + (ballParent==null?"NULL":ballParent.toPlainString())
                                + " ---> " + (ball==null?"NULL":ball.a.b.toPlainString())
                                + " == " + ball.a.b.subtract(ballParent==null?BigDecimal.ZERO:ballParent));

                        error = true;
                    }
                }
            }
            if (error) {
                LOGGER.error(" WRONG COMPU orphan " + mess + " [" + (heightParent + 1) + "] "
                        + " totalFee: " + this.getTotalFee()
                        + " bonusFee: " + this.getBonusFee());

                error = false;
            }
        }
    }

    // TODO - make it trownable
    public void process_after(Controller cnt, DCSet dcSet) throws Exception {

        //PROCESS FEE
        feeProcess(dcSet, false);
        
		/*
		if (!dcSet.isFork()) {
			int lastHeight = dcSet.getBlocksHeadMap().getLastBlock().getHeight(dcSet);
			LOGGER.error("*** core.block.Block.process(DBSet)[" + (this.getParentHeight(dcSet) + 1)
					+ "] SET new last Height: " + lastHeight
					+ " getHeightMap().getHeight: " + this.height_process);
		}
		 */

        if (heightBlock % BlockChain.MAX_ORPHAN == 0) {
            cnt.blockchainSyncStatusUpdate(heightBlock);
        }

        if (BlockChain.TEST_FEE_ORPHAN > 0 && BlockChain.TEST_FEE_ORPHAN > this.heightBlock) {
            setCOMPUbals(dcSet, this.heightBlock);
        }

        // MAKE CALCULATER TRANSACTIONS
        if (this.txCalculated != null && !this.txCalculated.isEmpty()) {
            TransactionFinalMap finalMap = dcSet.getTransactionFinalMap();
            R_Calculated txCalculated;
            int size = this.txCalculated.size();
            int indexStart = this.transactionCount + 1;
            long key;
            int index;
            for (int i = 0; i < size; i++) {
                if (cnt.isOnStopping())
                    return;

                index = i + indexStart;
                txCalculated = this.txCalculated.get(i);
                txCalculated.setHeightSeq(this.heightBlock, index);
                finalMap.set(txCalculated);

            }
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

        LOGGER.debug("getBlocksHeadMap().set timer: " + (System.currentTimeMillis() - timerStart));

        //this.heightBlock = dcSet.getBlockSignsMap().getHeight(this.signature);

        if (BlockChain.TEST_FEE_ORPHAN > 0 && BlockChain.TEST_FEE_ORPHAN > this.heightBlock) {
            // TEST COMPU ORPHANs
            compareCOMPUbals(dcSet, this.heightBlock - 1, "before PROCESS");
        }

        // for DEBUG
        if (this.heightBlock == 65431
                || this.heightBlock == 86549) {
            int rrrr =0;
        }

        //PROCESS TRANSACTIONS
        int seq = 1;
        byte[] blockSignature = this.getSignature();
        byte[] transactionSignature;

        this.getTransactions();

        if (this.transactionCount > 0) {
            this.txCalculated = new ArrayList<R_Calculated>();

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
                transaction.setBlock(this, dcSet, Transaction.FOR_NETWORK, ++seqNo);

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

                Long key = Transaction.makeDBRef(this.heightBlock, seq);

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

            long tickets = System.currentTimeMillis() - start;
            LOGGER.debug("[" + this.heightBlock + "] processing time: " + tickets * 0.001
                    + " for org.erachain.records:" + this.transactionCount + " millsec/record:" + tickets / this.transactionCount);

        }

        this.process_after(cnt, dcSet);

        timerStart = System.currentTimeMillis();
        dcSet.getBlockMap().add(this);
        LOGGER.debug("BlockMap add timer: " + (System.currentTimeMillis() - timerStart) + " [" + this.heightBlock + "]");

    }

    public void orphan(DCSet dcSet) throws Exception {

        Controller cnt = Controller.getInstance();
        if (cnt.isOnStopping())
            throw new Exception("on stoping");

        //LOGGER.debug("<<< core.block.Block.orphan(DBSet) #0");
        int height = this.getHeight();
        if (height == 1) {
            // GENESIS BLOCK cannot be orphanED
            return;
        }

        if ( this.heightBlock > 162045 &&  this.heightBlock < 162050 ) {
            LOGGER.error(" [" + this.heightBlock + "] BONUS = 0???");
        }

        if (BlockChain.TEST_FEE_ORPHAN > 0 && BlockChain.TEST_FEE_ORPHAN > this.heightBlock) {
            // TEST COMPU ORPHANs
            compareCOMPUbals(dcSet, height, "before ORPHAN");
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
                + " for org.erachain.records:" + this.getTransactionCount() + " millsec/record:" + tickets / (this.getTransactionCount() + 1));

        //this.parentBlock = null;
        byte[] lastSignature = dcSet.getBlockMap().getLastBlockSignature();
        if (!Arrays.equals(lastSignature, this.reference)) {
            LOGGER.debug("[" + this.heightBlock + "] orphaning time: " + (System.currentTimeMillis() - start) * 0.001
                    + "  ERROR ");

        }

        if (BlockChain.TEST_FEE_ORPHAN > 0 && BlockChain.TEST_FEE_ORPHAN > this.heightBlock) {
            // TEST COMPU ORPHANs
            compareCOMPUbals(dcSet, height - 1, "after ORPHAN");
        }

        //this.heightBlock = -1;

    }

    private void orphanTransactions(DCSet dcSet, int height) throws Exception {

        Controller cnt = Controller.getInstance();
        //DBSet dbSet = Controller.getInstance().getDBSet();

        TransactionMap unconfirmedMap = dcSet.getTransactionMap();
        TransactionFinalMap finalMap = dcSet.getTransactionFinalMap();
        TransactionFinalMapSigns transFinalMapSinds = dcSet.getTransactionFinalMapSigns();

        this.getTransactions();
        //ORPHAN ALL TRANSACTIONS IN DB BACK TO FRONT
        int seqNo;
        for (int i = this.transactionCount - 1; i >= 0; i--) {
            seqNo = i + 1;
            if (cnt.isOnStopping())
                throw new Exception("on stoping");

            Transaction transaction = transactions.get(i);
            //LOGGER.debug("<<< core.block.Block.orphanTransactions\n" + transaction.toJson());

            // (!) seqNo = i + 1
            transaction.setBlock(this, dcSet, Transaction.FOR_NETWORK, seqNo);

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

            Long key = Transaction.makeDBRef(height, seqNo);

            finalMap.delete(key);
            transFinalMapSinds.delete(transaction.getSignature());
            List<byte[]> signatures = transaction.getSignatures();
            if (signatures != null) {
                for (byte[] itemSignature : signatures) {
                    transFinalMapSinds.delete(itemSignature);
                }
            }

        }

        // DELETE ALL CALCULATED
        finalMap.delete(height);
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
                + " H: " + this.getHeight()
                + " C: " + this.getCreator().getPersonAsString();
    }

}
