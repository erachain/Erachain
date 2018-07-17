package test;

/**
 * main setting for test
 */
public class SettingTests {

    /**
     * Access RPC
     * <p>
     * url local node
     */
    protected final static String URL_LOCAL_NODE_RPC = "http://127.0.0.1:9068";
    /**
     * Access RPC
     * <p>
     * url local node
     */
    protected final static String URL_REMOTE_NODE_RPC = "http://explorer.erachain.org:9068";
    /**
     * Access WEB
     * <p>
     * url local node
     */
    protected final static String URL_LOCAL_NODE_API = "http://127.0.0.1:9067";
    /**
     * Access WEB
     * <p>
     * url remote node
     */
    protected final static String URL_REMOTE_NODE_API = "http://explorer.erachain.org:9067";
    /**
     * Password for test wallet
     */
  public  final static String WALLET_PASSWORD = "12345678";

    protected void Init() throws Exception {

        Class<? extends String[]> initArgs = (new String[]{"test5"}).getClass();
        Class.forName("Start").getMethod("main", initArgs).invoke(null);

    }


}

