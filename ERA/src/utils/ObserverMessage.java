package utils;

public class ObserverMessage {

	public static final int ADD_BLOCK_TYPE = 1;
	public static final int REMOVE_BLOCK_TYPE = 2;
	public static final int LIST_BLOCK_TYPE = 3;
	
	public static final int ADD_TRANSACTION_TYPE = 4;
	public static final int REMOVE_TRANSACTION_TYPE = 5;
	public static final int LIST_TRANSACTION_TYPE = 6;
	
	public static final int ADD_PEER_TYPE = 7;
	public static final int REMOVE_PEER_TYPE = 8;
	public static final int LIST_PEER_TYPE = 9;
	
	public static final int ADD_ACCOUNT_TYPE = 10;
	public static final int REMOVE_ACCOUNT_TYPE = 11;
	
	public static final int WALLET_STATUS = 12;
	public static final int NETWORK_STATUS = 13;
	
	public static final int ADD_NAME_TYPE = 14;
	public static final int REMOVE_NAME_TYPE = 15;
	public static final int LIST_NAME_TYPE = 16;
	
	public static final int ADD_NAME_SALE_TYPE = 17;
	public static final int REMOVE_NAME_SALE_TYPE = 18;
	public static final int LIST_NAME_SALE_TYPE = 19;

	public static final int ADD_POLL_TYPE = 20;
	public static final int REMOVE_POLL_TYPE = 21;
	public static final int LIST_POLL_TYPE = 22;
	
	public static final int ADD_ASSET_TYPE = 23;
	public static final int REMOVE_ASSET_TYPE = 24;
	public static final int LIST_ASSET_TYPE = 25;
	
	public static final int ADD_ORDER_TYPE = 26;
	public static final int REMOVE_ORDER_TYPE = 27;
	public static final int LIST_ORDER_TYPE = 28;
	
	public static final int ADD_TRADE_TYPE = 29;
	public static final int REMOVE_TRADE_TYPE = 30;
	public static final int LIST_TRADE_TYPE = 31;
	
	public static final int ADD_BALANCE_TYPE = 32;
	public static final int REMOVE_BALANCE_TYPE = 33;
	public static final int LIST_BALANCE_TYPE = 34;
	
	public static final int LIST_ASSET_FAVORITES_TYPE = 35;
	
	public static final int FORGING_STATUS = 36;

	public static final int LIST_ATS = 37;
	public static final int ADD_AT_TYPE = 38;
	public static final int ADD_AT_TX_TYPE = 39;
	public static final int LIST_AT_TXS = 40;
	public static final int REMOVE_AT_TYPE = 41;
	public static final int REMOVE_AT_TX = 42;
	
	public static final int WALLET_SYNC_STATUS = 43;
	public static final int BLOCKCHAIN_SYNC_STATUS = 44;

	public static final int ADD_IMPRINT_TYPE = 45;
	public static final int REMOVE_IMPRINT_TYPE = 46;
	public static final int LIST_IMPRINT_TYPE = 47;
	//not used public static final int LIST_IMPRINT_FAVORITES_TYPE = 48;

	public static final int ADD_NOTE_TYPE = 49;
	public static final int REMOVE_NOTE_TYPE = 50;
	public static final int LIST_NOTE_TYPE = 51;
	public static final int LIST_NOTE_FAVORITES_TYPE = 52;
		
	public static final int ADD_PERSON_TYPE = 53;
	public static final int REMOVE_PERSON_TYPE = 54;
	public static final int LIST_PERSON_TYPE = 55;
	public static final int LIST_PERSON_FAVORITES_TYPE = 56;
	public static final int ADD_PERSON_FAVORITES_TYPE = 56001;
	public static final int DELETE_PERSON_FAVORITES_TYPE = 56002;

	public static final int ADD_STATUS_TYPE = 57;
	public static final int REMOVE_STATUS_TYPE = 58;
	public static final int LIST_STATUS_TYPE = 59;
	public static final int LIST_STATUS_FAVORITES_TYPE = 60;

	public static final int ADD_UNION_TYPE = 61;
	public static final int REMOVE_UNION_TYPE = 62;
	public static final int LIST_UNION_TYPE = 63;
	public static final int LIST_UNION_FAVORITES_TYPE = 64;

	public static final int ADD_ASSET_STATUS_TYPE = 65;
	public static final int REMOVE_ASSET_STATUS_TYPE = 66;
	public static final int LIST_ASSET_STATUS_TYPE = 67;
	public static final int LIST_ASSET_STATUS_FAVORITES_TYPE = 68;

	public static final int ADD_PERSON_STATUS_TYPE = 69;
	public static final int REMOVE_PERSON_STATUS_TYPE = 70;
	public static final int LIST_PERSON_STATUS_TYPE = 71;
	public static final int LIST_PERSON_STATUS_FAVORITES_TYPE = 72;

	public static final int ADD_UNION_STATUS_TYPE = 73;
	public static final int REMOVE_UNION_STATUS_TYPE = 74;
	public static final int LIST_UNION_STATUS_TYPE = 75;
	public static final int LIST_UNION_STATUS_FAVORITES_TYPE = 76;

	public static final int ADD_ASSET_UNION_TYPE = 77;
	public static final int REMOVE_ASSET_UNION_TYPE = 78;
	public static final int LIST_ASSET_UNION_TYPE = 79;
	public static final int LIST_ASSET_UNION_FAVORITES_TYPE = 80;

	public static final int ADD_PERSON_UNION_TYPE = 81;
	public static final int REMOVE_PERSON_UNION_TYPE = 82;
	public static final int LIST_PERSON_UNION_TYPE = 83;
	public static final int LIST_PERSON_UNION_FAVORITES_TYPE = 84;

	public static final int ADD_STATUS_UNION_TYPE = 85;
	public static final int REMOVE_STATUS_UNION_TYPE = 86;
	public static final int LIST_STATUS_UNION_TYPE = 87;
	public static final int LIST_STATUS_UNION_FAVORITES_TYPE = 88;

	public static final int ADD_UNION_UNION_TYPE = 89;
	public static final int REMOVE_UNION_UNION_TYPE = 90;
	public static final int LIST_UNION_UNION_TYPE = 91;
	public static final int LIST_UNION_UNION_FAVORITES_TYPE = 92;

	public static final int ADD_PERSON_STATUS_UNION_TYPE = 93;
	public static final int REMOVE_PERSON_STATUS_UNION_TYPE = 94;
	public static final int LIST_PERSON_STATUS_UNION_TYPE = 95;
	public static final int LIST_PERSON_STATUS_UNION_FAVORITES_TYPE = 96;

	public static final int ADD_STATEMENT_TYPE = 97;
	public static final int REMOVE_STATEMENT_TYPE = 98;
	public static final int LIST_STATEMENT_TYPE = 99;
	public static final int LIST_STATEMENT_FAVORITES_TYPE = 100;
	
	public static final int ADD_VOUCH_TYPE = 101;
	public static final int REMOVE_VOUCH_TYPE = 102;
	public static final int LIST_VOUCH_TYPE = 103;
	public static final int LIST_VOUCH_FAVORITES_TYPE = 104;
	
	


	private int type;
	private Object value;
	
	public ObserverMessage(int type, Object value)
	{
		this.type = type;
		this.value = value;
	}
	
	public int getType()
	{
		return this.type;
	}
	
	public Object getValue()
	{
		return this.value;
	}
	
}
