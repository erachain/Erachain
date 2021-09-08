package org.erachain.core.exdata.exActions;

import org.erachain.core.account.Account;
import org.junit.Test;
import org.mapdb.Fun.Tuple3;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class ExListPaysTest {

    private int flags;

    private Long assetKey = 2L;
    private int balancePos = 1;
    private boolean backward = false;
    Random rand = new Random();
    byte[] short1 = new byte[20];
    byte[] short2 = new byte[20];
    byte[] short3 = new byte[20];
    Account account1;
    Account account2;
    Account account3;

    Tuple3<byte[], BigDecimal, String>[] addressesList;

    void init() {
        rand.nextBytes(short1);
        rand.nextBytes(short2);
        rand.nextBytes(short3);

        addressesList = new Tuple3[]{
                new Tuple3(short1, new BigDecimal("123.34"), "STR1"),
                new Tuple3(short2, new BigDecimal("3.04"), "STR2"),
                new Tuple3(short3, new BigDecimal("0.0014"), "--STR2")
        };
    }

    @Test
    public void toBytes() {
        init();

        ExListPays exListPays = new ExListPays(flags, assetKey, balancePos, backward, addressesList);
        byte[] bytes = null;
        try {
            bytes = exListPays.toBytes();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        assertEquals(bytes.length, exListPays.length());


    }

    @Test
    public void parse() {
        init();

        ExListPays exListPays = new ExListPays(flags, assetKey, balancePos, backward, addressesList);
        byte[] bytes = null;
        try {
            bytes = exListPays.toBytes();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        assertEquals(bytes.length, exListPays.length());

        ExListPays exPaysParsed = null;
        try {
            exPaysParsed = ExListPays.parse(bytes, 0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        assertEquals(Arrays.equals(exListPays.getAddresses()[2].a, exPaysParsed.getAddresses()[2].a), true);
        assertEquals(exListPays.getAddresses()[2].c, exPaysParsed.getAddresses()[2].c);
    }

    @Test
    public void parseJSON_local() {
    }

    @Test
    public void isValid() {
    }

    @Test
    public void process() {
    }

    @Test
    public void orphan() {
    }
}