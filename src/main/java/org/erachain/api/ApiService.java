package org.erachain.api;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.IPAccessHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.erachain.settings.Settings;
import org.erachain.utils.ObserverMessage;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class ApiService extends Observable {

    public Server server;

    public ApiService() {
        //CREATE CONFIG
        Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(RPCResource.class);
        s.add(CoreResource.class);
        s.add(SeedResource.class);
        s.add(PeersResource.class);
        s.add(TransactionsResource.class);
        s.add(TelegramsResource.class);
        s.add(BlocksResource.class);
        s.add(AddressesResource.class);
        s.add(WalletResource.class);
        s.add(RSendResource.class);
        s.add(RSignNoteResource.class);
        s.add(RecPaymentResource.class);
        s.add(FPoolResource.class);
        s.add(ArbitraryTransactionsResource.class);
        s.add(ATResource.class);
        s.add(BlogPostResource.class);
        s.add(BlogResource.class);
        //s.add(CalcFeeResource.class);

        s.add(RecResource.class);
        s.add(RLinkedHashesResource.class);

        s.add(ItemAssetsResource.class);
        s.add(ItemPersonsResource.class);
        s.add(ItemPollsResource.class);
        s.add(ItemStatusesResource.class);
        s.add(ItemTemplatesResource.class);

        s.add(RecStatementResource.class);
        s.add(MultiPaymentResource.class);
        s.add(TradeResource.class);
        s.add(UtilResource.class);

        ResourceConfig config = new ResourceConfig(s);

        //CREATE CONTAINER
        ServletContainer container = new ServletContainer(config);

        //CREATE CONTEXT
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(container), "/*");

        //CREATE WHITELIST
        IPAccessHandler accessHandler = new IPAccessHandler();
        accessHandler.setWhite(Settings.getInstance().getRpcAllowed());
        accessHandler.setHandler(context);

        //CREATE RPC SERVER
        this.server = new Server(Settings.getInstance().getRpcPort());
        this.server.setHandler(accessHandler);
    }

    public void start() {
        try {
            //START RPC
            server.start();
            setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.RPC_WORK_TYPE, true));
        } catch (Exception e) {
            //FAILED TO START RPC
        }
    }

    public void stop() {
        try {
            //STOP RPC
            server.stop();
            setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.RPC_WORK_TYPE, false));
        } catch (Exception e) {
            //FAILED TO STOP RPC
        }
    }
    @Override
    public void addObserver(Observer o) {

         // ADD OBSERVER
        super.addObserver(o);
        setChanged();
            this.notifyObservers(
                    new ObserverMessage(ObserverMessage.RPC_WORK_TYPE, Settings.getInstance().isRpcEnabled())); /// SLOW .size()));
        }


}
