package no.java.incogito.web.servlet;

import fj.data.List;
import fj.data.TreeMap;
import no.java.incogito.dto.SessionXml;
import org.joda.time.LocalDate;
import org.joda.time.Interval;

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
    private final Map<String, String> attendanceMap;

    private final LinkedHashMap<LocalDate, Collection<String>> roomsByDate;

    private final LinkedHashMap<LocalDate, Collection<Interval>> timeslotsByDate;

    private final TreeMap<LocalDate, TreeMap<String, List<SessionXml>>> dayToRoomToPresentationsMap;

    private final TreeMap<LocalDate, List<SessionXml>> quickiesByDay;

    public WebCalendar(Map<String, String> attendanceMap,
                       LinkedHashMap<LocalDate, Collection<String>> roomsByDate,
                       LinkedHashMap<LocalDate, Collection<Interval>> timeslotsByDate,
                       TreeMap<LocalDate, TreeMap<String, List<SessionXml>>> dayToRoomToPresentationsMap,
                       TreeMap<LocalDate, List<SessionXml>> quickiesByDay) {
        this.attendanceMap = attendanceMap;
        this.roomsByDate = roomsByDate;
        this.timeslotsByDate = timeslotsByDate;
        this.dayToRoomToPresentationsMap = dayToRoomToPresentationsMap;
        this.quickiesByDay = quickiesByDay;
    }

    public Map<String, String> getAttendanceMap() {
        return attendanceMap;
    }

    public LinkedHashMap<LocalDate, Collection<String>> getRoomsByDate() {
        return roomsByDate;
    }

    public LinkedHashMap<LocalDate, Collection<Interval>> getTimeslotsByDate() {
        return timeslotsByDate;
    }

    public TreeMap<LocalDate, TreeMap<String, List<SessionXml>>> getDayToRoomToPresentationsMap() {
        return dayToRoomToPresentationsMap;
    }

    public TreeMap<LocalDate, List<SessionXml>> getQuickiesByDay() {
        return quickiesByDay;
    }
}
