package org.erachain.utils;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class URLViewer {

    static Logger LOGGER = LoggerFactory.getLogger(URLViewer.class.getName());

    public static void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public static void openWebpage(URL url) {
        try {
            openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    // https://127.0.0.1/7pay_in/tools/block_proc/ERA
    public int url_note(String url_string) {
        try {
            //SPLIT

            //CREATE CONNECTION
            URL url = new URL(url_string);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            //EXECUTE
            return connection.getResponseCode();
        } catch (Exception e) {
            return -1;
        }
    }
}