package no.java.incogito.dto;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlRootElement(name = "attendanceMarker")
public class AttendanceMarkerXml {
    public enum State {
        ATTENDING,
        INTEREST
    }

    private String sessionId;
    private State state;

    public AttendanceMarkerXml() {
    }

    public AttendanceMarkerXml(String sessionId, State state) {
        this.sessionId = sessionId;
        this.state = state;
    }

    public String getSessionId() {
        return sessionId;
    }

    public State getState() {
        return state;
    }
}
