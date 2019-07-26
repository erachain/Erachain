package org.erachain.core.block;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.apache.commons.net.util.Base64;
import org.erachain.at.ATBlock;
import org.erachain.at.ATController;
import org.erachain.at.ATException;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.Synchronizer;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.blockexplorer.BlockExplorer;
import org.erachain.core.blockexplorer.ExplorerJsonLine;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.RCalculated;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionFactory;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMap;
import org.erachain.datachain.TransactionFinalMapSigns;
import org.erachain.datachain.TransactionMap;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.APIUtils;
import org.erachain.utils.Converter;
import org.erachain.utils.NumberAsString;
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
public class Block implements ExplorerJsonLine {

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
    public List<RCalculated> txCalculated;

    // BODY
    protected List<Transaction> transactions;
    protected byte[] rawTransactions = null;
    //protected Long atFees;
    protected byte[] atBytes;

    // FORGING INFO
    // при обработке трнзакций используем для запоминания что данные менялись
    protected List<Account> forgingInfoUpdate;

    // was validated
    protected boolean wasValidated;

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

        public int calcWinValueTargeted() {
            return (int) (BlockChain.BASE_TARGET * winValue / target);
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
            head.put("winValueTargeted", calcWinValueTargeted());
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

    public JSONObject jsonForExplorerPage(JSONObject langObj) {
        JSONObject blockJSON = new JSONObject();
        blockJSON.put("height", heightBlock);
        blockJSON.put("signature", Base58.encode(signature));
        blockJSON.put("generator", creator.getAddress());
        blockJSON.put("transactionsCount", getTransactionCount());
        blockJSON.put("timestamp", getTimestamp());

        ///loadHeadMind(DCSet.getInstance());
        blockJSON.put("totalFee", viewFeeAsBigDecimal());
        Tuple2<Integer, Integer> forgingPoint = blockHead.creator.getForgingData(DCSet.getInstance(), heightBlock);
        if (forgingPoint != null) {
            blockJSON.put("deltaHeight", blockHead.heightBlock - forgingPoint.a);
        }
        blockJSON.put("generatingBalance", blockHead.forgingValue);
        blockJSON.put("target", blockHead.target);
        blockJSON.put("winValue", blockHead.winValue);
        blockJSON.put("winValueTargeted", blockHead.calcWinValueTargeted());
        return blockJSON;
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
            }

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
        if (data.length < (useHeight <= 0 ? BASE_LENGTH + HEIGHT_LENGTH : BASE_LENGTH)
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
        if (transactionCount < 0 || transactionCount > BlockChain.MAX_BLOCK_SIZE) {
            throw new Exception("Block parse - transactionCount error for useHeight[" + useHeight + "] with height:" + height);
        }
        position += TRANSACTIONS_COUNT_LENGTH;

        //SET TRANSACTIONDATA
        block.setTransactionData(transactionCount, Arrays.copyOfRange(data, position, data.length));

        return block;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        return Ints.fromByteArray(signature);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Block)
            return Arrays.equals(this.signature, ((Block) obj).signature);
        return false;
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

    public boolean isValidated() {
        return this.wasValidated;
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
        blockHead = dcSet.getBlocksHeadsMap().get(heightBlock);
        forgingValue = blockHead.forgingValue;
        winValue = blockHead.winValue;
        target = blockHead.target;
        totalFee = blockHead.totalFee;
        emittedFee = blockHead.emittedFee;
        //this.transactionCount = blockHead.transactionsCount;
        //this.version = blockHead.version;
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

    /**
     * Обновляет данные об измененных форжинговых балансах - используется при обработке транзакций
     * Копит все для каждого счета результирующее и потом разом в блоке изменим
     * Так обходится неопределенность при откате - если несколько транзакций для одного счета
     * меняли инфо по форжингу
     * @param account
     */
    public void addForgingInfoUpdate(Account account) {
        if (this.forgingInfoUpdate == null) {
            this.forgingInfoUpdate = new ArrayList<Account>();
            this.forgingInfoUpdate.add(account);
            return;
        }

        // проверим может уже естьт ам такой счет
        for (Account item: this.forgingInfoUpdate) {
            if (account.equals(item))
                return;
        }
        this.forgingInfoUpdate.add(account);

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

        if (true) {
            bonusFee = BlockChain.MIN_FEE_IN_BLOCK_4_10;
            if (this.heightBlock < inDay30 << 1)
                return BigDecimal.valueOf(70000, BlockChain.FEE_SCALE); // need SCALE for .unscaled()
            else if (this.heightBlock < inDay30 << 2) // 120 days = 4 mounth
                return BigDecimal.valueOf(60000, BlockChain.FEE_SCALE); // need SCALE for .unscaled()
            else if (this.heightBlock < inDay30 << 3) // 16 mounth - 72000
                return BigDecimal.valueOf(50000, BlockChain.FEE_SCALE); // need SCALE for .unscaled()
            else if (false && this.heightBlock < inDay30 << 4) //  64 mounth
                return BigDecimal.valueOf(40000, BlockChain.FEE_SCALE); // need SCALE for .unscaled()
            else if (false && this.heightBlock < inDay30 << 6) //  256 mounth
                return BigDecimal.valueOf(30000, BlockChain.FEE_SCALE); // need SCALE for .unscaled()
            else
                return BigDecimal.valueOf(20000, BlockChain.FEE_SCALE); // need SCALE for .unscaled()
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

    private BigDecimal getTotalFee(DCSet db) {
        BigDecimal fee = this.getFeeByProcess(db);
        return fee.add(this.getBonusFee());
    }

    private BigDecimal getTotalFee() {
        return getTotalFee(DCSet.getInstance());
    }

    private BigDecimal getFeeByProcess(DCSet db) {
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

    /**
     * need only for TESTs
     * @param transactions
     */
    public void setTransactions(List<Transaction> transactions) {
        this.setTransactions(transactions, transactions == null ? 0 : transactions.size());
    }

    public void setTransactions(List<Transaction> transactions, int count) {
        this.transactions = transactions;
        this.transactionCount = count;
        //this.atBytes = null;
        if (this.transactionsHash == null)
            this.transactionsHash = makeTransactionsHash(this.creator.getPublicKey(), transactions, null);
    }

    public int getTransactionSeq(byte[] signature) {
        int seqNo = 1;
        for (Transaction transaction : this.getTransactions()) {
            if (Arrays.equals(transaction.getSignature(), signature)) {
                return seqNo;
            }
            seqNo++;
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

    public BigDecimal getFeeAsBigDecimal() {

        return BigDecimal.valueOf(this.totalFee, BlockChain.FEE_SCALE);
    }

    public String viewFeeAsBigDecimal() {

        return NumberAsString.formatAsString(BigDecimal.valueOf(blockHead.totalFee, BlockChain.FEE_SCALE));
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
        block.put("winValueTargeted", blockHead.calcWinValueTargeted());
        block.put("creator", this.creator.getAddress());
        block.put("fee", this.viewFeeAsBigDecimal());
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

    private int dataLength = -1;
    public int getDataLength(boolean withHeight) {

        if (dataLength >= 0)
            return dataLength;

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
	public static int getPreviousForgingHeightForIncomes(DLSet dcSet, Account creator, int height) {

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

    public boolean isValidHead(DCSet dcSet) {

        Controller cnt = Controller.getInstance();

        if (BlockChain.BLOCK_COUNT > 0 && this.heightBlock > BlockChain.BLOCK_COUNT) {
            LOGGER.debug("*** Block[" + this.heightBlock + "] - Max count reached");
            return false;
        }

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
        //logger.debug("*** Block[" + height + "] " + new Timestamp(myTime));

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
			logger.debug("*** Block[" + height + "] REPEATED WIN invalid");
			return false;
		}
		 */

        // TEST STRONG of win Value
        //int base = BlockChain.getMinTarget(height);
        ///int targetedWinValue = this.calcWinValueTargeted(dcSet);

        this.forgingValue = creator.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).intValue();

        this.winValue = BlockChain.calcWinValue(dcSet, this.creator, this.heightBlock, this.forgingValue);
        if (this.winValue < 1) {
            LOGGER.debug("*** Block[" + this.heightBlock + "] WIN_VALUE not in BASE RULES " + this.winValue);
            LOGGER.debug("*** forgingValue: " + this.forgingValue);
            return false;
        }

        this.parentBlockHead = dcSet.getBlocksHeadsMap().get(this.heightBlock - 1);

        long currentTarget = this.parentBlockHead.target;
        int targetedWinValue = BlockChain.calcWinValueTargetedBase(dcSet, this.heightBlock, this.winValue, currentTarget);
        if (targetedWinValue < 1) {
            //targetedWinValue = this.calcWinValueTargeted(dcSet);
            LOGGER.debug("*** Block[" + this.heightBlock + "] targeted WIN_VALUE < MINIMAL TARGET " + targetedWinValue + " < " + currentTarget);
            return false;
        }
        this.target = BlockChain.calcTarget(this.heightBlock, currentTarget, this.winValue);
        if (this.target == 0) {
            BlockChain.calcTarget(this.heightBlock, currentTarget, this.winValue);
            LOGGER.debug("*** Block[" + this.heightBlock + "] TARGET = 0");
            LOGGER.debug("*** currentTarget: " + currentTarget);
            return false;
        }

        if (this.atBytes != null && this.atBytes.length > 0) {
            try {

                ATBlock atBlock = ATController.validateATs(this.getBlockATs(), dcSet.getBlockMap().last().getHeight() + 1, dcSet);
                //this.atFees = atBlock.getTotalFees();
            } catch (NoSuchAlgorithmException | ATException e) {
                LOGGER.error(e.getMessage(), e);
                return false;
            }
        }

        if (dcSet.getBlockSignsMap().contains(signature)) {
            LOGGER.debug("*** Block[" + Base58.encode(signature) + "] already exist");
            return false;
        }

        return true;
    }

    public boolean isValid(DCSet dcSet, boolean andProcess) {

        LOGGER.debug("*** Block[" + this.heightBlock + "] try Validate");

        // TRY CHECK HEAD
        if (!this.isValidHead(dcSet))
            return false;

        Controller cnt = Controller.getInstance();

        long timerStart = System.currentTimeMillis();

        //CHECK TRANSACTIONS

        if (this.transactionCount == 0) {
            // empty transactions
        } else {

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
                    + (BlockChain.DEVELOP_USE ? BlockChain.GENERATING_MIN_BLOCK_TIME_MS : BlockChain.FLUSH_TIMEPOINT)
                    - BlockChain.UNCONFIRMED_SORT_WAIT_MS
                    + 10;
            // because time filter used by parent block timestamp on core.BlockGenerator.run()
            //long timestampBeg = this.getParent(dcSet).getTimestamp(dcSet);

            DCSet validatingDC;

            // RESET forginf Info Updates
            this.forgingInfoUpdate = null;

            if (andProcess) {
                validatingDC = dcSet;
                if (dcSet.isFork() || cnt.noCalculated) {
                    this.txCalculated = null;
                } else {
                    // make pool for calculated
                    this.txCalculated = new ArrayList<RCalculated>();
                }
            } else {
                long processTiming = System.nanoTime();
                validatingDC = dcSet.fork();
                processTiming = (System.nanoTime() - processTiming) / 1000;
                if (processTiming < 999999999999l) {
                    LOGGER.debug("VALIDATING[" + this.heightBlock + "]="
                            + this.transactionCount + " db.FORK: " + processTiming + "[us]");
                }
                this.txCalculated = null;
            }

            long processTiming = System.nanoTime();
            long processTimingLocal;
            long processTimingLocalDiff;

            //DLSet dbSet = Controller.getInstance().getDBSet();
            TransactionMap unconfirmedMap = validatingDC.getTransactionMap();
            TransactionFinalMap finalMap = validatingDC.getTransactionFinalMap();
            TransactionFinalMapSigns transFinalMapSinds = validatingDC.getTransactionFinalMapSigns();

            int seqNo = 0;
            for (Transaction transaction : this.transactions) {
                if (cnt.isOnStopping())
                    return false;

                seqNo++;

                if (true) {
                    /**
                     * короче какая-то фиггня была - прилетал блок при тестах в котром транзакции были по номерам перепуьаны
                     * и ХЭШ блока не сходился с расчитываемым тут - как это могло произойти?
                     * Я ловил где было не совпадение - оно было в 6 на 7 трнзакции в блоке 264590
                     * потом этот блок откатился ситемой и заново пересобрался и все норм стало
                     */
                    String peerIP = Controller.getInstance().getSynchronizer().getPeer().getAddress().getHostName();
                    String txStr = APIUtils.openUrl(
                            //"http://138.68.225.51:9047/apirecords/getbynumber/"
                            "http://" + peerIP + ":" + Settings.getInstance().getWebPort() + "/apirecords/getbynumber/"
                        + this.heightBlock + "-" + seqNo);
                    if (txStr == null) {
                        Long error = null;
                        LOGGER.debug(peerIP + " -- " + this.heightBlock + "-" + seqNo
                                + " NOT FOUND");
                        break;
                    } else if (!txStr.contains(transaction.viewSignature())) {
                        Long error = null;
                        LOGGER.debug(peerIP + " -- " + this.heightBlock + "-" + seqNo
                                + " WRONG SIGNATURE");
                        break;
                    } else {
                        LOGGER.debug(peerIP + " -- " + this.heightBlock + "-" + seqNo
                                + " good!");
                    }
                }

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
                        LOGGER.debug("*** " + this.heightBlock + "-" + seqNo
                                + ":" + transaction.viewFullTypeName()
                                + " signature  invalid!"
                                + " " + Base58.encode(transaction.getSignature()));
                        return false;
                    }

                    //CHECK TIMESTAMP AND DEADLINE
                    if (this.heightBlock > 105999
                            && transaction.getTimestamp() > timestampEnd
                        //|| transaction.getDeadline() <= timestampBeg // не нужно так как при слиянии цепочек
                        // могут и должны страрые транзакции заноситься
                    ) {
                        LOGGER.debug("*** " + this.heightBlock + "-" + seqNo
                                + ":" + transaction.viewFullTypeName()
                                + " timestampEnd invalid"
                                + " " + Base58.encode(transaction.getSignature()));
                        return false;
                    }

                    transaction.setDC(validatingDC, Transaction.FOR_NETWORK, this.heightBlock, seqNo);

                    //CHECK IF VALID
                    // так как мы в блоке такие транзакции уже проверяем то коллизию с неподтвержденными не проверяем
                    // все равно их потом удалим - иначе при откатах может случиться оказия - что и в блоке она есть и в неподтвержденных
                    if (transaction.isValid(Transaction.FOR_NETWORK, Transaction.NOT_VALIDATE_KEY_COLLISION)
                            != Transaction.VALIDATE_OK) {
                        LOGGER.debug("*** " + this.heightBlock + "-" + seqNo
                                + ":" + transaction.viewFullTypeName()
                                + " invalid code: " + transaction.isValid(Transaction.FOR_NETWORK, 0l)
                                + " " + Base58.encode(transaction.getSignature()));
                        return false;
                    }

                    processTimingLocal = System.nanoTime();
                    try {
                        transaction.process(this, Transaction.FOR_NETWORK);
                    } catch (Exception e) {
                        if (cnt.isOnStopping())
                            return false;

                        LOGGER.error("*** " + this.heightBlock + "-" + seqNo
                                + ":" + transaction.viewFullTypeName() + e.getMessage(), e);
                        return false;
                    }

                    processTimingLocalDiff = System.nanoTime() - processTimingLocal;
                    if (processTimingLocalDiff < 999999999999l)
                        timerProcess += processTimingLocalDiff / 1000;

                } else {

                    transaction.setDC(validatingDC, Transaction.FOR_NETWORK, this.heightBlock, seqNo);

                    //UPDATE REFERENCE OF SENDER
                    if (transaction.isReferenced())
                        // IT IS REFERENCED RECORD?
                        transaction.getCreator().setLastTimestamp(transaction.getTimestamp(), validatingDC);
                }

                transactionSignature = transaction.getSignature();

                if (andProcess) {

                    //SET PARENT
                    ///logger.debug("[" + seqNo + "] try refsMap.set" );
                    if (isPrimarySet) {
                        //REMOVE FROM UNCONFIRMED DATABASE
                        ///logger.debug("[" + seqNo + "] try unconfirmedMap delete" );
                        processTimingLocal = System.nanoTime();
                        unconfirmedMap.delete(transactionSignature);
                        processTimingLocalDiff = System.nanoTime() - processTimingLocal;
                        if (processTimingLocalDiff < 999999999999l)
                            timerUnconfirmedMap_delete += processTimingLocalDiff / 1000;
                    }

                    if (cnt.isOnStopping())
                        return false;

                    if (BlockChain.TEST_DB_TXS_OFF && transaction.getType() == Transaction.SEND_ASSET_TRANSACTION
                            && ((RSend)transaction).getAssetKey() != 1) {
                        ;
                    } else {

                        ///logger.debug("[" + seqNo + "] try finalMap.set" );
                        processTimingLocal = System.nanoTime();
                        Long key = Transaction.makeDBRef(this.heightBlock, seqNo);
                        finalMap.set(key, transaction);
                        processTimingLocalDiff = System.nanoTime() - processTimingLocal;
                        if (processTimingLocalDiff < 999999999999l)
                            timerFinalMap_set += processTimingLocalDiff / 1000;

                        processTimingLocal = System.nanoTime();
                        transFinalMapSinds.set(transactionSignature, key);
                        List<byte[]> signatures = transaction.getSignatures();
                        if (signatures != null) {
                            for (byte[] itemSignature : signatures) {
                                transFinalMapSinds.set(itemSignature, key);
                            }
                        }
                        processTimingLocalDiff = System.nanoTime() - processTimingLocal;
                        if (processTimingLocalDiff < 999999999999l)
                            timerTransFinalMapSinds_set += processTimingLocalDiff / 1000;

                    }

                } else {

                    if (BlockChain.TEST_DB_TXS_OFF && transaction.getType() == Transaction.SEND_ASSET_TRANSACTION
                            && ((RSend) transaction).getAssetKey() != 1) {
                        ;
                    } else {

                        // for some TRANSACTIONs need add to FINAM MAP etc.
                        // RSertifyPubKeys - in same BLOCK with IssuePersonRecord

                        processTimingLocal = System.nanoTime();
                        Long key = Transaction.makeDBRef(this.heightBlock, seqNo);
                        finalMap.set(key, transaction);
                        processTimingLocalDiff = System.nanoTime() - processTimingLocal;
                        if (processTimingLocalDiff < 999999999999l)
                            timerFinalMap_set += processTimingLocalDiff / 1000;

                        processTimingLocal = System.nanoTime();
                        transFinalMapSinds.set(transactionSignature, key);
                        List<byte[]> signatures = transaction.getSignatures();
                        if (signatures != null) {
                            for (byte[] itemSignature : signatures) {
                                transFinalMapSinds.set(itemSignature, key);
                            }
                        }
                        processTimingLocalDiff = System.nanoTime() - processTimingLocal;
                        if (processTimingLocalDiff < 999999999999l)
                            timerTransFinalMapSinds_set += processTimingLocalDiff / 1000;
                    }
                }

                transactionsSignatures = Bytes.concat(transactionsSignatures, transactionSignature);
            }

            if (validatingDC.isFork()) {
                validatingDC.close();
            }

            transactionsSignatures = Crypto.getInstance().digest(transactionsSignatures);
            if (!Arrays.equals(this.transactionsHash, transactionsSignatures)) {
                byte[] digest = makeTransactionsHash(creator.getPublicKey(), transactions, null);

                LOGGER.debug("*** Block[" + this.heightBlock + "].digest(transactionsSignatures) invalid");
                return false;
            }

            if (!dcSet.isFork()) {
                // если это просчет уже для записи в нашу базу данных а не при выборе Цепочки для синхронизации
                processTiming = System.nanoTime() - processTiming;
                if (processTiming < 999999999999l) {
                    // при переполнении может быть минус
                    // в миеросекундах подсчет делаем
                    ////////// сдесь очень много времени занимает форканье базы данных - поэтому Счетчик трнзакций = 10 сразу
                    // не выше поставил точку времени после создания форка базы данных - чтобы не влияло
                    // так как форкнуть базу можно заранее - хотя для каждого блока который прилетает это нужно отдельно делать и
                    // это тоже время требует...
                    Controller.getInstance().getBlockChain().updateTXValidateTimingAverage(processTiming, this.transactionCount);
                }

                long tickets = System.currentTimeMillis() - timerStart;
                LOGGER.debug("VALIDATING[" + this.heightBlock + "]="
                        + this.transactionCount + " " + tickets + "[ms] " + tickets / this.transactionCount + "[ms/tx]"
                        + " Proc[us]: " + timerProcess
                        + (andProcess ?
                        " UnconfDel[us]: " + timerUnconfirmedMap_delete
                        : "")
                        + " SignsKey[us]: " + timerTransFinalMapSinds_set
                        + " FinalSet[us]: " + timerFinalMap_set
                );
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

            timerStart = System.nanoTime();
            dcSet.getBlockMap().add(this);
            timerStart = System.nanoTime() - timerStart;
            if (timerStart < 999999999999l)
                LOGGER.debug("BlockMap add timer [us]: " + timerStart / 1000 + " [" + this.heightBlock + "]");

        }

        this.wasValidated = true;
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
            if (!dcSet.isFork() && !asOrphan && !Controller.getInstance().noCalculated) {
                if (this.txCalculated == null)
                    this.txCalculated = new ArrayList<RCalculated>();

                this.txCalculated.add(new RCalculated(this.creator, Transaction.FEE_KEY,
                        totalFee, "forging", Transaction.makeDBRef(this.heightBlock, 0)));
            }
        }

        if (emittedFee != 0) {
            // SUBSTRACT from EMISSION (with minus)
            GenesisBlock.CREATOR.changeBalance(dcSet, !asOrphan, Transaction.FEE_KEY,
                    new BigDecimal(emittedFee).movePointLeft(BlockChain.AMOUNT_DEDAULT_SCALE), true);
        }

        //logger.debug("<<< core.block.Block.orphan(DLSet) #3");

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

        if (this.forgingInfoUpdate != null) {
            // обновить форжинговые данные - один раз для всех трнзакций в блоке
            // Обрабатывает данные об измененных форжинговых балансах
            // Для каждого счета берем результирующее изменения по форжинговой инфо
            // и разом в тут блоке изменим
            // Так обходится неопределенность при откате - если несколько транзакций для одного счета
            // меняли инфо по форжингу

            for (Account account: this.forgingInfoUpdate) {

                Tuple2<Integer, Integer> privousForgingPoint = account.getLastForgingData(dcSet);
                int currentForgingBalance = account.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).intValue();
                if (privousForgingPoint == null) {
                    if (currentForgingBalance >= BlockChain.MIN_GENERATING_BALANCE) {
                        account.setForgingData(dcSet, this.heightBlock, currentForgingBalance);
                    }
                } else {
                    // если это не инициализация то может на счете ранее нулевой баланс был
                    // надо обновить приход
                    if (privousForgingPoint.b < BlockChain.MIN_GENERATING_BALANCE
                            && currentForgingBalance >= BlockChain.MIN_GENERATING_BALANCE) {
                        account.setForgingData(dcSet, this.heightBlock, currentForgingBalance);
                    }
                }

            }

        }
        
		/*
		if (!dcSet.isFork()) {
			int lastHeight = dcSet.getBlocksHeadMap().getLastBlock().getHeight(dcSet);
			logger.error("*** core.block.Block.process(DLSet)[" + (this.getParentHeight(dcSet) + 1)
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
            RCalculated txCalculated;
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
        byte[] blockSignature = this.getSignature();
        byte[] transactionSignature;

        // RESET forginf Info Updates
        this.forgingInfoUpdate = null;

        this.getTransactions();

        if (this.transactionCount > 0) {
            if (dcSet.isFork() || cnt.noCalculated) {
                this.txCalculated = null;
            } else {
                // make pool for calculated
                this.txCalculated = new ArrayList<RCalculated>();
            }

            //DLSet dbSet = Controller.getInstance().getDBSet();
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

                ++seqNo;

                //logger.debug("[" + seqNo + "] record is process" );

                // NEED set DC for WIPED too
                transaction.setDC(dcSet, Transaction.FOR_NETWORK, this.heightBlock, seqNo);

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
                ///logger.debug("[" + seqNo + "] try refsMap.set" );

                //REMOVE FROM UNCONFIRMED DATABASE
                ///logger.debug("[" + seqNo + "] try unconfirmedMap delete" );
                timerStart = System.currentTimeMillis();
                unconfirmedMap.delete(transactionSignature);
                timerUnconfirmedMap_delete += System.currentTimeMillis() - timerStart;

                if (BlockChain.TEST_DB_TXS_OFF && transaction.getType() == Transaction.SEND_ASSET_TRANSACTION
                        && ((RSend)transaction).getAssetKey() != 1) {

                } else {

                    Long key = Transaction.makeDBRef(this.heightBlock, seqNo);

                    if (cnt.isOnStopping())
                        throw new Exception("on stoping");

                    ///logger.debug("[" + seqNo + "] try finalMap.set" );
                    timerStart = System.currentTimeMillis();
                    finalMap.set(key, transaction);
                    timerFinalMap_set += System.currentTimeMillis() - timerStart;
                    //logger.debug("[" + seqNo + "] try transFinalMapSinds.set" );
                    timerStart = System.currentTimeMillis();
                    transFinalMapSinds.set(transactionSignature, key);
                    List<byte[]> signatures = transaction.getSignatures();
                    if (signatures != null) {
                        for (byte[] itemSignature : signatures) {
                            transFinalMapSinds.set(itemSignature, key);
                        }
                    }
                    timerTransFinalMapSinds_set += System.currentTimeMillis() - timerStart;
                }

            }

            LOGGER.debug("timerProcess: " + timerProcess + "  timerRefsMap_set: " + timerRefsMap_set
                    + "  timerUnconfirmedMap_delete: " + timerUnconfirmedMap_delete + "  timerFinalMap_set:" + timerFinalMap_set
                    + "  timerTransFinalMapSinds_set: " + timerTransFinalMapSinds_set);

            long tickets = System.currentTimeMillis() - start;
            LOGGER.debug("[" + this.heightBlock + "] processing time: " + tickets * 0.001
                    + " TXs = " + this.transactionCount + " millsec/record:" + tickets / this.transactionCount);

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

        //logger.debug("<<< core.block.Block.orphan(DLSet) #0");
        if (this.heightBlock == 1) {
            // GENESIS BLOCK cannot be orphanED
            return;
        }

        if ( this.heightBlock > 162045 &&  this.heightBlock < 162050 ) {
            LOGGER.error(" [" + this.heightBlock + "] BONUS = 0???");
        }

        if (BlockChain.TEST_FEE_ORPHAN > 0 && BlockChain.TEST_FEE_ORPHAN > this.heightBlock) {
            // TEST COMPU ORPHANs
            compareCOMPUbals(dcSet, heightBlock, "before ORPHAN");
        }

        long start = System.currentTimeMillis();

        // RESET forginf Info Updates
        this.forgingInfoUpdate = null;

        //ORPHAN TRANSACTIONS
        //logger.debug("<<< core.block.Block.orphan(DLSet) #2 ORPHAN TRANSACTIONS");
        this.orphanTransactions(dcSet, heightBlock);

        //logger.debug("<<< core.block.Block.orphan(DLSet) #2f FEE");

        //REMOVE FEE
        feeProcess(dcSet, true);

        if (this.forgingInfoUpdate != null) {
            // обновить форжинговые данные - один раз для всех трнзакций в блоке
            // Обрабатывает данные об измененных форжинговых балансах
            // Для каждого счета берем результирующее изменения по форжинговой инфо
            // и разом в тут блоке изменим
            // Так обходится неопределенность при откате - если несколько транзакций для одного счета
            // меняли инфо по форжингу
            for (Account account: this.forgingInfoUpdate) {
                if (!this.getCreator().equals(account)) {
                    // если этот блок не собирался этим человеком
                    Tuple2<Integer, Integer> lastForgingPoint = account.getLastForgingData(dcSet);
                    if (lastForgingPoint != null && lastForgingPoint.a == heightBlock
                            && !this.getCreator().equals(account)) {
                        account.delForgingData(dcSet, heightBlock);
                    }
                }
            }
        }

        //DELETE BLOCK FROM DB
        dcSet.getBlockMap().remove(this.signature, this.reference, this.creator);

        //logger.debug("<<< core.block.Block.orphan(DLSet) #4");

        long tickets = System.currentTimeMillis() - start;
        LOGGER.debug("[" + this.heightBlock + "] orphaning time: " + (System.currentTimeMillis() - start) * 0.001
                + " TXs = " + this.getTransactionCount() + " millsec/record:" + tickets / (this.getTransactionCount() + 1));

        //this.parentBlock = null;
        byte[] lastSignature = dcSet.getBlockMap().getLastBlockSignature();
        if (!Arrays.equals(lastSignature, this.reference)) {
            LOGGER.debug("[" + this.heightBlock + "] orphaning time: " + (System.currentTimeMillis() - start) * 0.001
                    + "  ERROR ");

        }

        if (BlockChain.TEST_FEE_ORPHAN > 0 && BlockChain.TEST_FEE_ORPHAN > this.heightBlock) {
            // TEST COMPU ORPHANs
            compareCOMPUbals(dcSet, heightBlock - 1, "after ORPHAN");
        }

        //this.heightBlock = -1;

    }

    private void orphanTransactions(DCSet dcSet, int height) throws Exception {

        Controller cnt = Controller.getInstance();
        //DLSet dbSet = Controller.getInstance().getDBSet();

        boolean notFork = !dcSet.isFork();

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
            //logger.debug("<<< core.block.Block.orphanTransactions\n" + transaction.toJson());

            // (!) seqNo = i + 1
            transaction.setDC(dcSet, Transaction.FOR_NETWORK, height, seqNo);

            if (!transaction.isWiped()) {
                transaction.orphan(this, Transaction.FOR_NETWORK);
            } else {
                // IT IS REFERENCED RECORD?
                if (transaction.isReferenced()) {
                    //UPDATE REFERENCE OF SENDER
                    transaction.getCreator().removeLastTimestamp(dcSet);
                }
            }

            if (notFork) {
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

        }

        // DELETE ALL CALCULATED
        finalMap.delete(height);
    }

    @Override
    public String toString() {
        return "[" + this.getHeight() + "]"
                + (this.winValue != 0 ? " WV: " + this.winValue : "")
                + " TX: " + this.transactionCount
                + " CR:" + this.getCreator().getPersonAsString();
    }

}
