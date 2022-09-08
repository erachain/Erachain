package org.erachain.core.block;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.at.ATBlock;
import org.erachain.at.ATController;
import org.erachain.at.ATException;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.TransactionsPool;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.blockexplorer.ExplorerJsonLine;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.OrderProcess;
import org.erachain.core.transaction.RCalculated;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.core.transaction.TransactionFactory;
import org.erachain.dapp.DAPP;
import org.erachain.datachain.*;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.ntp.NTP;
import org.erachain.utils.NumberAsString;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.*;

/**
 * обработка блоков - все что с ними связано. Без базы данных - сухие данные в вакууме
 */
public class Block implements Closeable, ExplorerJsonLine {

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
    public static final int TOTAL_WIN_VALUE_LENGTH = 8;
    public static final int FEE_LENGTH = 8;

    public static final int INVALID_NONE = 0; // GOOD
    public static final int INVALID_BRANCH = 1; // это не важная ошибка - не блокируем из-за нее при приеме побежных в буфер
    // ниже все блокируем
    public static final int INVALID_BLOCK_TIME = 5;
    public static final int INVALID_REFERENCE = 10;
    public static final int INVALID_MAX_COUNT = 11;
    public static final int INVALID_BLOCK_VERSION = 12;
    public static final int INVALID_BLOCK_WIN = 13;

    public static final int BASE_LENGTH = VERSION_LENGTH + REFERENCE_LENGTH + CREATOR_LENGTH
            + TRANSACTIONS_HASH_LENGTH + SIGNATURE_LENGTH + TRANSACTIONS_COUNT_LENGTH;
    private static final int AT_LENGTH = 0 + AT_BYTES_LENGTH;
    public static final int DATA_SIGN_LENGTH = VERSION_LENGTH + REFERENCE_LENGTH + TRANSACTIONS_HASH_LENGTH;
    static Logger LOGGER = LoggerFactory.getLogger(Block.class.getSimpleName());

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
    protected long totalWinValue;
    protected long totalFee;
    protected long emittedFee;
    public Block.BlockHead blockHead;

    // BODY
    protected byte[] rawTransactions = null;
    protected int rawTransactionsLength;
    //protected Long atFees;
    protected byte[] atBytes;

    private boolean fromTrustedPeer = false;

    // was validated
    protected boolean wasValidated;

    /**
     * ******* CALCULATED below - need to clear on CLOSE ******
     */

    protected List<Transaction> transactions;
    protected List<RCalculated> txCalculated;

    // FORGING INFO
    // при обработке транзакций используем для запоминания, что данные менялись
    protected List<Account> forgingInfoUpdate;

    /**
     * 1 - EARN FEE, 2 - BURNED
     */
    protected HashMap<AssetCls, Tuple2<BigDecimal, BigDecimal>> earnedAllAssets;

    protected DCSet validatedForkDB;

    /**
     * end CALCULATED
     */

    /////////////////////////////////////// BLOCK HEAD //////////////////////////////
    public static class BlockHead implements ExplorerJsonLine {

        public static final int BASE_LENGTH = VERSION_LENGTH + REFERENCE_LENGTH + CREATOR_LENGTH
                + TRANSACTIONS_COUNT_LENGTH + TRANSACTIONS_HASH_LENGTH + SIGNATURE_LENGTH
                + HEIGHT_LENGTH + GENERATING_BALANCE_LENGTH + WIN_VALUE_LENGTH + TOTAL_WIN_VALUE_LENGTH + WIN_VALUE_LENGTH
                + FEE_LENGTH + FEE_LENGTH + Integer.BYTES;

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
        public final long totalWinValue;
        public final long totalFee;
        public final long emittedFee;
        public final int size;

        public BlockHead(int version, byte[] reference, PublicKeyAccount creator, int transactionCount,
                         byte[] transactionsHash, byte[] signature,
                         int heightBlock, int forgingValue, long winValue, long target,
                         long totalWinValue, long totalFee, long emittedFee, int size) {
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
            this.totalWinValue = totalWinValue;
            this.totalFee = totalFee;
            this.emittedFee = emittedFee;
            this.size = size;
        }

        public BlockHead(Block block, int heightBlock, int forgingValue, long winValue, long target,
                         long totalFee, long emittedFee, long totalWinValue) {
            this.version = block.version;
            this.reference = block.reference;
            this.creator = block.creator;
            this.transactionsCount = block.transactionCount;
            this.transactionsHash = block.transactionsHash;
            this.signature = block.signature;
            this.size = block.getDataLength(false);

            this.heightBlock = heightBlock;
            this.forgingValue = forgingValue;
            this.winValue = winValue;
            this.target = target;
            this.totalWinValue = totalWinValue;
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
            this.size = block.getDataLength(false);

            this.heightBlock = block.heightBlock;
            this.forgingValue = block.forgingValue;
            this.winValue = block.winValue;
            this.target = block.target;
            this.totalWinValue = block.totalWinValue;
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
            this.totalWinValue = block.totalWinValue;
            this.totalFee = block.totalFee;
            this.emittedFee = block.emittedFee;
            this.size = block.getDataLength(false);
        }

        public long getTimestamp() {
            BlockChain blockChain = Controller.getInstance().getBlockChain();
            return blockChain.getTimestamp(this.heightBlock);
        }

        public String viewFeeAsBigDecimal() {
            return NumberAsString.formatAsString(BigDecimal.valueOf(totalFee, BlockChain.FEE_SCALE));
        }

        public byte[] toBytes() {

            int pos = 0;
            byte[] data = new byte[BASE_LENGTH];

            //WRITE VERSION
            byte[] versionBytes = Ints.toByteArray(this.version);
            System.arraycopy(versionBytes, 0, data, pos, VERSION_LENGTH);
            pos += VERSION_LENGTH;

            //WRITE REFERENCE
            byte[] flagsBytes = Bytes.ensureCapacity(this.reference, REFERENCE_LENGTH, 0);
            System.arraycopy(flagsBytes, 0, data, pos, REFERENCE_LENGTH);
            pos += REFERENCE_LENGTH;

            //WRITE GENERATOR
            byte[] generatorBytes = Bytes.ensureCapacity(this.creator.getPublicKey(), CREATOR_LENGTH, 0);
            System.arraycopy(generatorBytes, 0, data, pos, CREATOR_LENGTH);
            pos += CREATOR_LENGTH;

            //WRITE TRANSACTION COUNT
            byte[] transactionCountBytes = Ints.toByteArray(this.transactionsCount);
            System.arraycopy(transactionCountBytes, 0, data, pos, TRANSACTIONS_COUNT_LENGTH);
            pos += TRANSACTIONS_COUNT_LENGTH;

            //WRITE TRANSACTIONS HASH
            System.arraycopy(transactionsHash, 0, data, pos, TRANSACTIONS_HASH_LENGTH);
            pos += TRANSACTIONS_HASH_LENGTH;

            //WRITE SIGNATURE
            System.arraycopy(signature, 0, data, pos, SIGNATURE_LENGTH);
            pos += SIGNATURE_LENGTH;

            //WRITE HEIGHT
            byte[] heightBytes = Ints.toByteArray(this.heightBlock);
            System.arraycopy(heightBytes, 0, data, pos, HEIGHT_LENGTH);
            pos += HEIGHT_LENGTH;

            //WRITE GENERATING BALANCE
            byte[] generatingBalanceBytes = Ints.toByteArray(this.forgingValue);
            System.arraycopy(generatingBalanceBytes, 0, data, pos, GENERATING_BALANCE_LENGTH);
            pos += GENERATING_BALANCE_LENGTH;

            //WRITE WIN VALUE
            byte[] winValueBytes = Longs.toByteArray(this.winValue);
            System.arraycopy(winValueBytes, 0, data, pos, WIN_VALUE_LENGTH);
            pos += WIN_VALUE_LENGTH;

            //WRITE TARGET
            byte[] targetBytes = Longs.toByteArray(this.target);
            System.arraycopy(targetBytes, 0, data, pos, WIN_VALUE_LENGTH);
            pos += WIN_VALUE_LENGTH;

            //WRITE TOTAL WIN VALUE
            byte[] totalWinValueBytes = Longs.toByteArray(this.totalWinValue);
            System.arraycopy(totalWinValueBytes, 0, data, pos, TOTAL_WIN_VALUE_LENGTH);
            pos += TOTAL_WIN_VALUE_LENGTH;

            //WRITE TOTAL FEE
            byte[] totalFeeBytes = Longs.toByteArray(this.totalFee);
            System.arraycopy(totalFeeBytes, 0, data, pos, FEE_LENGTH);
            pos += FEE_LENGTH;

            //WRITE EMITTED FEE
            byte[] emittedFeeBytes = Longs.toByteArray(this.emittedFee);
            System.arraycopy(emittedFeeBytes, 0, data, pos, FEE_LENGTH);
            pos += FEE_LENGTH;

            //WRITE BLOCK SIZE
            byte[] blockSizeBytes = Ints.toByteArray(this.size);
            System.arraycopy(blockSizeBytes, 0, data, pos, Integer.BYTES);
            pos += Integer.BYTES;

            return data;
        }

        public static BlockHead parse(byte[] data) throws Exception {

            if (data.length == 0) {
                return null;
            }

            //CHECK IF WE HAVE MINIMUM BLOCK LENGTH
            if (data.length < BASE_LENGTH) {
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

            //READ TOTAL WIN VALUE
            byte[] totalWinValueBytes = Arrays.copyOfRange(data, position, position + TOTAL_WIN_VALUE_LENGTH);
            long tolalWinValue = Longs.fromByteArray(totalWinValueBytes);
            position += TOTAL_WIN_VALUE_LENGTH;

            //READ TOTAL FEE
            byte[] totalFeeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
            long totalFee = Longs.fromByteArray(totalFeeBytes);
            position += FEE_LENGTH;

            //READ EMITTED FEE
            byte[] emittedFeeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
            long emittedFee = Longs.fromByteArray(emittedFeeBytes);
            position += FEE_LENGTH;

            //READ BLOCK SIZE
            byte[] blockSizeBytes = Arrays.copyOfRange(data, position, position + Integer.BYTES);
            int size = Ints.fromByteArray(blockSizeBytes);
            position += Integer.BYTES;

            return new BlockHead(version, reference, creator, transactionCount, transactionsHash, signature,
                    height, forgingValue, winValue, target, tolalWinValue, totalFee, emittedFee, size);

        }

        /**
         * for percentage
         *
         * @return
         */
        public float calcWinValueTargeted() {
            return 100f * winValue / target;
        }

        @SuppressWarnings("unchecked")
        public JSONObject toJson() {
            JSONObject head = new JSONObject();

            head.put("version", this.version);
            head.put("reference", Base58.encode(this.reference));
            head.put("timestamp", this.getTimestamp());
            head.put("forgingValue", this.forgingValue);
            head.put("size", this.size);
            head.put("winValue", this.winValue);
            head.put("winValueTargeted", calcWinValueTargeted());
            head.put("target", this.target);
            head.put("creator", this.creator.getAddress());
            head.put("fee", this.totalFee);
            head.put("reward", this.totalFee);
            head.put("txFee", this.totalFee - this.emittedFee);
            head.put("emittedFee", this.emittedFee);
            head.put("transactionsCount", this.transactionsCount);
            head.put("transactionsHash", Base58.encode(this.transactionsHash));
            head.put("signature", Base58.encode(this.signature));
            head.put("height", this.heightBlock);

            return head;
        }

        public JSONObject jsonForExplorerPage(JSONObject langObj, Object[] args) {
            JSONObject blockJSON = new JSONObject();
            blockJSON.put("height", heightBlock);
            blockJSON.put("signature", Base58.encode(signature));
            blockJSON.put("generator", creator.getAddress());
            blockJSON.put("transactionsCount", transactionsCount);
            blockJSON.put("timestamp", getTimestamp());
            blockJSON.put("size", this.size);

            ///loadHeadMind(DCSet.getInstance());
            blockJSON.put("totalFee", viewFeeAsBigDecimal());
            Tuple3<Integer, Integer, Integer> forgingPoint = creator.getForgingData(DCSet.getInstance(), heightBlock);
            if (forgingPoint != null) {
                blockJSON.put("deltaHeight", heightBlock - forgingPoint.a);
            }
            blockJSON.put("generatingBalance", forgingValue);
            blockJSON.put("target", target);
            blockJSON.put("winValue", winValue);
            blockJSON.put("winValueTargeted", calcWinValueTargeted());
            return blockJSON;
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

        this.transactionsHash = transactionsHash;

        this.signature = signature;

    }

    public Block(int version, Block parentBlock, PublicKeyAccount generator,
                 Tuple2<List<Transaction>, Integer> transactionsItem,
                 byte[] atBytes,
                 int forgingValue, long winValue, long target) {
        // TODO Auto-generated constructor stub
        this.version = version;
        this.reference = parentBlock.signature;
        this.creator = generator;
        this.heightBlock = parentBlock.heightBlock + 1;

        this.transactions = transactionsItem.a;
        this.transactionCount = transactionsItem.b;
        this.atBytes = atBytes;

        makeTransactionsRAWandHASH();

        this.parentBlockHead = parentBlock.blockHead;
        this.forgingValue = forgingValue;
        this.winValue = winValue;
        this.target = target;

    }

    //GETTERS/SETTERS


    /**
     * USE only for TESTS !
     *
     * @param resference
     */
    public void setReferenceForTests(byte[] resference) {
        this.reference = resference;
    }

    /**
     * делает Хэш и сырые данные из набора транзакций
     *
     * @return
     */
    public void makeTransactionsRAWandHASH() {

        int atBytesLength;
        if (atBytes == null) {
            atBytesLength = 0;
        } else {
            atBytesLength = atBytes.length;
        }

        byte[] hashData;
        if (transactionCount == 0) {
            // TODO: убрать в новой версии как ненужное - если трнзакций нету то не используем вообще
            /**
             * а на самом деле если нет транзакций и AT_DATA пустая - а оана пустая
             * то нет необходимости делать подпись на основе этого хэша -
             * лучше его пустым делать - и не использовать чтобы не тормозить лишний раз
             * хотя когда блоки пустые особо и не тормозится
             */
            hashData = new byte[CREATOR_LENGTH + atBytesLength];
            System.arraycopy(creator.getPublicKey(), 0, hashData, 0, CREATOR_LENGTH);
            if (atBytesLength > 0) {
                System.arraycopy(atBytes, 0, hashData, CREATOR_LENGTH, atBytesLength);
            }

            // SAVE RAW
            rawTransactionsLength = 0;
            rawTransactions = new byte[0];

        } else {
            hashData = new byte[transactionCount * SIGNATURE_LENGTH + atBytesLength];

            rawTransactionsLength = getDataLengthTXs();
            rawTransactions = new byte[rawTransactionsLength];

            int rawPos = 0;
            int hashPos = 0;

            //MAKE TRANSACTIONS HASH
            for (Transaction transaction : transactions) {

                //WRITE TRANSACTION LENGTH
                int transactionLength = transaction.getDataLength(Transaction.FOR_NETWORK, true);
                byte[] transactionLengthBytes = Ints.toByteArray(transactionLength);
                System.arraycopy(transactionLengthBytes, 0, rawTransactions, rawPos, TRANSACTION_SIZE_LENGTH);
                rawPos += TRANSACTION_SIZE_LENGTH;

                // WRITE TRANSACTION
                System.arraycopy(transaction.toBytes(Transaction.FOR_NETWORK, true), 0, rawTransactions, rawPos, transactionLength);
                rawPos += transactionLength;

                // ACCUMULATE SINGNs FOR HASH
                System.arraycopy(transaction.getSignature(), 0, hashData, hashPos, SIGNATURE_LENGTH);
                hashPos += SIGNATURE_LENGTH;

            }

            if (atBytesLength > 0) {
                System.arraycopy(atBytes, 0, hashData, hashPos, atBytesLength);
            }

        }

        transactionsHash = Crypto.getInstance().digest(hashData);

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

            block = new Block(version, reference, generator, height, transactionsHash, atBytes, signature); //, atFeesL);
        } else {
            // GENESIS BLOCK version = 0
            block = new Block(version, reference, generator, height, transactionsHash, new byte[0], signature);
        }

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

    public void setFromTrustedPeer() {
        this.fromTrustedPeer = true;
    }
    public boolean isFromTrustedPeer() {
        return this.fromTrustedPeer;
    }

    public byte[] getSignature() {
        return this.signature;
    }

    public int getHeight() {
        return this.heightBlock;
    }

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
            return dcSet.getBlockMap().getAndProcess(parentHeight);
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
    }

    /**
     * если parentBlockHead == null возьмет его из базы данных
     *
     * @param dcSet
     */
    public void makeHeadMind(DCSet dcSet) {
        this.forgingValue = creator.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).intValue();

        this.winValue = BlockChain.calcWinValue(dcSet, this.creator, this.heightBlock, this.forgingValue, null);

        if (this.parentBlockHead == null) {
            this.parentBlockHead = dcSet.getBlocksHeadsMap().get(this.heightBlock - 1);
        }

        final long currentTarget = this.parentBlockHead.target;
        this.target = BlockChain.calcTarget(this.heightBlock, currentTarget, this.winValue);

        // STORE in HEAD
        this.blockHead = new BlockHead(this);
    }

    public void setParentHeadMind(BlockHead parentHead) {
        this.parentBlockHead = parentHead;
    }

    public Block getChild(DCSet db) {
        return db.getBlockMap().getAndProcess(this.getHeight() + 1);
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

    public static long getTimestamp(int height) {
        BlockChain blockChain = Controller.getInstance().getBlockChain();
        return blockChain.getTimestamp(height);
    }

    public long getTimestamp() {
        BlockChain blockChain = Controller.getInstance().getBlockChain();
        return blockChain.getTimestamp(this.heightBlock);
    }

    // balance on creator account when making this block
    public int getForgingValue() {
        return this.forgingValue;
    }

    /**
     * Обновляет данные об измененных форжинговых балансах - используется при обработке транзакций
     * Копит все для каждого счета результирующее и потом разом в блоке изменим
     * Так обходится неопределенность при откате - если несколько транзакций для одного счета
     * меняли инфо по форжингу
     *
     * @param account
     */
    public void addForgingInfoUpdate(Account account) {
        if (this.forgingInfoUpdate == null) {
            this.forgingInfoUpdate = new ArrayList<Account>();
            this.forgingInfoUpdate.add(account);
            return;
        }

        // проверим мочь уже есть там такой счет
        for (Account item : this.forgingInfoUpdate) {
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

        // in OLD protocol it USED - and heightBlock get AS 1
        int inDay30 = BlockChain.BLOCKS_PER_DAY(1) * 30;

        if (this.heightBlock < inDay30 << 1)
            return BigDecimal.valueOf(70000, BlockChain.FEE_SCALE); // need SCALE for .unscaled()
        else if (this.heightBlock < inDay30 << 2) // 120 days = 4 mounth
            return BigDecimal.valueOf(60000, BlockChain.FEE_SCALE); // need SCALE for .unscaled()
        else if (this.heightBlock < inDay30 << 3) // 16 mounth - 72000
            return BigDecimal.valueOf(50000, BlockChain.FEE_SCALE); // need SCALE for .unscaled()
        else if (this.heightBlock > BlockChain.VERS_30SEC && this.heightBlock <= BlockChain.FREE_FEE_FROM_HEIGHT)
            return BigDecimal.valueOf(2000, BlockChain.FEE_SCALE); // need SCALE for .unscaled()
        else if (this.heightBlock > BlockChain.FREE_FEE_FROM_HEIGHT)
            return BigDecimal.valueOf(2125, BlockChain.FEE_SCALE); // need SCALE for .unscaled()
        else
            return BigDecimal.valueOf(20000, BlockChain.FEE_SCALE); // need SCALE for .unscaled()

    }

    private BigDecimal getFeeByProcess(DCSet db) {
        int fee = 0;

        for (Transaction transaction : this.getTransactions()) {
            fee += transaction.getForgedFee();
        }

        return BigDecimal.valueOf(fee, BlockChain.FEE_SCALE);

    }

    private BigDecimal getTotalFee(DCSet db) {
        BigDecimal fee = this.getFeeByProcess(db);
        return fee.add(this.getBonusFee());
    }

    public HashMap<AssetCls, Tuple2<BigDecimal, BigDecimal>> getEarnedAllAssets() {
        return earnedAllAssets;
    }

    public void setTransactionData(int transactionCount, byte[] rawTransactions) {

        this.transactionCount = transactionCount;
        this.rawTransactions = rawTransactions;
        this.rawTransactionsLength = rawTransactions.length;
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

                    //ADD TO TRANSACTIONS
                    this.transactions.add(TransactionFactory.getInstance().parse(transactionBytes, Transaction.FOR_NETWORK));

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
     *
     * @param transactions
     */
    public void setTransactionsForTests(List<Transaction> transactions) {
        this.setTransactionsForTests(transactions, transactions == null ? 0 : transactions.size());
    }

    /**
     * clear old data and set new Transactions
     *
     * @param transactions
     * @param count
     */
    public void setTransactionsForTests(List<Transaction> transactions, int count) {
        this.transactions = transactions;
        this.transactionCount = count;
        this.atBytes = null;
        makeTransactionsRAWandHASH();
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

    public String viewTotalFeeAsBigDecimal() {
        return NumberAsString.formatAsString(BigDecimal.valueOf(blockHead.totalFee, BlockChain.FEE_SCALE));
    }
    public String viewEmittedFeeAsBigDecimal() {
        return NumberAsString.formatAsString(BigDecimal.valueOf(blockHead.emittedFee, BlockChain.FEE_SCALE));
    }
    public String viewTXFeeAsBigDecimal() {
        return NumberAsString.formatAsString(BigDecimal.valueOf(blockHead.totalFee - blockHead.emittedFee, BlockChain.FEE_SCALE));
    }

    //PARSE/CONVERT

    @SuppressWarnings("unchecked")
    public JSONObject toJson() {
        JSONObject block = new JSONObject();

        if (blockHead == null)
            loadHeadMind(DCSet.getInstance());

        DCSet dcSet = DCSet.getInstance();
        block.put("version", this.version);
        block.put("reference", Base58.encode(this.reference));
        block.put("timestamp", this.getTimestamp());
        block.put("generatingBalance", this.forgingValue);
        block.put("winValue", this.getWinValue());
        block.put("target", this.getTarget());
        block.put("winValueTargeted", blockHead.calcWinValueTargeted());
        block.put("creator", this.creator.getAddress());
        block.put("fee", BigDecimal.valueOf(totalFee, BlockChain.FEE_SCALE));
        block.put("reward", BigDecimal.valueOf(totalFee, BlockChain.FEE_SCALE));
        block.put("emittedFee", BigDecimal.valueOf(emittedFee, BlockChain.FEE_SCALE));
        block.put("txFee", BigDecimal.valueOf(totalFee - emittedFee, BlockChain.FEE_SCALE));
        block.put("transactionsCount", transactionCount);
        block.put("transactionsHash", Base58.encode(this.transactionsHash));
        block.put("signature", Base58.encode(this.signature));
        block.put("height", this.getHeight());
        block.put("size", this.getDataLength(false));

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
            block.put("blockATs", Base58.encode(atBytes)); //Converter.toHex(atBytes));
            //block.put("atFees", this.atFees);
        }

        //RETURN
        return block;
    }

    public JSONObject jsonForExplorerPage(JSONObject langObj, Object[] args) {
        JSONObject blockJSON = new JSONObject();
        if (blockHead == null) {
            // LOAD HEAD
            loadHeadMind((DCSet) DCSet.getInstance());
        }

        blockJSON.put("height", heightBlock);
        blockJSON.put("signature", Base58.encode(signature));
        blockJSON.put("generator", creator.getAddress());
        blockJSON.put("transactionsCount", getTransactionCount());
        blockJSON.put("timestamp", getTimestamp());
        blockJSON.put("size", blockHead.size);

        ///loadHeadMind(DCSet.getInstance());
        blockJSON.put("totalFee", viewTotalFeeAsBigDecimal());
        Tuple3<Integer, Integer, Integer> forgingPoint = blockHead.creator.getForgingData(DCSet.getInstance(), heightBlock);
        if (forgingPoint != null) {
            blockJSON.put("deltaHeight", blockHead.heightBlock - forgingPoint.a);
        }
        blockJSON.put("generatingBalance", blockHead.forgingValue);
        blockJSON.put("target", blockHead.target);
        blockJSON.put("winValue", blockHead.winValue);
        blockJSON.put("winValueTargeted", blockHead.calcWinValueTargeted());
        return blockJSON;
    }

    public byte[] toBytes(boolean withSign, boolean forDB) {

        int pos = 0;
        byte[] data = new byte[getDataLength(forDB)];

        //WRITE VERSION
        byte[] versionBytes = Ints.toByteArray(this.version);
        versionBytes = Bytes.ensureCapacity(versionBytes, VERSION_LENGTH, 0);
        System.arraycopy(versionBytes, 0, data, pos, VERSION_LENGTH);
        pos += VERSION_LENGTH;

        //WRITE REFERENCE
        byte[] flagsBytes = Bytes.ensureCapacity(this.reference, REFERENCE_LENGTH, 0);
        System.arraycopy(flagsBytes, 0, data, pos, REFERENCE_LENGTH);
        pos += REFERENCE_LENGTH;

        //WRITE GENERATOR
        byte[] generatorBytes = Bytes.ensureCapacity(this.creator.getPublicKey(), CREATOR_LENGTH, 0);
        System.arraycopy(generatorBytes, 0, data, pos, CREATOR_LENGTH);
        pos += CREATOR_LENGTH;

        if (forDB) {
            //WRITE HEIGHT
            byte[] heightBytes = Ints.toByteArray(this.heightBlock);
            heightBytes = Bytes.ensureCapacity(heightBytes, HEIGHT_LENGTH, 0);
            System.arraycopy(heightBytes, 0, data, pos, HEIGHT_LENGTH);
            pos += HEIGHT_LENGTH;
        }

        //WRITE TRANSACTIONS HASH
        System.arraycopy(transactionsHash, 0, data, pos, TRANSACTIONS_HASH_LENGTH);
        pos += TRANSACTIONS_HASH_LENGTH;

        if (!withSign) {
            // make HEAD data for signature
            return data;
        }

        //WRITE GENERATOR SIGNATURE
        System.arraycopy(signature, 0, data, pos, SIGNATURE_LENGTH);
        pos += SIGNATURE_LENGTH;

        //ADD ATs BYTES
        if (this.version > 1) {
            if (atBytes != null) {
                byte[] atBytesCount = Ints.toByteArray(atBytes.length);
                System.arraycopy(atBytesCount, 0, data, pos, AT_BYTES_LENGTH);
                pos += AT_BYTES_LENGTH;

                System.arraycopy(atBytes, 0, data, pos, atBytes.length);
                pos += atBytes.length;

            } else {
                byte[] atBytesCount = new byte[AT_BYTES_LENGTH];
                System.arraycopy(atBytesCount, 0, data, pos, AT_BYTES_LENGTH);
                pos += AT_BYTES_LENGTH;

            }
        }

        //WRITE TRANSACTION COUNT
        byte[] transactionCountBytes = Ints.toByteArray(this.getTransactionCount());
        transactionCountBytes = Bytes.ensureCapacity(transactionCountBytes, TRANSACTIONS_COUNT_LENGTH, 0);
        System.arraycopy(transactionCountBytes, 0, data, pos, TRANSACTIONS_COUNT_LENGTH);
        pos += TRANSACTIONS_COUNT_LENGTH;

        if (transactionCount > 0) {
            if (rawTransactionsLength == 0) {
                assert (false);
            } else {
                // уже есть готовые сырые данные
                System.arraycopy(rawTransactions, 0, data, pos, rawTransactionsLength);
            }
        }

        return data;
    }

    public byte[] toBytesForSign() {
        int pos = 0;
        byte[] data = new byte[DATA_SIGN_LENGTH];

        //WRITE VERSION
        byte[] versionBytes = Ints.toByteArray(this.version);
        versionBytes = Bytes.ensureCapacity(versionBytes, VERSION_LENGTH, 0);
        System.arraycopy(versionBytes, 0, data, pos, VERSION_LENGTH);
        pos += VERSION_LENGTH;

        //WRITE REFERENCE
        byte[] flagsBytes = Bytes.ensureCapacity(this.reference, REFERENCE_LENGTH, 0);
        System.arraycopy(flagsBytes, 0, data, pos, REFERENCE_LENGTH);
        pos += REFERENCE_LENGTH;

        System.arraycopy(transactionsHash, 0, data, pos, TRANSACTIONS_HASH_LENGTH);

        return data;
    }

    public void sign(PrivateKeyAccount account) {
        byte[] data = toBytesForSign();
        this.signature = Crypto.getInstance().sign(account, data);
    }

    public int getDataLengthTXs() {
        int length = 0;
        for (Transaction transaction : transactions) {
            length += TRANSACTION_SIZE_LENGTH + transaction.getDataLength(Transaction.FOR_NETWORK, true);
        }
        return length;
    }

    private int dataLength = -1;

    public int getDataLength(boolean forDB) {

        if (dataLength >= 0)
            return dataLength;

        int length = BASE_LENGTH;
        if (forDB)
            length += HEIGHT_LENGTH;

        if (this.version > 1) {
            length += AT_LENGTH;
            if (this.atBytes != null) {
                length += atBytes.length;
            }
        }

        if (transactionCount > 0) {
            if (rawTransactionsLength == 0) {
                // прийдется с нуля собирать размер
                length += getDataLengthTXs();
            } else {
                length += rawTransactionsLength;
            }
        }

        return length;
    }

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
    }

    public long calcWinValue(DCSet dcSet) {

        if (this.version == 0 || this.creator == null) {
            // GENESIS
            return this.winValue = BlockChain.GENESIS_WIN_VALUE;
        }

        return this.winValue = BlockChain.calcWinValue(dcSet, this.creator, this.heightBlock, this.forgingValue, null);
    }

    public void setWinValue(long newWinValue) {
        this.winValue = newWinValue;
    }

    public void setTarget(long newTarget) {
        this.target = newTarget;
    }

    public void setTotalWinValue(long newTotalWinValue) {
        this.totalWinValue = newTotalWinValue;
    }

    public int calcWinValueTargeted() {

        if (this.version == 0 || this.creator == null) {
            // GENESIS - getBlockChain = null
            return BlockChain.BASE_TARGET;
        }

        return BlockChain.calcWinValueTargeted(this.winValue, this.target);
    }

    public int isValidHead(DCSet dcSet) {

        if (BlockChain.BLOCK_COUNT > 0 && this.heightBlock > BlockChain.BLOCK_COUNT) {
            LOGGER.warn("*** Block[" + this.heightBlock + "] - Max count reached");
            return INVALID_MAX_COUNT;
        }

        //CHECK IF PARENT EXISTS
        if (this.heightBlock < 2 || this.reference == null) {
            LOGGER.warn("*** Block[" + this.heightBlock + "].reference invalid");
            return INVALID_REFERENCE;
        }

        byte[] lastSignature = dcSet.getBlockMap().getLastBlockSignature();
        if (!Arrays.equals(lastSignature, this.reference)) {
            LOGGER.warn("*** Block[" + this.heightBlock + "].reference from fork");
            return INVALID_BRANCH;
        }

        if (transactionCount > BlockChain.MAX_BLOCK_SIZE) {
            LOGGER.warn("*** Block[" + this.heightBlock + "] MAX_BLOCK_SIZE");
            return INVALID_MAX_COUNT;
        }
        if (rawTransactionsLength > BlockChain.MAX_BLOCK_SIZE_BYTES) {
            LOGGER.warn("*** Block[" + this.heightBlock + "] MAX_BLOCK_SIZE_BYTES");
            return INVALID_MAX_COUNT;
        }

        // TODO - show it to USER
        long blockTime = this.getTimestamp();
        long thisTimestamp = NTP.getTime();

        // необходимо разрешить более ранюю сборку - так чтобы мой собственный блок можно было собрать заранее
        // и потом его провалидировать и послать куда подальше
        // свой блок собирается аккурат мо моему NTP.getTime() и поэтому нет смысла вносить большие задержки от смещения мирового
        // однако если блок прилетел из-вне то смещения мировые могут его сделать невалидными и норм
        if (blockTime - 100 > thisTimestamp) {
            LOGGER.warn("*** Block[" + this.heightBlock + ":" + Base58.encode(this.signature).substring(0, 10) + "].timestamp invalid >NTP.getTime(): "
                    + " \n Block time: " + new Timestamp(blockTime) + " -- NTP: " + new Timestamp(thisTimestamp));
            return INVALID_BLOCK_TIME;
        }

        //CHECK IF VERSION IS CORRECT
        if (this.version != 1) {
            LOGGER.warn("*** Block[" + this.heightBlock + "].version invalid");
            return INVALID_BLOCK_VERSION;
        }
        if (this.version < 2 && this.atBytes != null && this.atBytes.length > 0) // || this.atFees != 0))
        {
            LOGGER.warn("*** Block[" + this.heightBlock + "].version AT invalid");
            return INVALID_BLOCK_VERSION;
        }

        if (BlockChain.FREEZED_FORGING.contains(creator.getAddress())) {
            return INVALID_BLOCK_WIN;
        }

        // TEST STRONG of win Value
        this.forgingValue = creator.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).intValue();

        this.winValue = BlockChain.calcWinValue(dcSet, this.creator, this.heightBlock, this.forgingValue, null);
        // если по ALL_VALID_BEFORE пройдет дальше то там корректировка значения стоит!
        if (this.winValue < 1 && this.heightBlock > BlockChain.WIN_VAL_ALL_VALID && this.heightBlock > BlockChain.ALL_VALID_BEFORE) {
            this.forgingValue = creator.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).intValue();
            this.winValue = BlockChain.calcWinValue(dcSet, this.creator, this.heightBlock, this.forgingValue, null);

            Tuple3<Integer, Integer, Integer> forgingPoint = creator.getLastForgingData(dcSet);
            LOGGER.warn("*** Block[" + this.heightBlock + "] WIN_VALUE not in BASE RULES " + this.winValue
                    + " Creator: " + this.creator.getAddress());
            LOGGER.warn("*** forging Value: " + this.forgingValue
                    + " creator DataPoint: " + creator.getForgingData(dcSet, forgingPoint == null ? heightBlock : forgingPoint.a)
                    + " creator LAST Data: " + creator.getLastForgingData(dcSet));
            return INVALID_BLOCK_WIN;
        }

        this.parentBlockHead = dcSet.getBlocksHeadsMap().get(this.heightBlock - 1);
        if (parentBlockHead == null) {
            LOGGER.warn("*** Block[" + this.heightBlock + "] not found Parent HEAD OR my BlocksHeadsMap was broken");
            return INVALID_REFERENCE;
        }

        if (this.winValue < 1) {
            // значит проскочило по BlockChain.ALL_VALID_BEFORE
            // присвоим его как предыдущее значение
            this.winValue = parentBlockHead.winValue
                    // на всякий случай поднимем немного иначе может не пройти если цель увеличится малость
                    + (parentBlockHead.winValue >> 4);
        }

        // вычислив всю силу цепочки
        this.totalWinValue = this.parentBlockHead.totalWinValue + this.winValue;

        final long currentTarget = this.parentBlockHead.target;
        int targetedWinValue = BlockChain.calcWinValueTargetedBase(dcSet, this.heightBlock, this.winValue, currentTarget);
        if (targetedWinValue < 1) {
            if (BlockChain.validBlocks.contains(heightBlock)) {
                targetedWinValue = (int) currentTarget >> 1;
            } else {
                //targetedWinValue = this.calcWinValueTargeted(dcSet);
                LOGGER.warn("*** Block[" + this.heightBlock + "] targeted WIN_VALUE < MINIMAL TARGET " + targetedWinValue + " < " + currentTarget);
                return INVALID_BLOCK_WIN;
            }
        }
        this.target = BlockChain.calcTarget(this.heightBlock, currentTarget, this.winValue);
        if (this.target == 0) {
            BlockChain.calcTarget(this.heightBlock, currentTarget, this.winValue);
            LOGGER.warn("*** Block[" + this.heightBlock + "] TARGET = 0");
            LOGGER.warn("*** currentTarget: " + currentTarget);
            return INVALID_BLOCK_WIN;
        }

        if (this.atBytes != null && this.atBytes.length > 0) {
            try {

                ATBlock atBlock = ATController.validateATs(this.getBlockATs(), dcSet.getBlockMap().last().getHeight() + 1, dcSet);
                //this.atFees = atBlock.getTotalFees();
            } catch (NoSuchAlgorithmException | ATException e) {
                LOGGER.error(e.getMessage(), e);
                return INVALID_BLOCK_VERSION;
            }
        }

        if (dcSet.getBlockSignsMap().contains(signature)) {
            LOGGER.warn("*** Block[" + Base58.encode(signature) + "] already exist");
            return INVALID_BRANCH;
        }

        return INVALID_NONE;
    }

    /**
     * проверка блока с возможностью исполнения. При этом с заданной базой где делать форк.
     * Если проверка одного блока то в памяти можно делать форк
     *
     * @param dcSetPlace DB place - make it outside
     * @param andProcess and process it
     * @return
     */
    public int isValid(DCSet dcSetPlace, boolean andProcess) {

        if (validatedForkDB != null) {
            LOGGER.error("is Valid validatedForkDB " + validatedForkDB + " not NULL [" + heightBlock + "]");
            close();
        }
        wasValidated = false;

        LOGGER.debug("*** Block[" + this.heightBlock + "] try Validate");

        // TRY CHECK HEAD
        int invalid = this.isValidHead(dcSetPlace);
        if (invalid > 0)
            return invalid;

        Controller cnt = Controller.getInstance();

        long timerStart = System.currentTimeMillis();

        //CHECK TRANSACTIONS

        int atBytesLength;
        if (atBytes != null && atBytes.length > 0) {
            atBytesLength = atBytes.length;
        } else {
            atBytesLength = 0;
        }
        byte[] transactionsSignatures;
        int transactionsSignaturesPos = 0;

        if (this.transactionCount == 0) {
            /**
             * см. ниже - нет необходимости ХЭШ проверять для 0 транзакций - пиши сюда что хочешь
             */
            if (false) {
                // empty transactions - USE CREATOR for HASH
                transactionsSignatures = new byte[CREATOR_LENGTH + atBytesLength];
                System.arraycopy(creator.getPublicKey(), 0, transactionsSignatures, 0, CREATOR_LENGTH);
                transactionsSignaturesPos += CREATOR_LENGTH;
            }

        } else {

            transactionsSignatures = new byte[SIGNATURE_LENGTH * transactionCount + atBytesLength];
            byte[] transactionSignature;

            boolean isPrimarySet = !dcSetPlace.isFork();

            long timerProcess = 0;
            long timerRefsMap_set = 0;
            long timerUnconfirmedMap_delete = 0;
            long timerFinalMap_set = 0;
            long timerTransFinalMapSinds_set = 0;

            long timestampEnd = this.getTimestamp() - BlockChain.UNCONFIRMED_SORT_WAIT_MS(heightBlock);

            // RESET forging Info Updates
            this.forgingInfoUpdate = null;

            if (andProcess) {
                if (cnt.noCalculated) {
                    this.txCalculated = null;
                } else {
                    // даже если это в Форке - если Полный Расчет то Калкулатед нужно вычислять
                    // для последующего слива в цепочку
                    // make pool for calculated
                    this.txCalculated = new ArrayList<RCalculated>();
                }

            } else {
                this.txCalculated = null;
            }

            makeHoldRoyalty(dcSetPlace, false);
            DAPP.processByBlock(dcSetPlace, this, false);

            this.getTransactions();

            long processTiming = System.nanoTime();
            long processTimingLocal;
            long processTimingLocalDiff;

            //TransactionMapImpl unconfirmedMap = dcSetPlace.getTransactionTab();
            TransactionsPool txMemPool = Controller.getInstance().transactionsPool;
            TransactionFinalMapImpl finalMap = dcSetPlace.getTransactionFinalMap();
            TransactionFinalMapSigns transFinalMapSigns = dcSetPlace.getTransactionFinalMapSigns();

            // CLEAR ASSETS FEE
            earnedAllAssets = new HashMap<>();

            int seqNo = 0;
            for (Transaction transaction : this.transactions) {
                if (cnt.isOnStopping())
                    return INVALID_BRANCH;

                seqNo++;
                transactionSignature = transaction.getSignature();

                if (!transaction.isWiped()) {

                    //CHECK IF NOT GENESIS TRANSACTION
                    if (transaction.getCreator() == null) {
                        // ALL GENESIS transaction
                        LOGGER.warn("*** Block[" + this.heightBlock
                                + "].Tx[" + seqNo + " : " ///this.getTransactionSeq(transaction.getSignature()) + " : "
                                + transaction + "]"
                                + "creator is Null!"
                        );
                        return INVALID_BLOCK_VERSION;
                    }

                    boolean isSignatureValid = false;
                    // TRY QUICK check SIGNATURE by FIND in POOL
                    try {
                        if (txMemPool.contains(transactionSignature)) {
                            if (isSignatureValid = transaction.trueEquals(txMemPool.get(transactionSignature))) {
                                // если транзакция была в пуле ожидания - она уже проверена на Дубль
                                transaction.checkedByPool = true;
                            }
                        }
                    } catch (java.lang.Throwable e) {
                        if (false && e instanceof java.lang.IllegalAccessError) {
                        } else {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }

                    if (!isSignatureValid) {
                        // for check SIGN need HEIGHT
                        transaction.setHeightSeq(heightBlock, seqNo);
                        if (!transaction.isSignatureValid(dcSetPlace)
                                && BlockChain.ALL_VALID_BEFORE < heightBlock) {
                            //
                            LOGGER.warn("*** signature invalid!!! " + this.heightBlock + "-" + seqNo
                                    + ": " + transaction);
                            return INVALID_BLOCK_VERSION;
                        }
                    }

                    //CHECK TIMESTAMP AND DEADLINE
                    if ((BlockChain.TEST_MODE || BlockChain.CLONE_MODE || heightBlock > 278989) &&
                            transaction.getTimestamp() > timestampEnd + BlockChain.GENERATING_MIN_BLOCK_TIME_MS(heightBlock)
                    ) {
                        LOGGER.warn("*** timestamp Overhead!!! " + this.heightBlock + "-" + seqNo
                                + ":" + transaction
                                + " for diff: " + (transaction.getTimestamp() - timestampEnd)
                        );
                        return INVALID_BLOCK_VERSION;
                    }

                    transaction.setDC(dcSetPlace, Transaction.FOR_NETWORK, this.heightBlock, seqNo);

                    //CHECK IF VALID
                    // так как мы в блоке такие транзакции уже проверяем то коллизию с неподтвержденными не проверяем
                    // все равно их потом удалим - иначе при откатах может случиться оказия - что и в блоке она есть и в неподтвержденных
                    if (transaction.isValid(Transaction.FOR_NETWORK, Transaction.NOT_VALIDATE_KEY_COLLISION)
                            != Transaction.VALIDATE_OK
                            && BlockChain.ALL_VALID_BEFORE < heightBlock) {
                        int error = transaction.isValid(Transaction.FOR_NETWORK, Transaction.NOT_VALIDATE_KEY_COLLISION);
                        LOGGER.warn("*** " + this.heightBlock + "-" + seqNo
                                + " invalid code: " + OnDealClick.resultMess(error) + "[" + error + "]"
                                + (transaction.errorValue == null ? "" : " {" + transaction.errorValue + "}")
                                + ": " + transaction);
                        return INVALID_BLOCK_VERSION;
                    }

                    processTimingLocal = System.nanoTime();
                    try {
                        transaction.process(this, Transaction.FOR_NETWORK);
                    } catch (Exception e) {
                        if (cnt.isOnStopping())
                            return INVALID_BRANCH;

                        LOGGER.error("*** " + e.getMessage() + "!!! " + this.heightBlock + "-" + seqNo
                                + ": " + transaction, e);
                        throw e;
                    }

                    processTimingLocalDiff = System.nanoTime() - processTimingLocal;
                    if (processTimingLocalDiff < 999999999999l)
                        timerProcess += processTimingLocalDiff / 1000;

                } else {

                    transaction.setDC(dcSetPlace, Transaction.FOR_NETWORK, this.heightBlock, seqNo);

                    //UPDATE REFERENCE OF SENDER
                    transaction.getCreator().setLastTimestamp(
                            new long[]{transaction.getTimestamp(), transaction.getDBRef()}, dcSetPlace);
                }

                if (andProcess) {

                    //SET PARENT
                    //REMOVE FROM UNCONFIRMED DATABASE
                    processTimingLocal = System.nanoTime();
                    txMemPool.offerMessage(transactionSignature);
                    processTimingLocalDiff = System.nanoTime() - processTimingLocal;
                    if (processTimingLocalDiff < 999999999999l)
                        timerUnconfirmedMap_delete += processTimingLocalDiff / 1000;

                    if (cnt.isOnStopping())
                        return INVALID_BRANCH;

                    ///logger.debug("[" + seqNo + "] try finalMap.set" );
                    processTimingLocal = System.nanoTime();
                    Long key = Transaction.makeDBRef(this.heightBlock, seqNo);

                    // добавляем после процессинга, когда в транзакции новые данные наросли
                    finalMap.put(key, transaction);
                    processTimingLocalDiff = System.nanoTime() - processTimingLocal;
                    if (processTimingLocalDiff < 999999999999l)
                        timerFinalMap_set += processTimingLocalDiff / 1000;

                    processTimingLocal = System.nanoTime();
                    transFinalMapSigns.put(transactionSignature, key);
                    List<byte[]> signatures = transaction.getOtherSignatures();
                    if (signatures != null) {
                        for (byte[] itemSignature : signatures) {
                            transFinalMapSigns.put(itemSignature, key);
                        }
                    }
                    processTimingLocalDiff = System.nanoTime() - processTimingLocal;
                    if (processTimingLocalDiff < 999999999999l)
                        timerTransFinalMapSinds_set += processTimingLocalDiff / 1000;

                } else {

                    // for some TRANSACTIONS need add to FINAL MAP etc.
                    // RCertifyPubKeys - in same BLOCK with IssuePersonRecord

                    processTimingLocal = System.nanoTime();
                    Long key = Transaction.makeDBRef(this.heightBlock, seqNo);
                    finalMap.put(key, transaction);
                    processTimingLocalDiff = System.nanoTime() - processTimingLocal;
                    if (processTimingLocalDiff < 999999999999l)
                        timerFinalMap_set += processTimingLocalDiff / 1000;

                    processTimingLocal = System.nanoTime();
                    transFinalMapSigns.put(transactionSignature, key);
                    List<byte[]> signatures = transaction.getOtherSignatures();
                    if (signatures != null) {
                        for (byte[] itemSignature : signatures) {
                            transFinalMapSigns.put(itemSignature, key);
                        }
                    }
                    processTimingLocalDiff = System.nanoTime() - processTimingLocal;
                    if (processTimingLocalDiff < 999999999999l)
                        timerTransFinalMapSinds_set += processTimingLocalDiff / 1000;
                }

                System.arraycopy(transactionSignature, 0, transactionsSignatures, transactionsSignaturesPos, SIGNATURE_LENGTH);
                transactionsSignaturesPos += SIGNATURE_LENGTH;

            }

            if (andProcess) {
                // если это просчет уже для записи в нашу базу данных
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
            }

            long tickets = System.currentTimeMillis() - timerStart;
            if (isPrimarySet || tickets / (transactionCount + 1) > 1) {
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

            /**
             * Только если есть транзакции тогда имеет смысл проверять их общий ХЭШ
             * иначе - пиши туда что хочешь - он просто будет участвовать в подписи
             * тогда можно разные подписи делать с его помощью?
             * а может тогда иключить его из подписи если нет транзакций?
             * Тогда нельзя будет генерировать разные подписи... да вроде пока и не используется это ни как
             */

            // ADD AT_BYTES
            if (atBytesLength > 0) {
                System.arraycopy(atBytes, 0, transactionsSignatures, transactionsSignaturesPos, atBytesLength);
            }

            transactionsSignatures = Crypto.getInstance().digest(transactionsSignatures);
            if (!Arrays.equals(this.transactionsHash, transactionsSignatures)) {
                LOGGER.warn("*** Block[" + this.heightBlock + "].digest(transactionsSignatures) invalid"
                        + " transactionCount: " + transactionCount
                        + (atBytesLength > 0 ? " atBytes: " + atBytesLength : ""));
                return INVALID_BLOCK_VERSION;
            }

        }

        //BLOCK IS VALID
        if (andProcess) {
            try {
                this.process_after(cnt, dcSetPlace);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return INVALID_BLOCK_VERSION;
            }

            timerStart = System.nanoTime();
            dcSetPlace.getBlockMap().putAndProcess(this);
            timerStart = System.nanoTime() - timerStart;
            if (timerStart < 999999999999l)
                LOGGER.debug("BlockMap add timer [us]: " + timerStart / 1000 + " [" + this.heightBlock + "]");

        }

        this.wasValidated = true;
        return INVALID_NONE;
    }

    public synchronized void setValidatedForkDB(DCSet validatedForkDB) {
        if (this.validatedForkDB != null) {
            LOGGER.debug("CLOSE on SET: " + this.validatedForkDB + " for " + this.toString());
            validatedForkDB.makedIn += " setValidatedForkDB: " + validatedForkDB + " for " + this.toString();
            this.validatedForkDB.close();
            this.validatedForkDB = null;
        }
        this.validatedForkDB = validatedForkDB;
        validatedForkDB.makedIn += " setValidatedForkDB: " + validatedForkDB + " for " + this.toString();
        LOGGER.debug(validatedForkDB.makedIn);
    }

    public boolean hasValidatedForkDB() {
        return this.validatedForkDB != null;
    }

    private boolean isClosed;

    /**
     * Закрывает базу в котрой производилась проверка блока
     */

    public synchronized void close() {

        txCalculated = null;
        forgingInfoUpdate = null;
        earnedAllAssets = null;

        if (validatedForkDB != null) {
            try {
                validatedForkDB.close();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            validatedForkDB = null;
        }

        if (transactions != null) {
            try {
                // ОЧЕНЬ ВАЖНО чтобы Finalizer мог спокойно удалять их и DCSet.fork
                // иначе Финализер не может зацикленные ссылки порвать и не очищает HEAP
                for (Transaction transaction : transactions) {
                    transaction.resetDCSet();
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            transactions = null;
        }

        isClosed = true;
    }

    @Override
    protected void finalize() throws Throwable {
        if (!isClosed) {
            boolean hasValidatedForkDB = validatedForkDB != null;
            close();
            if (hasValidatedForkDB && BlockChain.CHECK_BUGS > 9) {
                LOGGER.debug("validatedForkDB is FINALIZED: " + this);
            }
        }

        super.finalize();
    }

    public void saveToChainFromvalidatedForkDB() {
        validatedForkDB.writeToParent();
    }

    //PROCESS/ORPHAN

    /**
     * add some earn for forger and set some burn value
     *
     * @param asset
     * @param assetFeeAdd
     * @param assetFeeBurnAdd
     */
    public void addAssetFee(AssetCls asset, BigDecimal assetFeeAdd, BigDecimal assetFeeBurnAdd) {

        Tuple2<BigDecimal, BigDecimal> earnedPair;
        BigDecimal assetFee;
        BigDecimal assetFeeBurn;

        if (earnedAllAssets.containsKey(asset)) {
            earnedPair = earnedAllAssets.get(asset);
            assetFee = earnedPair.a;
            assetFeeBurn = earnedPair.b;
        } else {
            assetFee = BigDecimal.ZERO;
            assetFeeBurn = BigDecimal.ZERO;
        }

        if (assetFeeAdd != null && assetFeeAdd.signum() != 0) {
            assetFee = assetFee.add(assetFeeAdd);
        }

        if (assetFeeBurnAdd != null && assetFeeBurnAdd.signum() != 0) {
            assetFee = assetFee.subtract(assetFeeBurnAdd);
            assetFeeBurn = assetFeeBurn.add(assetFeeBurnAdd);
        }

        earnedPair = new Tuple2(assetFee, assetFeeBurn);
        earnedAllAssets.put(asset, earnedPair);
    }

    public boolean addCalculated(Account creator, long assetKey, BigDecimal amount,
                                 String message, long dbRef) {

        if (txCalculated != null) {
            txCalculated.add(new RCalculated(creator, assetKey, amount,
                    message, dbRef, 0L));
            return true;
        }
        return false;

    }

    public List<RCalculated> getTXCalculated() {
        return txCalculated;
    }

    public void feeProcess(DCSet dcSet, boolean asOrphan) {
        //REMOVE FEE

        long emittedFee;
        if (blockHead == null) {
            this.blockHead = new BlockHead(this, this.getTotalFee(dcSet).unscaledValue().longValue(),
                    this.getBonusFee().unscaledValue().longValue());
        }

        if (BlockChain.ROBINHOOD_USE) {
            // find rich account
            byte[] rich = Account.getRichWithForks(dcSet, Transaction.FEE_KEY);

            if (!this.creator.equals(rich)) {
                emittedFee = this.blockHead.totalFee >> 1;

                Account richAccount = new Account(rich);
                richAccount.changeBalance(dcSet, !asOrphan, false, Transaction.FEE_KEY,
                        new BigDecimal(emittedFee).movePointLeft(BlockChain.FEE_SCALE), false, false, true);
            } else {
                emittedFee = this.blockHead.emittedFee;
            }

        } else {
            emittedFee = this.blockHead.emittedFee;
        }

        //UPDATE GENERATOR BALANCE WITH FEE
        if (this.blockHead.totalFee > 0) {
            BigDecimal forgerEarn;
            if (BlockChain.CLONE_MODE) {
                // Авторские начисления на счет Эрачейн от всех комиссий в блоке
                long blockFeeRoyaltyLong = this.blockHead.totalFee / 20; // 5%
                BlockChain.CLONE_ROYALTY_ERACHAIN_ACCOUNT.changeBalance(dcSet, asOrphan, false, Transaction.FEE_KEY,
                        new BigDecimal(blockFeeRoyaltyLong).movePointLeft(BlockChain.FEE_SCALE), false, false, false);

                forgerEarn = new BigDecimal(this.blockHead.totalFee - blockFeeRoyaltyLong).movePointLeft(BlockChain.FEE_SCALE)
                        .setScale(BlockChain.FEE_SCALE);
            } else {
                forgerEarn = new BigDecimal(this.blockHead.totalFee).movePointLeft(BlockChain.FEE_SCALE);
            }

            this.creator.changeBalance(dcSet, asOrphan, false, Transaction.FEE_KEY,
                    forgerEarn, false, false, true);

            // учтем что нафоржили
            this.creator.changeCOMPUStatsBalances(dcSet, asOrphan, forgerEarn, Account.FEE_BALANCE_SIDE_FORGED);

            // MAKE CALCULATED TRANSACTIONS
            if (!asOrphan && this.txCalculated != null) {
                this.txCalculated.add(new RCalculated(this.creator, Transaction.FEE_KEY,
                        forgerEarn, BlockChain.MESS_FORGING, Transaction.makeDBRef(this.heightBlock, 0), 0L));
            }
        }

        if (emittedFee != 0) {
            // SUBSTRACT from EMISSION (with minus)
            BlockChain.FEE_ASSET_EMITTER.changeBalance(dcSet, !asOrphan, false, Transaction.FEE_KEY,
                    new BigDecimal(emittedFee).movePointLeft(BlockChain.FEE_SCALE), false, false, true);
        }

    }

    /**
     * Начисляет подати с конкретных транзакций по активу - разные выплаты.
     * Необходимо перед просчетом транзакций очищать earnedAllAssets
     *
     * @param dcSet
     * @param asOrphan
     */
    public void assetsFeeProcess(DCSet dcSet, boolean asOrphan) {

        if (BlockChain.TEST_MODE) {
            // EMIT
            if (earnedAllAssets == null)
                earnedAllAssets = new HashMap<>();

            BigDecimal emitted = BigDecimal.ONE;
            addAssetFee(BlockChain.ERA_ASSET, emitted, null);

            BlockChain.ERA_ASSET.getMaker().changeBalance(dcSet, !asOrphan, false,
                    AssetCls.ERA_KEY, emitted, false, false, false);

        }

        if (transactionCount > 0) {
            // подсчет наград с ПЕРЕВОДОВ
            for (Transaction transaction : getTransactions()) {
                if (transaction.assetFEE != null)
                    addAssetFee(transaction.getAsset(), transaction.assetFEE.a, transaction.assetFEE.b);

                if (transaction.assetsPacketFEE != null && !transaction.assetsPacketFEE.isEmpty()) {
                    Tuple2<BigDecimal, BigDecimal> rowTAX;
                    for (AssetCls asset : transaction.assetsPacketFEE.keySet()) {
                        rowTAX = transaction.assetsPacketFEE.get(asset);
                        addAssetFee(asset, rowTAX.a, rowTAX.b);
                    }
                }
            }
        }

        if (earnedAllAssets == null || earnedAllAssets.isEmpty())
            return;

        // FOR ASSETS
        Tuple2<BigDecimal, BigDecimal> earnedPair;
        for (AssetCls asset : earnedAllAssets.keySet()) {
            earnedPair = earnedAllAssets.get(asset);

            // учтем для форжера что он нафоржил
            if (earnedPair.a.signum() != 0) {
                this.creator.changeBalance(dcSet, asOrphan, false, asset.getKey(),
                        earnedPair.a, false, false, true);
                if (!asOrphan && this.txCalculated != null) {
                    this.txCalculated.add(new RCalculated(this.creator, asset.getKey(),
                            earnedPair.a, BlockChain.MESS_FORGING, Transaction.makeDBRef(this.heightBlock, 0), 0L));
                }
            }

            // учтем для эмитента что для него сгорело
            if (earnedPair.b.signum() != 0) {
                asset.getMaker().changeBalance(dcSet, asOrphan, false, asset.getKey(),
                        earnedPair.b, false, false, true);
                if (!asOrphan && this.txCalculated != null) {
                    this.txCalculated.add(new RCalculated(asset.getMaker(), asset.getKey(),
                            earnedPair.b, "Asset Total Burned", Transaction.makeDBRef(this.heightBlock, 0), 0L));
                }
            }

        }

    }

    public void setCOMPUbals(DCSet dcSet, int height) {

        // TEST COMPU ORPHANs
        HashMap bals = new HashMap();
        Collection<byte[]> keys = dcSet.getAssetBalanceMap().keySet();
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal totalNeg = BigDecimal.ZERO;
        ItemAssetBalanceMap map = dcSet.getAssetBalanceMap();
        for (byte[] key : keys) {
            if (ItemAssetBalanceMap.getAssetKeyFromKey(key) == 2l) {
                Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> ball =
                    map.get(key);

                bals.put(ItemAssetBalanceMap.getShortAccountFromKey(key), ball.a.b);
            }
        }
        totalCOMPUtest.put(height, bals);
    }


    public void compareCOMPUbals(DCSet dcSet, int heightParent, String mess) {

        HashMap parentBalanses = (HashMap) totalCOMPUtest.get(heightParent);
        if (parentBalanses != null) {
            Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> ball;
            BigDecimal ballParent;
            Collection<byte[]> keys = dcSet.getAssetBalanceMap().keySet();
            ItemAssetBalanceMap map = dcSet.getAssetBalanceMap();
            boolean error = false;
            for (byte[] key : keys) {
                if (ItemAssetBalanceMap.getAssetKeyFromKey(key) == 2l) {
                    ball = dcSet.getAssetBalanceMap().get(key);

                    ballParent = (BigDecimal) parentBalanses.get(ItemAssetBalanceMap.getShortAccountFromKey(key));
                    if (ballParent != null && ballParent.compareTo(ball.a.b) != 0
                            || ballParent == null && ball.a.b.signum() != 0) {
                        LOGGER.error(" WRONG COMPU orphan " + mess + " [" + (heightParent + 1) + "] for ADDR :" + ItemAssetBalanceMap.getShortAccountFromKey(key)
                                + " balParent : " + (ballParent == null ? "NULL" : ballParent.toPlainString())
                                + " ---> " + (ball == null ? "NULL" : ball.a.b.toPlainString())
                                + " == " + ball.a.b.subtract(ballParent == null ? BigDecimal.ZERO : ballParent));

                        error = true;
                    }
                }
            }
            if (error) {
                LOGGER.error(" WRONG COMPU orphan " + mess + " [" + (heightParent + 1) + "] "
                        + " totalFee: " + this.getTotalFee(dcSet)
                        + " bonusFee: " + this.getBonusFee());

                error = false;
            }
        }
    }

    /**
     * Вызывается только если идет обработка попутно
     *
     * @param cnt
     * @param dcSet
     * @throws Exception
     */
    // TODO - make it trownable
    public void process_after(Controller cnt, DCSet dcSet) throws Exception {

        //PROCESS FEE
        feeProcess(dcSet, false);

        //PROCESS ASSETS FEE
        assetsFeeProcess(dcSet, false);

        if (this.forgingInfoUpdate != null) {
            // обновить форжинговые данные - один раз для всех транзакций в блоке
            // Обрабатывает данные об измененных форжинговых балансах
            // Для каждого счета берем результирующее изменения по форжинговой инфо
            // и разом в тут блоке изменим
            // Так обходится неопределенность при откате - если несколько транзакций для одного счета
            // меняли инфо по форжингу

            for (Account account : this.forgingInfoUpdate) {

                Tuple3<Integer, Integer, Integer> privousForgingPoint = account.getLastForgingData(dcSet);
                int currentForgingBalance = account.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).intValue();
                if (privousForgingPoint == null) {
                    if (currentForgingBalance >= BlockChain.MIN_GENERATING_BALANCE) {
                        if (BlockChain.ERA_COMPU_ALL_UP) {
                            // запоминаем чтобы не было отказов в сборке блоков
                            account.setForgingData(dcSet, this.heightBlock - BlockChain.DEVELOP_FORGING_START,
                                    currentForgingBalance);
                        } else {
                            account.setForgingData(dcSet, this.heightBlock, currentForgingBalance);
                        }
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

        processTail(dcSet);

        if (heightBlock % BlockChain.MAX_ORPHAN == 0) {
            cnt.blockchainSyncStatusUpdate(heightBlock);
        }

        if (BlockChain.TEST_FEE_ORPHAN > 0 && BlockChain.TEST_FEE_ORPHAN > this.heightBlock) {
            setCOMPUbals(dcSet, this.heightBlock);
        }

        // MAKE CALCULATED TRANSACTIONS
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
                // if here ERROR in DB SERIALIZER - chek transaction in block before!
                finalMap.put(txCalculated);

            }
        }

    }

    /**
     * Начисление всем за участие в проекте
     */
    private void makeHoldRoyalty(DCSet dcSet, boolean asOrphan) {

        // ловим блок когда можно начислять
        if (BlockChain.HOLD_ROYALTY_PERIOD_DAYS <= 0
                || heightBlock % (BlockChain.BLOCKS_PER_DAY(heightBlock) * BlockChain.HOLD_ROYALTY_PERIOD_DAYS) != 0)
            return;

        // если сумма малая - не начисляем
        BigDecimal readyToRoyalty = BlockChain.FEE_ASSET_EMITTER.getBalance(dcSet, BlockChain.FEE_KEY, TransactionAmount.ACTION_DEBT).b.negate();
        if (readyToRoyalty.compareTo(BlockChain.HOLD_ROYALTY_MIN) < 0)
            return;

        ItemAssetBalanceMap map = dcSet.getAssetBalanceMap();
        AssetCls asset = dcSet.getItemAssetMap().get(BlockChain.HOLD_ROYALTY_ASSET);
        BigDecimal totalHold = asset.getReleased(dcSet);
        BigDecimal koeff = readyToRoyalty.divide(totalHold, BlockChain.FEE_SCALE + 5, RoundingMode.DOWN);
        BigDecimal totalPayedRoyalty = BigDecimal.ZERO;

        try (IteratorCloseable<byte[]> iterator = map.getIteratorByAsset(BlockChain.HOLD_ROYALTY_ASSET)) {
            BigDecimal balanceHold;
            Account holder;
            long txReference = Transaction.makeDBRef(heightBlock, 0);
            while (iterator.hasNext()) {
                byte[] key = iterator.next();
                holder = new Account(ItemAssetBalanceMap.getShortAccountFromKey(key));
                if (holder.equals(BlockChain.FEE_ASSET_EMITTER)
                        || holder.equals(asset.getMaker()))
                    continue;

                balanceHold = map.get(key).a.b;
                balanceHold = balanceHold.multiply(koeff).setScale(BlockChain.FEE_SCALE, RoundingMode.DOWN);

                if (balanceHold.signum() <= 0)
                    continue;

                holder.changeBalance(dcSet, asOrphan, false, BlockChain.FEE_KEY, balanceHold, false, false, false);
                // учтем что получили бонусы
                holder.changeCOMPUStatsBalances(dcSet, asOrphan, balanceHold, Account.FEE_BALANCE_SIDE_TOTAL_EARNED);

                // у эмитента снимем
                BlockChain.FEE_ASSET_EMITTER.changeBalance(dcSet, !asOrphan, false, BlockChain.FEE_KEY, balanceHold,
                        false, false, false);
                BlockChain.FEE_ASSET_EMITTER.changeCOMPUStatsBalances(dcSet, !asOrphan, balanceHold, Account.FEE_BALANCE_SIDE_TOTAL_EARNED);

                if (this.txCalculated != null) {
                    txCalculated.add(new RCalculated(holder, BlockChain.FEE_KEY, balanceHold,
                            "AS-stacking", txReference, 0L));
                }

                totalPayedRoyalty = totalPayedRoyalty.add(balanceHold);
            }

            // учтем снятие с начисления для держателей долей
            BlockChain.FEE_ASSET_EMITTER.changeBalance(dcSet, asOrphan, false, -BlockChain.FEE_KEY,
                    totalPayedRoyalty, false, false, true);

            if (this.txCalculated != null) {
                txCalculated.add(new RCalculated(BlockChain.FEE_ASSET_EMITTER, BlockChain.FEE_KEY, totalPayedRoyalty.negate(),
                        "AS-stacking OUT", txReference, 0L));
            }

        } catch (IOException e) {
            //e.printStackTrace();
        }

    }

    private void processTimed(DCSet dcSet) {

        // time wait process
        TimeTXWaitMap timeWaitMap = dcSet.getTimeTXWaitMap();
        TimeTXDoneMap timewDoneMap = dcSet.getTimeTXDoneMap();
        TransactionFinalMapImpl txMap = dcSet.getTransactionFinalMap();
        Transaction tx;
        Tuple2<Integer, Long> key;
        try (IteratorCloseable<Tuple2<Integer, Long>> iteraator = timeWaitMap.getTXIterator(false)) {
            while (iteraator.hasNext()) {
                key = iteraator.next();
                // reversed pair - key = <Block, dbRef>
                if (key.a > heightBlock)
                    break;

                timeWaitMap.remove(key.b);
                timewDoneMap.put(key.b, key.a);

                tx = txMap.get(key.b);
                tx.processByTime(this);

            }
        } catch (IOException e) {
            Controller.getInstance().stopAndExit(99999);
        }
    }

    /**
     * обработка всего что в конце блока прилипло
     *
     * @param dcSet
     */
    private void processTail(DCSet dcSet) {
        // clear old orders
        OrderProcess.clearOldOrders(dcSet, this, false);

    }

    // TODO - make it trownable
    public void process(DCSet dcSet) throws Exception {

        Controller cnt = Controller.getInstance();
        if (cnt.isOnStopping())
            throw new Exception("on stoping");

        long timerStart;
        long start = System.currentTimeMillis();

        //ADD TO DB

        if (BlockChain.TEST_FEE_ORPHAN > 0 && BlockChain.TEST_FEE_ORPHAN > this.heightBlock) {
            // TEST COMPU ORPHANs
            compareCOMPUbals(dcSet, this.heightBlock - 1, "before PROCESS");
        }

        //PROCESS TRANSACTIONS
        byte[] transactionSignature;

        // RESET forginf Info Updates
        this.forgingInfoUpdate = null;

        if (/// теперь нужно считать так как у нас из Форка слив напрямую идет dcSet.isFork() ||
                cnt.noCalculated) {
            this.txCalculated = null;
        } else {
            // make pool for calculated
            this.txCalculated = new ArrayList<RCalculated>();
        }

        makeHoldRoyalty(dcSet, false);
        DAPP.processByBlock(dcSet, this, false);

        this.getTransactions();

        if (this.transactionCount > 0) {

            TransactionMap unconfirmedMap = dcSet.getTransactionTab();
            TransactionFinalMapImpl finalMap = dcSet.getTransactionFinalMap();
            TransactionFinalMapSigns transFinalMapSigns = dcSet.getTransactionFinalMapSigns();

            long timerProcess = 0;
            long timerRefsMap_set = 0;
            long timerUnconfirmedMap_delete = 0;
            long timerFinalMap_set = 0;
            long timerTransFinalMapSinds_set = 0;

            // CLEAR ASSETS FEE
            earnedAllAssets = new HashMap<>();

            int seqNo = 0;
            for (Transaction transaction : this.transactions) {

                if (cnt.isOnStopping())
                    throw new Exception("on stoping");

                ++seqNo;

                //logger.debug("[" + seqNo + "] record is process" );

                // NEED set DC for WIPED too
                // здесь ще нет ничего в базе данных - нечего наращивать
                transaction.setDC(dcSet, Transaction.FOR_NETWORK, this.heightBlock, seqNo);

                //PROCESS
                if (transaction.isWiped()
                ) {
                    //UPDATE REFERENCE OF SENDER
                    transaction.getCreator().setLastTimestamp(
                            new long[]{transaction.getTimestamp(), transaction.getDBRef()}, dcSet);
                } else {
                    timerStart = System.currentTimeMillis();
                    transaction.process(this, Transaction.FOR_NETWORK);
                    timerProcess += System.currentTimeMillis() - timerStart;
                }

                transactionSignature = transaction.getSignature();

                //SET PARENT
                //REMOVE FROM UNCONFIRMED DATABASE
                timerStart = System.currentTimeMillis();
                try {
                    unconfirmedMap.delete(transactionSignature);
                } catch (java.lang.Throwable e) {
                    if (e instanceof java.lang.IllegalAccessError) {
                        // налетели на закрытую таблицу
                        unconfirmedMap = dcSet.getTransactionTab();
                    } else {
                        throw new Exception(e);
                    }
                }
                timerUnconfirmedMap_delete += System.currentTimeMillis() - timerStart;

                Long key = Transaction.makeDBRef(this.heightBlock, seqNo);

                if (cnt.isOnStopping())
                    throw new Exception("on stoping");

                timerStart = System.currentTimeMillis();
                // добавляем после процессинга, когда в транзакции новые данные наросли
                finalMap.put(key, transaction);
                timerFinalMap_set += System.currentTimeMillis() - timerStart;
                timerStart = System.currentTimeMillis();
                transFinalMapSigns.put(transactionSignature, key);
                List<byte[]> signatures = transaction.getOtherSignatures();
                if (signatures != null) {
                    for (byte[] itemSignature : signatures) {
                        transFinalMapSigns.put(itemSignature, key);
                    }
                }
                timerTransFinalMapSinds_set += System.currentTimeMillis() - timerStart;

            }

            LOGGER.debug("timerProcess: " + timerProcess + "  timerRefsMap_set: " + timerRefsMap_set
                    + "  timerUnconfirmedMap_delete: " + timerUnconfirmedMap_delete + "  timerFinalMap_set:" + timerFinalMap_set
                    + "  timerTransFinalMapSinds_set: " + timerTransFinalMapSinds_set);

        }

        processTimed(dcSet);

        timerStart = System.currentTimeMillis();
        this.process_after(cnt, dcSet);
        LOGGER.debug("BLOCK process_after: " + (System.currentTimeMillis() - timerStart) + " [" + this.heightBlock + "]");

        timerStart = System.currentTimeMillis();
        dcSet.getBlockMap().putAndProcess(this);
        LOGGER.debug("BlockMap add timer: " + (System.currentTimeMillis() - timerStart) + " [" + this.heightBlock + "]");

        long tickets = System.currentTimeMillis() - start;
        if (transactionCount > 0 && tickets > 10) {
            LOGGER.debug("[" + this.heightBlock + "] TOTAL processing time: " + tickets
                    + " ms, TXs= " + this.transactionCount
                    + (transactionCount == 0 ? "" : " - " + (this.transactionCount * 1000 / tickets) + " tx/sec"));
        }

    }

    /**
     * Обязательно надо это вызвать после того как удалили все Вычисленные транзакции - так как иначе удалени активов тут вызовет крах в них
     *
     * @param dcSet
     */
    private void orphanTimed(DCSet dcSet) {

        // time wait process
        TimeTXWaitMap timeWaitMap = dcSet.getTimeTXWaitMap();
        TimeTXDoneMap timeDoneMap = dcSet.getTimeTXDoneMap();
        TransactionFinalMapImpl txMap = dcSet.getTransactionFinalMap();
        Transaction tx;
        Tuple2<Integer, Long> key;
        try (IteratorCloseable<Tuple2<Integer, Long>> iteraator = timeDoneMap.getTXIterator(true)) {
            while (iteraator.hasNext()) {
                key = iteraator.next();
                // reversed pair - key = <Block, dbRef>
                if (key.a < heightBlock)
                    break;

                timeDoneMap.remove(key.b);
                timeWaitMap.put(key.b, key.a);

                tx = txMap.get(key.b);
                tx.orphanByTime(this);

            }
        } catch (IOException e) {
            Controller.getInstance().stopAndExit(99999);
        }

        // clear old orders
        OrderProcess.clearOldOrders(dcSet, this, true);

    }

    /**
     * обработка всего что в конце блока прилипло
     *
     * @param dcSet
     */
    private void orphanHead(DCSet dcSet) {

        // clear old orders
        OrderProcess.clearOldOrders(dcSet, this, true);

    }

    public void orphan(DCSet dcSet, boolean notStoreTXs) throws Exception {

        Controller cnt = Controller.getInstance();
        if (cnt.isOnStopping())
            throw new Exception("on stoping");

        if (this.heightBlock < 2) {
            // GENESIS BLOCK cannot be orphaned
            return;
        }

        if (BlockChain.TEST_FEE_ORPHAN > 0 && BlockChain.TEST_FEE_ORPHAN > this.heightBlock) {
            // TEST COMPU ORPHANs
            compareCOMPUbals(dcSet, heightBlock, "before ORPHAN");
        }

        long start = System.currentTimeMillis();

        //REMOVE FEE
        orphanHead(dcSet);

        //REMOVE FEE
        feeProcess(dcSet, true);

        /////// ORPHAN TRANSACTIONS

        // CLEAR ASSETS FEE
        earnedAllAssets = new HashMap<>();

        this.orphanTransactions(dcSet, heightBlock, notStoreTXs);

        //PROCESS ASSETS FEE - after orphanTransactions! - так как тут они будут вычислены
        assetsFeeProcess(dcSet, true);

        DAPP.processByBlock(dcSet, this, true);
        makeHoldRoyalty(dcSet, true);

        if (this.forgingInfoUpdate != null) {
            // обновить форжинговые данные - один раз для всех трнзакций в блоке
            // Обрабатывает данные об измененных форжинговых балансах
            // Для каждого счета берем результирующее изменения по форжинговой инфо
            // и разом в тут блоке изменим
            // Так обходится неопределенность при откате - если несколько транзакций для одного счета
            // меняли инфо по форжингу
            for (Account account : this.forgingInfoUpdate) {
                if (!this.getCreator().equals(account)) {
                    // если этот блок не собирался этим человеком
                    Tuple3<Integer, Integer, Integer> lastForgingPoint = account.getLastForgingData(dcSet);
                    if (true // теперь можно удалять полностью - внутри идет проверка
                            || lastForgingPoint != null && lastForgingPoint.a == heightBlock
                            && !this.getCreator().equals(account)) {
                        account.delForgingData(dcSet, heightBlock);
                    }
                }
            }
        }

        // RESET forging Info Updates
        this.forgingInfoUpdate = null;

        //DELETE BLOCK FROM DB
        dcSet.getBlockMap().deleteAndProcess(this.signature, this.reference, this.creator, this.heightBlock);

        long tickets = System.currentTimeMillis() - start;
        LOGGER.debug("[" + this.heightBlock + "] orphaning time: " + (System.currentTimeMillis() - start) * 0.001
                + " TXs = " + this.getTransactionCount() + " millsec/record:" + tickets / (this.getTransactionCount() + 1));

        byte[] lastSignature = dcSet.getBlockMap().getLastBlockSignature();
        if (!Arrays.equals(lastSignature, this.reference)) {
            LOGGER.debug("[" + this.heightBlock + "] orphaning time: " + (System.currentTimeMillis() - start) * 0.001
                    + "  ERROR ");

        }

        if (BlockChain.TEST_FEE_ORPHAN > 0 && BlockChain.TEST_FEE_ORPHAN > this.heightBlock) {
            // TEST COMPU ORPHANs
            compareCOMPUbals(dcSet, heightBlock - 1, "after ORPHAN");
        }

    }

    public void orphan(DCSet dcSet) throws Exception {
        orphan(dcSet, false);
    }

    /**
     * ORPHAN ALL TRANSACTIONS IN DB BACK TO FRONT
     *
     * @param dcSet
     * @param height
     * @param notStoreTXs
     * @throws Exception
     */
    private void orphanTransactions(DCSet dcSet, int height, boolean notStoreTXs) throws Exception {

        Controller cnt = Controller.getInstance();

        TransactionFinalMapImpl finalMap = dcSet.getTransactionFinalMap();
        TransactionFinalMapSigns transFinalMapSinds = dcSet.getTransactionFinalMapSigns();
        TransactionsPool pool = Controller.getInstance().transactionsPool;

        boolean calculatedBefore = true;

        // DELETE ALL BY DB ITERATOR
        // It delete all CALCULATED FIRST correct and all txs too
        /// если форк их тут вообще нету - нужно выцепить из Родительской таблицы
        Long dbRef;
        try (IteratorCloseable<Long> iterator = finalMap.getOneBlockIterator(height, true)) {
            while (iterator.hasNext()) {
                if (cnt.isOnStopping()) {
                    throw new Exception("on stoping");
                }

                dbRef = iterator.next();

                // внутри уже есть setDC
                Transaction transaction = finalMap.get(dbRef);

                if (!(transaction instanceof RCalculated)) {

                    if (calculatedBefore) {
                        calculatedBefore = false;
                        // если перед этим были только вычисляемые и начались обычные, то сперва все запуски по времени откатим
                        orphanTimed(dcSet);
                    }

                    if (!transaction.isWiped()) {
                        transaction.orphan(this, Transaction.FOR_NETWORK);
                    } else {
                        // IT IS REFERENCED RECORD?
                        transaction.getCreator().removeLastTimestamp(dcSet, transaction.getTimestamp());
                    }

                    transFinalMapSinds.delete(transaction.getSignature());

                    // Обязательно надо делать иначе некоторые транзакции будут потом невалидны (удостоверение ключей и регистрация подписанной персоны)
                    List<byte[]> signatures = transaction.getOtherSignatures();
                    if (signatures != null) {
                        for (byte[] itemSignature : signatures) {
                            transFinalMapSinds.delete(itemSignature);
                        }
                    }

                    // сбросим данные блока - для правильного отображения неподтвержденных
                    transaction.resetSeqNo();

                    if (!notStoreTXs) {
                        pool.offerMessage(transaction);
                    }

                }

                // полное удаление включая Поисковые Метки tags и Вычисленные
                finalMap.delete(dbRef);
            }
        }

        if (calculatedBefore) {
            // если не было транзакций обычных вообще, только вычисляемые, то теперь откатим запуски по времени
            orphanTimed(dcSet);
        }

    }

    @Override
    public String toString() {
        return "[" + this.getHeight() + "]"
                + (this.winValue != 0 ? " WV: " + this.winValue : "")
                + " TX: " + this.transactionCount
                + " CR:" + this.getCreator().getPersonAsString();
    }
}

