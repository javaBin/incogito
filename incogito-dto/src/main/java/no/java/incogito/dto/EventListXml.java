package no.java.incogito.dto;

import fj.F;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlList;
import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlRootElement
public class EventListXml {
    private List<EventXml> events;

    public EventListXml() {
    }

    public EventListXml(List<EventXml> events) {
        this.events = events;
    }

    public static final F<ArrayList<EventXml>, EventListXml> eventListXml = new F<ArrayList<EventXml>, EventListXml>() {
        public EventListXml f(ArrayList<EventXml> eventXmls) {
            return new EventListXml(eventXmls);
        }
    };

    public List<EventXml> getEvents() {
        return events;
    }

    public void setEvents(List<EventXml> events) {
        this.events = events;
    }
}
