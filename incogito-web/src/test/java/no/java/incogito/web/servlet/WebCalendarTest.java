package no.java.incogito.web.servlet;

import fj.data.List;
import static fj.data.List.list;
import fj.data.Option;
import static fj.data.Option.some;
import fj.data.TreeMap;
import fj.pre.Ord;
import junit.framework.TestCase;
import no.java.incogito.domain.Comment;
import no.java.incogito.domain.Event;
import static no.java.incogito.domain.Event.EventId.eventId;
import no.java.incogito.domain.IncogitoTestData;
import no.java.incogito.domain.Label;
import no.java.incogito.domain.Level;
import no.java.incogito.domain.Level.LevelId;
import no.java.incogito.domain.Room;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.Speaker;
import no.java.incogito.domain.UserSessionAssociation;
import no.java.incogito.domain.WikiString;
import no.java.incogito.dto.SessionXml;
import org.joda.time.DateMidnight;
import org.joda.time.Duration;
import org.joda.time.Interval;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class WebCalendarTest extends TestCase {
    Event event = new Event(eventId(UUID.randomUUID().toString()), "FunZone", Option.<String>none(),
            List.<Room>nil(), TreeMap.<LevelId, Level>empty(LevelId.ord),
            TreeMap.<String, Label>empty(Ord.stringOrd));

    Session templateSession = new Session(new SessionId("123"), "Session 1", Option.<WikiString>none(),
            Option.<WikiString>none(), Option.<Level>none(), Option.<Interval>none(), Option.<String>none(),
            List.<Label>nil(), List.<Speaker>nil(), List.<Comment>nil());

    Option<String> room1 = some("Room 1");
    Option<String> room2 = some("Room 2");

    Option<Interval> day1 = some(new Interval(new DateMidnight(2009, 9, 9), new Duration(1000)));
    Option<Interval> day2 = some(new Interval(new DateMidnight(2009, 9, 10), new Duration(1000)));

    public void testEmptyCalendar() {
        WebCalendar calendar = createCalendar(list(templateSession));
        assertEquals(0, calendar.getDayToRoomToSessionMap().size());
        assertEquals(0, calendar.getRooms().size());

        calendar = createCalendar(list(templateSession.room(room1), templateSession.room(room2)));
        assertEquals(0, calendar.getDayToRoomToSessionMap().size());
        assertEquals(2, calendar.getRooms().size());

        calendar = createCalendar(list(templateSession.timeslot(day1), templateSession.timeslot(day2)));
        assertEquals(0, calendar.getDayToRoomToSessionMap().size());
        assertEquals(0, calendar.getRooms().size());
    }

    public void testCalendarWithSessions() {
        WebCalendar calendar = createCalendar(IncogitoTestData.sessions);
        assertEquals(7, calendar.getRooms().size());
        Collection<Map<String, List<SessionXml>>> dayToRoomToSessionMap = calendar.getDayToRoomToSessionMap();
        assertEquals(2, dayToRoomToSessionMap.size());

        // Make sure that all days have a list
        for (Map<String, List<SessionXml>> dayMap : dayToRoomToSessionMap) {
            for (String s : calendar.getRooms()) {
                assertNotNull(dayMap.get(s));
            }
        }
    }

    private WebCalendar createCalendar(List<Session> sessions) {
        Schedule schedule = new Schedule(event, sessions, TreeMap.<SessionId, UserSessionAssociation>empty(SessionId.ord));
        return new WebCalendar(schedule);
    }

    public static void testRegexp() {
        assertEquals(" Trygve \\n er \\\"kul\\\".", " Trygve \n er \"kul\".".replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n"));
    }
}
