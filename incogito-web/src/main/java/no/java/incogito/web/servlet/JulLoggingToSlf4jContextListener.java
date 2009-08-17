package no.java.incogito.web.servlet;

import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import no.java.incogito.util.ConnectJulToSlf4j;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class JulLoggingToSlf4jContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        ConnectJulToSlf4j.doIt();
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }
}
