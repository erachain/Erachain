package api;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Test;
import test.SettingTests;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class TelegramsResourceTest extends SettingTests {
    /**
     * In this test we checked:
     * 1) send telegram
     * 2) check field response
     *
     * @throws Exception
     */
    @Test
    public void send() throws Exception {

        new ApiClient().executeCommand("POST wallet/unlock 1234567");

        String resultAddresses = new ApiClient().executeCommand("GET addresses");
        String[] parse = (resultAddresses.replace("\r\n", "").split(","));
        String address = parse[0].replace("[", "").replace("]", "");


/**
 *Send telegram to address
 * check send
 */
        String sendTelegram = new ApiClient().executeCommand("GET telegrams/send/" + address.trim()
                .replace("\"", "") + "/7C5HJALxTbAhzyhwVZeDCsGqVnSwcdEtqu/2/0.0001/title/message/true/false/1");
        String sendRequest = "[ " + sendTelegram + "]";
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = (JSONArray) jsonParser.parse(sendRequest);

        Map<String, String> requestField = new HashMap<String, String>() {{
            put("signature", "signature");
        }};
        Iterator<Object> iterator = jsonArray.iterator();
        while (iterator.hasNext()) {
            JSONObject jsonObject = (JSONObject) iterator.next();
            for (Object key : jsonObject.keySet()) {
                Assert.assertNotNull(requestField.get(key));
            }
        }
/**
 * again lock wallet
 */
        new ApiClient().executeCommand("GET wallet/lock");
    }

    @Test
    public void sendPost() throws Exception {
        new ApiClient().executeCommand("POST wallet/unlock 1234567");

        String resultAddresses = new ApiClient().executeCommand("GET addresses");
        String[] parse = (resultAddresses.replace("\r\n", "").split(","));
        String address = (parse[0].replace("[", "").replace("]", ""))
                .trim().replace("\"", "");

        String sendTelegram = new ApiClient().executeCommand("POST telegrams/send {\"sender\":\"" + address + "\",\"recipient\":\"7Dpv5Gi8HjCBgtDN1P1niuPJQCBQ5H8Zob\",\"asset\":\"643\",\"amount\":\"0.01\",\"title\":\"NPL\",\"istextmessage\":\"true\",\"encrypt\":\"true\",\"password\":\"123456789\"}");

        String sendRequest = "[ " + sendTelegram + "]";
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = (JSONArray) jsonParser.parse(sendRequest);

        Map<String, String> requestField = new HashMap<String, String>() {{
            put("signature", "signature");
        }};
        Iterator<Object> iterator = jsonArray.iterator();
        while (iterator.hasNext()) {
            JSONObject jsonObject = (JSONObject) iterator.next();
            for (Object key : jsonObject.keySet()) {
                Assert.assertEquals(key.toString(), requestField.get(key));
            }
        }
    }

    @Test
    public void getTelegramsTwo() {

    }

    @Test
    public void getTelegramsTimestamp() {
    }
}