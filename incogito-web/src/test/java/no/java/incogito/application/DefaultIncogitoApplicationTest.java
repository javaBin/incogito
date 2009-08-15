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

        assertEquals(1, configuration.getLabels(eventId).size());
        assertEquals(5, configuration.getLevels(eventId).size());

        assertEquals("MyLabel", configuration.getLabels(eventId).iterator().next()._1());
        Label myLabel = configuration.getLabels(eventId).iterator().next()._2();
        assertEquals("My label", myLabel.displayName);
        assertEquals("MyLabel", myLabel.id);
    }
}
