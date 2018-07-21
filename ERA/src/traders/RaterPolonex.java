package traders;
// 30/03 ++

import api.ApiErrorFactory;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;

import java.math.BigDecimal;

// import org.apache.log4j.Logger;
//import core.BlockChain;
//import database.DBSet;
//import database.TransactionMap;
//import lang.Lang;

public class RaterPolonex extends Rater {

    private static final Logger LOGGER = Logger.getLogger(RaterPolonex.class);

    public RaterPolonex(Traders trader, int sleepSec) {
        super(trader, sleepSec);

        // https://poloniex.com/support/api/v1/
        //https://poloniex.com/public?command=returnTicker&
        //https://poloniex.com/public?command=returnTicker&pair=BTC_ETH - return ALL
        this.apiURL = "https://poloniex.com/public?command=returnTicker";

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

        if (json.containsKey("USDT_BTC")) {
            pair = (JSONObject) json.get("USDT_BTC");
            price = new BigDecimal((String)pair.get("last"));
            price = price.multiply(this.shiftRate).setScale(10, BigDecimal.ROUND_HALF_UP);
            Rater.rates.put(new Fun.Tuple2<Long, Long>(95L, 12L), price);
        }

    }
}
