package org.erachain.api;

import org.erachain.CallRemoteApi;
import org.erachain.SettingTests;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;
//import test.CallRemoteApi;
//import test.SettingTests;

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

        new ApiClient().executeCommand("POST wallet/unlock " + WALLET_PASSWORD);

        String resultAddresses = new ApiClient().executeCommand("GET addresses");
        JSONParser jsonParserRequest = new JSONParser();
        JSONArray jsonObjectRequest = (JSONArray) jsonParserRequest.parse(resultAddresses);


/**
 *Send telegram to address
 * check send
 */


        String sendTelegram = new ApiClient().executeCommand("GET telegrams/send/" + jsonObjectRequest.get(0)
                + "/7C5HJALxTbAhzyhwVZeDCsGqVnSwcdEtqu/2/0.0001/title/message/true/false/1");
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
        new ApiClient().executeCommand("POST wallet/unlock " + WALLET_PASSWORD);

        String resultAddresses = new ApiClient().executeCommand("GET addresses");

        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArrayAddress = (JSONArray) jsonParser.parse(resultAddresses);
        String address = jsonArrayAddress.get(0).toString();

        String sendTelegram = new ApiClient().executeCommand("POST telegrams/send {\"sender\":\"" + address + "\",\"recipient\":\"7Dpv5Gi8HjCBgtDN1P1niuPJQCBQ5H8Zob\",\"asset\":\"643\",\"amount\":\"0.01\",\"title\":\"NPL\",\"istext\":\"true\",\"encrypt\":\"true\",\"password\":\"" + WALLET_PASSWORD + "\"}");

        String sendRequest = "[ " + sendTelegram + "]";
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
    public void deleteTelegram() throws Exception {

        new ApiClient().executeCommand("POST wallet/unlock " + WALLET_PASSWORD);
        String resultAddresses = new ApiClient().executeCommand("GET addresses");

        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArrayAddress = (JSONArray) jsonParser.parse(resultAddresses);
        String address = jsonArrayAddress.get(0).toString();
        for (int i = 0; i < 5; i++) {
            new ApiClient().executeCommand("POST telegrams/send {\"sender\":\"" + address +
                    "\",\"recipient\":\"7Dpv5Gi8HjCBgtDN1P1niuPJQCBQ5H8Zob\",\"asset\":\"643\",\"amount\":\"0.01\",\"title\":\"NPL\",\"istext\":\"true\",\"encrypt\":\"true\",\"password\":\"123456789\"}");
        }

        String telegramList = new ApiClient().executeCommand("GET telegrams/timestamp/1");
        JSONArray jsonArray = (JSONArray) jsonParser.parse(telegramList);
        JSONArray jsonArraySign = new JSONArray();
        for (Object object : jsonArray) {
            jsonArraySign.add(((JSONObject) ((JSONObject) object).get("transaction")).get("signature").toString());
        }
        JSONObject result = new JSONObject();
        result.put("list", jsonArraySign);

        String callRemoteAPI = new CallRemoteApi().ResponseValueAPI
                (URL_LOCAL_NODE_RPC + "/telegrams/delete", "post", result.toJSONString());
        //   String callRemoteAPI = new ApiClient().executeCommand("POST telegrams/delete " + result.toJSONString());
        JSONObject countTelegrams = (JSONObject) jsonParser.parse(callRemoteAPI);
        Assert.assertEquals(countTelegrams.size(), 0);
    }

    @Test
    public void getTelegramsTimestamp() throws ParseException {
        new ApiClient().executeCommand("POST wallet/unlock " + WALLET_PASSWORD);
        String resultAddresses = new ApiClient().executeCommand("GET addresses");
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArrayAddress = (JSONArray) jsonParser.parse(resultAddresses);
        String address = jsonArrayAddress.get(0).toString();

        String recipient = "7Dpv5Gi8HjCBgtDN1P1niuPJQCBQ5H8Zob";

        new ApiClient().executeCommand("POST telegrams/send {\"sender\":\"" + address +
                "\",\"recipient\":\"" + recipient + "\",\"asset\":\"643\",\"amount\":\"0.01\"," +
                "\"title\":\"NPL\",\"istext\":\"true\",\"encrypt\":\"true\",\"password\":\"123456789\"}");

        String getTelegram = new ApiClient().executeCommand("GET telegrams/address/" + recipient + "/timestamp/1");
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
            put("title", "title");
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