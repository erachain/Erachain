package org.erachain.core.exdata.exActions;

import org.junit.Test;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class ExAirDropTest {

    private int flags;

    private Long assetKey = 2L;
    private int balancePos = 1;
    private boolean backward = false;
    Random rand = new Random();
    byte[] short1 = new byte[20];
    byte[] short2 = new byte[20];
    byte[] short3 = new byte[20];

    BigDecimal amount = new BigDecimal("123.123");
    byte[][] addressesList;

    void init() {
        rand.nextBytes(short1);
        rand.nextBytes(short2);
        rand.nextBytes(short3);

        addressesList = new byte[][]{short1, short2, short3};
    }

    @Test
    public void toBytes() {
    }

    @Test
    public void parse() {
    }

    @Test
    public void parseJSON_local() {
        init();

        ExAirDrop exAirPays = new ExAirDrop(flags, assetKey, amount, balancePos, backward, addressesList);

        ExAirDrop exPaysParsed = null;
        try {
            Fun.Tuple2<ExAction, String> result = ExAirDrop.parseJSON_local(exAirPays.toJson());
            exPaysParsed = (ExAirDrop) result.a;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        assertEquals(Arrays.equals(exAirPays.getAddresses()[2], exPaysParsed.getAddresses()[2]), true);
        assertEquals(exAirPays.getAmount(), exPaysParsed.getAmount());
    }

}