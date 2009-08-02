package no.java.incogito.dto;

import fj.data.Option;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlRootElement
public class EventXml {
    public String id;

    public String name;

    public String welcomeHtml;

    public EventXml() {
    }

    public EventXml(String id, String name, Option<String> welcomeHtml) {
        this.id = id;
        this.name = name;
        this.welcomeHtml = welcomeHtml.orSome((String) null);
    }

    public String getName() {
        return name;
    }

    public String getWelcomeHtml() {
        return welcomeHtml;
    }
}
