package no.java.incogito.dto;

import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlType(name = "label")
public class LevelXml {
    public String id;

    public String displayName;

    public String iconUrl;

    public LevelXml() {
    }

    public LevelXml(String id, String displayName, String iconUrl) {
        this.id = id;
        this.displayName = displayName;
        this.iconUrl = iconUrl;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}
