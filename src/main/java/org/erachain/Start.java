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
        File file = new File("startARGS.txt");
        if (file.exists()) {
            try {
                List<String> lines = Files.readLines(file, Charsets.UTF_8);

                String parsString = "";
                for (String line : lines) {
                    if ((line = line.trim()).startsWith("//") || line.isEmpty()) {
                        // пропускаем //
                        continue;
                    }
                    parsString += " -" + line.trim(); // add -
                }

                String[] pars = parsString.trim().split(" ");
                String[] argsNew = new String[args.length + pars.length];
                int i = 0;
                // PARS has LOW priority
                for (String par : pars) {
                    argsNew[i++] = par;
                }
                // ARGS has HIGH priority и затрут потом в аврсинге значенийц те что были в файле заданы
                for (String arg : args) {
                    argsNew[i++] = arg;
                }

                args = argsNew;

            } catch (Exception e) {
                LOGGER.info("Error while reading " + file.getAbsolutePath());
                LOGGER.error(e.getMessage(), e);
                System.exit(3);
            }
        }

        ///////////////////  SIDECHAINS ///////////
        file = new File("sideGENESIS.json");
        if (true && file.exists()) {
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
                JSONArray appArray = (JSONArray) Settings.genesisJSON.get(0);
                Settings.APP_NAME = appArray.get(0).toString();
                Settings.APP_FULL_NAME = appArray.get(1).toString();
                JSONArray timeArray = (JSONArray) Settings.genesisJSON.get(1);
                Settings.genesisStamp = new Long(timeArray.get(0).toString());

                // если там пустой список то включаем "у всех все есть"
                JSONArray holders = (JSONArray) Settings.genesisJSON.get(2);
                if (holders.isEmpty()) {
                    Settings.ERA_COMPU_ALL_UP = true;
                }

                Settings.NET_MODE = Settings.NET_MODE_SIDE;

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
                    Settings.simpleTestNet = true;
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
