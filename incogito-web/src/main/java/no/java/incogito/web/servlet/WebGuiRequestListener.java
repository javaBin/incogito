package no.java.incogito.web.servlet;

import org.springframework.web.context.support.WebApplicationContextUtils;
import static org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletRequestListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

import no.java.incogito.application.IncogitoApplication;

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

        ApplicationContext context = getRequiredWebApplicationContext(sce.getServletContext());

        // This is enough for now
        String baseurl = sce.getServletContext().getContextPath();

        sce.getServletContext().setAttribute("incogito", new IncogitoBean(baseurl));
        sce.getServletContext().setAttribute("app", context.getBean("incogitoApplication", IncogitoApplication.class));
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }
}
