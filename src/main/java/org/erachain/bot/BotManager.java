package org.erachain.bot;

import org.erachain.bot.telegram.ErachainBotCls;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.ntp.NTP;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import static org.erachain.core.item.assets.AssetCls.FEE_KEY;
import static org.erachain.core.transaction.Transaction.VALIDATE_OK;

public class BotManager {
    public final static ConcurrentHashMap<String, String> knownPubKey = new ConcurrentHashMap<>();

    TimerTask task;

    public BotManager(Controller cnt) {

        task = new Job(cnt);

        // Запустим работу раз в час
        new Timer().schedule(task, 5000,
                // каждый час просмотр балансов
                1000 * 60 * 60);
    }

    public void cancel() {
        task.cancel();
    }

    class Job extends TimerTask {

        public final ConcurrentHashMap<Account, Fun.Tuple3<ErachainBotCls, BigDecimal, JSONObject>> charges = new ConcurrentHashMap<>();
        int lastDay = -1;

        Controller cnt;
        BigDecimal half = new BigDecimal("0.5");

        String lang = "ru";

        Job(Controller cnt) {
            this.cnt = cnt;
        }

        private void reCharge(Fun.Tuple3<ErachainBotCls, BigDecimal, JSONObject> charge, Account account) {
            long key = FEE_KEY;
            String title = charge.a.getBotUserNameF() + " recharge @" + charge.c.get("name");
            byte[] message = null;
            byte[] isText = new byte[]{1};
            byte[] encrypted = new byte[]{0};
            long flags = 0L;
            Transaction rSend = new RSend(charge.a.getBotPrivateKey(), account, key, charge.b, title, message, isText, encrypted, flags);
            cnt.createForNetwork(rSend);
            int validate = cnt.afterCreateForNetwork(rSend, false, false);
            if (validate != VALIDATE_OK) {
                String mess = String.format("Заканчиваются средства на счете %s бота %s", charge.a.getBotPrivateKey().getAddress(), charge.a.getBotUserNameMD());

                JSONObject out = new JSONObject();
                rSend.updateMapByError2(out, validate, lang);
                mess += "\n```java Error\n" + out.toJSONString() + "```";
                charge.a.sendToAdminMessage(mess);
            }

        }

        @Override
        public void run() {

            Timestamp time = new Timestamp(NTP.getTime());
            int newDay = time.getDay();
            if (lastDay != newDay) {
                lastDay = newDay;
                // если новый день, то сбросим
                cnt.botsErachain.forEach(bot -> bot.getRechargeable(charges));
            }

            List<String> toRemove = new ArrayList<>();
            charges.forEach((account, charge) -> {
                BigDecimal balance = account.getBalanceForPosition(cnt.getDCSet(), AssetCls.FEE_KEY, Account.BALANCE_POS_OWN).b;
                if (charge.b.multiply(half).compareTo(balance) > 0) {
                    // Пополним на весь объем и вычеркнем из наблюдений на сегодня
                    charges.remove(account);
                    reCharge(charge, account);
                }
            });
        }
    }
}
