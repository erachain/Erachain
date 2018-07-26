package traders;
// 30/03 ++

import api.ApiErrorFactory;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.TreeMap;

// import org.apache.log4j.Logger;
//import core.BlockChain;
//import database.DBSet;
//import database.TransactionMap;
//import lang.Lang;

public class TraderA extends Trader {

    private static final Logger LOGGER = Logger.getLogger(TraderA.class);

    public TraderA(TradersManager tradersManager, String accountStr, int sleepSec, long haveKey, long wantKey,
                   TreeMap<BigDecimal, BigDecimal> scheme, boolean cleanAllOnStart) {
        super(tradersManager, accountStr, sleepSec, cleanAllOnStart);

        this.scheme = scheme;

        this.haveKey = haveKey;
        this.wantKey = wantKey;

        this.haveAsset = dcSet.getItemAssetMap().get(haveKey);
        this.wantAsset = dcSet.getItemAssetMap().get(wantKey);

    }

}
