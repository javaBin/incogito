package no.java.incogito.application;

import fj.*;
import fj.data.List;
import static fj.data.List.*;
import fj.data.TreeMap;
import fj.pre.*;
import junit.framework.*;
import no.java.ems.external.v2.*;
import no.java.incogito.application.IncogitoConfiguration.*;
import no.java.incogito.domain.*;
import no.java.incogito.ems.client.*;
import org.apache.commons.httpclient.*;
import org.codehaus.httpcache4j.cache.*;
import org.codehaus.httpcache4j.client.*;
import org.joda.time.*;

import java.util.*;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ConfigurationLoaderServiceTest extends TestCase {
    private LocalDate sep17th = new LocalDate(2008, 9, 17);
    private LocalDate sep18th = new LocalDate(2008, 9, 18);
    private List<Room> roomsDay1 = list(new Room("Lab I"), new Room("Lab II"), new Room("BoF"));
    private List<Room> roomsDay2 = list(new Room("Lab I"), new Room("Lab II"));

    public void testReconfigure() throws Exception {
        HTTPCache cache = new HTTPCache(
            new MemoryCacheStorage(),
            new HTTPClientResponseResolver(new HttpClient(new MultiThreadedHttpConnectionManager())));

        EmsWrapper emsWrapper = new EmsWrapper(new RESTfulEmsV2Client(cache));
        EventV2 event = new EventV2();
        event.setUuid(UUID.randomUUID().toString());
        event.setName("JavaZone 2009");

        final TreeMap<String, EventV2> events = TreeMap.<String, EventV2>empty(Ord.stringOrd).set(event.getName(), event);
        emsWrapper.getEvents = P.p(events.values());
        emsWrapper.findEventByName = DefaultIncogitoApplicationTest.mockFindEventByName.f(events);
        DefaultIncogitoApplication application = new DefaultIncogitoApplication(DefaultIncogitoApplicationTest.getIncogitoHome(), null, emsWrapper);

        application.afterPropertiesSet();

        IncogitoConfiguration configuration = application.getConfiguration();

        EventConfiguration eventConfiguration = configuration.findEventConfigurationByName(event.getName()).some();

        assertEquals(2, eventConfiguration.dayConfigurations.length());
        assertEquals(sep17th, eventConfiguration.dayConfigurations.index(0)._1());
        assertEquals(sep18th, eventConfiguration.dayConfigurations.index(1)._1());

        assertEquals(2, eventConfiguration.dayConfigurations.index(0)._2().rooms.length());
        assertEquals(new Room("Lab I"), eventConfiguration.dayConfigurations.index(0)._2().rooms.index(0));
        assertEquals(new Room("Lab II"), eventConfiguration.dayConfigurations.index(0)._2().rooms.index(1));

        DateTime s17 = sep17th.toDateMidnight().toDateTime();
        DateTime s18 = sep17th.toDateMidnight().toDateTime();

        assertEquals(8, eventConfiguration.dayConfigurations.index(0)._2().timeslots.length());
        assertEquals(new Interval(s17.withHourOfDay(9), s17.withHourOfDay(10)), eventConfiguration.dayConfigurations.index(0)._2().timeslots.index(0));
        assertEquals(new Interval(s17.withHourOfDay(18).withMinuteOfHour(15), s17.withHourOfDay(19).withMinuteOfHour(15)), eventConfiguration.dayConfigurations.index(0)._2().timeslots.index(7));

        assertEquals(3, eventConfiguration.dayConfigurations.index(1)._2().rooms.length());
        assertEquals(new Room("Lab I"), eventConfiguration.dayConfigurations.index(1)._2().rooms.index(0));
        assertEquals(new Room("Lab II"), eventConfiguration.dayConfigurations.index(1)._2().rooms.index(1));
        assertEquals(new Room("BoF"), eventConfiguration.dayConfigurations.index(1)._2().rooms.index(2));

        assertEquals(2, eventConfiguration.labels.length());
        assertEquals(eventConfiguration.labels.length(), eventConfiguration.labelMap.size());
        Label actualMyLabel = eventConfiguration.labelMap.get("MyLabel").some();
        assertEquals("My label", actualMyLabel.displayName);
        assertEquals("MyLabel", actualMyLabel.id);
        assertEquals("MyLabel", actualMyLabel.emsId);

        Label actualRenamedLabel = eventConfiguration.labelMap.get("renamed-label").some();
        assertEquals("Renamed Label", actualRenamedLabel.displayName);
        assertEquals("renamed-label", actualRenamedLabel.id);
        assertEquals("Renamed label", actualRenamedLabel.emsId);

        assertEquals("MyLabel", eventConfiguration.labels.index(0).id);
        assertEquals("renamed-label", eventConfiguration.labels.index(1).id);

        assertEquals(5, eventConfiguration.levels.size());
    }
}
