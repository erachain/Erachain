package org.erachain.bot.telegram;

import com.google.gson.Gson;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.datachain.DCSet;
import org.erachain.settings.Settings;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.fail;

public class ErachainBotClsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErachainStorageBotTest.class.getSimpleName());

    static Gson GSON = new Gson();
    byte[] seed = Crypto.getInstance().digest("tes213sdffsdft".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);

    byte[] signature;

    byte feePow = 0;
    Controller cnt;
    DCSet dcSet;
    BlockChain chain;
    GenesisBlock gb;

    ErachainStorageBot bot;

    private void init() {
        Settings.NET_MODE = Settings.NET_MODE_DEMO;
        //Settings.NET_MODE = Settings.NET_MODE_MAIN;

        DCSet.reCreateDBinMEmory(false, false);

        cnt = Controller.getInstance();
        cnt.setDCSet(dcSet);

        try {
            cnt.start();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            fail(e.getMessage());
        }

        chain = cnt.blockChain;
        dcSet = cnt.getDCSet();

        gb = chain.getGenesisBlock();

        bot = (ErachainStorageBot) cnt.botsErachain.get(1);
        bot.test = true;

    }


    /**
     * Для запуска бота задать переменную среды (или в параметрах запуска)
     * bot_storage_token=731...
     */
    @Test
    public void botAnswerAdmin() {
        init();

        Long chatId = -1002147068743L;

        JSONObject chatJson = new JSONObject();
        chatJson.put("id", chatId);
        JSONObject messageJson = new JSONObject();
        messageJson.put("chat", chatJson);

        bot.botAnswerAdmin("receiver = 7QKXrHLRM1gd7bFjD2K5RYjYJfCPqJwoHf 7BzBGpBuZ9ZKRnzYYdVgJyFRVq7okg8ryf".split(" "), null, null);

    }

}