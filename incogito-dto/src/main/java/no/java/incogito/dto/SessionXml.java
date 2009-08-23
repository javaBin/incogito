package no.java.incogito.dto;

import fj.data.Option;
import static no.java.incogito.dto.DtoUtil.toList;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlRootElement(name = "session")
public class SessionXml {

    @XmlType(name = "type")
    @XmlEnum(String.class)
    public enum FormatXml {
        Presentation,
        Lightning_talk,
        BoF
    }

    public String selfUri;
    public String id;
    public FormatXml format;
    public String title;
    public String abstractHtml;
    public String bodyHtml;
    public LevelXml level;
    public String room;
    public XMLGregorianCalendar start;
    public XMLGregorianCalendar end;
    public List<SpeakerXml> speakers = new ArrayList<SpeakerXml>();
    public List<LabelXml> labels = new ArrayList<LabelXml>();

    public SessionXml() {
    }

    public SessionXml(String selfUri, FormatXml format, String id, String title, Option<String> abstractHtml,
                      Option<String> bodyHtml, Option<LevelXml> level, Option<String> room, Option<XMLGregorianCalendar> start,
                      Option<XMLGregorianCalendar> end, Iterable<SpeakerXml> speakers, Iterable<LabelXml> labels) {
        this.selfUri = selfUri;
        this.id = id;
        this.format = format;
        this.title = title;
        this.abstractHtml = abstractHtml.orSome((String) null);
        this.bodyHtml = bodyHtml.orSome((String) null);
        this.level = level.orSome((LevelXml) null);
        this.room = room.orSome((String) null);
        this.start = start.orSome((XMLGregorianCalendar) null);
        this.end = end.orSome((XMLGregorianCalendar) null);
        this.speakers = toList(speakers);
        this.labels = toList(labels);
    }

    public String getId() {
        return id;
    }

    public FormatXml getFormat() {
        return format;
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

    public LevelXml getLevel() {
        return level;
    }

    public String getRoom() {
        return room;
    }

    public XMLGregorianCalendar getStart() {
        return start;
    }

    public XMLGregorianCalendar getEnd() {
        return end;
    }

    public List<SpeakerXml> getSpeakers() {
        return speakers;
    }

    public List<LabelXml> getLabels() {
        return labels;
    }
}
