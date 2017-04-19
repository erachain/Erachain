package webserver.wrapper;

import core.naming.Name;
import core.transaction.Transaction;
import utils.NumberAsString;

/**
 * Used for read only access on names for pebble injection. Here name
 * @author Skerberus
 *
 */
public class WebName {

	
	private final String name;
	private final String owner;
	private final String namebalanceString;

	public WebName(Name name) {
		this.name = name.getName();
		this.owner = name.getOwner().getAddress();
		namebalanceString = NumberAsString.getInstance().numberAsString(name.getOwner().getConfBalance3(0, Transaction.FEE_KEY).a) + " - " + name.getName();
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
