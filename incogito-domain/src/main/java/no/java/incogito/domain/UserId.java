package no.java.incogito.domain;

import fj.F;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class UserId {
    public final String value;

    public UserId(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserId userId = (UserId) o;

        return value.equals(userId.value);

    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public static final F<String, UserId> fromString = new F<String, UserId>() {
        public UserId f(String value) {
            return new UserId(value);
        }
    };

    public static final F<UserId, String> toString = new F<UserId, String>() {
        public String f(UserId userId) {
            return userId.value;
        }
    };
}
