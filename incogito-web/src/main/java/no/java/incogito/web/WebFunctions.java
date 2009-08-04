package no.java.incogito.web;

import fj.F;
import fj.F2;
import fj.Function;
import static fj.Function.curry;
import fj.P2;
import fj.data.List;
import static fj.data.List.list;
import fj.data.Stream;
import static fj.data.Stream.stream;
import no.java.incogito.Functions;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Room;

import java.text.NumberFormat;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class WebFunctions {

    private static final NumberFormat oneDigitFormat;

    public static final float minutesPerEm = 7.5f;
    public static final float emPerRoom = 11;

    static {
        oneDigitFormat = NumberFormat.getNumberInstance();
        oneDigitFormat.setMaximumFractionDigits(1);
        oneDigitFormat.setMinimumFractionDigits(1);
    }

    public static final F<Event, Stream<String>> generateCss = new F<Event, Stream<String>>() {
        public Stream<String> f(Event event) {
            List<String> rooms = event.rooms.zipIndex().map(P2.tuple(roomToCss));

            float sessionEmStart = 2.5f;
            Stream<Float> sessionEms = Stream.iterate(new F<Float, Float>() {
                public Float f(Float sessionEm) {
                    return sessionEm + 2;
                }
            }, sessionEmStart);

            Stream<String> sessions = Functions.List_product(list("09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"),
                    list("00", "15", "30", "45"), Functions.prepend).
                    toStream().
                    zipWith(sessionEms, WebFunctions.hourToSessionCss);

            Stream<String> durations = stream(15, 60).zapp(Stream.repeat(durationToCss.f(minutesPerEm)));

            return Stream.join(stream(rooms.toStream(), sessions, durations));
        }
    };

    // div.room.r1 { left: 0; position: absolute; }
    public static final F<Room, F<Integer, String>> roomToCss = curry(new F2<Room, Integer, String>() {
        public String f(Room room, Integer integer) {
            return ".room" + integer + " {left: " + (integer * emPerRoom) + "em; position: absolute;}";
        }
    });

    // div.room.r1 { left: 0; position: absolute; }
    public static final F<Float, F<Integer, String>> durationToCss = curry(new F2<Float, Integer, String>() {
        public String f(Float minutesPerEm, Integer minutes) {
            // div.session.d15 { height: 1em; margin: 0; padding: 0; }
            return ".duration" + minutes + " { height: " + (minutes / minutesPerEm) + "em; margin: 0; padding: 0; }";
        }
    });

    public static final F<String, F<Float, String>> hourToSessionCss = Function.curry(new F2<String, Float, String>() {

        public String f(String id, Float em) {
            return ".start" + id + " { top: " + oneDigitFormat.format(em) + "em; }";
        }
    });
}
