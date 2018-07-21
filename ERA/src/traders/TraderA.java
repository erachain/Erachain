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

public class TraderA extends Trader {

    private static final Logger LOGGER = Logger.getLogger(TraderA.class);

    public TraderA(TradersManager tradersManager, int sleepSec) {
        super(tradersManager, sleepSec);

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
            Rater.rates.put(new Fun.Tuple2<Long, Long>(12L, 92L), price);
        }
        if (json.containsKey("btc_usd")) {
            pair = (JSONObject) json.get("btc_usd");
            price = new BigDecimal((Double)pair.get("avg")).setScale(10, BigDecimal.ROUND_HALF_UP);
            price = price.multiply(this.shiftRate).setScale(10, BigDecimal.ROUND_HALF_UP);
            Rater.rates.put(new Fun.Tuple2<Long, Long>(12L, 95L), price);
        }

    }
}
