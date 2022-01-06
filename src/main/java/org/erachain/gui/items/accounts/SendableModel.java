package org.erachain.gui.items.accounts;

import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;

public interface SendableModel {

    PublicKeyAccount getCreator(int row);

    Account getRecipent(int row);

}
