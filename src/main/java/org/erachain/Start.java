package org.erachain;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.erachain.controller.Controller;
import org.erachain.settings.Settings;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

//import org.erachain.utils.Logging;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
//import org.springframework.boot.builder.SpringApplicationBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.web.servlet.DispatcherServlet;

//@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
public class Start {

    private static final Logger LOGGER = LoggerFactory.getLogger(Settings.class);

    //@Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)

    // logger of WEBSERVERS
//    public DispatcherServlet dispatcherServlet() {
//        return new Logging();
//    }


    public static void main(String args[]) throws IOException {

        //SpringApplicationBuilder builder = new SpringApplicationBuilder(Start.class);

        //builder.headless(false).run(args);

        File file = new File("sideGENESIS.json");
        if (file.exists()) {
            // START SIDE CHAIN
            try {
                List<String> lines = Files.readLines(file, Charsets.UTF_8);

                String jsonString = "";
                for (String line : lines) {
                    if (line.trim().startsWith("//")) {
                        // пропускаем //
                        continue;
                    }
                    jsonString += line;
                }

                //CREATE JSON OBJECT
                Settings.genesisJSON = (JSONArray) JSONValue.parse(jsonString);
                Settings.APP_NAME = Settings.genesisJSON.get(0).toString();
                Settings.genesisStamp = new Long(Settings.genesisJSON.get(1).toString());
                Settings.NET_MODE = Settings.NET_MODE_SIDE;

                //Settings.genesisStamp = -1;

            } catch (Exception e) {
                LOGGER.info("Error while reading " + file.getAbsolutePath());
                LOGGER.error(e.getMessage(), e);
                System.exit(3);
            }

        } else {
            long genesisStamp;
            for (String arg : args) {
                if (arg.equals("-testnet")) {
                    genesisStamp = -1;
                    Settings.genesisStamp = genesisStamp;
                    Settings.NET_MODE = Settings.NET_MODE_TEST;
                    break;
                } else if (arg.startsWith("-testnet=") && arg.length() > 9) {
                    try {
                        genesisStamp = Long.parseLong(arg.substring(9));
                        Settings.NET_MODE = Settings.NET_MODE_TEST;

                    } catch (Exception e) {
                        genesisStamp = Settings.DEFAULT_DEMO_NET_STAMP;
                        Settings.NET_MODE = Settings.NET_MODE_DEMO;
                    }
                    Settings.genesisStamp = genesisStamp;
                    break;
                } else if (arg.startsWith("-testdb=") && arg.length() > 8) {
                    try {
                        Settings.TEST_DB_MODE = Integer.parseInt(arg.substring(8));
                        break;
                    } catch (Exception e) {
                    }
                }
            }
        }

        Settings.getInstance();

        Controller.getInstance().startApplication(args);

    }

}
