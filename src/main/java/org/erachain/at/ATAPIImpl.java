/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.

 */

package org.erachain.at;

import org.erachain.core.transaction.Transaction;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class ATAPIImpl implements ATAPI {


    private static final Logger LOGGER = LoggerFactory.getLogger(ATAPIImpl.class);

    ATAPIPlatformImpl platform = ATAPIPlatformImpl.getInstance();


    @Override
    public long get_A1(ATMachineState state) {
        return ATAPIHelper.getLong(state.get_A1());
    }

    @Override
    public long get_A2(ATMachineState state) {
        return ATAPIHelper.getLong(state.get_A2());
    }

    @Override
    public long get_A3(ATMachineState state) {
        return ATAPIHelper.getLong(state.get_A3());
    }

    @Override
    public long get_A4(ATMachineState state) {
        return ATAPIHelper.getLong(state.get_A4());
    }

    @Override
    public long get_B1(ATMachineState state) {
        return ATAPIHelper.getLong(state.get_B1());
    }

    @Override
    public long get_B2(ATMachineState state) {
        return ATAPIHelper.getLong(state.get_B2());
    }

    @Override
    public long get_B3(ATMachineState state) {
        return ATAPIHelper.getLong(state.get_B3());
    }

    @Override
    public long get_B4(ATMachineState state) {
        return ATAPIHelper.getLong(state.get_B4());
    }

    @Override
    public void set_A1(long val, ATMachineState state) {
        state.set_A1(ATAPIHelper.getByteArray(val));
    }

    @Override
    public void set_A2(long val, ATMachineState state) {
        state.set_A2(ATAPIHelper.getByteArray(val));
    }

    @Override
    public void set_A3(long val, ATMachineState state) {
        state.set_A3(ATAPIHelper.getByteArray(val));
    }

    @Override
    public void set_A4(long val, ATMachineState state) {
        state.set_A4(ATAPIHelper.getByteArray(val));
    }

    @Override
    public void set_A1_A2(long val1, long val2, ATMachineState state) {
        state.set_A1(ATAPIHelper.getByteArray(val1));
        state.set_A2(ATAPIHelper.getByteArray(val2));
    }

    @Override
    public void set_A3_A4(long val1, long val2, ATMachineState state) {
        state.set_A3(ATAPIHelper.getByteArray(val1));
        state.set_A4(ATAPIHelper.getByteArray(val2));

    }

    @Override
    public void set_B1(long val, ATMachineState state) {
        state.set_B1(ATAPIHelper.getByteArray(val));
    }

    @Override
    public void set_B2(long val, ATMachineState state) {
        state.set_B2(ATAPIHelper.getByteArray(val));
    }

    @Override
    public void set_B3(long val, ATMachineState state) {
        state.set_B3(ATAPIHelper.getByteArray(val));
    }

    @Override
    public void set_B4(long val, ATMachineState state) {
        state.set_B4(ATAPIHelper.getByteArray(val));
    }

    @Override
    public void set_B1_B2(long val1, long val2, ATMachineState state) {
        state.set_B1(ATAPIHelper.getByteArray(val1));
        state.set_B2(ATAPIHelper.getByteArray(val2));
    }

    @Override
    public void set_B3_B4(long val3, long val4, ATMachineState state) {
        state.set_B3(ATAPIHelper.getByteArray(val3));
        state.set_B4(ATAPIHelper.getByteArray(val4));
    }

    @Override
    public void clear_A(ATMachineState state) {
        byte[] b = new byte[8];
        state.set_A1(b);
        state.set_A2(b);
        state.set_A3(b);
        state.set_A4(b);
    }

    @Override
    public void clear_B(ATMachineState state) {
        byte[] b = new byte[8];
        state.set_B1(b);
        state.set_B2(b);
        state.set_B3(b);
        state.set_B4(b);
    }

    @Override
    public void copy_A_From_B(ATMachineState state) {
        state.set_A1(state.get_B1());
        state.set_A2(state.get_B2());
        state.set_A3(state.get_B3());
        state.set_A4(state.get_B4());
    }

    @Override
    public void copy_B_From_A(ATMachineState state) {
        state.set_B1(state.get_A1());
        state.set_B2(state.get_A2());
        state.set_B3(state.get_A3());
        state.set_B4(state.get_A4());
    }

    @Override
    public long check_A_Is_Zero(ATMachineState state) {
        byte[] b = new byte[8];
        return (Arrays.equals(state.get_A1(), b) &&
                Arrays.equals(state.get_A2(), b) &&
                Arrays.equals(state.get_A3(), b) &&
                Arrays.equals(state.get_A4(), b)) ? 0 : 1;
    }

    @Override
    public long check_B_Is_Zero(ATMachineState state) {
        byte[] b = new byte[8];
        return (Arrays.equals(state.get_B1(), b) &&
                Arrays.equals(state.get_B2(), b) &&
                Arrays.equals(state.get_B3(), b) &&
                Arrays.equals(state.get_B4(), b)) ? 0 : 1;
    }

    public long check_A_equals_B(ATMachineState state) {
        return (Arrays.equals(state.get_A1(), state.get_B1()) &&
                Arrays.equals(state.get_A2(), state.get_B2()) &&
                Arrays.equals(state.get_A3(), state.get_B3()) &&
                Arrays.equals(state.get_A4(), state.get_B4())) ? 1 : 0;
    }

    @Override
    public void swap_A_and_B(ATMachineState state) {
        byte[] b = new byte[8];

        b = state.get_A1().clone();
        state.set_A1(state.get_B1());
        state.set_B1(b);

        b = state.get_A2().clone();
        state.set_A2(state.get_B2());
        state.set_B2(b);

        b = state.get_A3().clone();
        state.set_A3(state.get_B3());
        state.set_B3(b);

        b = state.get_A4().clone();
        state.set_A4(state.get_B4());
        state.set_B4(b);

    }

    @Override
    public void add_A_to_B(ATMachineState state) {
        BigInteger a = ATAPIHelper.getBigInteger(state.get_A1(), state.get_A2(), state.get_A3(), state.get_A4());
        BigInteger b = ATAPIHelper.getBigInteger(state.get_B1(), state.get_B2(), state.get_B3(), state.get_B4());
        BigInteger result = a.add(b);
        ByteBuffer resultBuffer = ByteBuffer.wrap(ATAPIHelper.getByteArray(result));
        resultBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byte[] temp = new byte[8];
        resultBuffer.get(temp, 0, 8);
        state.set_B1(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_B2(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_B3(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_B4(temp);
    }

    @Override
    public void add_B_to_A(ATMachineState state) {
        BigInteger a = ATAPIHelper.getBigInteger(state.get_A1(), state.get_A2(), state.get_A3(), state.get_A4());
        BigInteger b = ATAPIHelper.getBigInteger(state.get_B1(), state.get_B2(), state.get_B3(), state.get_B4());
        BigInteger result = a.add(b);
        ByteBuffer resultBuffer = ByteBuffer.wrap(ATAPIHelper.getByteArray(result));
        resultBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byte[] temp = new byte[8];
        resultBuffer.get(temp, 0, 8);
        state.set_A1(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_A2(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_A3(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_A4(temp);
    }

    @Override
    public void sub_A_from_B(ATMachineState state) {
        BigInteger a = ATAPIHelper.getBigInteger(state.get_A1(), state.get_A2(), state.get_A3(), state.get_A4());
        BigInteger b = ATAPIHelper.getBigInteger(state.get_B1(), state.get_B2(), state.get_B3(), state.get_B4());
        BigInteger result = b.subtract(a);
        ByteBuffer resultBuffer = ByteBuffer.wrap(ATAPIHelper.getByteArray(result));
        resultBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byte[] temp = new byte[8];
        resultBuffer.get(temp, 0, 8);
        state.set_B1(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_B2(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_B3(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_B4(temp);
    }

    @Override
    public void sub_B_from_A(ATMachineState state) {
        BigInteger a = ATAPIHelper.getBigInteger(state.get_A1(), state.get_A2(), state.get_A3(), state.get_A4());
        BigInteger b = ATAPIHelper.getBigInteger(state.get_B1(), state.get_B2(), state.get_B3(), state.get_B4());
        BigInteger result = a.subtract(b);
        ByteBuffer resultBuffer = ByteBuffer.wrap(ATAPIHelper.getByteArray(result));
        resultBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byte[] temp = new byte[8];
        resultBuffer.get(temp, 0, 8);
        state.set_A1(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_A2(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_A3(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_A4(temp);
    }

    @Override
    public void mul_A_by_B(ATMachineState state) {
        BigInteger a = ATAPIHelper.getBigInteger(state.get_A1(), state.get_A2(), state.get_A3(), state.get_A4());
        BigInteger b = ATAPIHelper.getBigInteger(state.get_B1(), state.get_B2(), state.get_B3(), state.get_B4());
        BigInteger result = a.multiply(b);
        ByteBuffer resultBuffer = ByteBuffer.wrap(ATAPIHelper.getByteArray(result));
        resultBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byte[] temp = new byte[8];
        resultBuffer.get(temp, 0, 8);
        state.set_B1(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_B2(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_B3(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_B4(temp);
    }

    @Override
    public void mul_B_by_A(ATMachineState state) {
        BigInteger a = ATAPIHelper.getBigInteger(state.get_A1(), state.get_A2(), state.get_A3(), state.get_A4());
        BigInteger b = ATAPIHelper.getBigInteger(state.get_B1(), state.get_B2(), state.get_B3(), state.get_B4());
        BigInteger result = a.multiply(b);
        ByteBuffer resultBuffer = ByteBuffer.wrap(ATAPIHelper.getByteArray(result));
        resultBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byte[] temp = new byte[8];
        resultBuffer.get(temp, 0, 8);
        state.set_A1(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_A2(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_A3(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_A4(temp);
    }

    @Override
    public void div_A_by_B(ATMachineState state) {
        BigInteger a = ATAPIHelper.getBigInteger(state.get_A1(), state.get_A2(), state.get_A3(), state.get_A4());
        BigInteger b = ATAPIHelper.getBigInteger(state.get_B1(), state.get_B2(), state.get_B3(), state.get_B4());
        if (b.compareTo(BigInteger.ZERO) == 0)
            return;
        BigInteger result = a.divide(b);
        ByteBuffer resultBuffer = ByteBuffer.wrap(ATAPIHelper.getByteArray(result));
        resultBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byte[] temp = new byte[8];
        resultBuffer.get(temp, 0, 8);
        state.set_B1(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_B2(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_B3(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_B4(temp);
    }

    @Override
    public void div_B_by_A(ATMachineState state) {
        BigInteger a = ATAPIHelper.getBigInteger(state.get_A1(), state.get_A2(), state.get_A3(), state.get_A4());
        BigInteger b = ATAPIHelper.getBigInteger(state.get_B1(), state.get_B2(), state.get_B3(), state.get_B4());
        if (a.compareTo(BigInteger.ZERO) == 0)
            return;
        BigInteger result = b.divide(a);
        ByteBuffer resultBuffer = ByteBuffer.wrap(ATAPIHelper.getByteArray(result));
        resultBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byte[] temp = new byte[8];
        resultBuffer.get(temp, 0, 8);
        state.set_A1(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_A2(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_A3(temp);
        resultBuffer.get(temp, 0, 8);
        state.set_A4(temp);
    }

    @Override
    public void or_A_with_B(ATMachineState state) {
        ByteBuffer a = ByteBuffer.allocate(32);
        a.order(ByteOrder.LITTLE_ENDIAN);
        a.put(state.get_A1());
        a.put(state.get_A2());
        a.put(state.get_A3());
        a.put(state.get_A4());
        a.clear();

        ByteBuffer b = ByteBuffer.allocate(32);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.put(state.get_B1());
        b.put(state.get_B2());
        b.put(state.get_B3());
        b.put(state.get_B4());
        b.clear();

        state.set_A1(ATAPIHelper.getByteArray(a.getLong(0) | b.getLong(0)));
        state.set_A2(ATAPIHelper.getByteArray(a.getLong(8) | b.getLong(8)));
        state.set_A3(ATAPIHelper.getByteArray(a.getLong(16) | b.getLong(16)));
        state.set_A4(ATAPIHelper.getByteArray(a.getLong(24) | b.getLong(24)));
    }

    @Override
    public void or_B_with_A(ATMachineState state) {
        ByteBuffer a = ByteBuffer.allocate(32);
        a.order(ByteOrder.LITTLE_ENDIAN);
        a.put(state.get_A1());
        a.put(state.get_A2());
        a.put(state.get_A3());
        a.put(state.get_A4());
        a.clear();

        ByteBuffer b = ByteBuffer.allocate(32);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.put(state.get_B1());
        b.put(state.get_B2());
        b.put(state.get_B3());
        b.put(state.get_B4());
        b.clear();

        state.set_B1(ATAPIHelper.getByteArray(a.getLong(0) | b.getLong(0)));
        state.set_B2(ATAPIHelper.getByteArray(a.getLong(8) | b.getLong(8)));
        state.set_B3(ATAPIHelper.getByteArray(a.getLong(16) | b.getLong(16)));
        state.set_B4(ATAPIHelper.getByteArray(a.getLong(24) | b.getLong(24)));
    }

    @Override
    public void and_A_with_B(ATMachineState state) {
        ByteBuffer a = ByteBuffer.allocate(32);
        a.order(ByteOrder.LITTLE_ENDIAN);
        a.put(state.get_A1());
        a.put(state.get_A2());
        a.put(state.get_A3());
        a.put(state.get_A4());
        a.clear();

        ByteBuffer b = ByteBuffer.allocate(32);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.put(state.get_B1());
        b.put(state.get_B2());
        b.put(state.get_B3());
        b.put(state.get_B4());
        b.clear();

        state.set_A1(ATAPIHelper.getByteArray(a.getLong(0) & b.getLong(0)));
        state.set_A2(ATAPIHelper.getByteArray(a.getLong(8) & b.getLong(8)));
        state.set_A3(ATAPIHelper.getByteArray(a.getLong(16) & b.getLong(16)));
        state.set_A4(ATAPIHelper.getByteArray(a.getLong(24) & b.getLong(24)));
    }

    @Override
    public void and_B_with_A(ATMachineState state) {
        ByteBuffer a = ByteBuffer.allocate(32);
        a.order(ByteOrder.LITTLE_ENDIAN);
        a.put(state.get_A1());
        a.put(state.get_A2());
        a.put(state.get_A3());
        a.put(state.get_A4());
        a.clear();

        ByteBuffer b = ByteBuffer.allocate(32);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.put(state.get_B1());
        b.put(state.get_B2());
        b.put(state.get_B3());
        b.put(state.get_B4());
        b.clear();

        state.set_B1(ATAPIHelper.getByteArray(a.getLong(0) & b.getLong(0)));
        state.set_B2(ATAPIHelper.getByteArray(a.getLong(8) & b.getLong(8)));
        state.set_B3(ATAPIHelper.getByteArray(a.getLong(16) & b.getLong(16)));
        state.set_B4(ATAPIHelper.getByteArray(a.getLong(24) & b.getLong(24)));
    }

    @Override
    public void xor_A_with_B(ATMachineState state) {
        ByteBuffer a = ByteBuffer.allocate(32);
        a.order(ByteOrder.LITTLE_ENDIAN);
        a.put(state.get_A1());
        a.put(state.get_A2());
        a.put(state.get_A3());
        a.put(state.get_A4());
        a.clear();

        ByteBuffer b = ByteBuffer.allocate(32);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.put(state.get_B1());
        b.put(state.get_B2());
        b.put(state.get_B3());
        b.put(state.get_B4());
        b.clear();

        state.set_A1(ATAPIHelper.getByteArray(a.getLong(0) ^ b.getLong(0)));
        state.set_A2(ATAPIHelper.getByteArray(a.getLong(8) ^ b.getLong(8)));
        state.set_A3(ATAPIHelper.getByteArray(a.getLong(16) ^ b.getLong(16)));
        state.set_A4(ATAPIHelper.getByteArray(a.getLong(24) ^ b.getLong(24)));
    }

    @Override
    public void xor_B_with_A(ATMachineState state) {
        ByteBuffer a = ByteBuffer.allocate(32);
        a.order(ByteOrder.LITTLE_ENDIAN);
        a.put(state.get_A1());
        a.put(state.get_A2());
        a.put(state.get_A3());
        a.put(state.get_A4());
        a.clear();

        ByteBuffer b = ByteBuffer.allocate(32);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.put(state.get_B1());
        b.put(state.get_B2());
        b.put(state.get_B3());
        b.put(state.get_B4());
        b.clear();

        state.set_B1(ATAPIHelper.getByteArray(a.getLong(0) ^ b.getLong(0)));
        state.set_B2(ATAPIHelper.getByteArray(a.getLong(8) ^ b.getLong(8)));
        state.set_B3(ATAPIHelper.getByteArray(a.getLong(16) ^ b.getLong(16)));
        state.set_B4(ATAPIHelper.getByteArray(a.getLong(24) ^ b.getLong(24)));
    }

    @Override
    public void MD5_A_to_B(ATMachineState state) {
        ByteBuffer b = ByteBuffer.allocate(16);
        b.order(ByteOrder.LITTLE_ENDIAN);

        b.put(state.get_A1());
        b.put(state.get_A2());

        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            ByteBuffer mdb = ByteBuffer.wrap(md5.digest(b.array()));
            mdb.order(ByteOrder.LITTLE_ENDIAN);

            state.set_B1(ATAPIHelper.getByteArray(mdb.getLong(0)));
            state.set_B1(ATAPIHelper.getByteArray(mdb.getLong(8)));

        } catch (NoSuchAlgorithmException e) {
            //not expected to reach that point
            LOGGER.error(e.getMessage(), e);
        }
    }


    @Override
    public long check_MD5_A_with_B(ATMachineState state) {

        ByteBuffer b = ByteBuffer.allocate(16);
        b.order(ByteOrder.LITTLE_ENDIAN);

        b.put(state.get_A1());
        b.put(state.get_A2());

        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            ByteBuffer mdb = ByteBuffer.wrap(md5.digest(b.array()));
            mdb.order(ByteOrder.LITTLE_ENDIAN);

            b.clear();

            b.put(state.get_B1());
            b.put(state.get_B2());

            return Arrays.equals(b.array(), mdb.array()) ? 1 : 0;

        } catch (NoSuchAlgorithmException e) {
            //not expected to reach that point
            LOGGER.error(e.getMessage(), e);
        }

        return 0;
    }

    @Override
    public void HASH160_A_to_B(ATMachineState state) {
        ByteBuffer b = ByteBuffer.allocate(32);
        b.order(ByteOrder.LITTLE_ENDIAN);

        b.put(state.get_A1());
        b.put(state.get_A2());
        b.put(state.get_A3());
        b.put(state.get_A4());

        RIPEMD160 ripemd160 = new RIPEMD160();
        ByteBuffer ripemdb = ByteBuffer.wrap(ripemd160.digest(b.array()));
        ripemdb.order(ByteOrder.LITTLE_ENDIAN);

        state.set_B1(ATAPIHelper.getByteArray(ripemdb.getLong(0)));
        state.set_B2(ATAPIHelper.getByteArray(ripemdb.getLong(8)));
        state.set_B3(ATAPIHelper.getByteArray((long) ripemdb.getInt(16)));

    }

    @Override
    public long check_HASH160_A_with_B(ATMachineState state) {
        ByteBuffer b = ByteBuffer.allocate(32);
        b.order(ByteOrder.LITTLE_ENDIAN);

        b.put(state.get_A1());
        b.put(state.get_A2());
        b.put(state.get_A3());
        b.put(state.get_A4());

        RIPEMD160 ripemd160 = new RIPEMD160();
        ByteBuffer ripemdb = ByteBuffer.allocate(32);
        ripemdb.order(ByteOrder.LITTLE_ENDIAN);
        ripemdb.put(ripemd160.digest(b.array()));

        b.clear();

        b.put(state.get_B1());
        b.put(state.get_B2());
        b.put(state.get_B3());
        b.put(state.get_B4());

        return (Arrays.equals(b.array(), ripemdb.array())) ? 1 : 0;
    }

    @Override
    public void SHA256_A_to_B(ATMachineState state) {
        ByteBuffer b = ByteBuffer.allocate(32);
        b.order(ByteOrder.LITTLE_ENDIAN);

        b.put(state.get_A1());
        b.put(state.get_A2());
        b.put(state.get_A3());
        b.put(state.get_A4());

        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            ByteBuffer shab = ByteBuffer.wrap(sha256.digest(b.array()));
            shab.order(ByteOrder.LITTLE_ENDIAN);

            state.set_B1(ATAPIHelper.getByteArray(shab.getLong(0)));
            state.set_B2(ATAPIHelper.getByteArray(shab.getLong(8)));
            state.set_B3(ATAPIHelper.getByteArray(shab.getLong(16)));
            state.set_B4(ATAPIHelper.getByteArray(shab.getLong(24)));

        } catch (NoSuchAlgorithmException e) {
            //not expected to reach that point
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public long check_SHA256_A_with_B(ATMachineState state) {
        ByteBuffer b = ByteBuffer.allocate(32);
        b.order(ByteOrder.LITTLE_ENDIAN);

        b.put(state.get_A1());
        b.put(state.get_A2());
        b.put(state.get_A3());
        b.put(state.get_A4());

        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] shab = sha256.digest(b.array());

            b.clear();
            b.put(state.get_B1());
            b.put(state.get_B2());
            b.put(state.get_B3());
            b.put(state.get_B4());
            return (Arrays.equals(b.array(), shab)) ? 1 : 0;

        } catch (NoSuchAlgorithmException e) {
            //not expected to reach that point
            LOGGER.error(e.getMessage(), e);
        }
        return 0;

    }

    @Override
    public long get_Block_Timestamp(ATMachineState state) {
        return platform.get_Block_Timestamp(state);

    }

    @Override
    public long get_Creation_Timestamp(ATMachineState state) {
        return platform.get_Creation_Timestamp(state);
    }

    @Override
    public long get_Last_Block_Timestamp(ATMachineState state) {
        return platform.get_Last_Block_Timestamp(state);
    }

    @Override
    public void put_Last_Block_Hash_In_A(ATMachineState state) {
        platform.put_Last_Block_Hash_In_A(state);

    }

    @Override
    public void A_to_Tx_after_Timestamp(long val, ATMachineState state) {
        platform.A_to_Tx_after_Timestamp(val, state);

    }

    @Override
    public void A_to_Tx_at_Timestamp(long val, ATMachineState state) {
        platform.A_to_Tx_at_Timestamp(val, state);
    }

    @Override
    public long get_Type_for_Tx_in_A(ATMachineState state) {
        return platform.get_Type_for_Tx_in_A(state);
    }

    @Override
    public long get_Amount_for_Tx_in_A(ATMachineState state) {
        return platform.get_Amount_for_Tx_in_A(state);
    }

    @Override
    public long get_Timestamp_for_Tx_in_A(ATMachineState state) {
        return platform.get_Timestamp_for_Tx_in_A(state);
    }

    @Override
    public long get_Random_Id_for_Tx_in_A(ATMachineState state) {
        return platform.get_Random_Id_for_Tx_in_A(state);
    }

    @Override
    public void message_from_Tx_in_A_to_B(ATMachineState state) {
        platform.message_from_Tx_in_A_to_B(state);
    }

    @Override
    public void B_to_Address_of_Tx_in_A(ATMachineState state) {

        platform.B_to_Address_of_Tx_in_A(state);
    }

    @Override
    public void B_to_Address_of_Creator(ATMachineState state) {
        platform.B_to_Address_of_Creator(state);

    }

    @Override
    public long get_Current_Balance(ATMachineState state) {
        return platform.get_Current_Balance(state);
    }

    @Override
    public long get_Previous_Balance(ATMachineState state) {
        return platform.get_Previous_Balance(state);
    }

    @Override
    public void send_to_Address_in_B(long key, long val, ATMachineState state) {
        platform.send_to_Address_in_B(Transaction.FEE_KEY, val, state);
    }

    @Override
    public void send_All_to_Address_in_B(long key, ATMachineState state) {
        platform.send_All_to_Address_in_B(Transaction.FEE_KEY, state);
    }

    @Override
    public void send_Old_to_Address_in_B(ATMachineState state) {
        platform.send_Old_to_Address_in_B(state);
    }

    @Override
    public void send_A_to_Address_in_B(ATMachineState state) {
        platform.send_A_to_Address_in_B(state);
    }

    @Override
    public long add_Minutes_to_Timestamp(long val1, long val2, ATMachineState state) {
        return platform.add_Minutes_to_Timestamp(val1, val2, state);
    }

    @Override
    public void set_Min_Activation_Amount(long val, ATMachineState state) {
        state.setMinActivationAmount(val);
    }

    @Override
    public void put_Last_Block_Generation_Signature_In_A(ATMachineState state) {
        platform.put_Last_Block_Generation_Signature_In_A(state);
    }

    @Override
    public void SHA256_to_B(long val1, long val2, ATMachineState state) {
        if (val1 < 0 || val2 < 0 ||
                (val1 + val2 - 1) < 0 ||
                ((long) val1) * 8 + 8 > ((long) Integer.MAX_VALUE) ||
                val1 * 8 + 8 > state.getDsize() ||
                ((long) val1 + (long) val2 - 1) * 8 + 8 > ((long) Integer.MAX_VALUE) ||
                (val1 + val2 - 1) * 8 + 8 > state.getDsize()) {
            return;
        }

        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(state.getAp_data().array(), (int) val1, (int) (val2 > 256 ? 256 : val2));
            ByteBuffer shab = ByteBuffer.wrap(sha256.digest());
            shab.order(ByteOrder.LITTLE_ENDIAN);

            state.set_B1(ATAPIHelper.getByteArray(shab.getLong(0)));
            state.set_B2(ATAPIHelper.getByteArray(shab.getLong(8)));
            state.set_B3(ATAPIHelper.getByteArray(shab.getLong(16)));
            state.set_B4(ATAPIHelper.getByteArray(shab.getLong(24)));

        } catch (NoSuchAlgorithmException e) {
            //not expected to reach that point
            LOGGER.error(e.getMessage(), e);
        }
    }

}