package org.erachain.core.exdata.exActions;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class ExFilteredPaysTest {

    private int flags;

    private Long assetKey = 2L;
    private int balancePos = 1;
    private boolean backward = false;
    private BigDecimal amountMin = BigDecimal.ONE;
    private BigDecimal amountMax = BigDecimal.TEN;

    private int payMethod = 0; // 0 - by Total, 1 - by Percent
    private BigDecimal payMethodValue = BigDecimal.TEN;

    private Long filterAssetKey = 10L;
    private int filterBalancePos = 3;
    private int filterBalanceSide = 2;
    private BigDecimal filterBalanceLessThen = BigDecimal.ONE;
    ;
    private BigDecimal filterBalanceMoreThen = BigDecimal.TEN;

    private int filterTXType = 31;
    private Long filterTXStartSeqNo = 124213435L;
    private Long filterTXEndSeqNo = 3253245325L;

    private final int filterByGender = 1; // = gender or all
    private boolean selfPay = false;

    @Test
    public void toByte() {

        ExFilteredPays exFilteredPays = new ExFilteredPays(flags, assetKey, balancePos, backward, payMethod, payMethodValue, amountMin, amountMax,
                filterAssetKey, filterBalancePos, filterBalanceSide,
                filterBalanceMoreThen, filterBalanceLessThen,
                filterTXType, filterTXStartSeqNo, filterTXEndSeqNo,
                filterByGender, selfPay);
        byte[] bytes = null;
        try {
            bytes = exFilteredPays.toBytes();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        assertEquals(bytes.length, exFilteredPays.length());

    }

    @Test
    public void length() {
    }

    @Test
    public void parse() {
        ExFilteredPays exFilteredPays = new ExFilteredPays(flags, assetKey, balancePos, backward, payMethod, payMethodValue, amountMin, amountMax,
                filterAssetKey, filterBalancePos, filterBalanceSide,
                filterBalanceMoreThen, filterBalanceLessThen,
                filterTXType, filterTXStartSeqNo, filterTXEndSeqNo,
                filterByGender, selfPay);
        byte[] bytes = null;
        try {
            bytes = exFilteredPays.toBytes();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        assertEquals(bytes.length, exFilteredPays.length());

        ExFilteredPays exFilteredPaysParsed = null;
        try {
            exFilteredPaysParsed = ExFilteredPays.parse(bytes, 0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        assertEquals(exFilteredPays.filterTimeEnd, exFilteredPaysParsed.filterTimeEnd);

    }

    @Test
    public void make() {
    }
}