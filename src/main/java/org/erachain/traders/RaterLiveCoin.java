package org.erachain.traders;
// 30/03 ++

import org.erachain.api.ApiErrorFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;

import java.math.BigDecimal;

// import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
//import org.erachain.core.BlockChain;
//import org.erachain.database.DLSet;
//import database.TransactionMap;
//import org.erachain.lang.Lang;

public class RaterLiveCoin extends Rater {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaterLiveCoin.class);

    public RaterLiveCoin(TradersManager tradersManager, int sleepSec) {
        super(tradersManager, "livecoin", sleepSec);

        this.apiURL = "https://api.livecoin.net/exchange/ticker?currencyPair=ETH/BTC";

    }

    protected void parse(String result) {
        JSONObject json = null;
        try {
            //READ JSON
            json = (JSONObject) JSONValue.parse(result);
        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            ///logger.error(e.getMessage());
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
