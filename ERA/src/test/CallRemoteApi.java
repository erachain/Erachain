package test;

import org.junit.Test;
import utils.StrJSonFine;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Class Call API for test
 */
public class CallRemoteApi extends SettingTests {

    /**
     * Call remote/local full node API for code request
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
     * Call remote/local full node API for check data request
     *
     * @param urlNode       set remote url full node
     * @param requestMethod request method (get, post, etc...)
     * @param value         is value for only "post" method
     * @return data request answer
     * @throws Exception
     */
    public String ResponseValueAPI(String urlNode, String requestMethod, String value) throws Exception {

        URL obj = new URL(urlNode);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod(requestMethod.toUpperCase());

        switch (requestMethod.toUpperCase()) {
            case "GET":
                con.setRequestMethod("GET");
                break;
            case "POST":
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                con.getOutputStream().write(value.getBytes(StandardCharsets.UTF_8));
                con.getOutputStream().flush();
                con.getOutputStream().close();
                break;
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
            response.append(in.readLine());
        }
        in.close();
        return StrJSonFine.convert(response.toString());
    }
}
