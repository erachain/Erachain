package org.erachain.webserver;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.IPAccessHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.cert.Certificate;
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
        s.add(APITXResource.class);

        s.add(APIItemAsset.class);
        s.add(APIItemPerson.class);
        s.add(APIItemPoll.class);
        s.add(APIItemStatus.class);
        s.add(APIItemTemplate.class);

        s.add(APIExchange.class);
        s.add(APITelegramsResource.class);
        s.add(APIDocuments.class);
        s.add(APIFPool.class);

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

    public boolean isStoped (){
        if(instance == null) return true;
      return server.isStopped() ;
    }
    public void clearInstance(){
        instance=null;
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
        boolean certificateSslIsOk = true;
        // verify SSL certifycate
        if(Settings.getInstance().isWebUseSSL()){
            try {
                Fun.Tuple3<KeyStore, Certificate, String> result = SslUtils.getWebKeyStore(Settings.getInstance().getWebKeyStorePath(), Settings.getInstance().getWebKeyStorePassword(), Settings.getInstance().getWebStoreSourcePassword());
                if (result.a == null) {
                    LOGGER.error(Lang.T("WEB SSL not started: ") + ": " + result.c);
                    certificateSslIsOk = false;
                } else {
                    LOGGER.info(Lang.T("Start SSL is OK"));
                    LOGGER.info("SSL public key: " + result.b.getPublicKey().toString());
                }
            } catch (FileNotFoundException e1) {
               // e1.printStackTrace();
                LOGGER.error(e1.getLocalizedMessage());
                certificateSslIsOk = false;

            }
        }

        if (Settings.getInstance().isWebUseSSL() && certificateSslIsOk) {
            // HTTPS WEB SERVER
            try {
                addHttpsConnector(
                        this.server,
                        Settings.getInstance().getWebPort(),
                        Settings.getInstance().getWebKeyStorePath(),
                        Settings.getInstance().getWebKeyStorePassword(),
                        Settings.getInstance().getWebStoreSourcePassword());

            } catch (IOException e) {
                //e.printStackTrace();
                LOGGER.error(e.getLocalizedMessage());
            } catch (URISyntaxException e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        } else {
            try {
                addHttpConnector(this.server, Settings.getInstance().getWebPort());
            } catch (URISyntaxException e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
    }
}