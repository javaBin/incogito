package no.java.incogito.application;

import fj.F;
import fj.P1;
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

    public final Option<String> message;

    public final Option<T> data;

    public OperationResult(Status status, Option<String> message, Option<T> data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static class OkOperationResult<T> extends OperationResult<T> {

        public OkOperationResult(T data) {
            super(Status.OK, Option.<String>none(), some(data));
        }
    }

    public static class ConflictOperationResult<T> extends OperationResult<T> {

        public ConflictOperationResult(String message) {
            super(Status.CONFLICT, some(message), Option.<T>none());
        }
    }

    public static class NotFoundOperationResult<T> extends OperationResult<T> {

        public NotFoundOperationResult(String message) {
            super(Status.NOT_FOUND, some(message), Option.<T>none());
        }
    }

    public static <T> F<T, OperationResult<T>> ok_() {
        return new F<T, OperationResult<T>>() {
            public OperationResult<T> f(T t) {
                return new OkOperationResult<T>(t);
            }
        };
    }

    public static <T> P1<OperationResult<T>> $ok(final T t) {
        return new P1<OperationResult<T>>() {
            @Override
            public OperationResult<T> _1() {
                return new OkOperationResult<T>(t);
            }
        };
    }

    public static <T> P1<OperationResult<T>> $conflict(final String message) {
        return new P1<OperationResult<T>>() {
            public OperationResult<T> _1() {
                return new ConflictOperationResult<T>(message);
            }
        };
    }

    public static <T> F<T, OperationResult<T>> conflict_(final String message) {
        return new F<T, OperationResult<T>>() {
            public OperationResult<T> f(T t) {
                return new ConflictOperationResult<T>(message);
            }
        };
    }

    public static <T> P1<OperationResult<T>> $notFound(final String message) {
        return new P1<OperationResult<T>>() {
            public OperationResult<T> _1() {
                return new NotFoundOperationResult<T>(message);
            }
        };
    }
}
