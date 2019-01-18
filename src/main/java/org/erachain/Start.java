package org.erachain;

import org.erachain.controller.Controller;
import org.erachain.utils.Logging;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
public class Start {

    @Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)

    // LOGGER of WEBSERVERS
    public DispatcherServlet dispatcherServlet() {
        return new Logging();
    }


    public static void main(String args[]) throws IOException {

        SpringApplicationBuilder builder = new SpringApplicationBuilder(Start.class);

        builder.headless(false).run(args);

        Controller.getInstance().startApplication(args);

    }

}
