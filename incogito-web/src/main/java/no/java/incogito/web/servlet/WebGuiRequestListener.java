package no.java.incogito.web.servlet;

import no.java.incogito.application.*;
import org.springframework.context.*;
import static org.springframework.web.context.support.WebApplicationContextUtils.*;

import javax.servlet.*;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class WebGuiRequestListener implements ServletContextListener, ServletRequestListener {
    private IncogitoApplication app;

    // -----------------------------------------------------------------------
    // Context
    // -----------------------------------------------------------------------

    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        ApplicationContext applicationContext = getRequiredWebApplicationContext(servletContext);
        app = (IncogitoApplication) applicationContext.getBean("incogitoApplication", IncogitoApplication.class);
        servletContext.setAttribute("app", app);
    }

    public void contextDestroyed(ServletContextEvent event) {
    }

    // -----------------------------------------------------------------------
    // Request
    // -----------------------------------------------------------------------

    public void requestInitialized(ServletRequestEvent event) {
        ServletRequest servletRequest = event.getServletRequest();
        servletRequest.setAttribute("incogito", app.getConfiguration());
    }

    public void requestDestroyed(ServletRequestEvent event) {
    }
}
