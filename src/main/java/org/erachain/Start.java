package org.erachain;

import org.erachain.api.ApiClient;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.gui.AboutFrame;
import org.erachain.gui.Gui;
import org.erachain.gui.library.Issue_Confirm_Dialog;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.SysTray;
import org.erachain.webserver.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
public class Start {

    //@Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
    //public DispatcherServlet dispatcherServlet() {
    //    return new Logging();
    //}


    public static void main(String args[]) throws IOException {

       // SpringApplicationBuilder builder = new SpringApplicationBuilder(Start.class);

       // builder.headless(false).run(args);

        Controller.getInstance().startApplication(args);


    }


}
