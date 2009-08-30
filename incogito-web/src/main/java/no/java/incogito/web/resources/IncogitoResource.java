package no.java.incogito.web.resources;

import fj.F;
import fj.F2;
import static fj.Function.curry;
import fj.P1;
import fj.control.parallel.Callables;
import fj.data.Java;
import fj.data.List;
import fj.data.Option;
import static fj.data.Option.fromString;
import static fj.data.Option.join;
import static fj.data.Option.some;
import no.java.incogito.Functions;
import no.java.incogito.IO;
import no.java.incogito.PatternMatcher;
import static no.java.incogito.Functions.compose;
import no.java.incogito.application.IncogitoApplication;
import no.java.incogito.application.OperationResult;
import no.java.incogito.application.OperationResult.NotFoundOperationResult;
import no.java.incogito.application.OperationResult.OkOperationResult;
import static no.java.incogito.application.OperationResult.fromOption;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.IncogitoUri;
import no.java.incogito.domain.IncogitoUri.IncogitoRestEventsUri.IncogitoRestEventUri;
import no.java.incogito.domain.IncogitoUri.IncogitoEventsUri.IncogitoEventUri;
import no.java.incogito.domain.Label;
import no.java.incogito.domain.Level;
import no.java.incogito.domain.Level.LevelId;
import no.java.incogito.domain.Room;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.User;
import no.java.incogito.domain.UserSessionAssociation.InterestLevel;
import static no.java.incogito.dto.EventListXml.eventListXml;
import no.java.incogito.dto.EventXml;
import no.java.incogito.dto.IncogitoXml;
import no.java.incogito.dto.SessionListXml;
import no.java.incogito.dto.SessionXml;
import no.java.incogito.web.WebFunctions;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.File;

/**
 * REST-ful wrapper around IncogitoApplication.
 * <p/>
 * TODO: Add checks on every method that uses SecurityContext to check for nulls.
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

    private final CacheControl cacheForOneHourCacheControl;

    @Autowired
    public IncogitoResource(IncogitoApplication incogito) {
        this.incogito = incogito;

        cacheForOneHourCacheControl = new CacheControl();
        cacheForOneHourCacheControl.setMaxAge(3600);
    }

    @GET
    public Response getIncogito(@Context SecurityContext securityContext) {
        return Response.ok(new IncogitoXml(incogito.getConfiguration().baseurl, getUserName.f(securityContext))).build();
    }

    @Path("/events")
    @GET
    public Response getEvents(@Context UriInfo uriInfo) {
        F<List<Event>, List<EventXml>> eventListToXml = XmlFunctions.eventListToXml.f(new IncogitoUri(uriInfo.getRequestUriBuilder()));

        return toJsr311(incogito.getEvents().
                ok().map(compose(eventListXml, compose(Java.<EventXml>List_ArrayList(), eventListToXml))));
    }

    @Path("/events/{eventName}/calendar.css")
    @GET
    @Produces("text/css")
    public Response getEventCalendarCss(@PathParam("eventName") final String eventName) {
        final F<List<Room>, List<String>> generateCss = WebFunctions.generateCalendarCss.f(incogito.getConfiguration().cssConfiguration);

        return toJsr311(incogito.getEventByName(eventName).ok().map(new F<Event, String>() {
            public String f(Event event) {
                return generateCss.f(event.rooms).foldRight(Functions.String_join.f("\n"), "");
            }
        }), cacheForOneHourCacheControl);
    }

    @Path("/events/{eventName}/session.css")
    @GET
    @Produces("text/css")
    public Response getEventSessionCss(@PathParam("eventName") final String eventName) {
        final F<Event, List<String>> generateSessionCss = WebFunctions.generateSessionCss.f(incogito.getConfiguration());

        return toJsr311(incogito.getEventByName(eventName).ok().map(new F<Event, String>() {
            public String f(Event event) {
                return generateSessionCss.f(event).foldRight(Functions.String_join.f("\n"), "");
            }
        }), cacheForOneHourCacheControl);
    }

    @Path("/events/{eventName}/icons/levels/{level}.png")
    @GET
    @Produces("image/png")
    public Response getLevelIcon(@PathParam("eventName") final String eventName,
                                 @PathParam("level") final String level) {
        OperationResult<Event> eventResult = incogito.getEventByName(eventName);

        if (!eventResult.isOk()) {
            return toJsr311(eventResult);
        }

        Option<LevelId> levelOption = some(level).bind(Level.LevelId.valueOf);

        if (!levelOption.isSome()) {
            return toJsr311(OperationResult.<Object>notFound("Level '" + level + "' not known."));
        }

        Option<File> fileOption = eventResult.value().levels.get(levelOption.some()).map(Level.iconFile_);

        if (!fileOption.isSome()) {
            return toJsr311(OperationResult.<Object>notFound("No icon for level '" + level + "'."));
        }

        // TODO: How about some caching here?

        Option<byte[]> bytes = join(fileOption.
                map(IO.<byte[]>runFileInputStream_().f(IO.ByteArrays.streamToByteArray)).
                map(compose(P1.<Option<byte[]>>__1(), Callables.<byte[]>option())));

        return toJsr311(fromOption(bytes, "Unable to read level file."), cacheForOneHourCacheControl);
    }

    @Path("/events/{eventName}/icons/labels/{label}.png")
    @GET
    @Produces("image/png")
    public Response getLabelIcon(@PathParam("eventName") final String eventName,
                                 @PathParam("label") final String label) {
        OperationResult<Event> eventResult = incogito.getEventByName(eventName);

        if (!eventResult.isOk()) {
            return toJsr311(eventResult);
        }

        Option<File> fileOption = eventResult.value().labels.get(label).map(Label.iconFile_);

        if (!fileOption.isSome()) {
            return toJsr311(OperationResult.<Object>notFound("No icon for label '" + label + "'."));
        }

        // TODO: How about some caching here?

        Option<byte[]> bytes = join(fileOption.
                map(IO.<byte[]>runFileInputStream_().f(IO.ByteArrays.streamToByteArray)).
                map(compose(P1.<Option<byte[]>>__1(), Callables.<byte[]>option())));

        return toJsr311(fromOption(bytes, "Unable to read label file."), cacheForOneHourCacheControl);
    }

    @Path("/events/{eventName}")
    @GET
    public Response getEvent(@Context final UriInfo uriInfo,
                             @PathParam("eventName") final String eventName) {
        return toJsr311(incogito.getEventByName(eventName).ok().map(eventToXml.f(new IncogitoUri(uriInfo.getRequestUriBuilder()))));
    }

    @Path("/events/{eventName}/sessions")
    @GET
    public Response getSessionsForEvent(@Context final UriInfo uriInfo,
                                        @PathParam("eventName") final String eventName) {

        IncogitoUri incogitoUri = new IncogitoUri(uriInfo.getRequestUriBuilder());
        F<List<Session>, List<SessionXml>> sessionToXmlList = List.<Session, SessionXml>map_().
                f(XmlFunctions.sessionToXml.f(incogitoUri.restEvents().eventUri(eventName)).f(incogitoUri.events().eventUri(eventName)));

        return toJsr311(incogito.getSessions(eventName).
                ok().map(compose(SessionListXml.sessionListXml, sessionToXmlList)));
    }

    @Path("/events/{eventName}/sessions/{sessionId}")
    @GET
    public Response getSessionForEvent(@Context final UriInfo uriInfo,
                                       @PathParam("eventName") final String eventName,
                                       @PathParam("sessionId") final String sessionId) {
        IncogitoUri incogitoUri = new IncogitoUri(uriInfo.getRequestUriBuilder());
        IncogitoRestEventUri restEventUri = incogitoUri.restEvents().eventUri(eventName);
        IncogitoEventUri eventUri = incogitoUri.events().eventUri(eventName);

        // TODO: Consider replacing this with the configured host name and base url
        F<Session, SessionXml> sessionToXml = XmlFunctions.sessionToXml.f(restEventUri).f(eventUri);

        return toJsr311(incogito.getSession(eventName, new SessionId(sessionId)).
                ok().map(sessionToXml));
    }

    @Path("/events/{eventName}/sessions/{sessionId}/speaker-photos/{index}")
    @GET
    public Response getPersonPhoto(@Context final UriInfo uriInfo,
                                   @PathParam("eventName") final String eventName,
                                   @PathParam("sessionId") final String sessionId,
                                   @PathParam("index") final int index) {
        return toJsr311(incogito.getSpeakerPhotoForSession(sessionId, index), cacheForOneHourCacheControl);
    }

    @Path("/events/{eventName}/{sessionId}/session-interest")
    @POST
    public Response setSessionInterest(@Context final UriInfo uriInfo,
                                       @Context final SecurityContext securityContext,
                                       @PathParam("eventName") final String eventName,
                                       @PathParam("sessionId") final String sessionId,
                                       String payload) {

        Option<String> userName = getUserName.f(securityContext);

        if (userName.isNone()) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        OperationResult<User> result = incogito.setInterestLevel(userName.some(),
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
        IncogitoUri incogitoUri = new IncogitoUri(uriInfo.getRequestUriBuilder());
        IncogitoRestEventUri restEventUri = incogitoUri.restEvents().eventUri(eventName);
        IncogitoEventUri eventUri = incogitoUri.events().eventUri(eventName);

        return toJsr311(incogito.getSchedule(eventName, userName).ok().
                map(XmlFunctions.scheduleToXml.f(restEventUri).f(eventUri)));
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private <T> F<T, ResponseBuilder> ok() {
        return new F<T, ResponseBuilder>() {
            public ResponseBuilder f(T operationResult) {
                Object value = ((OperationResult) operationResult).value();
                ToStringBuilder.reflectionToString(value, ToStringStyle.MULTI_LINE_STYLE);
                return Response.ok(operationResult);
            }
        };
    }

    private F<CacheControl, F<ResponseBuilder, ResponseBuilder>> cacheControl = curry(new F2<CacheControl, ResponseBuilder, ResponseBuilder>() {
        public ResponseBuilder f(CacheControl cacheControl, ResponseBuilder responseBuilder) {
            return responseBuilder.cacheControl(cacheControl);
        }
    });

    private F<ResponseBuilder, Response> build = new F<ResponseBuilder, Response>() {
        public Response f(ResponseBuilder responseBuilder) {
            return responseBuilder.build();
        }
    };

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
                add(OkOperationResult.class, compose(build, this.<OperationResult<T>>ok())).
                match(result);
    }

    private <T> Response toJsr311(OperationResult<T> result, CacheControl cacheControl) {
        return this.<T>defaultResponsePatternMatcher().
                add(OkOperationResult.class, compose(build, this.cacheControl.f(cacheControl), this.<OperationResult<T>>ok())).
                match(result);
    }

    private F<SecurityContext, Option<String>> getUserName = new F<SecurityContext, Option<String>>() {
        public Option<String> f(SecurityContext securityContext) {
            return securityContext.getUserPrincipal() == null ? Option.<String>none() : fromString(securityContext.getUserPrincipal().getName());
        }
    };
}
