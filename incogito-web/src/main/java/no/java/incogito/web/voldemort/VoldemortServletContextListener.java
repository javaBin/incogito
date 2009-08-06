package no.java.incogito.web.voldemort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import voldemort.server.VoldemortConfig;
import voldemort.server.VoldemortServer;
import voldemort.utils.ConfigurationException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class VoldemortServletContextListener implements ServletContextListener {

    private VoldemortServer server;

    private static final Logger logger = LoggerFactory.getLogger(VoldemortServletContextListener.class.getName());

    public void contextInitialized(ServletContextEvent event) {

        String home = findVoldemortHome().getAbsolutePath();

        try {
            logger.info("Starting Voldemort...");
            server = new VoldemortServer(VoldemortConfig.loadFromVoldemortHome(home));
            server.start();
            logger.info("Voldemort started!");
        } catch (Exception e) {
            logger.error("Error loading voldemort server:", e);
            throw new ConfigurationException(e);
        }
    }

    private File findVoldemortHome() {
        return null;
    }

    public void contextDestroyed(ServletContextEvent event) {
        logger.info("Stopping Voldemort...");
        if (server != null)
            server.stop();
        logger.info("Voldemort stopped!");
    }
}
