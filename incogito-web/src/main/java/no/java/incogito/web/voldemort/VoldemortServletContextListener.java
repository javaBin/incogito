package no.java.incogito.web.voldemort;

import org.apache.log4j.Logger;
import voldemort.server.VoldemortConfig;
import voldemort.server.VoldemortServer;
import voldemort.server.http.gui.VelocityEngine;
import voldemort.utils.ConfigurationException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Copy of the VoldemortServletContextListener from 0.51, without the bug.
 *
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class VoldemortServletContextListener implements ServletContextListener {

    public static final String VOLDEMORT_TEMPLATE_DIR = "voldemort/server/http/gui/templates";
    public static final String SERVER_KEY = "vldmt_server";
    public static final String SERVER_CONFIG_KEY = "vldmt_config";
    public static final String VELOCITY_ENGINE_KEY = "vldmt_velocity_engine";

    private static final Logger logger = Logger.getLogger(VoldemortServletContextListener.class.getName());

    public void contextInitialized(ServletContextEvent event) {
        try {
            logger.info("Creating application...");
            VoldemortServer server = new VoldemortServer(VoldemortConfig.loadFromEnvironmentVariable());
            event.getServletContext().setAttribute(SERVER_KEY, server);
            event.getServletContext().setAttribute(SERVER_CONFIG_KEY, server.getVoldemortConfig());
            event.getServletContext().setAttribute(VELOCITY_ENGINE_KEY, new VelocityEngine(VOLDEMORT_TEMPLATE_DIR));
            server.start();
            logger.info("Application created.");
        } catch (ConfigurationException e) {
            logger.info("Error loading voldemort server:", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error loading voldemort server:", e);
            throw new ConfigurationException(e);
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
        logger.info("Calling application shutdown...");
        VoldemortServer server = (VoldemortServer) event.getServletContext().getAttribute(SERVER_KEY);
        if (server != null)
            server.stop();
        logger.info("Destroying application...");
        event.getServletContext().removeAttribute(SERVER_KEY);
        event.getServletContext().removeAttribute(SERVER_CONFIG_KEY);
        event.getServletContext().removeAttribute(VELOCITY_ENGINE_KEY);
    }
}
