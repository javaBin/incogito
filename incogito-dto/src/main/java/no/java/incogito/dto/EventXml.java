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

    public String welcomeHtml;

    public List<LabelXml> labels;

    public List<LevelXml> levels;

    public EventXml() {
    }

    public EventXml(String selfUrl, String id, String name, Option<String> welcomeHtml, Iterable<LabelXml> labels, Iterable<LevelXml> levels) {
        this.selfUrl = selfUrl;
        this.id = id;
        this.name = name;
        this.welcomeHtml = welcomeHtml.orSome((String) null);
        this.labels = toList(labels);
        this.levels = toList(levels);
    }

    public String getName() {
        return name;
    }

    public String getWelcomeHtml() {
        return welcomeHtml;
    }

    public List<LevelXml> getLevels() {
        return levels;
    }

    public List<LabelXml> getLabels() {
        return labels;
    }
}
