package org.erachain.webserver.wrapper;

import org.erachain.core.naming.Name;
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

    public WebName(Name name) {
        this.name = name.getName();
        this.owner = name.getOwner().getAddress();
        namebalanceString = NumberAsString.formatAsString(name.getOwner().getConfBalance3(0, Transaction.FEE_KEY).a) + " - " + name.getName();
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
