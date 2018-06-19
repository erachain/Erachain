package test.api;

import api.ApiClient;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class WalletResourceTest {

    @Test
    public void unlock() {
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

    }
}