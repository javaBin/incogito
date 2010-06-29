package no.java.incogito.web.servlet;

import java.util.Collection;
import java.util.LinkedHashMap;

import no.java.incogito.dto.SessionXml;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import fj.data.List;

/**
 * A wrapper around a schedule with utilities for sessions.jspx.
 *
 * @author <a href="mailto:janniche@gmail.com">Janniche Haugen</a>
 * @version $Id$
 */
public class WebSessionList {
	private final LinkedHashMap<LocalDate, Collection<Interval>> timeslotsByDate;

	private final LinkedHashMap<LocalDate, LinkedHashMap <Interval, List<SessionXml>>> sessionsByTimeslotByDate;
    
    public WebSessionList(LinkedHashMap<LocalDate, Collection<Interval>> timeslotsByDate,
                        LinkedHashMap<LocalDate, LinkedHashMap <Interval, List<SessionXml>>> sessionsByTimeslotByDate) {
        this.timeslotsByDate = timeslotsByDate;
        this.sessionsByTimeslotByDate = sessionsByTimeslotByDate;
    }

    public LinkedHashMap<LocalDate, Collection<Interval>> getTimeslotsByDate() {
        return timeslotsByDate;
    }

    public LinkedHashMap<LocalDate, LinkedHashMap <Interval, List<SessionXml>>> getSessionsByTimeslotByDate() {
    	return sessionsByTimeslotByDate;
    }

}
