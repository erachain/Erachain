package org.erachain.webserver;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.webserver.wrapper.WebAccount;
import org.erachain.webserver.wrapper.WebName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to get wallet specific information using pebble (read only)
 *
 * @author Skerberus
 */
public class ControllerWebResource {

    private static ControllerWebResource instance = new ControllerWebResource();

    public static ControllerWebResource getInstance() {
        if (instance == null) {
            instance = new ControllerWebResource();
        }
        return instance;
    }


    // we need to use string because of pebble here instead of boolean
    public List<WebName> getNames(String removeZeroBalance) {
        List<WebName> results = new ArrayList<>();
        List<Account> myNames = new ArrayList<Account>(Controller.getInstance()
                .getWalletAccounts());
        for (Account name : myNames) {
            if (Boolean.valueOf(removeZeroBalance)) {
                if (name.getConfBalance3(0, Transaction.FEE_KEY).a.compareTo(BigDecimal.ZERO) > 0) {
                    results.add(new WebName(name));
                }

            } else {
                results.add(new WebName(name));
            }
        }
        return results;
    }


    public List<WebAccount> getAccounts(String removeZeroBalance) {
        List<WebAccount> results = new ArrayList<>();

        if (Controller.getInstance().doesWalletDatabaseExists()) {
            ArrayList<Account> realAccs = new ArrayList<Account>(Controller.getInstance()
                    .getWalletAccounts());

            for (Account account : realAccs) {
                if (Boolean.valueOf(removeZeroBalance)) {
                    if (account.getConfBalance3(0, Transaction.FEE_KEY).a.compareTo(BigDecimal.ZERO) > 0) {
                        results.add(new WebAccount(account));
                    }
                } else {
                    results.add(new WebAccount(account));
                }
            }

        }

        return results;

    }

}
