package no.java.incogito.web;

import fj.data.List;
import fj.data.Option;
import fj.data.TreeMap;
import static fj.data.List.list;
import static fj.data.Option.some;
import fj.F;
import fj.P2;
import fj.P;
import fj.pre.Ord;
import junit.framework.TestCase;
import no.java.incogito.Functions;
import no.java.incogito.dto.SessionXml;
import no.java.incogito.web.servlet.WebCalendar;
import no.java.incogito.domain.Room;
import no.java.incogito.domain.CssConfiguration;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Level;
import no.java.incogito.domain.Label;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.WikiString;
import no.java.incogito.domain.Speaker;
import no.java.incogito.domain.Comment;
import no.java.incogito.domain.IncogitoTestData;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.UserSessionAssociation;
import no.java.incogito.domain.Level.LevelId;
import static no.java.incogito.domain.Event.EventId.eventId;

import java.util.UUID;
import java.util.Map;
import java.util.Collection;

import org.joda.time.Interval;
import org.joda.time.DateMidnight;
import org.joda.time.Duration;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class WebFunctionsTest extends TestCase {
    CssConfiguration cssConfiguration = new CssConfiguration(2.5, 20.0/60.0, 11);

    public void testFunctions() {
        List<Room> rooms = List.list(new Room("Room 1"), new Room("Room 2"));
        String s = WebFunctions.generateCss.f(cssConfiguration).f(rooms).foldRight(Functions.String_join.f("\n"), "");

        System.out.println(s);
    }

    public void testDuration() {
        F<Integer,String> f = WebFunctions.durationToCss.f(cssConfiguration);

        System.out.println("cssConfiguration.emPerMinute = " + cssConfiguration.emPerMinute);
        assertEquals(".duration15 { height: 5.0em; margin: 0; padding: 0; }", f.f(15));
        assertEquals(".duration30 { height: 10.0em; margin: 0; padding: 0; }", f.f(30));
        assertEquals(".duration60 { height: 20.0em; margin: 0; padding: 0; }", f.f(60));
    }

    public void testHourToCss() {
        F<P2<P2<String,String>, Integer>, String> f = WebFunctions.hourToSessionCss.f(cssConfiguration);

        assertEquals(".start0900 { top: 2.5em; }", f.f(P.p(P.p("09", "00"), 0)));
        assertEquals(".start0915 { top: 7.5em; }", f.f(P.p(P.p("09", "15"), 1)));
        assertEquals(".start0930 { top: 12.5em; }", f.f(P.p(P.p("09", "30"), 2)));
    }

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
        return WebFunctions.webCalendar.f(new Schedule(event, sessions, TreeMap.<SessionId, UserSessionAssociation>empty(SessionId.ord)));
    }

    public static void testRegexp() {
        assertEquals(" Trygve \\n er \\\"kul\\\".", " Trygve \n er \"kul\".".replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n"));
    }
}
