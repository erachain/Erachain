package org.erachain.at;


import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public abstract class ATController {


    private static final Logger LOGGER = LoggerFactory.getLogger(ATController.class);

    public static int runSteps(ATMachineState state, int blockHeight) {
        state.getMachineState().stopped = false;
        state.getMachineState().finished = false;
        state.getMachineState().steps = 0;
        state.getMachineState().dead = false;

        ATMachineProcessor processor = new ATMachineProcessor(state);

        int height = blockHeight;

        state.setFreeze(false);

        long stepFee = ATConstants.getInstance().STEP_FEE(height);

        int numSteps = 0;

        while (state.getMachineState().steps +
                (numSteps = getNumSteps(state.getAp_code().get(state.getMachineState().pc), height))
                <= ATConstants.getInstance().MAX_STEPS(height)) {
            if ((state.getG_balance() < stepFee * numSteps)) {
                state.setFreeze(true);
                return 3;
            }

            state.setG_balance(state.getG_balance() - (stepFee * numSteps));
            state.getMachineState().steps += numSteps;
            int rc = processor.processOp(false, false);

            if (rc >= 0) {
                if (state.getMachineState().stopped) {
                    LOGGER.trace("stopped");
                    state.getMachineState().running = false;
                    return 2;
                } else if (state.getMachineState().finished) {
                    LOGGER.trace("finished");
                    state.getMachineState().running = false;
                    return 1;
                }
            } else {
                if (rc == -1)
                    LOGGER.trace("error: overflow");
                else if (rc == -2)
                    LOGGER.trace("error: invalid code");
                else
                    LOGGER.trace("unexpected error");

                if (state.getMachineState().jumps.contains(state.getMachineState().err)) {
                    state.getMachineState().pc = state.getMachineState().err;
                } else {
                    state.getMachineState().dead = true;
                    state.getMachineState().running = false;
                    return 0;
                }
            }
        }
        return 5;
    }

    public static int getNumSteps(byte op, int height) {
        if (op >= 0x32 && op < 0x38)
            return (int) ATConstants.getInstance().API_STEP_MULTIPLIER(height);

        return 1;
    }

    public static void resetMachine(ATMachineState state) {
        state.getMachineState().reset();
        listCode(state, true, true);
    }

    public static void listCode(ATMachineState state, boolean disassembly, boolean determine_jumps) {

        ATMachineProcessor machineProcessor = new ATMachineProcessor(state);

        int opc = state.getMachineState().pc;
        int osteps = state.getMachineState().steps;

        state.getAp_code().order(ByteOrder.LITTLE_ENDIAN);
        state.getAp_data().order(ByteOrder.LITTLE_ENDIAN);

        state.getMachineState().pc = 0;
        state.getMachineState().opc = opc;

        while (true) {
            int rc = machineProcessor.processOp(disassembly, determine_jumps);
            if (rc <= 0) break;

            state.getMachineState().pc += rc;
        }

        state.getMachineState().steps = osteps;
        state.getMachineState().pc = opc;
    }


    public static int checkCreationBytes(byte[] creation, String type, long fee, int height, int forkHeight, DCSet db) throws ATException {
        if (creation == null)
            throw new ATException("Creation bytes cannot be null");

        try {
            ByteBuffer b = ByteBuffer.allocate(creation.length);
            b.order(ByteOrder.LITTLE_ENDIAN);

            b.put(creation);
            b.clear();

            ATConstants instance = ATConstants.getInstance();

            short version = b.getShort();
            if (version != instance.AT_VERSION(height)) {
                return ATError.INCORRECT_VERSION.getCode();
            }

            @SuppressWarnings("unused") //needed for AT don't delete
                    short reserved = b.getShort(); //future: reserved for future needs

            short codePages = b.getShort();
            if (codePages > instance.MAX_MACHINE_CODE_PAGES(height) || codePages < 1) {
                return ATError.INCORRECT_CODE_PAGES.getCode();
            }

            short dataPages = b.getShort();
            if (dataPages > instance.MAX_MACHINE_DATA_PAGES(height) || dataPages < 0) {
                return ATError.INCORRECT_DATA_PAGES.getCode();
            }

            short callStackPages = b.getShort();
            if (callStackPages > instance.MAX_MACHINE_CALL_STACK_PAGES(height) || callStackPages < 0) {
                return ATError.INCORRECT_CALL_PAGES.getCode();
            }

            short userStackPages = b.getShort();
            if (userStackPages > instance.MAX_MACHINE_USER_STACK_PAGES(height) || userStackPages < 0) {
                return ATError.INCORRECT_USER_PAGES.getCode();
            }

            @SuppressWarnings("unused") //needed for AT don't delete
                    long minActivationAmount = b.getLong();

            int codeLen;
            if (codePages * 256 < 257) {
                codeLen = b.get() & 0xff;
            } else if (codePages * 256 < Short.MAX_VALUE + 1) {
                codeLen = b.getShort() & 0xffff;
            } else if (codePages * 256 <= Integer.MAX_VALUE) {
                codeLen = b.getInt();
            } else {
                return ATError.INCORRECT_CODE_LENGTH.getCode();
            }
            if (codeLen < 1) {
                return ATError.INCORRECT_CODE_LENGTH.getCode();
            }
            byte[] code = new byte[codeLen];
            b.get(code, 0, codeLen);

            byte[] digestedCode = Crypto.getInstance().digest(Bytes.ensureCapacity(code, codePages * 256, 0));

            if (!db.getATMap().validTypeHash(digestedCode, type, forkHeight)) {
                return ATError.INCORRECT_TYPE.getCode();
            }

            int dataLen;
            if (dataPages * 256 < 257) {
                dataLen = b.get() & 0xff;
            } else if (dataPages * 256 < Short.MAX_VALUE + 1) {
                dataLen = b.getShort() & 0xffff;
            } else if (dataPages * 256 <= Integer.MAX_VALUE) {
                dataLen = b.getInt();
            } else {
                return ATError.INCORRECT_CODE_LENGTH.getCode();
            }
            if (dataLen < 0 || dataLen > dataPages * 256) {
                return ATError.INCORRECT_DATA_LENGTH.getCode();
            }
            byte[] data = new byte[dataLen];
            b.get(data, 0, dataLen);

            int totalPages = codePages + dataPages + userStackPages + callStackPages;

            if (totalPages * ATConstants.getInstance().COST_PER_PAGE(height) > fee) {
                return ATError.INCORRECT_CREATION_FEE.getCode();
            }

            if (b.position() != b.capacity()) {
                return ATError.INCORRECT_CREATION_TX.getCode();
            }

            //TODO template: run code in demo mode for checking if is valid

        } catch (BufferUnderflowException e) {
            throw new ATException(ATError.INCORRECT_CREATION_TX.getDescription());
        }
        return 0;
    }

    public static ATBlock getCurrentBlockATs(int freePayload, int blockHeight) {

        //HashMap<String, byte[]> states = new HashMap<String, byte[]>();

        DCSet fork = DCSet.getInstance();

        Iterator<String> orderedATs = AT.getOrderedATs(fork, blockHeight);

        List<AT> processedATs = new ArrayList<>();

        int costOfOneAT = getCostOfOneAT();
        int payload = 0;
        while (payload <= freePayload - costOfOneAT && orderedATs.hasNext()) {
            AT at = AT.getAT(orderedATs.next(), fork);

            long atAccountBalance = getATAccountBalance(at.getId());
            long atStateBalance = at.getG_balance();

            if (at.freezeOnSameBalance() && atAccountBalance - atStateBalance < at.minActivationAmount()) {
                continue;
            }

            if (atAccountBalance >= ATConstants.getInstance().STEP_FEE(blockHeight) * ATConstants.getInstance().API_STEP_MULTIPLIER(blockHeight)) {
                try {
                    at.setG_balance(atAccountBalance);
                    at.setHeight(blockHeight);
                    at.clearTransactions();
                    at.setWaitForNumberOfBlocks(at.getSleepBetween());
                    listCode(at, true, true);

                    ATAPIPlatformImpl.getInstance().setDBSet(fork);
                    runSteps(at, blockHeight);

                    long fee = at.getMachineState().steps * ATConstants.getInstance().STEP_FEE(blockHeight);
                    if (at.getMachineState().dead) {
                        fee += at.getG_balance();
                        at.setG_balance(0L);
                    }

                    ATTransaction feeTx = new ATTransaction(at.getId(), new byte[ATConstants.AT_ID_SIZE], Transaction.FEE_KEY, fee, null);
                    at.addTransaction(feeTx);

                    payload += costOfOneAT;

                    at.setP_balance(at.getG_balance());
                    processedATs.add(at);

                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);

                }
            }
        }

        long totalAmount = 0;
        long totalFee = 0;
        int seq = 0;
        for (AT at : processedATs) {
            Tuple2<Long, Long> results = makeTransactions(at, fork, blockHeight, seq, false);
            totalAmount += results.a;
            totalFee += results.b;
            seq += at.getTransactions().size();
        }

        byte[] bytesForBlock = null;

        try {
            bytesForBlock = getBlockATBytes(processedATs, payload);
        } catch (NoSuchAlgorithmException e) {
            //should not reach ever here
            LOGGER.error(e.getMessage(), e);
        }


        ATBlock atBlock = new ATBlock(totalFee, totalAmount, bytesForBlock);

        return atBlock;
    }

    public static ATBlock validateATs(byte[] blockATs, int blockHeight, DCSet dcSet) throws NoSuchAlgorithmException, ATException {

        LOGGER.trace("Validate ATs");
        if (blockATs == null) {
            return new ATBlock(0, 0, null, true);
        }

        LinkedHashMap<ByteBuffer, byte[]> ats = getATsFromBlock(blockATs);

        List<AT> processedATs = new ArrayList<>();

        HashMap<String, byte[]> tempAtStates = new HashMap<String, byte[]>();
        //HashMap< String, Long > atFees = new HashMap< String, Long >();

        boolean validated = true;

        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] md5 = null;
        for (ByteBuffer atIdBuffer : ats.keySet()) {
            byte[] atId = atIdBuffer.array();
            AT at = AT.getAT(atId, dcSet);

            byte[] state = at.getState().clone();

            try {
                at.clearTransactions();
                at.setHeight(blockHeight);
                at.setWaitForNumberOfBlocks(at.getSleepBetween());

                long atAccountBalance = getATAccountBalance(atId, dcSet);

                if (atAccountBalance < ATConstants.getInstance().STEP_FEE(blockHeight)) {
                    throw new ATException("AT has insufficient balance to run");
                }
                if (at.freezeOnSameBalance() && atAccountBalance == at.getG_balance()) {
                    throw new ATException("AT should be frozen due to unchanged balance");
                }

                at.setG_balance(atAccountBalance);

                listCode(at, true, true);

                ATAPIPlatformImpl.getInstance().setDBSet(dcSet);

                runSteps(at, blockHeight);

                long fee = at.getMachineState().steps * ATConstants.getInstance().STEP_FEE(blockHeight);
                if (at.getMachineState().dead) {
                    fee += at.getG_balance();
                    at.setG_balance(0L);
                }

                //atFees.put( Base58.encode( atId ) , fee );

                ATTransaction feeTx = new ATTransaction(atId, new byte[ATConstants.AT_ID_SIZE], Transaction.FEE_KEY, fee, null);
                at.addTransaction(feeTx);

                //totalFee += fee;

                at.setP_balance(at.getG_balance());
                processedATs.add(at);

                md5 = digest.digest(at.getBytes());
                if (!Arrays.equals(md5, ats.get(atIdBuffer))) {
                    throw new ATException("Calculated md5 and recieved md5 are not matching");
                }
                tempAtStates.put(new String(at.getId(), "UTF-8"), state);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw new ATException("ATs error. Block rejected");
            }
        }

        long totalAmount = 0;
        long totalFee = 0;
        int seq = 0;
        for (AT at : processedATs) {
            String atId = Base58.encode(at.getId());
            Account account = new Account(atId);
            LOGGER.trace("AT : " + account.getAddress() + " total balance: " + account.getBalance(dcSet, Transaction.FEE_KEY));
            //atLastState.put( atId ,  tempAtStates.get( atId ) );
            dcSet.getATMap().update(at, blockHeight);
            dcSet.getATStateMap().addOrUpdate(blockHeight, at.getId(), at.getState());

            Tuple2<Long, Long> results = makeTransactions(at, dcSet, blockHeight, seq, true);
            totalAmount += results.a;
            totalFee += results.b;
            seq += at.getTransactions().size();
        }
        ATBlock atBlock = new ATBlock(totalFee, totalAmount, new byte[1], validated);

        return atBlock;
    }

    public static LinkedHashMap<ByteBuffer, byte[]> getATsFromBlock(byte[] blockATs) throws ATException {
        if (blockATs.length > 0) {
            if (blockATs.length % (getCostOfOneAT()) != 0) {
                throw new ATException("blockATs must be a multiple of cost of one AT ( " + getCostOfOneAT() + " )");
            }
        }

        ByteBuffer b = ByteBuffer.wrap(blockATs);
        b.order(ByteOrder.LITTLE_ENDIAN);

        byte[] temp = new byte[ATConstants.AT_ID_SIZE];


        LinkedHashMap<ByteBuffer, byte[]> ats = new LinkedHashMap<>();

        while (b.position() < b.capacity()) {
            b.get(temp, 0, temp.length);
            byte[] md5 = new byte[16];
            b.get(md5, 0, md5.length);
            ByteBuffer atId = ByteBuffer.allocate(ATConstants.AT_ID_SIZE);
            atId.put(temp);
            atId.clear();
            if (ats.containsKey(atId)) {
                throw new ATException("AT included in block multiple times");
            }
            ats.put(atId, md5);
        }

        if (b.position() != b.capacity()) {
            throw new ATException("bytebuffer not matching");
        }

        return ats;
    }

    private static byte[] getBlockATBytes(List<AT> processedATs, int payload) throws NoSuchAlgorithmException {

        if (payload <= 0) {
            return null;
        }

        ByteBuffer b = ByteBuffer.allocate(payload);
        b.order(ByteOrder.LITTLE_ENDIAN);

        MessageDigest digest = MessageDigest.getInstance("MD5");
        for (AT at : processedATs) {
            b.put(at.getId());
            digest.update(at.getBytes());
            b.put(digest.digest());
        }

        return b.array();
    }

    private static int getCostOfOneAT() {
        return ATConstants.AT_ID_SIZE + 16;
    }

    //platform based implementations
    //platform based
    private static Tuple2<Long, Long> makeTransactions(AT at, DCSet dcSet, int height, int seq, boolean yes) {
        long totalAmount = 0;
        long totalFees = 0;
        Account sender = new Account(Base58.encode(at.getId()));
        for (ATTransaction tx : at.getTransactions()) {
            if (tx.getRecipientId() != null && !Arrays.equals(tx.getRecipientId(), new byte[ATConstants.AT_ID_SIZE])) {
                totalAmount += tx.getAmount();
            }
            if (yes) {
                dcSet.getATTransactionMap().add(height, seq++, tx);
                if (tx.getRecipientId() != null && !Arrays.equals(tx.getRecipientId(), new byte[ATConstants.AT_ID_SIZE])) {
                    Account recipient = new Account(Base58.encode(tx.getRecipientId()));
                    if (false && recipient.getLastTimestamp(dcSet) == null) {
                        recipient.setLastTimestamp(new long[]{0L, 0L}, dcSet);
                    }
                    //recipient.setBalance( Transaction.FEE_KEY, recipient.getBalance( dcSet, Transaction.FEE_KEY ).add( BigDecimal.valueOf( tx.getAmount()) ) , dcSet );
                } else {
                    totalFees += tx.getAmount();
                }
                //sender.setBalance( Transaction.FEE_KEY, sender.getBalance( dcSet, Transaction.FEE_KEY ).subtract( BigDecimal.valueOf( tx.getAmount() ) ) , dcSet );
                sender.changeBalance(dcSet, true, false, Transaction.FEE_KEY, BigDecimal.valueOf(tx.getAmount()), false, false, false, false);
                LOGGER.trace("Sender:" + sender.getAddress() + " total balance:" + sender.getBalance(dcSet, Transaction.FEE_KEY));
            }

        }
        return new Tuple2<Long, Long>(totalAmount, totalFees);
    }


    private static long getATAccountBalance(byte[] id) {
        return getATAccountBalance(id, DCSet.getInstance());
    }

    private static long getATAccountBalance(byte[] id, DCSet dcSet) {
        Account account = new Account(Base58.encode(id));

        BigDecimal balance = account.getBalance(dcSet, Transaction.FEE_KEY).a.b;

        byte[] balanceBytes = balance.unscaledValue().toByteArray();
        byte[] fill = new byte[8 - balanceBytes.length];
        balanceBytes = Bytes.concat(fill, balanceBytes);

        return Longs.fromByteArray(balanceBytes);
    }


}
