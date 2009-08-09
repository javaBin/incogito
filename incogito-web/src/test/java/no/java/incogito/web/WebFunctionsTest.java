package no.java.incogito.web;

import fj.data.List;
import fj.F;
import fj.P2;
import fj.P;
import junit.framework.TestCase;
import no.java.incogito.Functions;
import no.java.incogito.domain.Room;
import no.java.incogito.domain.CssConfiguration;

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
}
