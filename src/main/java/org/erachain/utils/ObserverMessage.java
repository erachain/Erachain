package org.erachain.utils;

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

    public static final int RESET_PEER_TYPE = 16;
    public static final int ADD_PEER_TYPE = 17;
    public static final int REMOVE_PEER_TYPE = 18;
    public static final int LIST_PEER_TYPE = 19;
    public static final int UPDATE_PEER_TYPE = 20;


    public static final int ADD_ACCOUNT_TYPE = 21;
    public static final int REMOVE_ACCOUNT_TYPE = 22;

    public static final int RESET_ALL_ACCOUNT_TYPE = 31;
    public static final int ADD_ALL_ACCOUNT_TYPE = 32;
    public static final int REMOVE_ALL_ACCOUNT_TYPE = 33;
    public static final int LIST_ALL_ACCOUNT_TYPE = 34;

    public static final int WALLET_DB_CLOSED = 36;
    public static final int WALLET_DB_OPEN = 37;
    public static final int WALLET_STATUS = 38;
    public static final int NETWORK_STATUS = 39;

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

    /**
     * -1 - start Synchr; height - stop; 0 - stop or unLock buttons
     */
    public static final int WALLET_SYNC_STATUS = 101;
    public static final int BLOCKCHAIN_SYNC_STATUS = 102;

    public static final int RESET_IMPRINT_TYPE = 111;
    public static final int ADD_IMPRINT_TYPE = 112;
    public static final int REMOVE_IMPRINT_TYPE = 113;
    public static final int LIST_IMPRINT_TYPE = 114;

    public static final int RESET_TEMPLATE_TYPE = 116;
    public static final int ADD_TEMPLATE_TYPE = 117;
    public static final int REMOVE_TEMPLATE_TYPE = 118;
    public static final int LIST_TEMPLATE_TYPE = 119;

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

    public static final int RESET_VOTEPOLL_TYPE = 151;
    public static final int ADD_VOTEPOLL_TYPE = 152;
    public static final int REMOVE_VOTEPOLL_TYPE = 153;
    public static final int LIST_VOTEPOLL_TYPE = 154;

    public static final int RESET_CLACULATED_TYPE = 166;
    public static final int ADD_CLACULATED_TYPE = 167;
    public static final int REMOVE_CLACULATED_TYPE = 168;
    public static final int LIST_CLACULATED_TYPE = 169;

    public static final int RESET_COMPL_ORDER_TYPE = 201;
    public static final int ADD_COMPL_ORDER_TYPE = 202;
    public static final int REMOVE_COMPL_ORDER_TYPE = 203;
    public static final int LIST_COMPL_ORDER_TYPE = 204;

    public static final int RESET_PAIR_TYPE = 266;
    public static final int ADD_PAIR_TYPE = 267;
    public static final int REMOVE_PAIR_TYPE = 268;
    public static final int LIST_PAIR_TYPE = 269;


    public static final int RESET_ASSET_STATUS_TYPE = 1151;
    public static final int ADD_ASSET_STATUS_TYPE = 1152;
    public static final int REMOVE_ASSET_STATUS_TYPE = 1153;
    public static final int LIST_ASSET_STATUS_TYPE = 1154;
    public static final int LIST_ASSET_STATUS_FAVORITES_TYPE = 1156;

    public static final int RESET_PERSON_STATUS_TYPE = 1161;
    public static final int ADD_PERSON_STATUS_TYPE = 1162;
    public static final int REMOVE_PERSON_STATUS_TYPE = 1163;
    public static final int LIST_PERSON_STATUS_TYPE = 1164;
    public static final int LIST_PERSON_STATUS_FAVORITES_TYPE = 1166;

    public static final int RESET_UNION_STATUS_TYPE = 1171;
    public static final int ADD_UNION_STATUS_TYPE = 1172;
    public static final int REMOVE_UNION_STATUS_TYPE = 1173;
    public static final int LIST_UNION_STATUS_TYPE = 1174;
    public static final int LIST_UNION_STATUS_FAVORITES_TYPE = 1176;

    public static final int RESET_ASSET_UNION_TYPE = 1181;
    public static final int ADD_ASSET_UNION_TYPE = 1182;
    public static final int REMOVE_ASSET_UNION_TYPE = 1183;
    public static final int LIST_ASSET_UNION_TYPE = 1184;
    public static final int LIST_ASSET_UNION_FAVORITES_TYPE = 1186;

    public static final int RESET_PERSON_UNION_TYPE = 1191;
    public static final int ADD_PERSON_UNION_TYPE = 1192;
    public static final int REMOVE_PERSON_UNION_TYPE = 1193;
    public static final int LIST_PERSON_UNION_TYPE = 1194;
    public static final int LIST_PERSON_UNION_FAVORITES_TYPE = 1186;

    public static final int RESET_STATUS_UNION_TYPE = 1191;
    public static final int ADD_STATUS_UNION_TYPE = 1192;
    public static final int REMOVE_STATUS_UNION_TYPE = 1193;
    public static final int LIST_STATUS_UNION_TYPE = 1194;
    public static final int LIST_STATUS_UNION_FAVORITES_TYPE = 1196;

    public static final int RESET_UNION_UNION_TYPE = 1201;
    public static final int ADD_UNION_UNION_TYPE = 1202;
    public static final int REMOVE_UNION_UNION_TYPE = 1203;
    public static final int LIST_UNION_UNION_TYPE = 1204;
    public static final int LIST_UNION_UNION_FAVORITES_TYPE = 1206;

    public static final int RESET_PERSON_STATUS_UNION_TYPE = 1211;
    public static final int ADD_PERSON_STATUS_UNION_TYPE = 1212;
    public static final int REMOVE_PERSON_STATUS_UNION_TYPE = 1213;
    public static final int LIST_PERSON_STATUS_UNION_TYPE = 1214;
    public static final int LIST_PERSON_STATUS_UNION_FAVORITES_TYPE = 1216;

    public static final int RESET_STATEMENT_TYPE = 1221;
    public static final int ADD_STATEMENT_TYPE = 1222;
    public static final int REMOVE_STATEMENT_TYPE = 1223;
    public static final int LIST_STATEMENT_TYPE = 1224;
    public static final int RESET_STATEMENT_FAVORITES_TYPE = 1226;
    public static final int ADD_STATEMENT_FAVORITES_TYPE = 1227;
    public static final int DELETE_STATEMENT_FAVORITES_TYPE = 1228;
    public static final int LIST_STATEMENT_FAVORITES_TYPE = 1229;

    public static final int RESET_VOUCH_TYPE = 1231;
    public static final int ADD_VOUCH_TYPE = 1232;
    public static final int REMOVE_VOUCH_TYPE = 1233;
    public static final int LIST_VOUCH_TYPE = 1234;
    public static final int LIST_VOUCH_FAVORITES_TYPE = 1236;

    public static final int RESET_CANCEL_ORDER_TYPE = 1241;
    public static final int ADD_CANCEL_ORDER_TYPE = 1242;
    public static final int REMOVE_CANCEL_ORDER_TYPE = 1243;
    public static final int LIST_CANCEL_ORDER_TYPE = 1244;

    public static final int RESET_POLL_UNION_TYPE = 1251;
    public static final int ADD_POLL_UNION_TYPE = 1252;
    public static final int REMOVE_POLL_UNION_TYPE = 1253;
    public static final int LIST_POLL_UNION_TYPE = 1254;
    public static final int LIST_POLL_UNION_FAVORITES_TYPE = 1256;

    public static final int RESET_EXLINK_TYPE = 1261;
    public static final int ADD_EXLINK_TYPE = 1262;
    public static final int REMOVE_EXLINK_TYPE = 1263;
    public static final int LIST_EXLINK_TYPE = 1264;
    public static final int LIST_EXLINK_FAVORITES_TYPE = 1266;


    ///////////////////////// CHAIN //////////////////////
    public static final int CHAIN_RESET_BLOCK_TYPE = 5501;
    public static final int CHAIN_ADD_BLOCK_TYPE = 5502;
    public static final int CHAIN_REMOVE_BLOCK_TYPE = 5503;
    public static final int CHAIN_LIST_BLOCK_TYPE = 5504;

    ///////////////////////// TRADERS /////////////////////
    public static final int TRADERS_UPDATE_TYPE = 8888;

    ///////////////////////// WALLET /////////////////////
    public static final int WALLET_RESET_BLOCK_TYPE = 10001;
    public static final int WALLET_ADD_BLOCK_TYPE = 10002;
    public static final int WALLET_REMOVE_BLOCK_TYPE = 10003;
    public static final int WALLET_LIST_BLOCK_TYPE = 10004;

    public static final int WALLET_RESET_TRANSACTION_TYPE = 10011;
    public static final int WALLET_ADD_TRANSACTION_TYPE = 10012;
    public static final int WALLET_REMOVE_TRANSACTION_TYPE = 10013;
    public static final int WALLET_LIST_TRANSACTION_TYPE = 10014;
    public static final int RESET_TRANSACTION_FAVORITES_TYPE = 10016;
    public static final int ADD_TRANSACTION_FAVORITES_TYPE = 10017;
    public static final int DELETE_TRANSACTION_FAVORITES_TYPE = 10018;
    public static final int LIST_TRANSACTION_FAVORITES_TYPE = 10019;

    public static final int WALLET_RESET_ORDER_TYPE = 10021;
    public static final int WALLET_ADD_ORDER_TYPE = 10022;
    public static final int WALLET_REMOVE_ORDER_TYPE = 10023;
    public static final int WALLET_LIST_ORDER_TYPE = 10024;

    public static final int WALLET_RESET_NAME_TYPE = 10031;
    public static final int WALLET_ADD_NAME_TYPE = 10032;
    public static final int WALLET_REMOVE_NAME_TYPE = 10033;
    public static final int WALLET_LIST_NAME_TYPE = 10034;

    public static final int WALLET_RESET_NAME_SALE_TYPE = 10041;
    public static final int WALLET_ADD_NAME_SALE_TYPE = 10042;
    public static final int WALLET_REMOVE_NAME_SALE_TYPE = 10043;
    public static final int WALLET_LIST_NAME_SALE_TYPE = 10044;

    public static final int WALLET_RESET_POLL_TYPE = 10051;
    public static final int WALLET_ADD_POLL_TYPE = 10052;
    public static final int WALLET_REMOVE_POLL_TYPE = 10053;
    public static final int WALLET_LIST_POLL_TYPE = 10054;
    public static final int WALLET_RESET_POLL_FAVORITES_TYPE = 10055;
    public static final int WALLET_ADD_POLL_FAVORITES_TYPE = 10058;
    public static final int WALLET_DELETE_POLL_FAVORITES_TYPE = 10059;
    public static final int WALLET_LIST_POLL_FAVORITES_TYPE = 10057;


    public static final int WALLET_RESET_ASSET_TYPE = 10061;
    public static final int WALLET_ADD_ASSET_TYPE = 10062;
    public static final int WALLET_REMOVE_ASSET_TYPE = 10063;
    public static final int WALLET_LIST_ASSET_TYPE = 10064;

    public static final int WALLET_RESET_IMPRINT_TYPE = 10071;
    public static final int WALLET_ADD_IMPRINT_TYPE = 10072;
    public static final int WALLET_REMOVE_IMPRINT_TYPE = 10073;
    public static final int WALLET_LIST_IMPRINT_TYPE = 10074;
    public static final int RESET_IMPRINT_FAVORITES_TYPE = 10076;
    public static final int ADD_IMPRINT_FAVORITES_TYPE = 10077;
    public static final int REMOVE_IMPRINT_FAVORITES_TYPE = 10078;
    public static final int LIST_IMPRINT_FAVORITES_TYPE = 10079;

    public static final int WALLET_RESET_TEMPLATE_TYPE = 10081;
    public static final int WALLET_ADD_TEMPLATE_TYPE = 10082;
    public static final int WALLET_REMOVE_TEMPLATE_TYPE = 10083;
    public static final int WALLET_LIST_TEMPLATE_TYPE = 10084;
    public static final int RESET_TEMPLATE_FAVORITES_TYPE = 10086;
    public static final int ADD_TEMPLATE_FAVORITES_TYPE = 10087;
    public static final int REMOVE_TEMPLATE_FAVORITES_TYPE = 10088;
    public static final int LIST_TEMPLATE_FAVORITES_TYPE = 10089;

    public static final int WALLET_RESET_PERSON_TYPE = 10091;
    public static final int WALLET_ADD_PERSON_TYPE = 10092;
    public static final int WALLET_REMOVE_PERSON_TYPE = 10093;
    public static final int WALLET_LIST_PERSON_TYPE = 10094;
    public static final int RESET_PERSON_FAVORITES_TYPE = 10096;
    public static final int ADD_PERSON_FAVORITES_TYPE = 10097;
    public static final int DELETE_PERSON_FAVORITES_TYPE = 10098;
    public static final int LIST_PERSON_FAVORITES_TYPE = 10099;

    public static final int WALLET_RESET_STATUS_TYPE = 12001;
    public static final int WALLET_ADD_STATUS_TYPE = 12002;
    public static final int WALLET_REMOVE_STATUS_TYPE = 12003;
    public static final int WALLET_LIST_STATUS_TYPE = 12004;
    public static final int RESET_STATUS_FAVORITES_TYPE = 12006;
    public static final int ADD_STATUS_FAVORITES_TYPE = 12007;
    public static final int DELETE_STATUS_FAVORITES_TYPE = 12008;
    public static final int LIST_STATUS_FAVORITES_TYPE = 12009;

    public static final int WALLET_RESET_UNION_TYPE = 12011;
    public static final int WALLET_ADD_UNION_TYPE = 12012;
    public static final int WALLET_REMOVE_UNION_TYPE = 12013;
    public static final int WALLET_LIST_UNION_TYPE = 12014;
    public static final int RESET_UNION_FAVORITES_TYPE = 12016;
    public static final int ADD_UNION_FAVORITES_TYPE = 12017;
    public static final int DELETE_UNION_FAVORITES_TYPE = 12018;
    public static final int LIST_UNION_FAVORITES_TYPE = 12019;

    public static final int WALLET_ACCOUNT_PROPERTIES_ADD = 12117;
    public static final int WALLET_ACCOUNT_PROPERTIES_DELETE = 12118;
    public static final int WALLET_ACCOUNT_PROPERTIES_LIST = 12119;
    public static final int WALLET_ACCOUNT_PROPERTIES_RESET = 12120;

    public static final int WALLET_ACCOUNT_FAVORITE_ADD = 12121;
    public static final int WALLET_ACCOUNT_FAVORITE_DELETE = 12122;
    public static final int WALLET_ACCOUNT_FAVORITE_LIST = 12123;
    public static final int WALLET_ACCOUNT_FAVORITE_RESET = 12124;

    public static final int WALLET_ADD_TELEGRAM_TYPE = 1000003;
    public static final int WALLET_REMOVE_TELEGRAM_TYPE = 1000001;
    public static final int WALLET_LIST_TELEGRAM_TYPE = 1000002;
    public static final int WALLET_RESET_TELEGRAM_TYPE = 1000004;

    // all telegramm observer meggase
    public static final int ALL_TELEGRAM_RESET_TYPE = -1003;
    public static final int ALL_TELEGRAMT_ADD_TYPE = -1001;
    public static final int ALL_TELEGRAMT_REMOVE_TYPE = -1002;
    public static final int ALL_TELEGRAMT_LIST_TYPE = -1000;

    public static final int  RPC_WORK_TYPE = 20000;

    public static final int  GUI_ABOUT_TYPE = 1000000001;
    public static final int  GUI_REPAINT = 1100000001;


    private int type;
    private Object value;

    public ObserverMessage(int type, Object value) {
        this.type = type;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

}
