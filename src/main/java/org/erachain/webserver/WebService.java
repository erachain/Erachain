package org.erachain.webserver;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.IPAccessHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.erachain.settings.Settings;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class WebService {

    private static WebService instance;
    public Server server;
    private static final Logger LOGGER = LoggerFactory.getLogger(Settings.class);

    private WebService() {
        //CREATE CONFIG
        Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(API.class);
        s.add(WebResource.class);
        s.add(APITransactionsResource.class);

        s.add(APIAsset.class);
        s.add(APIPerson.class);
        s.add(APIPoll.class);
        s.add(APIStatus.class);
        s.add(APITemplate.class);

        s.add(APIExchange.class);
        s.add(APITelegramsResource.class);
        s.add(APIDocuments.class);

        ResourceConfig config = new ResourceConfig(s);
        config.register(MultiPartFeature.class);
        //CREATE CONTAINER
        ServletContainer container = new ServletContainer(config);

        //CREATE CONTEXT
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(container), "/*");

        //CREATE WHITELIST
        IPAccessHandler accessHandler = new IPAccessHandler();
        accessHandler.setWhite(Settings.getInstance().getWebAllowed());
        accessHandler.setHandler(context);

        //CREATE WEB SERVER
        this.server = new Server();
        this.server.setHandler(accessHandler);
        conectorAdd();
    }

    public static WebService getInstance(){
        if (instance == null) {
            instance = new WebService();
        }
        return instance;

    }
    public void start() {
        try {
            //START WEB
            server.start();
        } catch (Exception e) {
            //FAILED TO START WEB
        }
    }

    public void stop() {
        try {
            //STOP WEB
            server.stop();
        } catch (Exception e) {
            //FAILED TO STOP WEB
        }
    }


    /* HTTPS connect
    add SSL
    server server
    int port
    String keyStoreFile  - path file
    String keyStorePassword
    String keyManagerPassword
    */
    private void addHttpsConnector(Server server, int port, String keyStoreFilePath, String keyStorePassword, String keyManagerPassword) throws IOException, URISyntaxException {

        SslContextFactory sslContextFactory = new SslContextFactory(keyStoreFilePath);
        sslContextFactory.setKeyStorePassword(keyStorePassword);
        sslContextFactory.setKeyManagerPassword(keyManagerPassword);
        HttpConfiguration httpsConfig = new HttpConfiguration();
        httpsConfig.setSecureScheme("https");
        httpsConfig.setSecurePort(port);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());
        ServerConnector connector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(httpsConfig));
        connector.setPort(port);
        server.addConnector(connector);
    }
    /* HTTP connect
   server server
   int port
   */
    private void addHttpConnector(Server server, int port) throws URISyntaxException {
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);
    }
    private void conectorAdd(){

        if (Settings.getInstance().isWebUseSSL()) {
            // HTTPS WEB SERVER
            try {
                addHttpsConnector(
                        this.server,
                        Settings.getInstance().getWebPort(),
                        Settings.getInstance().getWebKeyStorePath(),
                        Settings.getInstance().getWebKeyStorePassword(),
                        Settings.getInstance().getWebStoreSourcePassword());

            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error("Web server start SSL. Error IO");
            } catch (URISyntaxException e) {
                LOGGER.error("Web server start SSL. Error URL");
            }
        } else
        {
            try {
                addHttpConnector(this.server, Settings.getInstance().getWebPort());
            } catch (URISyntaxException e) {
                LOGGER.error("Web server start. Error URL");
            }
        }
    }
}