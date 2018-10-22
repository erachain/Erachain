package org.erachain.traders;
// 30/03 ++

import org.erachain.api.ApiErrorFactory;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;

import java.math.BigDecimal;

// import org.apache.log4j.Logger;
//import org.erachain.core.BlockChain;
//import org.erachain.database.DBSet;
//import database.TransactionMap;
//import org.erachain.lang.Lang;

public class RaterLiveCoin extends Rater {

    private static final Logger LOGGER = Logger.getLogger(RaterLiveCoin.class);

    public RaterLiveCoin(TradersManager tradersManager, int sleepSec) {
        super(tradersManager, "livecoin", sleepSec);

        this.apiURL = "https://org.erachain.api.livecoin.net/exchange/ticker?currencyPair=ETH/BTC";

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

        if (json.containsKey("symbol")
                && "ETH/BTC".equals((String)json.get("symbol"))) {
            price = new BigDecimal(json.get("vwap").toString()).setScale(10, BigDecimal.ROUND_HALF_UP);
            price = price.multiply(this.shiftRate).setScale(10, BigDecimal.ROUND_HALF_UP);
            setRate(14L, 12L, this.courseName, price);
        }

    }
}
