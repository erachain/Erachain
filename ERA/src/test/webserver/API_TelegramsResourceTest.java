package webserver;

import api.ApiClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;
import test.CallRemoteApi;
import test.SettingTests;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class API_TelegramsResourceTest extends SettingTests {

    /**
     * check documentation is not null and status 200
     */
    @Test
    public void Default() {
        Response annotation = new API_TelegramsResource().Default();
        Assert.assertEquals(annotation.getStatus(), 200);
        Assert.assertNotNull(annotation.getEntity());
    }

    @Test
    public void DefaultRemote() throws Exception {
        CallRemoteApi RemoteAPI = new CallRemoteApi();
        String resultStatus = RemoteAPI.ResponseCodeAPI(SettingTests.URL_REMOTE_NODE + "/apitelegrams", "get");
        Assert.assertEquals(Integer.parseInt(resultStatus), 200);

        String resultValue = RemoteAPI.ResponseValueAPI(SettingTests.URL_REMOTE_NODE + "/apitelegrams", "get");
        Assert.assertNotNull(resultValue);
        Assert.assertNotEquals(resultValue, "");

    }

    @Test
    public void getTelegramBySignature() {

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
            put("isText", "isText");
            put("timestamp", "timestamp");
        }};

        Iterator<java.lang.Object> iterator = g.iterator();

        while (iterator.hasNext()) {
            JSONObject jsonObject = (JSONObject) iterator.next();
            for (Object key : jsonObject.keySet()) {
                Assert.assertNotNull(requestField.get(key));
            }
        }

        new ApiClient().executeCommand("GET wallet/lock");
    }
}