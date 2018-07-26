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

/// result   "last":9577.769,"buy":9577.769,"sell":9509.466
public class RaterWEX extends Rater {

    private static final Logger LOGGER = Logger.getLogger(RaterWEX.class);

    public RaterWEX(TradersManager tradersManager, int sleepSec) {
        super(tradersManager, "wex", sleepSec);

        this.apiURL = "https://wex.nz/api/3/ticker/btc_rur-btc_usd-usd_rur";

    }

    private BigDecimal calcPrice(BigDecimal rateBye, BigDecimal rateSell, BigDecimal rateLast) {
        try {
            return (rateBye.add(rateSell).divide(new BigDecimal(2), 10, BigDecimal.ROUND_HALF_UP))
                .multiply(this.shiftRate).setScale(10, BigDecimal.ROUND_HALF_UP);
        } catch (NullPointerException | ClassCastException e) {
            return null;
        }

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

        LOGGER.info("WEX : " + result);

        JSONObject pair;
        BigDecimal price;
        BigDecimal rateLast = null;
        BigDecimal rateBuy = null;
        BigDecimal rateSell = null;

        if (json.containsKey("btc_rur")) {
            rateBuy = null;
            pair = (JSONObject) json.get("btc_rur");
            try {
                rateLast = new BigDecimal((Double)pair.get("last"));
                rateBuy = new BigDecimal((Double)pair.get("buy"));
                rateSell = new BigDecimal((Double)pair.get("sell"));
            } catch (Exception e){
                try {
                    rateLast = new BigDecimal((Long)pair.get("last"));
                    rateBuy = new BigDecimal((Long)pair.get("buy"));
                    rateSell = new BigDecimal((Long)pair.get("sell"));
                } catch (Exception e1){
                    try {
                        rateLast = new BigDecimal((String)pair.get("last"));
                        rateBuy = new BigDecimal((String)pair.get("buy"));
                        rateSell = new BigDecimal((String)pair.get("sell"));
                    } catch (Exception e2){
                        //LOGGER.error(e.getMessage(), e);
                    }
                }
            }

            if (rateBuy != null) {
                price = calcPrice(rateBuy, rateSell, rateLast);
                Rater.setRate(1079L, 1078L, this.courseName, price);
                LOGGER.info("WEX rate: BTC - RUB " + price);
            }

        }

        if (json.containsKey("btc_usd")) {
            rateBuy = null;
            pair = (JSONObject) json.get("btc_usd");
            try {
                rateLast = new BigDecimal((Double)pair.get("last"));
                rateBuy = new BigDecimal((Double)pair.get("buy"));
                rateSell = new BigDecimal((Double)pair.get("sell"));
            } catch (Exception e){
                try {
                    rateLast = new BigDecimal((Long)pair.get("last"));
                    rateBuy = new BigDecimal((Long)pair.get("buy"));
                    rateSell = new BigDecimal((Long)pair.get("sell"));
                } catch (Exception e1){
                    try {
                        rateLast = new BigDecimal((String)pair.get("last"));
                        rateBuy = new BigDecimal((String)pair.get("buy"));
                        rateSell = new BigDecimal((String)pair.get("sell"));
                    } catch (Exception e2){
                        //LOGGER.error(e.getMessage(), e);
                    }
                }
            }

            if (rateLast != null) {
                price = calcPrice(rateBuy, rateSell, rateLast);
                Rater.setRate(1079L, 1077L, this.courseName, price);
                LOGGER.info("WEX rate: BTC - USD " + price);
            }

        }

        if (json.containsKey("usd_rur")) {
            rateBuy = null;
            pair = (JSONObject) json.get("usd_rur");
            try {
                rateLast = new BigDecimal((Double)pair.get("last"));
                rateBuy = new BigDecimal((Double)pair.get("buy"));
                rateSell = new BigDecimal((Double)pair.get("sell"));
            } catch (Exception e){
                try {
                    rateLast = new BigDecimal((Long)pair.get("last"));
                    rateBuy = new BigDecimal((Long)pair.get("buy"));
                    rateSell = new BigDecimal((Long)pair.get("sell"));
                } catch (Exception e1){
                    try {
                        rateLast = new BigDecimal((String)pair.get("last"));
                        rateBuy = new BigDecimal((String)pair.get("buy"));
                        rateSell = new BigDecimal((String)pair.get("sell"));
                    } catch (Exception e2){
                        //LOGGER.error(e.getMessage(), e);
                    }
                }
            }

            if (rateBuy != null) {
                price = calcPrice(rateBuy, rateSell, rateLast);
                Rater.setRate(1077L, 1078L, this.courseName, price);
                LOGGER.info("WEX rate: USD - RUR " + price);
            }

        }

    }
}
