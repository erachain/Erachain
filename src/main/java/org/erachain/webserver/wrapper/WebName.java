package org.erachain.webserver.wrapper;

import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.utils.NumberAsString;

/**
 * Used for read only access on names for pebble injection. Here name
 *
 * @author Skerberus
 */
public class WebName {


    private final String name;
    private final String owner;
    private final String namebalanceString;

    public WebName(Account name) {
        this.name = name.getAddress();
        this.owner = name.getAddress();
        namebalanceString = NumberAsString.formatAsString(name.getConfBalance3(0, Transaction.FEE_KEY).a) + " - " + name.getFromFavorites();
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public String getNameBalanceString() {
        return namebalanceString;
    }
}
