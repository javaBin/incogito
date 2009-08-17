package no.java.incogito.web;

import fj.F;
import fj.F2;
import fj.F3;
import static fj.Function.curry;
import fj.P;
import fj.P2;
import fj.data.List;
import static fj.data.List.list;
import fj.data.Stream;
import static fj.data.Stream.stream;
import no.java.incogito.Functions;
import no.java.incogito.domain.CssConfiguration;
import no.java.incogito.domain.Room;

import java.text.NumberFormat;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class WebFunctions {

    private static final NumberFormat oneDigitFormat;

    static {
        oneDigitFormat = NumberFormat.getNumberInstance();
        oneDigitFormat.setMaximumFractionDigits(1);
        oneDigitFormat.setMinimumFractionDigits(1);
    }

    public static final F<CssConfiguration, F<List<Room>, List<String>>> generateCss = curry(new F2<CssConfiguration, List<Room>, List<String>>() {
        public List<String> f(CssConfiguration cssConfiguration, List<Room> roomList) {
          
            List<String> sessions = Functions.List_product(list("09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"),
                    list("00", "15", "30", "45"), P.<String, String>p2()).
                    zipIndex().
                    map(hourToSessionCss.f(cssConfiguration));

            Stream<String> durations = stream(10, 60).zapp(Stream.repeat(durationToCss.f(cssConfiguration)));

            return List.join(list(sessions, durations.toList()));
        }
    });

    // div.room.r1 { left: 0; position: absolute; }
    public static final F<CssConfiguration, F<Integer, String>> durationToCss = curry(new F2<CssConfiguration, Integer, String>() {
        public String f(CssConfiguration cssConfiguration, Integer minutes) {
            // div.session.d15 { height: 1em; margin: 0; padding: 0; }
            return ".duration" + minutes + " { height: 10em; margin: 0; padding: 0; }";
        }
    });

//    public static final F<CssConfiguration, F<Double, String>> hourToSessionCss = Function.curry(new F2<String, Double, String>() {
//        public String f(String id, Double em) {
//            return ".start" + id + " { top: " + oneDigitFormat.format(em) + "em; }";
//        }
//    });

    public static final F<CssConfiguration, F<P2<P2<String, String>, Integer>, String>> hourToSessionCss = curry(new F2<CssConfiguration, P2<P2<String, String>, Integer>, String>() {
        F<P2<String, String>, String> prepend = P2.tuple(Functions.prepend);

        public String f(CssConfiguration cssConfiguration, P2<P2<String, String>, Integer> p) {
            double em = cssConfiguration.sessionEmStart + (cssConfiguration.getHeightInEm(p._2() * 10));
            return ".start" + prepend.f(p._1()) + " { top: " + oneDigitFormat.format(em) + "em; }";
        }
    });
}
