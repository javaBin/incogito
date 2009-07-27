package no.java.incogito.domain;

import fj.data.Option;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Speaker {
    public final String name;
    public final Option<WikiString> bio;

    public Speaker(String name, Option<WikiString> bio) {
        this.name = name;
        this.bio = bio;
    }
}
