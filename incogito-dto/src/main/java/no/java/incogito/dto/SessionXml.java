package no.java.incogito.dto;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlRootElement
public class SessionXml {
    private String title;

    public SessionXml() {
    }

    public SessionXml(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
