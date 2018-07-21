package traders;
// 30/03 ++

import org.apache.log4j.Logger;

// import org.apache.log4j.Logger;
//import core.BlockChain;
//import database.DBSet;
//import database.TransactionMap;
//import lang.Lang;

public class RaterWEX extends Rater {

    private static final Logger LOGGER = Logger.getLogger(RaterWEX.class);

    public RaterWEX(Traders trader, int sleepSec) {
        super(trader, sleepSec);

        this.apiURL = "https://wex.nz/api/3/ticker/btc_rur-btc_usd";

    }

}
