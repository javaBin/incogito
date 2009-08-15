package no.java.incogito.web.servlet;

import fj.data.List;
import no.java.incogito.dto.SessionXml;

import java.util.Collection;
import java.util.Map;

/**
 * A wrapper around a schedule with utilities for iframe/calendar.jspx.
 *
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class WebCalendar {
    private final Collection<String> rooms;

    private final Collection<Integer> timeslotHours;

    private final Map<String, String> attendanceMap;

    private final Collection<Map<String, List<SessionXml>>> dayToRoomToSessionMap;

    public WebCalendar(Collection<String> rooms, Collection<Integer> timeslotHours, Map<String, String> attendanceMap, Collection<Map<String, List<SessionXml>>> dayToRoomToSessionMap) {
        this.rooms = rooms;
        this.timeslotHours = timeslotHours;
        this.attendanceMap = attendanceMap;
        this.dayToRoomToSessionMap = dayToRoomToSessionMap;
    }

    public Collection<Integer> getTimeslotHours() {
        return timeslotHours;
    }

    public Collection<String> getRooms() {
        return rooms;
    }

    public Map<String, String> getAttendanceMap() {
        return attendanceMap;
    }

    public Collection<Map<String, List<SessionXml>>> getDayToRoomToSessionMap() {
        return dayToRoomToSessionMap;
    }
}
