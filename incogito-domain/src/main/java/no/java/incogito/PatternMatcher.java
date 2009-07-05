package no.java.incogito;

import fj.F;
import fj.P2;
import fj.P;
import fj.Bottom;
import fj.data.List;

/**
 * This really should be <code>Class&lt;? extends A></code>.
 * @param <A>
 * @param <B>
 */
public class PatternMatcher<A, B> {

    private final List<P2<Class, F<A, B>>> matchers;

    PatternMatcher(List<P2<Class, F<A, B>>> matchers) {
        this.matchers = matchers;
    }

    public static <A, B> PatternMatcher<A, B> match() {
        return new PatternMatcher<A, B>(List.<P2<Class, F<A, B>>>nil());
    }

    public PatternMatcher<A, B> add(Class klass, F<A, B> f) {
        return new PatternMatcher<A, B>(matchers.cons(P.p(klass, f)));
    }

    public B match(A a) {
        for (P2<Class, F<A, B>> matcher : matchers.reverse()) {
            if (matcher._1().isAssignableFrom(a.getClass())) {
                return matcher._2().f(a);
            }
        }

        throw Bottom.error("No match");
    }
}
