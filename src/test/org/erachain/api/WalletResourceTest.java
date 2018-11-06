package org.erachain.api;

import org.erachain.SettingTests;
import org.erachain.api.ApiClient;
import org.junit.Assert;
import org.junit.Test;
//import test.SettingTests;

public class WalletResourceTest  {

    /**
     * unlock wallet in node run in localhost
     * <p>
     * Check wallet unlock
     */
    @Test
    public void unlock() {

        String resultWallet = new ApiClient().executeCommand("POST wallet/unlock "+ SettingTests.WALLET_PASSWORD);
        String data = resultWallet.replaceAll("\r\n", "");
        String[] pars = data.split(",");
        String val = pars[1].replace("}", "");
        boolean exist = val.toLowerCase().contains("true".toLowerCase());
        Assert.assertTrue(exist);

    }
}