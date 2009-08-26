package no.java.incogito.web.servlet;

import fj.F;
import fj.data.List;
import static fj.data.Option.fromNull;
import fj.data.TreeMap;
import no.java.incogito.application.IncogitoApplication;
import no.java.incogito.domain.IncogitoUri;
import no.java.incogito.domain.IncogitoUri.IncogitoEventsUri.IncogitoEventUri;
import no.java.incogito.domain.Schedule;
import no.java.incogito.dto.EventXml;
import no.java.incogito.dto.ScheduleXml;
import no.java.incogito.dto.SessionXml;
import no.java.incogito.web.WebFunctions;
import no.java.incogito.web.resources.XmlFunctions;
import static no.java.incogito.web.resources.XmlFunctions.sessionToXml;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoFunctions {

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

    public static Object treeMapGet(TreeMap<Object, Object> map, Object key) {
        return map.get(key);
    }

    public static Object mapGet(Map map, Object key, Object defaultValue) {
        Object value = map.get(key);
        return value == null ? defaultValue : value;
    }

    public static SessionXml castToSession(Object o) {
        return SessionXml.class.cast(o);
    }

    public static SessionXml[] castToSessionList(Object o) {
        List<SessionXml> list = (List<SessionXml>) o;

        return list.toArray(SessionXml[].class).array();
    }

    public static WebCalendar getCalendar(IncogitoApplication app, String eventName, String userName) {
        IncogitoUri uri = new IncogitoUri(app.getConfiguration().baseurl);
        return app.getSchedule(urlDecode(eventName), fromNull(userName)).ok().
                map(WebFunctions.webCalendar.f(uri.events().eventUri(eventName))).value();
    }

    public static EventXml[] getEvents(IncogitoApplication app) {
        IncogitoUri uri = new IncogitoUri(app.getConfiguration().baseurl);
        return app.getEvents().value().
                map(XmlFunctions.eventToXml.f(uri.events())).toArray(EventXml[].class).array();
    }

    public static SessionXml[] getSessions(IncogitoApplication app, String eventName) {
        IncogitoUri uri = new IncogitoUri(app.getConfiguration().baseurl);
        return app.getSessions(urlDecode(eventName)).value().
                map(sessionToXml.f(uri.events().eventUri(eventName))).toArray(SessionXml[].class).array();
    }

    public static EventXml getEventByName(IncogitoApplication app, String eventName) {
        IncogitoUri uri = new IncogitoUri(app.getConfiguration().baseurl);
        return app.getEventByName(urlDecode(eventName)).ok().map(XmlFunctions.eventToXml.f(uri.events())).value();
    }

    public static SessionXml getSessionByTitle(IncogitoApplication app, String eventName, String sessionTitle) {
        IncogitoEventUri uri = new IncogitoUri(app.getConfiguration().baseurl).events().eventUri(eventName);
        return sessionToXml.f(uri).f(app.getSessionByTitle(urlDecode(eventName), urlDecode(sessionTitle)).value());
    }

    public static ScheduleXml getSchedule(IncogitoApplication app, String eventName, String userName) {
        IncogitoUri uri = new IncogitoUri(app.getConfiguration().baseurl);
        F<Schedule, ScheduleXml> scheduleToXml = XmlFunctions.scheduleToXml.f(uri.events().eventUri(eventName));
        return app.getSchedule(urlDecode(eventName), fromNull(userName)).ok().map(scheduleToXml).value();
    }
}
