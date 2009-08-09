package no.java.incogito;

import fj.F;
import fj.F2;
import static fj.Function.curry;
import fj.pre.Ord;
import fj.pre.Ordering;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Enums {

    public static <T extends Enum<T>> F<T, String> toString_() {
        return new F<T, String>() {
            public String f(T anEnum) {
                return anEnum.toString();
            }
        };
    }

    public static <T extends Enum<T>> F<T, String> name_() {
        return new F<T, String>() {
            public String f(T anEnum) {
                return anEnum.name();
            }
        };
    }

    public static <T extends Enum<T>> F<Class<T>, F<String, T>> valueOf() {
        return curry(new F2<Class<T>, String, T>() {
            public T f(Class<T> klass, String string) {
                return Enum.valueOf(klass, string);
            }
        });
    }

    public static <A extends Enum> Ord<A> ord() {
        return Ord.ord(curry(new F2<A, A, Ordering>() {
            public Ordering f(A a, A b) {
                return Ord.stringOrd.compare(a.name(), b.name());
            }
        }));
    }
}
