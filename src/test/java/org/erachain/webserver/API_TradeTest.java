package org.erachain.webserver;

import org.erachain.SettingTests;
import org.erachain.traders.CallRemoteApi;
import org.junit.Assert;
import org.junit.Test;
//import test.CallRemoteApi;
//import test.SettingTests;

/**
 * Test Trade API
 */
public class API_TradeTest extends SettingTests {

    /**
     * Test on get documentation by API inside project
     */
    @Test
    public void Default() {

         }

    /**
     * Test check remote default API
     * 1) status response
     * 2) format
     *
     * @throws Exception
     */
    @Test
    public void RemoteDefault() throws Exception {

        CallRemoteApi RemoteAPI = new CallRemoteApi();
        String resultStatus = RemoteAPI.ResponseCodeAPI(URL_REMOTE_NODE_API + "/org/erachain/api", "get");
        Assert.assertEquals(Integer.parseInt(resultStatus), 200);

        String resultValue = RemoteAPI.ResponseValueAPI(URL_REMOTE_NODE_API + "/org/erachain/api", "get", "");
        Assert.assertNotNull(resultValue);
        Assert.assertNotEquals(resultValue, "");

    }
}

