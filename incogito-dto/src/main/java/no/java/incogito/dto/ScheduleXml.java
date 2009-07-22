package no.java.incogito.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlRootElement
public class ScheduleXml {
    private Collection<SessionXml> sessions = new ArrayList<SessionXml>();
    private Collection<AttendanceMarkerXml> attendanceMarkers = new ArrayList<AttendanceMarkerXml>();

    public ScheduleXml() {
    }

    public ScheduleXml(Collection<SessionXml> sessions, Collection<AttendanceMarkerXml> attendanceMarkers) {
        this.sessions = sessions;
        this.attendanceMarkers = attendanceMarkers;
    }

    public Collection<SessionXml> getSessions() {
        return sessions;
    }

    public Map<String, SessionXml> getSessionMap() {
        Map<String, SessionXml> sessionMap = new HashMap<String, SessionXml>();
        for (SessionXml session : sessions) {
            sessionMap.put(session.getId(), session);
        }
        return sessionMap;
    }

    public Collection<AttendanceMarkerXml> getAttendanceMarkers() {
        return attendanceMarkers;
    }
}
