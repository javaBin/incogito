package no.java.incogito.dto;

import fj.F;
import fj.data.Option;
import static fj.data.Option.none;
import static fj.data.Option.some;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public enum SessionRatingXml {
    BAD,
    OK,
    GOOD,
    NOT_SET;

    public static final F<String, Option<SessionRatingXml>> valueOf_ = new F<String, Option<SessionRatingXml>>() {
        public Option<SessionRatingXml> f(String s) {
            try {
                return some(SessionRatingXml.valueOf(s));
            } catch (IllegalArgumentException e) {
                return none();
            }
        }
    };
}
