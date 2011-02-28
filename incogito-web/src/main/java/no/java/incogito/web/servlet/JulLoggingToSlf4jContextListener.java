package no.java.incogito.web.servlet;

import no.java.incogito.util.*;

import javax.servlet.*;

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
