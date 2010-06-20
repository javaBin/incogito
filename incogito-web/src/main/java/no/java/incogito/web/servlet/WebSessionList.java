package no.java.incogito.web.servlet;

import java.util.Collection;
import java.util.LinkedHashMap;

import no.java.incogito.dto.SessionXml;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import fj.data.List;
import fj.data.TreeMap;

/**
 * A wrapper around a schedule with utilities for sessions.jspx.
 *
 * @author <a href="mailto:janniche@gmail.com">Janniche Haugen</a>
 * @version $Id$
 */
public class WebSessionList {
	private final LinkedHashMap<LocalDate, Collection<Interval>> timeslotsByDate;

	
    private final LinkedHashMap<LocalDate, Collection<String>> roomsByDate;


	private final LinkedHashMap<LocalDate, LinkedHashMap <Interval, List<SessionXml>>> sessionsByTimeslotByDate;
    
	
    private final TreeMap<LocalDate, TreeMap<String, List<SessionXml>>> dayToRoomToPresentationsMap;

    private final TreeMap<LocalDate, List<SessionXml>> quickiesByDay;
    

    public WebSessionList(LinkedHashMap<LocalDate, Collection<String>> roomsByDate,
                       LinkedHashMap<LocalDate, Collection<Interval>> timeslotsByDate,
                       TreeMap<LocalDate, TreeMap<String, List<SessionXml>>> dayToRoomToPresentationsMap,
                       TreeMap<LocalDate, List<SessionXml>> quickiesByDay, 
                        LinkedHashMap<LocalDate, LinkedHashMap <Interval, List<SessionXml>>> sessionsByTimeslotByDate) {
        this.roomsByDate = roomsByDate;
        this.timeslotsByDate = timeslotsByDate;
        this.dayToRoomToPresentationsMap = dayToRoomToPresentationsMap;
        this.quickiesByDay = quickiesByDay;
        this.sessionsByTimeslotByDate = sessionsByTimeslotByDate;
    }

    public LinkedHashMap<LocalDate, Collection<String>> getRoomsByDate() {
        return roomsByDate;
    }

    public LinkedHashMap<LocalDate, Collection<Interval>> getTimeslotsByDate() {
        return timeslotsByDate;
    }

    public LinkedHashMap<LocalDate, LinkedHashMap <Interval, List<SessionXml>>> getSessionsByTimeslotByDate() {
    	return sessionsByTimeslotByDate;
    }

    public TreeMap<LocalDate, TreeMap<String, List<SessionXml>>> getDayToRoomToPresentationsMap() {
        return dayToRoomToPresentationsMap;
    }

    public TreeMap<LocalDate, List<SessionXml>> getQuickiesByDay() {
        return quickiesByDay;
    }
}
