package org.erachain.utils;

import org.erachain.core.account.Account;
import org.erachain.core.naming.Name;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.NameStorageMap;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.NameMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NameUtils {

    public static Pair<Account, NameResult> nameToAdress(String name) {
        NameMap names = DCSet.getInstance().getNameMap();
        // NAME NOT REGISTERED?
        if (!names.contains(name)) {

            return new Pair<Account, NameUtils.NameResult>(null,
                    NameResult.NAME_NOT_REGISTERED);

        }

        // NAME STARTS OR ENDS WITH SPACE?
        if (name.startsWith(" ") || name.endsWith(" ")) {
            return new Pair<Account, NameUtils.NameResult>(null,
                    NameResult.NAME_WITH_SPACE);
        }

        // NAME FOR SALE?
        if (DCSet.getInstance().getNameExchangeMap().contains(name)) {
            return new Pair<Account, NameUtils.NameResult>(null,
                    NameResult.NAME_FOR_SALE);
        }

        // LOOKUP ADDRESS FOR NAME
        Name lookupName = names.get(name);
        String recipientAddress = lookupName.getOwner().getAddress();
        Account recipient = new Account(recipientAddress);
        return new Pair<Account, NameUtils.NameResult>(recipient, NameResult.OK);
    }

    public static List<Pair<String, String>> getWebsitesByValue(
            String searchvalue) {
        return getWebsitesbyValueInternal(searchvalue);

    }

    public static List<Pair<String, String>> getNamesContainingWebsites() {
        return getWebsitesbyValueInternal(null);
    }

    public static List<Pair<String, String>> getWebsitesbyValueInternal(
            String searchValueOpt) {


        List<Pair<String, String>> results = new ArrayList<Pair<String, String>>();


        NameStorageMap nameStorageMap = DCSet.getInstance().getNameStorageMap();
        Set<String> keys = nameStorageMap.getKeys();

        for (String key : keys) {
            String value = nameStorageMap.getOpt(key, Corekeys.WEBSITE.getKeyname());
            if (value != null) {
                if (searchValueOpt == null) {
                    results.add(new Pair<String, String>(key,
                            value));
                } else {
                    if (value.toLowerCase().contains(
                            searchValueOpt.toLowerCase())) {
                        results.add(new Pair<String, String>(key,
                                value));
                    }

                }
            }

        }

        return results;
    }

	/*
	@SuppressWarnings("unchecked")
	public static JSONObject getJsonForNameOpt(Name name) {

		String rawNameValue = null;
		List<Transaction> accountTransactions = getOwnUnconfirmedTX();

		
			for (Transaction transaction : accountTransactions) {
				
				if (transaction.getType() == Transaction.UPDATE_NAME_TRANSACTION) {
					UpdateNameTransaction updateNameTx = (UpdateNameTransaction) transaction;
					if (updateNameTx.getName().getName().equals(name.getName())) {
						rawNameValue = updateNameTx.getName().getValue();
						break;
					}
					
				}
			}

		if (rawNameValue == null) {
			rawNameValue = name.getValue();
		}

		String decompressedNameValue = GZIP.webDecompress(rawNameValue);

		JSONObject jsonValue;
		// THIS SIGNIFICANTLY INCREASES SPEED!
		if (!decompressedNameValue.startsWith("{")) {
			jsonValue = new JSONObject();
			jsonValue.put(Corekeys.DEFAULT.toString(), decompressedNameValue);
			return jsonValue;
		}

		try {
			jsonValue = (JSONObject) JSONValue.parse(decompressedNameValue);

			if (jsonValue == null) {
				jsonValue = new JSONObject();
				jsonValue.put(Corekeys.DEFAULT.toString(),
						decompressedNameValue);
			}

			return jsonValue;

		} catch (Exception e) {
			// no valid json

			jsonValue = new JSONObject();
			jsonValue.put(Corekeys.DEFAULT.toString(), decompressedNameValue);
			return jsonValue;
		}

	}

	public static List<Transaction> getOwnUnconfirmedTX() {
		List<Tuple2<List<byte[]>, Transaction>> transactions = DLSet.getInstance()
				.getTransactionMap().getTransactions();
		List<Transaction> accountTransactions = new ArrayList<Transaction>();

		
		boolean doesWalletDatabaseExists = Controller.getInstance().doesWalletDatabaseExists();
		
		for (Transaction transaction : transactions) {
			if (doesWalletDatabaseExists && Controller.getInstance().getAccounts()
					.contains(transaction.getCreator())) {
				accountTransactions.add(transaction);
			}
		}

		// SORT THEM BY TIMESTAMP
		Collections.sort(accountTransactions,
				new TransactionTimestampComparator());
		Collections.reverse(accountTransactions);
		return accountTransactions;
	}
	*/

    public enum NameResult {
        OK("OK", "OK", 0), NAME_NOT_REGISTERED("The name is not registered",
                "Invalid address or name not registered!",
                Transaction.NAME_DOES_NOT_EXIST), NAME_WITH_SPACE(
                "For security purposes sending payments to a name that starts or ends with spaces is forbidden.",
                "Name Payments with trailing or leading spaces are not allowed!",
                Transaction.NAME_WITH_SPACE), NAME_FOR_SALE(
                "For security purposes sending payments to a name that can be purchased through name exchange is disabled.",
                "Payments with names that are for sale are not allowed!",
                Transaction.NAME_NOT_FOR_SALE);

        private String statusMessage;
        private String shortStatusMessage;
        private int errorcode;

        private NameResult(String statusMessage, String shortStatusMessage,
                           int errorcode) {
            this.statusMessage = statusMessage;
            this.shortStatusMessage = shortStatusMessage;
            this.errorcode = errorcode;

        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public String getShortStatusMessage() {
            return shortStatusMessage;
        }

        public int getErrorCode() {
            return errorcode;
        }

    }
}
