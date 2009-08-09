package no.java.incogito.web;

import fj.Effect;
import fj.data.List;
import fj.data.Option;
import junit.framework.TestCase;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Event.EventId;
import static no.java.incogito.domain.Event.emptyLevelIconMap;
import static no.java.incogito.domain.Event.emptyLabelIconMap;
import no.java.incogito.domain.Room;

import java.util.UUID;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class WebFunctionsTest extends TestCase {
    public void testFunctions() {
        List<Room> rooms = List.list(new Room("Room 1"), new Room("Room 2"));
        Event event = new Event(EventId.eventId(UUID.randomUUID().toString()), "name", Option.<String>none(), rooms, 
                emptyLevelIconMap, emptyLabelIconMap);
        WebFunctions.generateCss.f(event).foreach(new Effect<String>() {
            public void e(String s) {
                System.out.println(s);
            }
        });
    }

    public void testDuration() {
        assertEquals(".duration15 { height: 2.0em; margin: 0; padding: 0; }", WebFunctions.durationToCss.f(7.5f).f(15));
        assertEquals(".duration30 { height: 4.0em; margin: 0; padding: 0; }", WebFunctions.durationToCss.f(7.5f).f(30));
        assertEquals(".duration60 { height: 8.0em; margin: 0; padding: 0; }", WebFunctions.durationToCss.f(7.5f).f(60));
    }
}
