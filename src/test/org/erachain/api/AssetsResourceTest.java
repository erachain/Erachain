package org.erachain.api;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Test;
import test.CallRemoteApi;
import test.SettingTests;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Testing RPC Asset
 */
public class AssetsResourceTest extends SettingTests {

    @Test
    public void getAseetsLite() throws Exception {

        CallRemoteApi CallRemoteApi = new CallRemoteApi();

        String resultRemote = CallRemoteApi.ResponseCodeAPI
                (URL_LOCAL_NODE_RPC + "/assets", "GET");
        switch (resultRemote) {
            case "0":
                fail("Remote EraChain node is not run");
                break;
            case "200":
                break;
            default:
                fail("Error(http): " + resultRemote);
        }
        Assert.assertEquals(resultRemote, "200");

        String resultAsset = CallRemoteApi.ResponseValueAPI
                (URL_LOCAL_NODE_RPC + "/assets", "GET", "");

        JSONParser jsonParser = new JSONParser();
        try {
            Map<String, String> AssetMap = new HashMap<String, String>(2) {
                {
                    put("1", "ERA");
                    put("2", "COMPU");
                }
            };
            JSONObject jsonObject = (JSONObject) jsonParser.parse(resultAsset);

            Set setAsset = AssetMap.keySet();
            for (Object key : setAsset) {
                Object valueByKey = jsonObject.get(key);
                if (valueByKey == null)
                    fail("'" + key + "' value key is not found in specified array");
            }
        } catch (Exception e) {
            fail("ERROR: " + e.getMessage());
        }
    }

    @Test
    public void getAssetsFull() throws Exception {
        CallRemoteApi CallRemoteApi = new CallRemoteApi();

        String resultRemote = CallRemoteApi.ResponseCodeAPI
                (URL_LOCAL_NODE_RPC + "/assets/full", "GET");
        switch (resultRemote) {
            case "0":
                fail("Remote EraChain node is not run");
                break;
            case "200":
                break;
            default:
                fail("Error(http): " + resultRemote);
        }
        Assert.assertEquals(resultRemote, "200");

        Map<String, String> AssetFullMap = new HashMap<String, String>() {
            {
                put("key", "key");
                put("name", "name");
                put("description", "description");
                put("owner", "owner");
                put("quantity", "quantity");
                put("scale", "scale");
                put("assetType", "assetType");
                put("img", "img");
                put("icon", "icon");
                put("operations", "operations");
            }
        };

        String resultAssetFull = CallRemoteApi.ResponseValueAPI(URL_LOCAL_NODE_RPC + "/assets/full", "GET", "");

        JSONParser jsonParser = new JSONParser();
        JSONObject arrayAssetFull = (JSONObject) jsonParser.parse(resultAssetFull);

        JSONObject element = (JSONObject) arrayAssetFull.get("1");
        for (Object key : element.keySet()) {
            Assert.assertEquals(AssetFullMap.get(key), key);
        }
    }

    @Test
    public void getAssetLite() {
    }

    @Test
    public void getAsset() {
    }
}