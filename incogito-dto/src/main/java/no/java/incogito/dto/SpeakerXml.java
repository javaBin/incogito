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
    public String photoUrl;

    public SpeakerXml() {
    }

    public SpeakerXml(String name, Option<String> bioHtml, String photoUrl) {
        this.name = name;
        this.bioHtml = bioHtml.orSome((String) null);
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public String getBioHtml() {
        return bioHtml;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
