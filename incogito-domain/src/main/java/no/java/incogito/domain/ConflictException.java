package no.java.incogito.domain;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String s) {
        super(s);
    }
}
