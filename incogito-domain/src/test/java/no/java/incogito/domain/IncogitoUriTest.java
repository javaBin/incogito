package no.java.incogito.domain;

import junit.framework.TestCase;
import no.java.incogito.domain.IncogitoUri.IncogitoRestEventsUri.IncogitoRestEventUri.IncogitoRestSessionUri;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoUriTest extends TestCase {
    public void testBasic() {
        IncogitoUri incogitoUri = new IncogitoUri("http://localhost");

        String what = incogitoUri.events().eventUri("JavaZone 2009").session(IncogitoTestData.session46);

        assertEquals("http://localhost/events/JavaZone%202009/sessions/Dokumentasjon%20uten%20pistol%20-%20g%C3%A5r%20det%20an%3F", what);

        IncogitoRestSessionUri session2RestUri = incogitoUri.restEvents().eventUri("JavaZone 2009").session(IncogitoTestData.session3);

        String s = session2RestUri.toString();

        assertEquals("http://localhost/rest/events/JavaZone%202009/sessions/b8a6034f-573d-4321-9155-a5ed31885958", s);
        assertEquals("http://localhost/rest/events/JavaZone%202009/sessions/b8a6034f-573d-4321-9155-a5ed31885958/speaker-photos/2", session2RestUri.speakerPhoto(2));
    }
}
