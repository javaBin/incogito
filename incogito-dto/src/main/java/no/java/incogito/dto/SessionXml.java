package no.java.incogito.dto;

import fj.data.Option;
import static no.java.incogito.dto.DtoUtil.toList;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlRootElement(name = "session")
public class SessionXml {
    public String selfUri;
    public String id;
    public String title;
    @XmlElement(name = "abstract")
    public String abstractHtml;
    public String bodyHtml;
    public String room;
    public XMLGregorianCalendar timeslot;
    public List<SpeakerXml> speakers = new ArrayList<SpeakerXml>();

    public SessionXml() {
    }

    public SessionXml(String selfUri, String id, String title, Option<String> abstractHtml, Option<String> bodyHtml,
                      Option<String> room, Option<XMLGregorianCalendar> timeslot, Iterable<SpeakerXml> speakers) {
        this.selfUri = selfUri;
        this.id = id;
        this.title = title;
        this.abstractHtml = abstractHtml.orSome((String) null);
        this.bodyHtml = bodyHtml.orSome((String) null);
        this.room = room.orSome((String) null);
        this.timeslot = timeslot.orSome((XMLGregorianCalendar) null);
        this.speakers = toList(speakers);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAbstractHtml() {
        return abstractHtml;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }

    public String getRoom() {
        return room;
    }

    public XMLGregorianCalendar getTimeslot() {
        return timeslot;
    }

    public List<SpeakerXml> getSpeakers() {
        return speakers;
    }
}
