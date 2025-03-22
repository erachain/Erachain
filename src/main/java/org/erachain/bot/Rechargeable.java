package org.erachain.bot;

import org.erachain.bot.telegram.ErachainBotCls;
import org.erachain.core.account.Account;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.Map;

public interface Rechargeable {

    void getRechargeable(Map<Account, Fun.Tuple3<ErachainBotCls, BigDecimal, JSONObject>> recharges);

}
