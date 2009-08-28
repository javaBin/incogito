package no.java.incogito.application;

import static fj.Function.flip;
import fj.P;
import fj.data.TreeMap;
import fj.pre.Ord;
import junit.framework.TestCase;
import no.java.ems.client.RestEmsService;
import no.java.ems.domain.Event;
import no.java.incogito.Functions;
import no.java.incogito.domain.Event.EventId;
import no.java.incogito.domain.Label;
import no.java.incogito.ems.client.EmsWrapper;
import no.java.incogito.util.TestPathFactoryBean;

import java.io.File;
import java.util.UUID;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultIncogitoApplicationTest extends TestCase {
    public void testReconfigure() throws Exception {
        TestPathFactoryBean testPathFactoryBean = new TestPathFactoryBean();
        testPathFactoryBean.setTestClass(getClass());
        testPathFactoryBean.setPath("src/test/resources/configuration-loading");
        File incogitoHome = (File) testPathFactoryBean.getObject();
        EmsWrapper emsWrapper = new EmsWrapper(new RestEmsService(null));
        Event event = new Event();
        event.setId(UUID.randomUUID().toString());
        event.setName("JavaZone 2009");
        EventId eventId = EventId.eventId(event.getId());

        TreeMap<String, Event> events = TreeMap.<String, Event>empty(Ord.stringOrd).set(event.getName(), event);
        emsWrapper.listEvents = P.p(events.values());
        emsWrapper.findEventByName = flip(Functions.<String, Event>TreeMap_get()).f(events);
        DefaultIncogitoApplication application = new DefaultIncogitoApplication(incogitoHome, null, emsWrapper);

        application.afterPropertiesSet();

        IncogitoConfiguration configuration = application.getConfiguration();

        assertEquals(2, configuration.labels.get(eventId).some().size());
        assertEquals(5, configuration.levels.get(eventId).some().size());

        Label actualMyLabel = configuration.labels.get(eventId).some().get("MyLabel").some();
        assertEquals("My label", actualMyLabel.displayName);
        assertEquals("MyLabel", actualMyLabel.id);
        assertEquals("MyLabel", actualMyLabel.emsId);

        Label actualRenamedLabel = configuration.labels.get(eventId).some().get("renamed-label").some();
        assertEquals("Renamed Label", actualRenamedLabel.displayName);
        assertEquals("renamed-label", actualRenamedLabel.id);
        assertEquals("Renamed label", actualRenamedLabel.emsId);
    }
}
