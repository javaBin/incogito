package no.java.incogito.dto;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlRootElement(name = "attendanceMarker")
@XmlAccessorType(FIELD)
public class AttendanceMarkerXml {
    @XmlEnum
    public enum State {
        NOT_ATTENDING,
        ATTENDING,
        INTERESTED
    }

    @XmlElement(required = true)
    public String sessionId;

    @XmlElement(required = true)
    public State state;

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
