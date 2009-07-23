package no.java.incogito.dto;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import java.net.URI;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlRootElement
public class SessionXml {
    public String selfUri;

    public String id;

    public String title;

    public SessionXml() {
    }

    public SessionXml(String selfUri, String id, String title) {
        this.selfUri = selfUri;
        this.id = id;
        this.title = title;
    }
}
