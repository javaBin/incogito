package no.java.incogito.web.servlet;

import fj.F;
import fj.F2;
import fj.P;
import fj.P1;
import fj.data.List;
import fj.data.Set;
import static fj.data.Set.empty;
import fj.data.TreeMap;
import fj.pre.Ord;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import no.java.incogito.dto.SessionXml;
import no.java.incogito.web.resources.XmlFunctions;
import org.joda.time.LocalDate;

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

    private final List<String> rooms;

    private final List<Integer> timeslotHours;

    public WebCalendar(Schedule schedule) {
        this.schedule = schedule;
        timeslotHours = schedule.sessions.foldLeft(timeslotFold, Set.<Integer>empty(Ord.intOrd)).toList().reverse();
        rooms = schedule.sessions.foldLeft(roomFolder, Set.<String>empty(Ord.stringOrd)).toList().reverse();
    }

    public Collection<Integer> getTimeslotHours() {
        return timeslotHours.toCollection();
    }

    public Collection<String> getRooms() {
        return rooms.toCollection();
    }

    public Collection<Map<String, List<SessionXml>>> getDayToRoomToSessionMap() {
        P1<UriBuilder> uriBuilder = P.p(UriBuilder.fromUri("http://poop"));

        F<Session,SessionXml> sessionToXml = XmlFunctions.sessionToXml.f(uriBuilder);

        List<Session> sessions = schedule.sessions.filter(new F<Session, Boolean>() {
            public Boolean f(Session session) {
                return session.timeslot.isSome() && session.room.isSome();
            }
        });

        F2<Set<LocalDate>, Session, Set<LocalDate>> folder = new F2<Set<LocalDate>, Session, Set<LocalDate>>() {
            public Set<LocalDate> f(Set<LocalDate> dateTimeSet, Session session) {
                return dateTimeSet.insert(session.timeslot.some().getStart().toLocalDate());
            }
        };

        Ord<LocalDate> ord = Ord.comparableOrd();
        Set<LocalDate> days = sessions.foldLeft(folder, empty(ord));

        final List<SessionXml> emptyList = List.nil();
        List<Map<String, List<SessionXml>>> list = List.nil();

        for (final LocalDate day : days) {
            F<Session, Boolean> dayFilter = new F<Session, Boolean>() {
                public Boolean f(Session session) {
                    return session.timeslot.some().getStart().toLocalDate().equals(day);
                }
            };

            TreeMap<String, List<SessionXml>> map = rooms.foldLeft(new F2<TreeMap<String, List<SessionXml>>, String, TreeMap<String, List<SessionXml>>>() {
                public TreeMap<String, List<SessionXml>> f(TreeMap<String, List<SessionXml>> stringListTreeMap, String room) {
                    return stringListTreeMap.set(room, emptyList);
                }
            }, TreeMap.<String, List<SessionXml>>empty(Ord.stringOrd));

            for (SessionXml session : sessions.filter(dayFilter).map(sessionToXml)) {
                map = map.set(session.room, map.get(session.room).some().cons(session));
            }

            list = list.cons(map.toMutableMap());
        }

        return list.toCollection();
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
