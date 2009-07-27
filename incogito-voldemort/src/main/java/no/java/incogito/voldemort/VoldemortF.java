package no.java.incogito.voldemort;

import voldemort.versioning.Versioned;
import fj.F;
import fj.data.Option;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class VoldemortF {
    public static <T> F<Versioned<T>, T> verionedGetValue() {
        return new F<Versioned<T>, T>() {
            public T f(Versioned<T> versioned) {
                return versioned.getValue();
            }
        };
    }
}
