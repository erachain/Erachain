package org.erachain.bot.telegram;

import com.google.gson.Gson;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.response.BaseResponse;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ErachainStorageBotTest {

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

    String lang = "ru";

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

    @Test
    public void getShortName() {
    }

    /**
     * Для запуска бота задать переменную среды (или в параметрах запуска)
     * bot_storage_token=731...
     */
    @Test
    public void processChat() {
        init();

        Long chatId = -1002147068743L;
        Long replyChatId = chatId;

        JSONObject chatJson = new JSONObject();
        chatJson.put("id", chatId);
        JSONObject messageJson = new JSONObject();
        messageJson.put("chat", chatJson);

        messageJson.put("text", "...simple test - Простой Тест");
        Message message = GSON.fromJson(messageJson.toString(), Message.class);
        bot.processMessage(chatId, replyChatId, message.chat(), message, lang);

        messageJson.put("text", ":store:open\n...Сохранить Открыто\nвесь текст");
        message = GSON.fromJson(messageJson.toString(), Message.class);
        bot.processMessage(chatId, replyChatId, message.chat(), message, lang);

        messageJson.put("text", "...Сохранить Открыто\nвесь текст\n:store:open");
        message = GSON.fromJson(messageJson.toString(), Message.class);
        bot.processMessage(chatId, replyChatId, message.chat(), message, lang);

        messageJson.put("text", "...Сохранить Открыто\n:store:open\nТолько этот кусок теста");
        message = GSON.fromJson(messageJson.toString(), Message.class);
        bot.processMessage(chatId, replyChatId, message.chat(), message, lang);

        messageJson.put("text", "...Сохранить Открыто\n:store:open\nТолько этот кусок теста\n:\nНо не этот кусок");
        message = GSON.fromJson(messageJson.toString(), Message.class);
        bot.processMessage(chatId, replyChatId, message.chat(), message, lang);

        messageJson.put("text", "...Сохранить Открыто - весь оригинал\n включая команду\n:store:open:orig");
        message = GSON.fromJson(messageJson.toString(), Message.class);
        bot.processMessage(chatId, replyChatId, message.chat(), message, lang);

    }

    @Test
    public void processHelp() {
        init();

        //Long chatId = -1002147068743L;
        Long chatId = 167208327L;

        String mess = bot.getGreetingsHelp(false, lang);
        //assertEquals("Для полной помощи используйте команды `help`, `помоги` или `помощь`\n", mess);
        System.out.println(mess);
        BaseResponse result = bot.sendMarkdown(chatId, mess);
        assertEquals(result.isOk(), true);

        bot.sendMarkdown(chatId, "```java\n{\"error1\":{\"code\":25,\"message\":\"Creator Account is not personalized\",\"lang\":\"ru\",\"value\":\"2Lq5CutaLEiesnbcaB3QLJg5V2goTKEm4mB7Hsh37oTS\",\"local\":\"Счет создателя не персонализирован\"}}```");
        bot.sendMarkdown(chatId, "```java {\"error1\":{\"code\":25,\"message\":\"Creator Account is not personalized\",\"lang\":\"ru\",\"value\":\"2Lq5CutaLEiesnbcaB3QLJg5V2goTKEm4mB7Hsh37oTS\",\"local\":\"Счет создателя не персонализирован\"}}```");
        bot.sendMarkdown(chatId, "```java\n\n{\"error2\":{\"code\":25,\"message\":\"Creator Account is not personalized\",\"lang\":\"ru\",\"value\":\"2Lq5CutaLEiesnbcaB3QLJg5V2goTKEm4mB7Hsh37oTS\",\"local\":\"Счет создателя не персонализирован\"}}\n```");
        bot.sendMarkdown(chatId, "```json\njava\n{\"error3\":{\"code\":25,\"message\":\"Creator Account is not personalized\",\"lang\":\"ru\",\"value\":\"2Lq5CutaLEiesnbcaB3QLJg5V2goTKEm4mB7Hsh37oTS\",\"local\":\"Счет создателя не персонализирован\"}}\n```");
        bot.sendMarkdown(chatId, "```json\n\n{\"error4\":{\"code\":25,\"message\":\"Creator Account is not personalized\",\"lang\":\"ru\",\"value\":\"2Lq5CutaLEiesnbcaB3QLJg5V2goTKEm4mB7Hsh37oTS\",\"local\":\"Счет создателя не персонализирован\"}}\n```");
        bot.sendMarkdown(chatId, "```json {\"error5\":{\"code\":25,\"message\":\"Creator Account is not personalized\",\"lang\":\"ru\",\"value\":\"2Lq5CutaLEiesnbcaB3QLJg5V2goTKEm4mB7Hsh37oTS\",\"local\":\"Счет создателя не персонализирован\"}}\n```");
        bot.sendMarkdown(chatId, "``` json\njava\n{\"error6\":{\"code\":25,\"message\":\"Creator Account is not personalized\",\"lang\":\"ru\",\"value\":\"2Lq5CutaLEiesnbcaB3QLJg5V2goTKEm4mB7Hsh37oTS\",\"local\":\"Счет создателя не персонализирован\"}}\n```");
        bot.sendMarkdown(chatId, "``` json\n\njava\n\n{\"error7\":{\"code\":25,\"message\":\"Creator Account is not personalized\",\"lang\":\"ru\",\"value\":\"2Lq5CutaLEiesnbcaB3QLJg5V2goTKEm4mB7Hsh37oTS\",\"local\":\"Счет создателя не персонализирован\"}}\n```");
        bot.sendMarkdown(chatId, "``` json java\n\n{\"error8\":{\"code\":25,\"message\":\"Creator Account is not personalized\",\"lang\":\"ru\",\"value\":\"2Lq5CutaLEiesnbcaB3QLJg5V2goTKEm4mB7Hsh37oTS\",\"local\":\"Счет создателя не персонализирован\"}}\n```");
    }
}