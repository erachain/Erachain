/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.

 */

package org.erachain.at;

import org.erachain.core.crypto.Base58;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeSet;


public class ATMachineState {

    private short version;
    private long g_balance;
    private long p_balance;
    private Machine_State machineState;
    private int csize;
    private int dsize;
    private int c_user_stack_bytes;
    private int c_call_stack_bytes;
    private byte[] atID = new byte[ATConstants.AT_ID_SIZE];
    private byte[] creator = new byte[ATConstants.AT_ID_SIZE];
    private int creationBlockHeight;
    private int waitForNumberOfBlocks;
    private int sleepBetween;
    private boolean freezeWhenSameBalance;
    private long minActivationAmount;
    private transient ByteBuffer ap_data;
    private transient ByteBuffer ap_code;
    private int height;
    private LinkedList<ATTransaction> transactions;

    public ATMachineState(byte[] atId, byte[] creator, short version,
                          byte[] stateBytes, int csize, int dsize, int c_user_stack_bytes, int c_call_stack_bytes,
                          int creationBlockHeight, int sleepBetween,
                          long minActivationAmount, byte[] apCode) {
        this.atID = atId;
        this.creator = creator;
        this.version = version;
        this.machineState = new Machine_State();
        this.setState(stateBytes);
        this.csize = csize;
        this.dsize = dsize;
        this.c_user_stack_bytes = c_user_stack_bytes;
        this.c_call_stack_bytes = c_call_stack_bytes;
        this.creationBlockHeight = creationBlockHeight;
        this.sleepBetween = sleepBetween;
        this.minActivationAmount = minActivationAmount;
        //this.height = height;

        this.ap_code = ByteBuffer.allocate(apCode.length);
        ap_code.order(ByteOrder.LITTLE_ENDIAN);
        ap_code.put(apCode);
        ap_code.clear();

        transactions = new LinkedList<ATTransaction>();


    }

    public ATMachineState(byte[] atId, byte[] creator, byte[] creationBytes, int height) {
        this.version = ATConstants.getInstance().AT_VERSION(height);
        this.atID = atId;
        this.creator = creator;

        ByteBuffer b = ByteBuffer.allocate(creationBytes.length);
        b.order(ByteOrder.LITTLE_ENDIAN);

        b.put(creationBytes);
        b.clear();

        this.version = b.getShort();

        b.getShort(); //future: reserved for future needs

        int pageSize = (int) ATConstants.getInstance().PAGE_SIZE(height);
        short codePages = b.getShort();
        short dataPages = b.getShort();
        short callStackPages = b.getShort();
        short userStackPages = b.getShort();

        this.csize = codePages * pageSize;
        this.dsize = dataPages * pageSize;
        this.c_call_stack_bytes = callStackPages * pageSize;
        this.c_user_stack_bytes = userStackPages * pageSize;

        this.minActivationAmount = b.getLong();

        int codeLen = 0;
        if (codePages * pageSize < pageSize + 1) {
            codeLen = b.get();
            if (codeLen < 0)
                codeLen += (Byte.MAX_VALUE + 1) * 2;
        } else if (codePages * pageSize < Short.MAX_VALUE + 1) {
            codeLen = b.getShort();
            if (codeLen < 0)
                codeLen += (Short.MAX_VALUE + 1) * 2;
        } else {
            codeLen = b.getInt();
        }
        byte[] code = new byte[codeLen];
        b.get(code, 0, codeLen);

        this.ap_code = ByteBuffer.allocate(csize);
        this.ap_code.order(ByteOrder.LITTLE_ENDIAN);
        this.ap_code.put(code);
        this.ap_code.clear();

        int dataLen = 0;
        if (dataPages * pageSize < 257) {
            dataLen = b.get();
            if (dataLen < 0)
                dataLen += (Byte.MAX_VALUE + 1) * 2;
        } else if (dataPages * pageSize < Short.MAX_VALUE + 1) {
            dataLen = b.getShort();
            if (dataLen < 0)
                dataLen += (Short.MAX_VALUE + 1) * 2;
        } else {
            dataLen = b.getInt();
        }
        byte[] data = new byte[dataLen];
        b.get(data, 0, dataLen);

        this.ap_data = ByteBuffer.allocate(this.dsize + this.c_call_stack_bytes + this.c_user_stack_bytes);
        this.ap_data.order(ByteOrder.LITTLE_ENDIAN);
        this.ap_data.put(data);
        this.ap_data.clear();

        this.creationBlockHeight = height;
        this.waitForNumberOfBlocks = 0;
        this.sleepBetween = 0;
        this.freezeWhenSameBalance = false;
        this.transactions = new LinkedList<>();
        this.g_balance = 0;
        this.p_balance = 0;
        this.height = height;
        this.machineState = new Machine_State();
    }

    @SuppressWarnings("unchecked")
    public JSONObject getStateJSON() {
        JSONObject json = new JSONObject();
        json.put("machineCode", Base58.encode(ap_code.array())); //Converter.toHex(ap_code.array()));
        json.put("machineData", Base58.encode(ap_data.array())); //Converter.toHex(ap_data.array()));
        json.put("currentBalance", BigDecimal.valueOf(getG_balance()).toPlainString());
        json.put("prevBalance", BigDecimal.valueOf(getP_balance()).toPlainString());
        json.put("frozen", freezeOnSameBalance());
        json.put("running", getMachineState().isRunning());
        json.put("stopped", getMachineState().isStopped());
        json.put("finished", getMachineState().isFinished());
        json.put("dead", getMachineState().isDead());

        return json;

    }

    protected byte[] get_A1() {
        return machineState.A1;
    }

    protected void set_A1(byte[] A1) {
        this.machineState.A1 = A1.clone();
    }

    protected byte[] get_A2() {
        return machineState.A2;
    }

    protected void set_A2(byte[] A2) {
        this.machineState.A2 = A2.clone();
    }

    protected byte[] get_A3() {
        return machineState.A3;
    }

    protected void set_A3(byte[] A3) {
        this.machineState.A3 = A3.clone();
    }

    protected byte[] get_A4() {
        return machineState.A4;
    }

    protected void set_A4(byte[] A4) {
        this.machineState.A4 = A4.clone();
    }

    protected byte[] get_B1() {
        return machineState.B1;
    }

    protected void set_B1(byte[] B1) {
        this.machineState.B1 = B1.clone();
    }

    protected byte[] get_B2() {
        return machineState.B2;
    }

    protected void set_B2(byte[] B2) {
        this.machineState.B2 = B2.clone();
    }

    protected byte[] get_B3() {
        return machineState.B3;
    }

    protected void set_B3(byte[] B3) {
        this.machineState.B3 = B3.clone();
    }

    protected byte[] get_B4() {
        return machineState.B4;
    }

    protected void set_B4(byte[] B4) {
        this.machineState.B4 = B4.clone();
    }

    protected void addTransaction(ATTransaction tx) {
        if (tx != null)
            transactions.add(tx);
    }

    protected void clearTransactions() {
        transactions.clear();
    }

    public Collection<ATTransaction> getTransactions() {
        return transactions;
    }

    protected ByteBuffer getAp_code() {
        return ap_code;
    }

    public ByteBuffer getAp_data() {
        return ap_data;
    }

    public byte[] getAp_Code() {
        return ap_code.array().clone();
    }

    protected int getC_call_stack_bytes() {
        return c_call_stack_bytes;
    }

    protected void setC_call_stack_bytes(int c_call_stack_bytes) {
        this.c_call_stack_bytes = c_call_stack_bytes;
    }

    protected int getC_user_stack_bytes() {
        return c_user_stack_bytes;
    }

    protected void setC_user_stack_bytes(int c_user_stack_bytes) {
        this.c_user_stack_bytes = c_user_stack_bytes;
    }

    protected int getCsize() {
        return csize;
    }

    protected void setCsize(int csize) {
        this.csize = csize;
    }

    protected int getDsize() {
        return dsize;
    }

    protected void setDsize(int dsize) {
        this.dsize = dsize;
    }

    public Long getG_balance() {
        return g_balance;
    }

    protected void setG_balance(Long g_balance) {
        this.g_balance = g_balance;
    }

    public Long getP_balance() {
        return p_balance;
    }

    protected void setP_balance(Long p_balance) {
        this.p_balance = p_balance;
    }

    public byte[] getId() {
        return atID;
    }

    public Machine_State getMachineState() {
        return machineState;
    }

    protected void setMachineState(Machine_State machineState) {
        this.machineState = machineState;
    }

    public int getWaitForNumberOfBlocks() {
        return this.waitForNumberOfBlocks;
    }

    protected void setWaitForNumberOfBlocks(int waitForNumberOfBlocks) {
        this.waitForNumberOfBlocks = waitForNumberOfBlocks;
    }

    public byte[] getCreator() {
        return this.creator;
    }

    public int getCreationBlockHeight() {
        return this.creationBlockHeight;
    }

    public boolean freezeOnSameBalance() {
        return this.freezeWhenSameBalance;
    }

    public long minActivationAmount() {
        return this.minActivationAmount;
    }

    protected void setMinActivationAmount(long minActivationAmount) {
        this.minActivationAmount = minActivationAmount;
    }

    public short getVersion() {
        return version;
    }

    protected int getSleepBetween() {
        return sleepBetween;
    }

    public int getHeight() {
        return height;
    }

    protected void setHeight(int height) {
        this.height = height;
    }

    public byte[] getState() {
        byte[] stateBytes = machineState.getMachineStateBytes();
        byte[] dataBytes = ap_data.array();

        ByteBuffer b = ByteBuffer.allocate(getStateSize());
        b.order(ByteOrder.LITTLE_ENDIAN);

        b.put(stateBytes);
        b.putLong(g_balance);
        b.putLong(p_balance);
        b.putInt(height);
        b.putInt(waitForNumberOfBlocks);
        b.put((byte) ((freezeWhenSameBalance) ? 1 : 0));
        b.put(dataBytes);

        return b.array();
    }

    public void setState(byte[] state) {
        ByteBuffer b = ByteBuffer.allocate(state.length);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.put(state);
        b.flip();

        int stateSize = this.machineState.getSize();
        byte[] machineState = new byte[stateSize];
        b.get(machineState, 0, stateSize);
        this.machineState.setMachineState(machineState);

        g_balance = b.getLong();
        p_balance = b.getLong();
        height = b.getInt();
        waitForNumberOfBlocks = b.getInt();

        freezeWhenSameBalance = (b.get() == 0) ? false : true;

        byte[] apData = new byte[b.capacity() - b.position()];
        b.get(apData);
        ap_data = ByteBuffer.allocate(apData.length);
        ap_data.order(ByteOrder.LITTLE_ENDIAN);
        ap_data.put(apData);
        ap_data.clear();

    }

    protected int getStateSize() {
        return (this.machineState.getSize() + 8 + 8 + 4 + 4 + 1 + ap_data.capacity());
    }

    protected byte[] getBytes() {
        byte[] state = getState();

        ByteBuffer bf = ByteBuffer.allocate(getSize());
        bf.order(ByteOrder.LITTLE_ENDIAN);

        bf.put(atID);
        bf.put(creator);
        bf.putShort(version);
        bf.putInt(csize);
        bf.putInt(dsize);
        bf.putInt(c_call_stack_bytes);
        bf.putInt(c_user_stack_bytes);
        bf.putLong(minActivationAmount);
        bf.putInt(creationBlockHeight);
        bf.putInt(sleepBetween);
        //bf.put( (byte) (( freezeWhenSameBalance ) ? 1 : 0) );
        bf.put(ap_code.array());
        bf.put(state);

        return bf.array();

    }

    protected int getSize() {
        return getStateSize() + atID.length + creator.length + 2 + 4 + 4 + 4 + 4 + 8 + 4 + 4 + ap_code.capacity();
    }

    protected void setFreeze(boolean freeze) {
        this.freezeWhenSameBalance = freeze;
    }

    public class Machine_State {
        transient boolean running;
        transient boolean stopped;
        transient boolean finished;
        transient boolean dead;

        int pc;
        int pcs;

        transient int opc;

        int cs;
        int us;

        int err;

        int steps;
        byte[] flags = new byte[2];
        TreeSet<Integer> jumps = new TreeSet<Integer>();
        private byte[] A1 = new byte[8];
        private byte[] A2 = new byte[8];
        private byte[] A3 = new byte[8];
        private byte[] A4 = new byte[8];
        private byte[] B1 = new byte[8];
        private byte[] B2 = new byte[8];
        private byte[] B3 = new byte[8];
        private byte[] B4 = new byte[8];

        Machine_State() {
            pcs = 0;
            reset();
        }

        boolean isRunning() {
            return running;
        }

        public boolean isStopped() {
            return stopped;
        }

        public boolean isFinished() {
            return finished;
        }

        public boolean isDead() {
            return dead;
        }

        void reset() {
            pc = pcs;
            opc = 0;
            cs = 0;
            us = 0;
            err = -1;
            steps = 0;
            if (!jumps.isEmpty())
                jumps.clear();
            flags[0] = 0;
            flags[1] = 0;
            running = false;
            stopped = true;
            finished = false;
            dead = false;
        }


        protected byte[] getMachineStateBytes() {

            ByteBuffer bytes = ByteBuffer.allocate(getSize());
            bytes.order(ByteOrder.LITTLE_ENDIAN);

            flags[0] = (byte) ((running ? 1 : 0)
                    | (stopped ? 1 : 0) << 1
                    | (finished ? 1 : 0 << 2)
                    | (dead ? 1 : 0) << 3);

            bytes.put(flags);

            bytes.putInt(machineState.pc);
            bytes.putInt(machineState.pcs);
            bytes.putInt(machineState.cs);
            bytes.putInt(machineState.us);
            bytes.putInt(machineState.err);

            bytes.put(A1);
            bytes.put(A2);
            bytes.put(A3);
            bytes.put(A4);
            bytes.put(B1);
            bytes.put(B2);
            bytes.put(B3);
            bytes.put(B4);


            return bytes.array();
        }

        private void setMachineState(byte[] machineState) {
            ByteBuffer bf = ByteBuffer.allocate(getSize());
            bf.order(ByteOrder.LITTLE_ENDIAN);
            bf.put(machineState);
            bf.flip();

            bf.get(flags, 0, 2);
            running = (flags[0] & 1) == 1;
            stopped = (flags[0] >>> 1 & 1) == 1;
            finished = (flags[0] >>> 2 & 1) == 1;
            dead = (flags[0] >>> 3 & 1) == 1;

            pc = bf.getInt();
            pcs = bf.getInt();
            cs = bf.getInt();
            us = bf.getInt();
            err = bf.getInt();
            bf.get(A1, 0, 8);
            bf.get(A2, 0, 8);
            bf.get(A3, 0, 8);
            bf.get(A4, 0, 8);
            bf.get(B1, 0, 8);
            bf.get(B2, 0, 8);
            bf.get(B3, 0, 8);
            bf.get(B4, 0, 8);

        }

        public int getSize() {
            return 2 + 4 + 4 + 4 + 4 + 4 + 4 * 8 + 4 * 8;
        }

        public long getSteps() {
            return steps;
        }
    }


}
