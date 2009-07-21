package no.java.incogito.web.servlet;

import no.java.incogito.application.IncogitoApplication;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.Speaker;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

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

    public static String urlEncode(String input) {
        try {
            return URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object some(fj.data.Option option) {
        return option.some();
    }

    public static Event[] getEvents(IncogitoApplication app) {
        return app.getEvents().value().toArray(Event[].class).array();
    }

    public static Session[] getSessions(IncogitoApplication app, String eventName) {
        return app.getSessions(urlDecode(eventName)).value().toArray(Session[].class).array();
    }

    public static Event getEventByName(IncogitoApplication app, String eventName) {
        return app.getEventByName(urlDecode(eventName)).value();
    }

    public static Session getSession(IncogitoApplication app, String eventName, String sessionTitle) {
        return app.getSession(urlDecode(eventName), urlDecode(sessionTitle)).value();
    }

    public static Speaker[] speakers(Session session) {
        return session.speakers.toArray(Speaker[].class).array();
    }
}
