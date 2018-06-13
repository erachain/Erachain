package test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Class Call API for test
 */
public class CallRemoteApi {
    /**
     * Call remote full node API for code request
     *
     * @param urlNode       set remote url full node
     * @param requestMethod request method (get, post, etc...)
     * @return number request answer
     */
    public String ResponseCodeAPI(String urlNode, String requestMethod) throws Exception {
        Integer result;
        URL obj = new URL(urlNode);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod(requestMethod.toUpperCase());
        result = con.getResponseCode();

        return result.toString();
    }

    /**
     * Call remote full node API for check data request
     *
     * @param urlNode       set remote url full node
     * @param requestMethod request method (get, post, etc...)
     * @return data request answer
     * @throws Exception
     */
    public String ResponseValueAPI(String urlNode, String requestMethod) throws Exception {

        URL obj = new URL(urlNode);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod(requestMethod.toUpperCase());

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }
}
