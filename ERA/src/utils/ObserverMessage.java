package utils;

public class ObserverMessage {

	public static final int RESET_BLOCK_TYPE = 1;
	public static final int ADD_BLOCK_TYPE = 2;
	public static final int REMOVE_BLOCK_TYPE = 3;
	public static final int LIST_BLOCK_TYPE = 4;
	
	public static final int RESET_TRANSACTION_TYPE = 6;
	public static final int ADD_TRANSACTION_TYPE = 7;
	public static final int REMOVE_TRANSACTION_TYPE = 8;
	public static final int LIST_TRANSACTION_TYPE = 9;
	
	public static final int RESET_UNC_TRANSACTION_TYPE = 11;
	public static final int ADD_UNC_TRANSACTION_TYPE = 12;
	public static final int REMOVE_UNC_TRANSACTION_TYPE = 13;
	public static final int LIST_UNC_TRANSACTION_TYPE = 14;
	public static final int COUNT_UNC_TRANSACTION_TYPE = 15;

	public static final int RESET_PEER_TYPE = 16;
	public static final int ADD_PEER_TYPE = 17;
	public static final int REMOVE_PEER_TYPE = 18;
	public static final int LIST_PEER_TYPE = 19;
	
	public static final int ADD_ACCOUNT_TYPE = 21;
	public static final int REMOVE_ACCOUNT_TYPE = 22;
	
	public static final int RESET_ALL_ACCOUNT_TYPE = 31;
	public static final int ADD_ALL_ACCOUNT_TYPE = 32;
	public static final int REMOVE_ALL_ACCOUNT_TYPE = 33;
	public static final int LIST_ALL_ACCOUNT_TYPE = 34;
	
	public static final int WALLET_STATUS = 36;
	public static final int NETWORK_STATUS = 37;
	
	public static final int RESET_NAME_TYPE = 41;
	public static final int ADD_NAME_TYPE = 42;
	public static final int REMOVE_NAME_TYPE = 43;
	public static final int LIST_NAME_TYPE = 44;
	
	public static final int RESET_NAME_SALE_TYPE = 46;
	public static final int ADD_NAME_SALE_TYPE = 47;
	public static final int REMOVE_NAME_SALE_TYPE = 48;
	public static final int LIST_NAME_SALE_TYPE = 49;

	public static final int RESET_POLL_TYPE = 51;
	public static final int ADD_POLL_TYPE = 52;
	public static final int REMOVE_POLL_TYPE = 53;
	public static final int LIST_POLL_TYPE = 54;
	
	public static final int RESET_ASSET_TYPE = 56;
	public static final int ADD_ASSET_TYPE = 57;
	public static final int REMOVE_ASSET_TYPE = 58;
	public static final int LIST_ASSET_TYPE = 59;
	
	public static final int RESET_ORDER_TYPE = 61;
	public static final int ADD_ORDER_TYPE = 62;
	public static final int REMOVE_ORDER_TYPE = 63;
	public static final int LIST_ORDER_TYPE = 64;
	
	public static final int RESET_TRADE_TYPE = 66;
	public static final int ADD_TRADE_TYPE = 67;
	public static final int REMOVE_TRADE_TYPE = 68;
	public static final int LIST_TRADE_TYPE = 69;
	
	public static final int RESET_BALANCE_TYPE = 71;
	public static final int ADD_BALANCE_TYPE = 72;
	public static final int REMOVE_BALANCE_TYPE = 73;
	public static final int LIST_BALANCE_TYPE = 74;
	
	public static final int RESET_ASSET_FAVORITES_TYPE = 76;
	public static final int ADD_ASSET_FAVORITES_TYPE = 77;
	public static final int DELETE_ASSET_FAVORITES_TYPE = 78;
	public static final int LIST_ASSET_FAVORITES_TYPE = 79;
	
	public static final int FORGING_STATUS = 81;

	public static final int RESET_AT_TYPE = 91;
	public static final int ADD_AT_TYPE = 92;
	public static final int REMOVE_AT_TYPE = 93;
	public static final int LIST_ATS = 94;

	public static final int RESET_AT_TX_TYPE = 96;
	public static final int ADD_AT_TX_TYPE = 97;
	public static final int REMOVE_AT_TX = 98;
	public static final int LIST_AT_TXS = 99;
	
	public static final int WALLET_SYNC_STATUS = 101;
	public static final int BLOCKCHAIN_SYNC_STATUS = 102;

	public static final int RESET_IMPRINT_TYPE = 111;
	public static final int ADD_IMPRINT_TYPE = 112;
	public static final int REMOVE_IMPRINT_TYPE = 113;
	public static final int LIST_IMPRINT_TYPE = 114;

	public static final int RESET_NOTE_TYPE = 116;
	public static final int ADD_NOTE_TYPE = 117;
	public static final int REMOVE_NOTE_TYPE = 118;
	public static final int LIST_NOTE_TYPE = 119;
		
	public static final int RESET_PERSON_TYPE = 121;
	public static final int ADD_PERSON_TYPE = 122;
	public static final int REMOVE_PERSON_TYPE = 123;
	public static final int LIST_PERSON_TYPE = 124;

	public static final int RESET_STATUS_TYPE = 131;
	public static final int ADD_STATUS_TYPE = 132;
	public static final int REMOVE_STATUS_TYPE = 133;
	public static final int LIST_STATUS_TYPE = 134;

	public static final int RESET_UNION_TYPE = 141;
	public static final int ADD_UNION_TYPE = 142;
	public static final int REMOVE_UNION_TYPE = 143;
	public static final int LIST_UNION_TYPE = 144;

	public static final int RESET_ASSET_STATUS_TYPE = 151;
	public static final int ADD_ASSET_STATUS_TYPE = 152;
	public static final int REMOVE_ASSET_STATUS_TYPE = 153;
	public static final int LIST_ASSET_STATUS_TYPE = 154;
	public static final int LIST_ASSET_STATUS_FAVORITES_TYPE = 156;
	
	public static final int RESET_PERSON_STATUS_TYPE = 161;
	public static final int ADD_PERSON_STATUS_TYPE = 162;
	public static final int REMOVE_PERSON_STATUS_TYPE = 163;
	public static final int LIST_PERSON_STATUS_TYPE = 164;
	public static final int LIST_PERSON_STATUS_FAVORITES_TYPE = 166;

	public static final int RESET_UNION_STATUS_TYPE = 171;
	public static final int ADD_UNION_STATUS_TYPE = 172;
	public static final int REMOVE_UNION_STATUS_TYPE = 7173;
	public static final int LIST_UNION_STATUS_TYPE = 174;
	public static final int LIST_UNION_STATUS_FAVORITES_TYPE = 176;

	public static final int RESET_ASSET_UNION_TYPE = 181;
	public static final int ADD_ASSET_UNION_TYPE = 182;
	public static final int REMOVE_ASSET_UNION_TYPE = 183;
	public static final int LIST_ASSET_UNION_TYPE = 184;
	public static final int LIST_ASSET_UNION_FAVORITES_TYPE = 186;

	public static final int RESET_PERSON_UNION_TYPE = 191;
	public static final int ADD_PERSON_UNION_TYPE = 192;
	public static final int REMOVE_PERSON_UNION_TYPE = 193;
	public static final int LIST_PERSON_UNION_TYPE = 194;
	public static final int LIST_PERSON_UNION_FAVORITES_TYPE = 186;

	public static final int RESET_STATUS_UNION_TYPE = 191;
	public static final int ADD_STATUS_UNION_TYPE = 192;
	public static final int REMOVE_STATUS_UNION_TYPE = 193;
	public static final int LIST_STATUS_UNION_TYPE = 194;
	public static final int LIST_STATUS_UNION_FAVORITES_TYPE = 196;

	public static final int RESET_UNION_UNION_TYPE = 201;
	public static final int ADD_UNION_UNION_TYPE = 202;
	public static final int REMOVE_UNION_UNION_TYPE = 203;
	public static final int LIST_UNION_UNION_TYPE = 204;
	public static final int LIST_UNION_UNION_FAVORITES_TYPE = 206;

	public static final int RESET_PERSON_STATUS_UNION_TYPE = 211;
	public static final int ADD_PERSON_STATUS_UNION_TYPE = 212;
	public static final int REMOVE_PERSON_STATUS_UNION_TYPE = 213;
	public static final int LIST_PERSON_STATUS_UNION_TYPE = 214;
	public static final int LIST_PERSON_STATUS_UNION_FAVORITES_TYPE = 216;

	public static final int RESET_STATEMENT_TYPE = 221;
	public static final int ADD_STATEMENT_TYPE = 222;
	public static final int REMOVE_STATEMENT_TYPE = 223;
	public static final int LIST_STATEMENT_TYPE = 224;
	public static final int RESET_STATEMENT_FAVORITES_TYPE = 226;
	public static final int ADD_STATEMENT_FAVORITES_TYPE = 227;
	public static final int DELETE_STATEMENT_FAVORITES_TYPE = 228;
	public static final int LIST_STATEMENT_FAVORITES_TYPE = 229;
	
	public static final int RESET_VOUCH_TYPE = 231;
	public static final int ADD_VOUCH_TYPE = 232;
	public static final int REMOVE_VOUCH_TYPE = 233;
	public static final int LIST_VOUCH_TYPE = 234;
	public static final int LIST_VOUCH_FAVORITES_TYPE = 236;

	public static final int RESET_CANCEL_ORDER_TYPE = 241;
	public static final int ADD_CANCEL_ORDER_TYPE = 242;
	public static final int REMOVE_CANCEL_ORDER_TYPE = 243;
	public static final int LIST_CANCEL_ORDER_TYPE = 244;

	///////////////////////// CHAIN //////////////////////
	public static final int CHAIN_RESET_BLOCK_TYPE = 501;
	public static final int CHAIN_ADD_BLOCK_TYPE = 502;
	public static final int CHAIN_REMOVE_BLOCK_TYPE = 503;
	public static final int CHAIN_LIST_BLOCK_TYPE = 504;

	///////////////////////// WALLET /////////////////////
	public static final int WALLET_RESET_BLOCK_TYPE = 1001;
	public static final int WALLET_ADD_BLOCK_TYPE = 1002;
	public static final int WALLET_REMOVE_BLOCK_TYPE = 1003;
	public static final int WALLET_LIST_BLOCK_TYPE = 1004;
	
	public static final int WALLET_RESET_TRANSACTION_TYPE = 1011;
	public static final int WALLET_ADD_TRANSACTION_TYPE = 1012;
	public static final int WALLET_REMOVE_TRANSACTION_TYPE = 1013;
	public static final int WALLET_LIST_TRANSACTION_TYPE = 1014;
	public static final int WALLET_COUNT_TRANSACTION_TYPE = 1015;
	
	public static final int WALLET_RESET_ORDER_TYPE = 1021;
	public static final int WALLET_ADD_ORDER_TYPE = 1022;
	public static final int WALLET_REMOVE_ORDER_TYPE = 1023;
	public static final int WALLET_LIST_ORDER_TYPE = 1024;
	
	public static final int WALLET_RESET_NAME_TYPE = 1031;
	public static final int WALLET_ADD_NAME_TYPE = 1032;
	public static final int WALLET_REMOVE_NAME_TYPE = 1033;
	public static final int WALLET_LIST_NAME_TYPE = 1034;

	public static final int WALLET_RESET_NAME_SALE_TYPE = 1041;
	public static final int WALLET_ADD_NAME_SALE_TYPE = 1042;
	public static final int WALLET_REMOVE_NAME_SALE_TYPE = 1043;
	public static final int WALLET_LIST_NAME_SALE_TYPE = 1044;

	public static final int WALLET_RESET_POLL_TYPE = 1051;
	public static final int WALLET_ADD_POLL_TYPE = 1052;
	public static final int WALLET_REMOVE_POLL_TYPE = 1053;
	public static final int WALLET_LIST_POLL_TYPE = 1054;

	public static final int WALLET_RESET_ASSET_TYPE = 1061;
	public static final int WALLET_ADD_ASSET_TYPE = 1062;
	public static final int WALLET_REMOVE_ASSET_TYPE = 1063;
	public static final int WALLET_LIST_ASSET_TYPE = 1064;

	public static final int WALLET_RESET_IMPRINT_TYPE = 1071;
	public static final int WALLET_ADD_IMPRINT_TYPE = 1072;
	public static final int WALLET_REMOVE_IMPRINT_TYPE = 1073;
	public static final int WALLET_LIST_IMPRINT_TYPE = 1074;
	public static final int LIST_IMPRINT_FAVORITES_TYPE = 1075;

	public static final int WALLET_RESET_NOTE_TYPE = 1081;
	public static final int WALLET_ADD_NOTE_TYPE = 1082;
	public static final int WALLET_REMOVE_NOTE_TYPE = 1083;
	public static final int WALLET_LIST_NOTE_TYPE = 1084;
	public static final int LIST_NOTE_FAVORITES_TYPE = 1086;

	public static final int WALLET_RESET_PERSON_TYPE = 1091;
	public static final int WALLET_ADD_PERSON_TYPE = 1092;
	public static final int WALLET_REMOVE_PERSON_TYPE = 1093;
	public static final int WALLET_LIST_PERSON_TYPE = 1094;
	public static final int RESET_PERSON_FAVORITES_TYPE = 1096;
	public static final int ADD_PERSON_FAVORITES_TYPE = 1097;
	public static final int DELETE_PERSON_FAVORITES_TYPE = 1098;
	public static final int LIST_PERSON_FAVORITES_TYPE = 1099;

	public static final int WALLET_RESET_STATUS_TYPE = 2001;
	public static final int WALLET_ADD_STATUS_TYPE = 2002;
	public static final int WALLET_REMOVE_STATUS_TYPE = 2003;
	public static final int WALLET_LIST_STATUS_TYPE = 2004;
	public static final int LIST_STATUS_FAVORITES_TYPE = 2006;

	public static final int WALLET_RESET_UNION_TYPE = 2011;
	public static final int WALLET_ADD_UNION_TYPE = 2012;
	public static final int WALLET_REMOVE_UNION_TYPE = 2013;
	public static final int WALLET_LIST_UNION_TYPE = 2014;
	public static final int LIST_UNION_FAVORITES_TYPE = 2016;

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
