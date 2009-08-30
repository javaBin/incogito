package no.java.incogito.web.resources;

import junit.framework.TestCase;
import static no.java.incogito.domain.IncogitoTestData.javaZone2008;
import static no.java.incogito.domain.IncogitoTestData.session43;
import no.java.incogito.domain.IncogitoUri;
import no.java.incogito.domain.IncogitoUri.IncogitoEventsUri.IncogitoEventUri;
import no.java.incogito.domain.IncogitoUri.IncogitoRestEventsUri.IncogitoRestEventUri;
import no.java.incogito.dto.SessionXml;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class XmlFunctionsTest extends TestCase {
    public void testBasic() {
        IncogitoUri incogitoUri = new IncogitoUri("http://localhost");
        IncogitoRestEventUri restEventUri = incogitoUri.restEvents().eventUri(javaZone2008.name);
        IncogitoEventUri eventUri = incogitoUri.events().eventUri(javaZone2008.name);

        SessionXml sessionXml = XmlFunctions.sessionToXml.f(restEventUri).f(eventUri).f(session43);

        assertEquals("http://localhost/events/JavaZone%202008/sessions/Design%20Sense%20-%20Cultivating%20Deep%20Software%20Design%20Skill", sessionXml.getSessionHtmlUrl());
    }
}
