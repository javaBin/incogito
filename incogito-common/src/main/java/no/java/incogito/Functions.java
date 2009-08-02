package no.java.incogito;

import fj.Bottom;
import fj.F;
import fj.F2;
import fj.F3;
import static fj.Function.curry;
import fj.P2;
import fj.data.Either;
import fj.data.List;
import static fj.data.List.cons;
import static fj.data.List.list;
import fj.data.Option;
import fj.data.TreeMap;
import fj.pre.Show;
import static fj.pre.Show.show;

import java.io.File;
import java.util.Iterator;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Functions {

    // -----------------------------------------------------------------------
    // java.lang.Strings
    // -----------------------------------------------------------------------

    public static final F<String, F<String, Boolean>> equals = curry( new F2<String, String, Boolean>() {
        public Boolean f(String s, String s1) {
            return s.equals(s1);
        }
    });

    public static final F<String, F<String, List<String>>> split = curry( new F2<String, String, List<String>>() {
        public List<String> f(String regex, String string) {
            return list( string.split( regex ) );
        }
    });

    public static final F<String, String> trim = new F<String, String>() {
        public String f(String s) {
            return s.trim();
        }
    };

    /**
     * f a b = a + b
     */
    public static final F<String, F<String, String>> append = curry( new F2<String, String, String>() {
        public String f(String a, String b) {
            return b.concat(a);
        }
    });

    /**
     * f a b = b + a
     */
    public static final F<String, F<String, String>> prepend = curry( new F2<String, String, String>() {
        public String f(String a, String b) {
            return a.concat(b);
        }
    });

    public static final F<Integer, F<String, String>> substring = curry( new F2<Integer, String, String>() {
        public String f(Integer integer, String s) {
            return s.substring(integer);
        }
    });

    public static final F<Integer, F<Integer, F<String, String>>> substring2 = curry( new F3<Integer, Integer, String, String>() {
        public String f(Integer beginIndex, Integer endIndex, String s) {
            return s.substring(beginIndex, endIndex);
        }
    });

    // -----------------------------------------------------------------------
    // java.io.File
    // -----------------------------------------------------------------------

    public static final F<String, File> newRelativeFile = new F<String, File>() {
        public File f(String s) {
            return new File(s);
        }
    };

    public static final F<File, F<String, File>> newFile = curry(new F2<File, String, File>() {
        public File f(File file, String s) {
            return new File(file, s);
        }
    });

    public static final F<File, Boolean> canRead = new F<File, Boolean>() {
        public Boolean f(File file) {
            return file.canRead();
        }
    };

    // -----------------------------------------------------------------------
    // fj.data.Either
    // -----------------------------------------------------------------------

    public static <R> R throwLeft(Either<Exception, R> either) throws Exception {
        if(either.isLeft()) {
            throw either.left().value();
        }

        return either.right().value();
    }

    // -----------------------------------------------------------------------
    // fj.data.Option
    // -----------------------------------------------------------------------

    // I'll get killed for this - trygve
    public static <A> A toNull(Option<A> option){
        return option.isSome() ? option.some() : null;
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

    public static <A> A Option_valueE(Option<A> option, String e) {
        if(option.isNone()) {
            throw Bottom.error(e);
        }

        return option.some();
    }

    // -----------------------------------------------------------------------
    // fj.data.TreeMap
    // -----------------------------------------------------------------------

    public static <K, V> F<K, F<TreeMap<K, V>, Option<V>>> TreeMap_get() {
        return new F<K, F<TreeMap<K, V>, Option<V>>>() {
            public F<TreeMap<K, V>, Option<V>> f(final K k) {
                return new F<TreeMap<K, V>, Option<V>>() {
                    public Option<V> f(TreeMap<K, V> map) {
                        return map.get(k);
                    }
                };
            }
        };
    }

    // -----------------------------------------------------------------------
    // fj.data.List
    // -----------------------------------------------------------------------

    public static <T> F<List<T>, T> head_() {
        return new F<List<T>, T>() {
            public T f(List<T> list) {
                return list.head();
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

    // -----------------------------------------------------------------------
    // fj.Function
    // -----------------------------------------------------------------------

    public static <A, B, C> F<A, C> compose(final F<B, C> g, final F<A, B> f) {
        return new F<A, C>() {
            public C f(A a) {
                return g.f(f.f(a));
            }
        };
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

    public static <A, B> Show<TreeMap<A, B>> treeMapShow(final Show<A> showA, final Show<B> showB) {
        return show(new F<TreeMap<A, B>, List<Character>>() {
            public List<Character> f(final TreeMap<A, B> map) {
                if(map.isEmpty()) {
                    return list('(', ')');
                }

                final List.Buffer<Character> buffer = List.Buffer.empty();

                Iterator<P2<A,B>> it = map.iterator();
                int size = map.size();
                for (int i = 0; i < size; i++) {
                    P2<A, B> p = it.next();

                    buffer.append(showA.show(p._1())).snoc('=').append(showB.show(p._2()));

                    if(i < size - 1) {
                        buffer.snoc(',');
                        buffer.snoc(' ');
                    }
                }
                buffer.snoc(')');
                return cons('(', buffer.toList());
            }
        });
    }
}
