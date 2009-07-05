package no.java.incogito.web.resources;

import fj.F;
import static fj.Function.compose;
import fj.data.Java;
import no.java.incogito.PatternMatcher;
import no.java.incogito.application.IncogitoApplication;
import no.java.incogito.application.OperationResult;
import no.java.incogito.domain.Event;
import no.java.incogito.dto.EventXml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Component
@Path("/")
public class IncogitoResource {

    private final IncogitoApplication incogito;

    @Autowired
    public IncogitoResource(IncogitoApplication incogito) {
        this.incogito = incogito;
    }

    @Path("events")
    @GET
    public Response getEvents() {
        OperationResult<ArrayList<EventXml>> result = incogito.getEvents().
                ok().map(compose(Java.<EventXml>List_ArrayList(), Functions.eventListToXml));

        return PatternMatcher.<OperationResult<ArrayList<EventXml>>, Response>match().
                add(OperationResult.OkOperationResult.class, this.<OperationResult<ArrayList<EventXml>>>ok()).
                match(result);
    }

    @Path("events/{eventId}")
    @GET
    public Response getEvent(@PathParam("eventId") String eventId) {
        return PatternMatcher.<OperationResult<EventXml>, Response>match().
                add(OperationResult.OkOperationResult.class, this.<OperationResult<EventXml>>ok()).
                match(incogito.getEvent(Event.id(eventId)).ok().map(Functions.eventToXml));
    }

    private <T> F<T, Response> ok() {
        return new F<T, Response>() {
            public Response f(T operationResult) {
                return Response.ok(operationResult).build();
            }
        };
    }
}
