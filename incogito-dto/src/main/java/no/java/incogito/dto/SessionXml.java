package no.java.incogito.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlRootElement
public class SessionXml {
    private String selfUri;

    private String id;

    private String title;

    public SessionXml() {
    }

    public SessionXml(String selfUri, String id, String title) {
        this.selfUri = selfUri;
        this.id = id;
        this.title = title;
    }

    public String getSelfUri() {
        return selfUri;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
