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

        this.apiURL = "https://wex.nz/api/3/ticker/btc_rur-btc_usd";

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

        if (json.containsKey("btc_rur")) {
            pair = (JSONObject) json.get("btc_rur");
            price = new BigDecimal((Double)pair.get("avg")).setScale(10, BigDecimal.ROUND_HALF_UP);
            price = price.multiply(this.shiftRate).setScale(10, BigDecimal.ROUND_HALF_UP);
            Rater.rates.put(new Fun.Tuple3<Long, Long, String>(12L, 92L, this.courseName), price);
        }
        if (json.containsKey("btc_usd")) {
            pair = (JSONObject) json.get("btc_usd");
            price = new BigDecimal((Double)pair.get("avg")).setScale(10, BigDecimal.ROUND_HALF_UP);
            price = price.multiply(this.shiftRate).setScale(10, BigDecimal.ROUND_HALF_UP);
            Rater.rates.put(new Fun.Tuple3<Long, Long, String>(12L, 95L, this.courseName), price);
        }

    }
}
