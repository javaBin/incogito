package no.java.incogito.web.servlet;

import fj.F2;
import fj.P;
import fj.P1;
import fj.F;
import fj.data.List;
import fj.data.Set;
import fj.data.TreeMap;
import fj.pre.Ord;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import no.java.incogito.dto.SessionXml;
import no.java.incogito.web.resources.XmlFunctions;

import javax.ws.rs.core.UriBuilder;
import java.util.Collection;
import java.util.Map;

/**
 * A wrapper around a schedule with utilities for iframe/calendar.jspx.
 *
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class WebCalendar {
    private final Schedule schedule;

    public WebCalendar(Schedule schedule) {
        this.schedule = schedule;
    }

    public Collection<Integer> getTimeslotHours() {
        return schedule.sessions.foldLeft(timeslotFold, Set.<Integer>empty(Ord.intOrd)).
                toList().reverse().toCollection();
    }

    public Collection<String> getRooms() {
        return schedule.sessions.foldLeft(roomFolder, Set.<String>empty(Ord.stringOrd)).
                toList().reverse().toCollection();
    }

    public Map<String, List<SessionXml>> getRoomToSessionMap() {
        P1<UriBuilder> y = P.p(UriBuilder.fromUri("http://poop"));

        F<Session,SessionXml> f = XmlFunctions.sessionToXml.f(y);

        List<SessionXml> emptyList = List.nil();
        TreeMap<String, List<SessionXml>> map = TreeMap.empty(Ord.stringOrd);

        for (Session session : schedule.sessions) {
            if(session.room.isNone()) {
                continue;
            }

            String room = session.room.some();
            map = map.set(room, map.get(room).orSome(emptyList).cons(f.f(session)));
        }

        return map.toMutableMap();
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private static final F2<Set<Integer>, Session, Set<Integer>> timeslotFold = new F2<Set<Integer>, Session, Set<Integer>>() {
        public Set<Integer> f(Set<Integer> hours, Session session) {
            if (session.timeslot.isNone()) {
                return hours;
            }

            return hours.insert(session.timeslot.some().getStart().getHourOfDay());
        }
    };

    private static final F2<Set<String>, Session, Set<String>> roomFolder = new F2<Set<String>, Session, Set<String>>() {
        public Set<String> f(Set<String> hours, Session session) {
            if (session.room.isNone()) {
                return hours;
            }

            return hours.insert(session.room.some());
        }
    };
}
