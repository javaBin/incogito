package no.java.incogito.domain;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Speaker {
    public final String name;
    public final WikiString bio;

    public Speaker(String name, WikiString bio) {
        this.name = name;
        this.bio = bio;
    }

    public String getName() {
        return name;
    }

    public WikiString getBio() {
        return bio;
    }
}
