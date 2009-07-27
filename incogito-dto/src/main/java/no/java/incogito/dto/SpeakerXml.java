package no.java.incogito.dto;

import fj.data.Option;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlRootElement
public class SpeakerXml {
    public String name;
    public String bioHtml;

    public SpeakerXml() {
    }

    public SpeakerXml(String name, Option<String> bioHtml) {
        this.name = name;
        this.bioHtml = bioHtml.orSome((String) null);
    }

    public String getName() {
        return name;
    }

    public String getBioHtml() {
        return bioHtml;
    }
}
