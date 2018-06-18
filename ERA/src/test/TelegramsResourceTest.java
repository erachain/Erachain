package test;

import api.ApiClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class TelegramsResourceTest extends SettingTests {
    /**
     * In this test we checked:
     * 1) unlock wallet in localhost node
     * 2) get address from wallet
     * 3) send telegram
     * 4) check field response
     *
     * @throws Exception
     */
    @Test
    public void send() throws Exception {

/**
 * unlock wallet in node run in localhost
 *
 * Check wallet unlock
 */
        String resultWallet = new ApiClient().executeCommand("POST wallet/unlock 1234567");
        String data = resultWallet.replaceAll("\r\n", "");
        String[] pars = data.split(",");
        String val = pars[1].replace("}", "");
        boolean exist = val.toLowerCase().contains("true".toLowerCase());
        Assert.assertTrue(exist);

/**
 * Get address from wallet in local node
 * check if address not exist
 */
        String resultAddresses = new ApiClient().executeCommand("GET addresses");

        String[] parse = (resultAddresses.replace("\r\n", "").split(","));
        String address = parse[0].replace("[", "").replace("]", "");
        Assert.assertNotEquals(address, "");


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


        String r = new ApiClient().executeCommand("POST telegrams/send {'sender': '" + address + "', " +
                "'recipient': '7C5HJALxTbAhzyhwVZeDCsGqVnSwcdEtqu', " +
                "'asset': 2, " +
                "'amount': '0.0001', " +
                "'title': 'title'," +
                "'message': 'some text', " +
                "'istextmessage': 'true', " +
                "'encrypt': false, " +
                "'password': '12' }");
    }
}