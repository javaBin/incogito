package no.java.incogito.voldemort;

import org.apache.log4j.Logger;
import voldemort.client.SocketStoreClientFactory;
import voldemort.client.StoreClientFactory;
import voldemort.server.VoldemortConfig;
import voldemort.server.VoldemortServer;
import voldemort.server.socket.SocketService;

import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoServer {
    public final File home;

    private VoldemortServer server;

    private StoreClientFactory storeClientFactory;

    private final Logger logger;

    IncogitoServer(File home) {
        this.home = home;
        logger = Logger.getLogger(getClass());
    }

    public void start() {

        logger.info("Starting Voldemort...");
        VoldemortConfig config = VoldemortConfig.loadFromVoldemortHome(home.getAbsolutePath());
        server = new VoldemortServer(config);
        server.start();

        SocketService service = (SocketService) server.getService("socket-service");
        int port = service.getPort();

        logger.info("Starting client connection on port " + port);
        storeClientFactory = new SocketStoreClientFactory("tcp://localhost:" + port);
    }

    public void stop() {
        logger.info("Stopping Voldemort...");
        if (server != null) {
            server.stop();
        }
        logger.info("Voldemort stopped");
    }

    public StoreClientFactory getStoreClientFactory() {
        return storeClientFactory;
    }
}
