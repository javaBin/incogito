package no.java.incogito.domain;

import fj.data.Option;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Speaker {
    public final String name;
    public final String uuid;
    public final Option<WikiString> bio;

    public Speaker(String name, String uuid, Option<WikiString> bio) {
        this.name = name;
        this.uuid = uuid;
        this.bio = bio;
    }
}
