package no.java.incogito;

import fj.data.List;
import fj.data.Stream;
import static fj.data.Stream.stream;
import static fj.data.List.list;
import fj.pre.Equal;
import fj.pre.Show;
import junit.framework.TestCase;
import static no.java.incogito.Functions.Stream_cycle;

import java.util.Iterator;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class FunctionsTest extends TestCase {
    public void testPrepend() {
        assertEquals("ab", Functions.prepend.f("a").f("b"));
        assertEquals("ba", Functions.append.f("a").f("b"));
    }

    public void testStreamProduct() {
        List<String> stringStream = Functions.List_product(list("09", "10", "11"), list("00", "30"), Functions.prepend);

        Show<List<String>> show = Show.listShow(Show.stringShow);
        show.println(stringStream);

//        Show.streamShow(Show.stringShow).println(stream("09", "10", "11").zipWith(Stream.cycle(stream("00", "30")), Functions.prepend));

//        Show.streamShow(Show.stringShow).println(stream("09", "10", "11").zipWith(Stream_cycle(stream("00", "30")), Functions.prepend));

        Equal<List<String>> equal = Equal.listEqual(Equal.stringEqual);
        assertTrue(equal.eq(list("0900", "0930", "1000", "1030", "1100", "1130"), stringStream));
    }
}
