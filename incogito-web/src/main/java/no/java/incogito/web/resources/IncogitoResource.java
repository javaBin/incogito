package no.java.incogito.web.resources;

import fj.F;
import fj.F2;
import fj.Function;
import static fj.Function.compose;
import static fj.Function.join;
import fj.data.Java;
import fj.data.List;
import fj.data.Option;
import static no.java.incogito.Functions.equals;
import no.java.incogito.PatternMatcher;
import no.java.incogito.application.IncogitoApplication;
import no.java.incogito.application.OperationResult;
import no.java.incogito.application.OperationResult.NotFoundOperationResult;
import no.java.incogito.application.OperationResult.OkOperationResult;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Session;
import static no.java.incogito.dto.EventListXml.eventListXml;
import no.java.incogito.dto.EventXml;
import no.java.incogito.dto.SessionListXml;
import no.java.incogito.dto.SessionXml;
import static no.java.incogito.web.resources.Functions.eventListToXml;
import static no.java.incogito.web.resources.Functions.eventToXml;
import static no.java.incogito.web.resources.Functions.sessionToURL;
import no.java.incogito.web.servlet.IncogitoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * REST-ful wrapper around IncogitoApplication.
 *
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Component
@Path("/rest")
@Produces({"application/xml", "application/json"})
public class IncogitoResource {

    private final IncogitoApplication incogito;

    private final IncogitoConfiguration configuration;

    @Autowired
    public IncogitoResource(IncogitoApplication incogito, IncogitoConfiguration configuration) {
        this.incogito = incogito;
        this.configuration = configuration;
    }

    @Path("/events")
    @GET
    public Response getEvents() {
        System.out.println("IncogitoResource.getEvents");
        return toJsr311(incogito.getEvents().
                ok().map(compose(eventListXml, compose(Java.<EventXml>List_ArrayList(), eventListToXml))));
    }

    @Path("/events/{eventName}")
    @GET
    public Response getEvent(@PathParam("eventName") final String eventName) {
        System.out.println("IncogitoResource.getEvent");
        return toJsr311(incogito.getEventByName(eventName).ok().map(eventToXml));
    }

    @Path("/events/{eventName}/sessions")
    @GET
    public Response getSessionsForEvent(@PathParam("eventName") final String eventName) {
        System.out.println("IncogitoResource.getSessionsForEvent");

        F<Session, SessionXml> sessionToXml = join(Function.compose(Functions.sessionToXml,
                sessionToURL.f(configuration.getBaseurl())));

        F<List<Session>, List<SessionXml>> sessionToXmlList = List.<Session, SessionXml>map_().f(sessionToXml);

        return toJsr311(incogito.getSessions(eventName).
                ok().map(compose(SessionListXml.sessionListXml, sessionToXmlList)));
    }

    @Path("/events/{eventName}/sessions/{sessionTitle}")
    @GET
    public Response getSessionForEvent(@PathParam("eventName") final String eventName,
                                       @PathParam("sessionTitle") final String sessionTitle) {
        System.out.println("IncogitoResource.getSessionForEvent");

        F<Session, SessionXml> sessionToXml = join(Function.compose(Functions.sessionToXml,
                sessionToURL.f(configuration.getBaseurl())));

        return toJsr311(incogito.getSession(eventName, sessionTitle).
                ok().map(sessionToXml));
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private <T> F<T, Response> ok() {
        return new F<T, Response>() {
            public Response f(T operationResult) {
                return Response.ok(operationResult).build();
            }
        };
    }

    Response notFound = Response.status(Status.NOT_FOUND).build();

    private F<String, F<List<Event>, Option<Event>>> findEvent = Function.curry(new F2<String, List<Event>, Option<Event>>() {
        public Option<Event> f(String eventName, List<Event> eventList) {
            return eventList.find(compose(equals.f(eventName), Event.getName));
        }
    });

    private <T> Response toJsr311(OperationResult<T> result) {
        return PatternMatcher.<OperationResult<T>, Response>match().
                add(OkOperationResult.class, this.<OperationResult<T>>ok()).
                add(NotFoundOperationResult.class, Function.<OperationResult<T>, Response>constant(notFound)).
                match(result);
    }
}
