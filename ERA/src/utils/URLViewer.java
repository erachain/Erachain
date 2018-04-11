package utils;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.log4j.Logger;

import settings.Settings;

public class URLViewer  {
	
	static Logger LOGGER = Logger.getLogger(URLViewer.class.getName());

    public static void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(),e);
            }
        }
    }

    public static void openWebpage(URL url) {
        try {
            openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(),e);
        }
    }
    
    // https://127.0.0.1/7pay_in/tools/block_proc/ERA
    public int url_note(String url_string) {
		try
		{
			//SPLIT
			
			//CREATE CONNECTION
			URL url = new URL(url_string);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			//EXECUTE
			return connection.getResponseCode();
		}					
		catch(Exception e)
		{
			return -1;
		}
    }
}