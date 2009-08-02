package no.java.incogito;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class FunctionsTest extends TestCase {
    public void testPrepend() {
        assertEquals("ab", Functions.prepend.f("a").f("b"));
        assertEquals("ba", Functions.append.f("a").f("b"));
    }
}
