package no.java.incogito.dto;

import fj.data.Option;
import static no.java.incogito.dto.DtoUtil.toList;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlRootElement
public class EventXml {
    public String selfUrl;

    public String id;

    public String name;

    public String blurb;

    public String frontpageContent;

    public String htmlUrl;

    public String sessionHtmlUrl;

    public String calendarHtmlUrl;

    public List<LabelXml> labels;

    public List<LevelXml> levels;

    public EventXml() {
    }

    public EventXml(String selfUrl, String id, String name, Option<String> blurb, Option<String> frontpageContent,
                    String htmlUrl, String sessionHtmlUrl, String calendarHtmlUrl,
                    Iterable<LabelXml> labels, Iterable<LevelXml> levels) {
        this.selfUrl = selfUrl;
        this.id = id;
        this.name = name;
        this.blurb = blurb.orSome((String) null);
        this.frontpageContent = frontpageContent.orSome((String) null);
        this.htmlUrl = htmlUrl;
        this.sessionHtmlUrl = sessionHtmlUrl;
        this.calendarHtmlUrl = calendarHtmlUrl;
        this.labels = toList(labels);
        this.levels = toList(levels);
    }

    public String getName() {
        return name;
    }

    public String getBlurb() {
        return blurb;
    }

    public String getFrontpageContent() {
        return frontpageContent;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public String getSessionHtmlUrl() {
        return sessionHtmlUrl;
    }

    public String getCalendarHtmlUrl() {
        return calendarHtmlUrl;
    }

    public List<LevelXml> getLevels() {
        return levels;
    }

    public List<LabelXml> getLabels() {
        return labels;
    }
}
