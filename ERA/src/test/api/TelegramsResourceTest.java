package test.api;

import api.ApiClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;
import test.SettingTests;

import java.util.*;

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
    public void removeTelegram()throws Exception {


    }

    @Test
    public void getTelegramsTimestamp() throws ParseException {
        new ApiClient().executeCommand("POST wallet/unlock 1234567");
        String resultAddresses = new ApiClient().executeCommand("GET addresses");
        String[] parse = (resultAddresses.replace("\r\n", "").split(","));
        String address = (parse[0].replace("[", "").replace("]", ""))
                .trim().replace("\"", "");

        String recipient = "7Dpv5Gi8HjCBgtDN1P1niuPJQCBQ5H8Zob";
        /* String sendTelegram =*/
        new ApiClient().executeCommand("POST telegrams/send {\"sender\":\"" + address +
                "\",\"recipient\":\"" + recipient + "\",\"asset\":\"643\",\"amount\":\"0.01\"," +
                "\"title\":\"NPL\",\"istextmessage\":\"true\",\"encrypt\":\"true\",\"password\":\"123456789\"}");

        String getTelegram = new ApiClient().executeCommand("GET telegrams/address/" + recipient + "/timestamp/1");
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = (JSONArray) jsonParser.parse(getTelegram);

        Object objTransaction = (((JSONObject) jsonArray.get(0)).get("transaction")).toString();
        JSONArray jsonArrayTransaction = new JSONArray();
        jsonArrayTransaction.add(objTransaction);
        JSONArray g = (JSONArray) jsonParser.parse("[" + jsonArrayTransaction.get(0).toString() + "]");

        Map<String, String> requestField = new HashMap<String, String>() {{
            put("type_name", "type_name");
            put("creator", "creator");
            put("amount", "amount");
            put("signature", "signature");
            put("fee", "fee");
            put("publickey", "publickey");
            put("type", "type");
            put("confirmations", "confirmations");
            put("version", "version");
            put("record_type", "record_type");
            put("property2", "property2");
            put("action_key", "action_key");
            put("head", "head");
            put("property1", "property1");
            put("size", "size");
            put("action_name", "action_name");
            put("recipient", "recipient");
            put("backward", "backward");
            put("asset", "asset");
            put("sub_type_name", "sub_type_name");
            put("timestamp", "timestamp");
        }};

        Iterator<java.lang.Object> iterator = g.iterator();

        while (iterator.hasNext()) {
            JSONObject jsonObject = (JSONObject) iterator.next();
            for (Object key : jsonObject.keySet()) {
                Assert.assertEquals(key.toString(), requestField.get(key));
            }
        }

        new ApiClient().executeCommand("GET wallet/lock");
    }
}