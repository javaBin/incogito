package no.java.incogito.dto;

import static no.java.incogito.dto.DtoUtil.toList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlRootElement(name = "schedule")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({SessionXml.class})
public class ScheduleXml {
    @XmlElementWrapper(name = "sessions")
    @XmlElement(name = "session")
    public List<SessionXml> sessions = new ArrayList<SessionXml>();

    @XmlElementWrapper(name = "sessionAssociations", nillable = false, required = true)
    @XmlElement(name = "attendanceMarker")
    public List<UserSessionAssociationXml> sessionAssociations = new ArrayList<UserSessionAssociationXml>();

    private Map<String, UserSessionAssociationXml> sessionAssociationMap;

    public ScheduleXml() {
    }

    public ScheduleXml(Iterable<SessionXml> sessions, Iterable<UserSessionAssociationXml> sessionAssociations) {
        this.sessions = toList(sessions);
        this.sessionAssociations = toList(sessionAssociations);

        sessionAssociationMap = new HashMap<String, UserSessionAssociationXml>();
        for (UserSessionAssociationXml sessionAssociation : this.sessionAssociations) {
            sessionAssociationMap.put(sessionAssociation.sessionId, sessionAssociation);
        }
    }

    public List<SessionXml> getSessions() {
        return sessions;
    }

    public List<UserSessionAssociationXml> getSessionAssociations() {
        return sessionAssociations;
    }

    public Map<String, UserSessionAssociationXml> getSessionAssociationMap() {
        return sessionAssociationMap;
    }
}
