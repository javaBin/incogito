package no.java.incogito.dto;

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

    public SpeakerXml(String name, String bioHtml) {
        this.name = name;
        this.bioHtml = bioHtml;
    }

    public String getName() {
        return name;
    }

    public String getBioHtml() {
        return bioHtml;
    }
}
