package no.java.incogito.web.servlet;

import no.java.incogito.application.IncogitoApplication;
import org.springframework.context.ApplicationContext;
import static org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.ServletRequest;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class WebGuiRequestListener implements ServletContextListener, ServletRequestListener {
    private IncogitoApplication app;

    public void requestInitialized(ServletRequestEvent event) {
        ServletRequest servletRequest = event.getServletRequest();
        servletRequest.setAttribute("incogito", app.getConfiguration());
    }

    public void requestDestroyed(ServletRequestEvent event) {
    }

    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        ApplicationContext applicationContext = getRequiredWebApplicationContext(servletContext);
        app = (IncogitoApplication) applicationContext.getBean("incogitoApplication", IncogitoApplication.class);
        servletContext.setAttribute("app", app);
    }

    public void contextDestroyed(ServletContextEvent event) {
    }
}
