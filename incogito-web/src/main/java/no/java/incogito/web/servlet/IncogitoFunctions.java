package no.java.incogito.web.servlet;

import fj.F;
import fj.P;
import fj.data.List;
import static fj.data.Option.fromNull;
import no.java.incogito.application.IncogitoApplication;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.Event;
import no.java.incogito.dto.EventXml;
import no.java.incogito.dto.ScheduleXml;
import no.java.incogito.dto.SessionXml;
import no.java.incogito.web.resources.XmlFunctions;

import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoFunctions {
    private static UriBuilder uriBuilder = UriBuilder.fromPath("http://you-re.doing.it.wrong");

    // The URI will never be used by the JSP pages. If they are, you're going it wrong.
    private static final F<Session, SessionXml> sessionToXml = XmlFunctions.sessionToXml.f(P.p(uriBuilder));

    private static final F<Schedule, ScheduleXml> scheduleToXml = XmlFunctions.scheduleToXml.f(P.p(uriBuilder));

    public static String urlDecode(String input) {
        try {
            return URLDecoder.decode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object some(fj.data.Option option) {
        return option.some();
    }

    public static Object mapGet(Map map, Object key) {
        return map.get(key);
    }

    public static SessionXml castToSession(Object o) {
        return SessionXml.class.cast(o);
    }

    public static SessionXml[] castToSessionList(Object o) {
        List<SessionXml> list = (List<SessionXml>) o;

        return list.toArray(SessionXml[].class).array();
    }

    public static WebCalendar getCalendar(IncogitoApplication app, String eventName, String userName) {
        return new WebCalendar(app.getSchedule(urlDecode(eventName), fromNull(userName)).value());
    }

    public static EventXml[] getEvents(IncogitoApplication app) {
        return app.getEvents().value().map(XmlFunctions.eventToXml).toArray(EventXml[].class).array();
    }

    public static SessionXml[] getSessions(IncogitoApplication app, String eventName) {
        return app.getSessions(urlDecode(eventName)).value().map(sessionToXml).toArray(SessionXml[].class).array();
    }

    public static EventXml getEventXmlByName(IncogitoApplication app, String eventName) {
        return app.getEventByName(urlDecode(eventName)).ok().map(XmlFunctions.eventToXml).value();
    }

    public static Event getEventByName(IncogitoApplication app, String eventName) {
        return app.getEventByName(urlDecode(eventName)).value();
    }

    public static SessionXml getSessionByTitle(IncogitoApplication app, String eventName, String sessionTitle) {
        return sessionToXml.f(app.getSessionByTitle(urlDecode(eventName), urlDecode(sessionTitle)).value());
    }

    public static ScheduleXml getSchedule(IncogitoApplication app, String eventName, String userName) {
        return app.getSchedule(urlDecode(eventName), fromNull(userName)).ok().map(scheduleToXml).value();
    }
}
