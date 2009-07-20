package no.java.incogito.web.servlet;

import javax.servlet.ServletRequestListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class WebGuiRequestListener implements ServletContextListener, ServletRequestListener {
    public void requestDestroyed(ServletRequestEvent sre) {
    }

    public void requestInitialized(ServletRequestEvent sre) {
    }

    public void contextInitialized(ServletContextEvent sce) {
        // This is enough for now
        String baseurl = sce.getServletContext().getContextPath();

        sce.getServletContext().setAttribute("incogito", new IncogitoBean(baseurl));
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }
}
