package no.java.incogito.web.resources;

import fj.F;
import static fj.Function.compose;
import fj.P1;
import fj.data.Java;
import fj.data.List;
import no.java.incogito.PatternMatcher;
import no.java.incogito.application.IncogitoApplication;
import no.java.incogito.application.OperationResult;
import no.java.incogito.application.OperationResult.NotFoundOperationResult;
import no.java.incogito.application.OperationResult.OkOperationResult;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.User;
import no.java.incogito.domain.UserSessionAssociation.InterestLevel;
import static no.java.incogito.dto.EventListXml.eventListXml;
import no.java.incogito.dto.EventXml;
import no.java.incogito.dto.SessionListXml;
import no.java.incogito.dto.SessionXml;
import static no.java.incogito.web.resources.XmlFunctions.eventListToXml;
import static no.java.incogito.web.resources.XmlFunctions.eventToXml;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * REST-ful wrapper around IncogitoApplication.
 *
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Component
@Path("/rest")
@Produces({"application/xml", "application/json"})
@SuppressWarnings({"UnusedDeclaration"})
public class IncogitoResource {

    private final IncogitoApplication incogito;

    @Autowired
    public IncogitoResource(IncogitoApplication incogito) {
        this.incogito = incogito;
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
    public Response getSessionsForEvent(@Context final UriInfo uriInfo,
                                        @PathParam("eventName") final String eventName) {
        System.out.println("IncogitoResource.getSessionsForEvent");

        P1<UriBuilder> uriBuilder = new P1<UriBuilder>() {
            public UriBuilder _1() {
                return uriInfo.getRequestUriBuilder();
            }
        };

        F<List<Session>, List<SessionXml>> sessionToXmlList = List.<Session, SessionXml>map_().f(XmlFunctions.sessionToXml.f(uriBuilder));

        return toJsr311(incogito.getSessions(eventName).
                ok().map(compose(SessionListXml.sessionListXml, sessionToXmlList)));
    }

    @Path("/events/{eventName}/sessions/{sessionId}")
    @GET
    public Response getSessionForEvent(@Context final UriInfo uriInfo,
                                       @PathParam("eventName") final String eventName,
                                       @PathParam("sessionId") final String sessionId) {
        System.out.println("IncogitoResource.getSessionForEvent");

        // TODO: Consider replacing this with the configured hostname and base url
        F<Session, SessionXml> sessionToXml = XmlFunctions.sessionToXml.f(uriBuilderClone.f(uriInfo.getRequestUriBuilder()));

        return toJsr311(incogito.getSession(eventName, new SessionId(sessionId)).
                ok().map(sessionToXml));
    }

    @Path("/events/{eventName}/{sessionId}/session-interest")
    @POST
    public Response setSessionInterest(@Context final UriInfo uriInfo,
                                       @Context final SecurityContext securityContext,
                                       @PathParam("eventName") final String eventName,
                                       @PathParam("sessionId") final String sessionId,
                                       String payload) {
        OperationResult<User> result = incogito.setInterestLevel(securityContext.getUserPrincipal().getName(),
                eventName,
                new SessionId(sessionId),
                InterestLevel.valueOf(payload));

        return this.<User>defaultResponsePatternMatcher().
                add(OkOperationResult.class, this.<OperationResult<User>>created()).
                match(result);
    }

    @Path("/events/{eventName}/my-schedule")
    @GET
    public Response getMySchedule(@Context final UriInfo uriInfo,
                                  @Context final SecurityContext securityContext,
                                  @PathParam("eventName") final String eventName) {
        String name = securityContext.getUserPrincipal().getName();

        if (name == null) {
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }

        return getScheduleForUser(uriInfo, securityContext, eventName, name);
    }

    @Path("/events/{eventName}/schedules/{userName}")
    @GET
    public Response getScheduleForUser(@Context final UriInfo uriInfo,
                                       @Context final SecurityContext securityContext,
                                       @PathParam("eventName") final String eventName,
                                       @PathParam("userName") final String userName) {
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().segment("events", eventName, "sessions");

        return toJsr311(incogito.getSchedule(eventName, userName).ok().
                map(XmlFunctions.scheduleToXml.f(uriBuilderClone.f(uriBuilder))));
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private <T> F<T, Response> ok() {
        return new F<T, Response>() {
            public Response f(T operationResult) {
                Object value = ((OperationResult) operationResult).value();
                ToStringBuilder.reflectionToString(value, ToStringStyle.MULTI_LINE_STYLE);
                return Response.ok(operationResult).build();
            }
        };
    }

    private <T> F<T, Response> created() {
        return new F<T, Response>() {
            public Response f(T operationResult) {
                Object value = ((OperationResult) operationResult).value();
                ToStringBuilder.reflectionToString(value, ToStringStyle.MULTI_LINE_STYLE);
                return Response.status(Status.CREATED).build();
            }
        };
    }

    private <T> F<T, Response> notFound() {
        return new F<T, Response>() {
            public Response f(T operationResult) {
                NotFoundOperationResult o = (NotFoundOperationResult) operationResult;

                return Response.status(NOT_FOUND).
                        header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).
                        entity(o.message + "\n").
                        build();
            }
        };
    }

    private <T> PatternMatcher<OperationResult<T>, Response> defaultResponsePatternMatcher() {
        return PatternMatcher.<OperationResult<T>, Response>match().
            add(NotFoundOperationResult.class, this.<OperationResult<T>>notFound());
    }

    private <T> Response toJsr311(OperationResult<T> result) {
        return this.<T>defaultResponsePatternMatcher().
                add(OkOperationResult.class, this.<OperationResult<T>>ok()).
                match(result);
    }

    private F<UriBuilder, P1<UriBuilder>> uriBuilderClone = new F<UriBuilder, P1<UriBuilder>>() {
        public P1<UriBuilder> f(final UriBuilder uriBuilder) {
            return new P1<UriBuilder>() {
                @Override
                public UriBuilder _1() {
                    return uriBuilder.clone();
                }
            };
        }
    };
}
