package no.java.incogito.web.servlet;

import fj.data.List;
import fj.data.TreeMap;
import no.java.incogito.dto.SessionXml;
import org.joda.time.LocalDate;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A wrapper around a schedule with utilities for iframe/calendar.jspx.
 *
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class WebCalendar {
    private final Collection<Integer> timeslotHours;

    private final Map<String, String> attendanceMap;

    private final LinkedHashMap<LocalDate, Collection<String>> roomsByDate;

    private final TreeMap<LocalDate, TreeMap<String, List<SessionXml>>> dayToRoomToPresentationsMap;

    private final TreeMap<LocalDate, List<SessionXml>> quickiesByDay;

    public WebCalendar(Collection<Integer> timeslotHours, Map<String, String> attendanceMap,
                       LinkedHashMap<LocalDate, Collection<String>> roomsByDate,
                       TreeMap<LocalDate, TreeMap<String, List<SessionXml>>> dayToRoomToPresentationsMap,
                       TreeMap<LocalDate, List<SessionXml>> quickiesByDay) {
        this.timeslotHours = timeslotHours;
        this.attendanceMap = attendanceMap;
        this.roomsByDate = roomsByDate;
        this.dayToRoomToPresentationsMap = dayToRoomToPresentationsMap;
        this.quickiesByDay = quickiesByDay;
    }

    public Map<String, String> getAttendanceMap() {
        return attendanceMap;
    }

    public LinkedHashMap<LocalDate, Collection<String>> getRoomsByDate() {
        return roomsByDate;
    }

    public TreeMap<LocalDate, TreeMap<String, List<SessionXml>>> getDayToRoomToPresentationsMap() {
        return dayToRoomToPresentationsMap;
    }

    public TreeMap<LocalDate, List<SessionXml>> getQuickiesByDay() {
        return quickiesByDay;
    }
}
