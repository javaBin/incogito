package no.java.incogito;

import fj.F;
import fj.F2;
import fj.data.List;
import fj.data.Option;
import static fj.Function.curry;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Functions {
    public static final F<String, F<String, Boolean>> equals = curry( new F2<String, String, Boolean>() {
        public Boolean f(String s, String s1) {
            return s.equals(s1);
        }
    });

    public static <T> F<List<T>, T> head_() {
        return new F<List<T>, T>() {
            public T f(List<T> list) {
                return list.head();
            }
        };
    }

    public static <T> F<List<T>, Option<T>> toOption_() {
        return new F<List<T>, Option<T>>() {
            public Option<T> f(List<T> list) {
                return list.toOption();
            }
        };
    }

    public static <A, B> F<Option<A>, Option<B>> Option_map(final F<A, B> f) {
        return new F<Option<A>, Option<B>>() {
            public Option<B> f(Option<A> option) {
                return option.map(f);
            }
        };
    }

    public static <A> F<Option<A>, A> Option_somes() {
        return new F<Option<A>, A>() {
            public A f(Option<A> option) {
                return option.some();
            }
        };
    }

    public static <A> F<Option<Option<A>>, Option<A>> Option_join_() {
        return new F<Option<Option<A>>, Option<A>>() {
            public Option<A> f(Option<Option<A>> option) {
                return Option.join(option);
            }
        };
    }

    public static <A> F<F<A, Boolean>, F<List<A>, List<A>>> List_filter() {
        return curry( new F2<F<A, Boolean>, List<A>, List<A>>() {
            public List<A> f(F<A, Boolean> filter, List<A> list) {
                return list.filter(filter);
            }
        });
    }

    public static <A, B, C, D> F<A, D> compose(final F<C, D> h, final F<B, C> g, final F<A, B> f) {
        return new F<A, D>() {
            public D f(A a) {
                return h.f(g.f(f.f(a)));
            }
        };
    }

    public static <A, B, C, D, E> F<A, E> compose(final F<D, E> i, final F<C, D> h, final F<B, C> g, final F<A, B> f) {
        return new F<A, E>() {
            public E f(A a) {
                return i.f(h.f(g.f(f.f(a))));
            }
        };
    }

    public static <A, B, C, D, E, F$> F<A, F$> compose(final F<E, F$> j, final F<D, E> i, final F<C, D> h, final F<B, C> g, final F<A, B> f) {
        return new F<A, F$>() {
            public F$ f(A a) {
                return j.f(i.f(h.f(g.f(f.f(a)))));
            }
        };
    }

    public static <A, B, C, D, E, F$, G> F<A, G> compose(final F<F$, G> k, final F<E, F$> j, final F<D, E> i, final F<C, D> h, final F<B, C> g, final F<A, B> f) {
        return new F<A, G>() {
            public G f(A a) {
                return k.f(j.f(i.f(h.f(g.f(f.f(a))))));
            }
        };
    }
}
