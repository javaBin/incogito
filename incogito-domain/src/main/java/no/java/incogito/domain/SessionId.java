package no.java.incogito.domain;

import fj.pre.Ord;
import fj.pre.Ordering;
import fj.Function;
import fj.F2;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SessionId {

    public final static Ord<SessionId> ord = Ord.ord(Function.curry( new F2<SessionId, SessionId, Ordering>() {
        public Ordering f(SessionId a, SessionId b) {
            return Ord.stringOrd.compare(a.value, b.value);
        }
    }));

    public final String value;

    public SessionId(String value) {
        this.value = value;
    }
}
