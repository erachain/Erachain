package org.erachain.traders;
// 30/03 ++

import org.erachain.core.BlockChain;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.TreeMap;

// import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
//import org.erachain.core.BlockChain;
//import org.erachain.database.DBSet;
//import database.TransactionMap;
//import org.erachain.lang.Lang;

// shift as ABSOLUTE in persent
public class StoneGuard extends Trader {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoneGuard.class);

    // in PERCENT
    protected BigDecimal limitUP;
    protected BigDecimal limitDown;

    public StoneGuard(TradersManager tradersManager, String accountStr, int sleepSec, long haveKey, long wantKey,
                      HashMap<BigDecimal, BigDecimal> scheme, BigDecimal limitUP, BigDecimal limitDown, boolean cleanAllOnStart) {
        super(tradersManager, accountStr, sleepSec, scheme, haveKey, wantKey, cleanAllOnStart);

        this.limitUP = limitUP;
        this.limitDown = limitDown;

    }

    protected boolean createOrder(BigDecimal schemeAmount) {

        String result;
        BigDecimal shiftPercentage = this.scheme.get(schemeAmount);

        long haveKey;
        long wantKey;

        BigDecimal amountHave;
        BigDecimal amountWant;

        if (schemeAmount.signum() > 0) {
            haveKey = this.haveKey;
            wantKey = this.wantKey;

            BigDecimal shift = BigDecimal.ONE.add(shiftPercentage.movePointLeft(2));

            amountHave = schemeAmount.stripTrailingZeros();
            amountWant = amountHave.multiply(this.rate).multiply(shift).stripTrailingZeros();

            // NEED SCALE for VALIDATE
            if (amountWant.scale() > this.wantAsset.getScale()) {
                amountWant = amountWant.setScale(wantAsset.getScale(), BigDecimal.ROUND_HALF_UP);
            }

        } else {
            haveKey = this.wantKey;
            wantKey = this.haveKey;

            BigDecimal shift = BigDecimal.ONE.subtract(shiftPercentage.movePointLeft(2));

            amountWant = schemeAmount.negate().stripTrailingZeros();
            amountHave = amountWant.multiply(this.rate).multiply(shift).stripTrailingZeros();

            // NEED SCALE for VALIDATE
            if (amountHave.scale() > this.wantAsset.getScale()) {
                amountHave = amountHave.setScale(wantAsset.getScale(), BigDecimal.ROUND_HALF_UP);
            }
        }

        return super.createOrder(schemeAmount, haveKey, wantKey, amountHave, amountWant);

    }

    private void shiftAll() {

        LOGGER.info(">>>>> shift ALL for " + this.haveAsset.viewName()
                + "/" + this.wantAsset.viewName() + " to " + this.rate.toString());

        // REMOVE ALL ORDERS
        if (cleanSchemeOrders()) {

            try {
                //Thread.sleep(BlockChain.GENERATING_MIN_BLOCK_TIME_MS);
                sleep(4000);
            } catch (Exception e) {
                //FAILED TO SLEEP
            }
        }

        //BigDecimal persent;
        for (BigDecimal amount : this.scheme.keySet()) {
            //persent = this.scheme.get(amount);
            createOrder(amount);
            try {
                sleep(100);
            } catch (Exception e) {
                //FAILED TO SLEEP
            }

        }
    }

    protected boolean process() {

        String callerResult = null;

        TreeMap<Fun.Tuple3<Long, Long, String>, BigDecimal> rates = Rater.getRates();
        BigDecimal newRate = rates.get(new Fun.Tuple3<Long, Long, String>(this.haveKey, this.wantKey, "wex"));

        if (newRate == null) {
            // если курса нет то отменим все ордера и ждем
            LOGGER.info("Rate " + this.haveAsset.getName() + "/" + this.wantAsset.getName() +  " not found - clear all orders anr awaiting...");
            cleanSchemeOrders();
            return false;
        }

        if (this.rate == null) {
            this.rate = newRate;
            shiftAll();

        } else {
            if (//!BlockChain.DEVELOP_USE &&
                    newRate.compareTo(this.rate) == 0)
                return updateCap();

            BigDecimal diffPerc = newRate.divide(this.rate, 8, BigDecimal.ROUND_HALF_UP)
                    .subtract(BigDecimal.ONE).multiply(M100);
            if (BlockChain.DEVELOP_USE ||
                    diffPerc.compareTo(this.limitUP) > 0
                    || diffPerc.abs().compareTo(this.limitDown) > 0) {

                this.rate = newRate;
                shiftAll();
            } else {
                return updateCap();
            }
        }

        return true;
    }

}
