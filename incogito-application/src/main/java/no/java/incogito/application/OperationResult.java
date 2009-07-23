package no.java.incogito.application;

import fj.Bottom;
import fj.Effect;
import fj.F;
import fj.P1;
import fj.Unit;
import fj.data.Option;
import static fj.data.Option.some;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class OperationResult<T> {
    enum Status {
        OK,
        CONFLICT,
        NOT_FOUND
    }

    public final Status status;

    private OperationResult(Status status) {
        this.status = status;
    }

    public boolean isOk() {
        return this instanceof OkOperationResult;
    }

    public boolean isConflict() {
        return this instanceof ConflictOperationResult;
    }

    public boolean isNotFound() {
        return this instanceof NotFoundOperationResult;
    }

    public abstract T value();

    public boolean hasValue() {
        return false;
    }

    // -----------------------------------------------------------------------
    // Data Constructors
    // -----------------------------------------------------------------------

    public static class OkOperationResult<T> extends OperationResult<T> {
        public final Option<T> data;

        private OkOperationResult(Option<T> data) {
            super(Status.OK);

            this.data = data;
        }

        public T value() {
            return data.some();
        }

        public boolean hasValue() {
            return data.isSome();
        }
    }

    public static <T> OperationResult<T> ok(T t) {
        return new OkOperationResult<T>(some(t));
    }

    public static <T> OperationResult<T> ok(Option<T> t) {
        return new OkOperationResult<T>(t);
    }

    public static OperationResult<Unit> emptyOk() {
        return new OkOperationResult<Unit>(Option.<Unit>none());
    }

    public static <T> F<T, OperationResult<T>> ok_() {
        return new F<T, OperationResult<T>>() {
            public OperationResult<T> f(T t) {
                return ok(t);
            }
        };
    }

    public static <T> P1<OperationResult<T>> $ok(final T t) {
        return new P1<OperationResult<T>>() {
            @Override
            public OperationResult<T> _1() {
                return ok(t);
            }
        };
    }

    public static class ConflictOperationResult<T> extends OperationResult<T> {
        public final String message;

        private ConflictOperationResult(String message) {
            super(Status.CONFLICT);

            this.message = message;
        }

        public T value() {
            throw Bottom.error(message);
        }
    }

    public static <T> OperationResult<T> conflict(String message) {
        return new ConflictOperationResult<T>(message);
    }

    public static <T> P1<OperationResult<T>> $conflict(final String message) {
        return new P1<OperationResult<T>>() {
            public OperationResult<T> _1() {
                return conflict(message);
            }
        };
    }

    public static <T> F<T, OperationResult<T>> conflict_(final String message) {
        return new F<T, OperationResult<T>>() {
            public OperationResult<T> f(T t) {
                return conflict(message);
            }
        };
    }

    public static class NotFoundOperationResult<T> extends OperationResult<T> {
        public final String message;

        private NotFoundOperationResult(String message) {
            super(Status.NOT_FOUND);

            this.message = message;
        }

        public T value() {
            throw Bottom.error(message);
        }
    }

    public static <T> OperationResult<T> notFound(String message) {
        return new NotFoundOperationResult<T>(message);
    }

    public static <T> P1<OperationResult<T>> $notFound(final String message) {
        return new P1<OperationResult<T>>() {
            public OperationResult<T> _1() {
                return notFound(message);
            }
        };
    }

    // -----------------------------------------------------------------------
    // Projections
    // -----------------------------------------------------------------------

    public OkProjection ok() {
        return new OkProjection();
    }

    public class OkProjection {
        public <B> OperationResult<B> map(F<T, B> f) {
            if (isOk()) {
                if (hasValue()) {
                    return new OkOperationResult<B>(some(f.f(value())));
                }

                return new OkOperationResult<B>(Option.<B>none());
            } else {
                //noinspection unchecked
                return (OperationResult<B>) OperationResult.this;
            }
        }

        public <B> OperationResult<B> bind(F<T, Option<B>> f) {
            if (isOk()) {
                if (hasValue()) {
                    return new OkOperationResult<B>(f.f(value()));
                }

                return new OkOperationResult<B>(Option.<B>none());
            } else {
                //noinspection unchecked
                return (OperationResult<B>) OperationResult.this;
            }
        }

        public void foreach(Effect<T> effect) {
            if (isOk()) {
                effect.e(value());
            }
        }
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public void foreach(Effect<T> effect) {
        if(isOk() && hasValue()) {
            effect.e(value());
        }
    }
}
