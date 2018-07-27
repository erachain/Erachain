package traders;
// 30/03 ++

import api.ApiErrorFactory;
import core.BlockChain;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.TreeMap;

// import org.apache.log4j.Logger;
//import core.BlockChain;
//import database.DBSet;
//import database.TransactionMap;
//import lang.Lang;

public class TraderA extends Trader {

    private static final Logger LOGGER = Logger.getLogger(TraderA.class);

    public TraderA(TradersManager tradersManager, String accountStr, int sleepSec, long haveKey, long wantKey,
                   HashMap<BigDecimal, BigDecimal> scheme, BigDecimal limitUP, BigDecimal limitDown, boolean cleanAllOnStart) {
        super(tradersManager, accountStr, sleepSec, scheme, haveKey, wantKey, limitUP, limitDown, cleanAllOnStart);
    }

    private void shiftAll() {

        LOGGER.info("shift ALL for " + this.haveAsset.viewName()
                + "/" + this.wantAsset.viewName() + " to " + this.rate.toString());

        // REMOVE ALL ORDERS
        if (cleanSchemeOrders()) {

            try {
                Thread.sleep(BlockChain.GENERATING_MIN_BLOCK_TIME_MS);
            } catch (Exception e) {
                //FAILED TO SLEEP
            }
        }

        //BigDecimal persent;
        for (BigDecimal amount : this.scheme.keySet()) {
            //persent = this.scheme.get(amount);
            createOrder(amount);
            try {
                Thread.sleep(100);
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

        if (newRate != null) {
            if (this.rate == null) {
                if (newRate == null)
                    return false;

                this.rate = newRate;
                shiftAll();

            } else {
                if (newRate == null || newRate.compareTo(this.rate) == 0)
                    return false;

                BigDecimal diffPerc = newRate.divide(this.rate, 8, BigDecimal.ROUND_HALF_UP)
                        .subtract(BigDecimal.ONE).multiply(Trader.M100);
                if (diffPerc.compareTo(this.limitUP) > 0
                        || diffPerc.abs().compareTo(this.limitDown) > 0) {

                    this.rate = newRate;
                    shiftAll();
                }
            }
        }

        try {
        } catch (Exception e) {
            //FAILED TO SLEEP
            return false;
        }

        return true;
    }

}
