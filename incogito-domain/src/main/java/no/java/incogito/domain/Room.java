package no.java.incogito.domain;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Room {
    public final String name;

    public Room(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
