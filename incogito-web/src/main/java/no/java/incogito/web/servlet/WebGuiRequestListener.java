package no.java.incogito.web.servlet;

import static org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletRequestListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;

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
        ServletContext context1 = sce.getServletContext();
        ApplicationContext context = getRequiredWebApplicationContext(context1);

        // This is enough for now
        String baseurl = context1.getContextPath();

        IncogitoConfiguration configuration = (IncogitoConfiguration)
                context.getBean("incogitoConfiguration", IncogitoConfiguration.class);

        configuration.setBaseurl(baseurl);

        context1.setAttribute("incogito", configuration);
        context1.setAttribute("app", context.getBean("incogitoApplication", IncogitoApplication.class));
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }
}
