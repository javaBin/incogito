package no.java.incogito;

import fj.F;
import fj.F2;
import static fj.Function.curry;

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

    public static <T extends Enum<T>> F<Class<T>, F<String, T>> valueOf() {
        return curry(new F2<Class<T>, String, T>() {
            public T f(Class<T> klass, String string) {
                return Enum.valueOf(klass, string);
            }
        });
    }
}
