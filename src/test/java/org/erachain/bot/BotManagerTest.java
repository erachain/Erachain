package org.erachain.bot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import junit.framework.TestCase;
import org.json.simple.JSONObject;

public class BotManagerTest extends TestCase {

    static Gson GSON = new Gson();

    public void test() {

        System.out.println("111");

        JSONObject json = new JSONObject();
        SendMessage mess1 = new SendMessage(123L, "probe");
        Message mess = new Message();

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        json.put("testGson", gson.toJsonTree(mess));
//        json.put("testSimle", new JSONObject(mess));

        System.out.println(json.get("testGson").toString());

    }
}