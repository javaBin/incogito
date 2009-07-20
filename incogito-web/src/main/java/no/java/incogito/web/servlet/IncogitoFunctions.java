package no.java.incogito.web.servlet;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoFunctions {
    private final URLDecoder decoder = new URLDecoder();

    public static String urlDecode(String input) {
        try {
            return URLDecoder.decode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
