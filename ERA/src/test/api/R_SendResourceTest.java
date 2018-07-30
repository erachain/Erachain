package test.api;

import api.ApiClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Test;
import test.SettingTests;

import java.util.HashMap;
import java.util.Map;


public class R_SendResourceTest extends SettingTests {


    @Test
    public void help() {
    }

    @Test
    public void sendGet() throws Exception {

        ApiClient ApiClient = new ApiClient();
        ApiClient.executeCommand("POST wallet/unlock " + SettingTests.WALLET_PASSWORD);

        String resultAddresses = new ApiClient().executeCommand("GET addresses");
        String[] parse = (resultAddresses.replace("\r\n", "").split(","));
        String address = parse[1].replace("[", "").replace("]", "").trim().replace("\"", "");
        String sendTransaction = ApiClient.executeCommand("GET r_send/" + address + "/79MXsjo9DEaxzu6kSvJUauLhmQrB4WogsH?message={\\\"msg\\\":\\\"123\\\"}&encrypt=false&password=123456789 ");
        String result = sendTransaction;

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(result);

        Map<String, String> requestField = new HashMap<String, String>() {{
            put("type_name", "type_name");
            put("creator", "creator");
            put("message", "message");
            put("signature", "signature");
            put("fee", "fee");
            put("publickey", "publickey");
            put("type", "type");
            put("confirmations", "confirmations");
            put("version", "version");
            put("record_type", "record_type");
            put("property2", "property2");
            put("property1", "property1");
            put("size", "size");
            put("encrypted", "encrypted");
            put("recipient", "recipient");
            put("sub_type_name", "sub_type_name");
            put("isText", "isText");
            put("timestamp", "timestamp");
        }};

        for (Object key : jsonObject.keySet()) {
            Assert.assertEquals(key.toString(), requestField.get(key));
        }

        ApiClient.executeCommand("GET wallet/lock");
    }

    @Test
    public void sendPost() throws Exception {
        ApiClient ApiClient = new ApiClient();
        ApiClient.executeCommand("POST wallet/unlock " + SettingTests.WALLET_PASSWORD);

        String resultAddresses = new ApiClient().executeCommand("GET addresses");
        String[] parse = (resultAddresses.replace("\r\n", "").split(","));
        String address = parse[0].replace("[", "").replace("]", "").trim().replace("\"", "");
        String sendTransaction = ApiClient.executeCommand("POST r_send {\"creator\":\"" + address + "\"," +
                "\"recipient\":\"79MXsjo9DEaxzu6kSvJUauLhmQrB4WogsH\",\"feePow\":\"1\",\"assetKey\":\"643\",\"amount\":\"1\"," +
                "\"title\":\"123\",\"message\":\"{\"\"msg\"\":\"\"1223\"\"}\",\"codebase\":\"0\",\"encrypt\":\"false\",\"password\":\"123456789\"}");

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(sendTransaction);

        Map<String, String> requestField = new HashMap<String, String>() {{
            put("type_name", "type_name");
            put("creator", "creator");
            put("amount", "amount");
            put("data", "data");
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
            put("encrypted", "encrypted");
            put("action_name", "action_name");
            put("recipient", "recipient");
            put("backward", "backward");
            put("asset", "asset");
            put("sub_type_name", "sub_type_name");
            put("isText", "isText");
            put("timestamp", "timestamp");
        }};


        for (Object key : jsonObject.keySet()) {
            Assert.assertEquals(key.toString(), requestField.get(key));
        }

        ApiClient.executeCommand("GET wallet/lock");

    }
}