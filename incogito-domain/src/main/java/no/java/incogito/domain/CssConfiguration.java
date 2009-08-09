package no.java.incogito.domain;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CssConfiguration {
    public final double sessionEmStart;
    public final double emPerMinute;
    public final double emPerRoom;

    public static final CssConfiguration defaultCssConfiguration = new CssConfiguration(2.5, 7.5, 11);

    public CssConfiguration(double sessionEmStart, double emPerMinute, double emPerRoom) {
        this.sessionEmStart = sessionEmStart;
        this.emPerMinute = emPerMinute;
        this.emPerRoom = emPerRoom;
    }

    public double getHeightInEm(Integer minutes) {
        return emPerMinute * minutes;
    }
}
