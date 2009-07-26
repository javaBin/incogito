package no.java.incogito.dto;

import fj.data.Option;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlRootElement(name = "session")
public class UserSessionAssociationXml {
    public String sessionId;
    public SessionRatingXml rating;
    public String ratingComment;
    public InterestLevelXml interestLevel;

    public UserSessionAssociationXml() {
    }

    public UserSessionAssociationXml(String sessionId, SessionRatingXml rating, Option<String> ratingComment, InterestLevelXml interestLevel) {
        this.sessionId = sessionId;
        this.rating = rating;
        this.ratingComment = ratingComment.orSome((String) null);
        this.interestLevel = interestLevel;
    }

    public String getSessionId() {
        return sessionId;
    }

    public SessionRatingXml getRating() {
        return rating;
    }

    public String getRatingComment() {
        return ratingComment;
    }

    public InterestLevelXml getInterestLevel() {
        return interestLevel;
    }
}
