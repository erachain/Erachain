package test.webserver;

import org.junit.Assert;
import org.junit.Test;
import test.CallRemoteApi;
import test.SettingTests;
import api.ApiClient;
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
        String resultStatus = RemoteAPI.ResponseCodeAPI(SettingTests.URL_REMOTE_NODE + "/api", "get");
        Assert.assertEquals(Integer.parseInt(resultStatus), 200);

        String resultValue = RemoteAPI.ResponseValueAPI(SettingTests.URL_REMOTE_NODE + "/api", "get", "");
        Assert.assertNotNull(resultValue);
        Assert.assertNotEquals(resultValue, "");

    }
}

