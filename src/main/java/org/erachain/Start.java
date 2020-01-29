package org.erachain;

import org.erachain.controller.Controller;
import org.erachain.settings.Settings;

import java.io.IOException;

//import org.erachain.utils.Logging;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
//import org.springframework.boot.builder.SpringApplicationBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.web.servlet.DispatcherServlet;

//@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
public class Start {

    //@Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)

    // logger of WEBSERVERS
//    public DispatcherServlet dispatcherServlet() {
//        return new Logging();
//    }


    public static void main(String args[]) throws IOException {

        //SpringApplicationBuilder builder = new SpringApplicationBuilder(Start.class);

        //builder.headless(false).run(args);

        long genesisStamp = Settings.DEFAULT_MAINNET_STAMP;
        for (String arg : args) {
            if (arg.equals("-testnet")) {
                genesisStamp = -1;
                break;
            } else if (arg.startsWith("-testnet=") && arg.length() > 9) {
                try {
                    genesisStamp = Long.parseLong(arg.substring(9));

                } catch (Exception e) {
                    genesisStamp = Settings.DEFAULT_DEV_NET_STAMP;
                }
                break;
            }
        }

        Settings.genesisStamp = genesisStamp;

        Settings.getInstance();

        Controller.getInstance().startApplication(args);

    }

}
