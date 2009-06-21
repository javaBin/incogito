package no.java.incogito.domain;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Comment {
    public final String user;
    public final String text;

    public Comment(String user, String text) {
        this.user = user;
        this.text = text;
    }
}
