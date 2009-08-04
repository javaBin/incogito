package no.java.incogito.web.servlet;

import fj.Effect;
import fj.data.Option;
import static fj.data.Option.fromNull;
import fj.pre.Show;
import no.java.incogito.application.IncogitoApplication;
import no.java.incogito.application.OperationResult;
import no.java.incogito.domain.Event;
import no.java.incogito.web.WebFunctions;
import static org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CssGeneratorServletGenerator extends HttpServlet {
    private IncogitoApplication incogito;

    public void init() throws ServletException {
        incogito = (IncogitoApplication) getRequiredWebApplicationContext(getServletContext()).
                getBean("incogitoApplication", IncogitoApplication.class);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Option<String> eventNameOption = fromNull(request.getAttribute("eventName")).map(Show.anyShow().showS_());

        String eventName = IncogitoFunctions.urlDecode(eventNameOption.some());

        OperationResult<Event> result = incogito.getEventByName(eventName);

        if(!result.isOk()) {
            if(result.isNotFound()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Event not found: '" + eventName + "'.");
                return;
            }

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        Event event = result.value();

        final PrintWriter writer = response.getWriter();

        WebFunctions.generateCss.f(event).foreach(new Effect<String>() {
            public void e(String s) {
                writer.println(s);
            }
        });
    }
}
