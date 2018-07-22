package traders;
// 30/03 ++

import api.ApiErrorFactory;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

// import org.apache.log4j.Logger;
//import core.BlockChain;
//import database.DBSet;
//import database.TransactionMap;
//import lang.Lang;

public class RaterWEX extends Rater {

    private static final Logger LOGGER = Logger.getLogger(RaterWEX.class);

    public RaterWEX(TradersManager tradersManager, int sleepSec) {
        super(tradersManager, "wex", sleepSec);

        this.apiURL = "https://wex.nz/api/3/ticker/btc_rur-btc_usd-usd_rur";

    }

    protected void parse(String result) {
        JSONObject json = null;
        try {
            //READ JSON
            json = (JSONObject) JSONValue.parse(result);
        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            ///LOGGER.info(e);
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }

        if (json == null)
            return;

        JSONObject pair;
        BigDecimal price;
        BigDecimal rate_avg = null;

        if (json.containsKey("btc_rur")) {
            pair = (JSONObject) json.get("btc_rur");
            rate_avg = null;
            try {
                rate_avg = new BigDecimal((Double)pair.get("avg"));
            } catch (Exception e){
                try {
                    rate_avg = new BigDecimal((Long)pair.get("avg"));
                } catch (Exception e1){
                    try {
                        rate_avg = new BigDecimal((String)pair.get("avg"));
                    } catch (Exception e2){
                        //LOGGER.error(e.getMessage(), e);
                    }
                }
            }

            if (rate_avg != null) {
                price = rate_avg;//.setScale(10, BigDecimal.ROUND_HALF_UP);
                price = price.multiply(this.shiftRate).setScale(10, BigDecimal.ROUND_HALF_UP);
                Rater.setRate(1079L, 1078L, this.courseName, price);
                LOGGER.info("WEX rate: BTC - RUB " + price);
            }

        }

        if (json.containsKey("btc_usd")) {
            pair = (JSONObject) json.get("btc_usd");
            rate_avg = null;
            try {
                rate_avg = new BigDecimal((Double)pair.get("avg"));
            } catch (Exception e){
                try {
                    rate_avg = new BigDecimal((Long)pair.get("avg"));
                } catch (Exception e1){
                    try {
                        rate_avg = new BigDecimal((String)pair.get("avg"));
                    } catch (Exception e2){
                        //LOGGER.error(e.getMessage(), e);
                    }
                }
            }

            if (rate_avg != null) {
                price = rate_avg; //).setScale(10, BigDecimal.ROUND_HALF_UP);
                price = price.multiply(this.shiftRate).setScale(10, BigDecimal.ROUND_HALF_UP);
                Rater.setRate(1079L, 1077L, this.courseName, price);
                LOGGER.info("WEX rate: BTC - USD " + price);
            }

        }

        if (json.containsKey("usd_rur")) {
            pair = (JSONObject) json.get("usd_rur");
            rate_avg = null;
            try {
                rate_avg = new BigDecimal((Double)pair.get("avg"));
            } catch (Exception e){
                try {
                    rate_avg = new BigDecimal((Long)pair.get("avg"));
                } catch (Exception e1){
                    try {
                        rate_avg = new BigDecimal((String)pair.get("avg"));
                    } catch (Exception e2){
                        //LOGGER.error(e.getMessage(), e);
                    }
                }
            }

            if (rate_avg != null) {
                price = rate_avg;//).setScale(10, BigDecimal.ROUND_HALF_UP);
                price = price.multiply(this.shiftRate).setScale(10, BigDecimal.ROUND_HALF_UP);
                Rater.setRate(1077L, 1078L, this.courseName, price);
                LOGGER.info("WEX rate: USD - RUR " + price);
            }

        }

    }
}
