package no.java.incogito.server;

import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DevNodeB {
    public static void main(String[] args) {
        Main.main(new String[]{new File("src/test/resources/node-b").getAbsolutePath()});
    }
}
