package no.java.incogito.domain;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoUriTest extends TestCase {
    public void testBasic() {
        IncogitoUri incogitoUri = new IncogitoUri("http://localhost");

        String what = incogitoUri.events().eventUri("JavaZone 2009").session("What?!");

        assertEquals("http://localhost/events/JavaZone%202009/sessions/What%3F!", what);
    }
}
