package no.java.incogito.dto;

import static no.java.incogito.dto.DtoUtil.toList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlRootElement(name = "schedule")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({SessionXml.class})
public class ScheduleXml {
    @XmlElementWrapper(name = "sessions")
    @XmlElement(name = "session")
    public List<SessionXml> sessions = new ArrayList<SessionXml>();

    @XmlElementWrapper(name = "attendanceMarkers", nillable = false, required = true)
    @XmlElement(name = "attendanceMarker")
    public List<AttendanceMarkerXml> attendanceMarkers = new ArrayList<AttendanceMarkerXml>();

    public ScheduleXml() {
    }

    public ScheduleXml(Iterable<SessionXml> sessions, Iterable<AttendanceMarkerXml> attendanceMarkers) {
        this.sessions = toList(sessions);
        this.attendanceMarkers = toList(attendanceMarkers);
    }

    public Map<String, SessionXml> getSessionMap() {
        Map<String, SessionXml> sessionMap = new HashMap<String, SessionXml>();
        for (SessionXml session : sessions) {
            sessionMap.put(session.id, session);
        }
        return sessionMap;
    }

    public List<SessionXml> getSessions() {
        return sessions;
    }

    public List<AttendanceMarkerXml> getAttendanceMarkers() {
        return attendanceMarkers;
    }
}
