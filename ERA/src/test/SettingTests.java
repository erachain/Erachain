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
    protected final static String URL_LOCAL_NODE = "http://127.0.0.1:9068";
    /**
     * Access WEB
     * <p>
     * url remote node
     */
    protected final static String URL_REMOTE_NODE = "http://explorer.erachain.org:9067";

    protected void Init() throws Exception {

        Class<? extends String[]> initArgs = (new String[]{"test5"}).getClass();
        Class.forName("Start").getMethod("main", initArgs).invoke(null);

    }


}

