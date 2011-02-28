package no.java.incogito.domain;

import junit.framework.TestCase;
import no.java.incogito.domain.User.UserId;
import static no.java.incogito.domain.UserSessionAssociation.InterestLevel.ATTEND;
import static no.java.incogito.domain.UserSessionAssociation.InterestLevel.INTEREST;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class AttendanceMarkerTest extends TestCase {
    SessionId sessionA = new SessionId("a");
    SessionId sessionB = new SessionId("b");
    SessionId sessionC = new SessionId("c");
    SessionId sessionD = new SessionId("d");
    SessionId sessionE = new SessionId("e");
    User user = User.createPristineUser(UserId.userIdFromString("trygvis"));

    public void testNewUserHasZeroAttendanceMarkers() {
        assertEquals(0, user.sessionAssociations.size());
    }

    public void testBasic() {
        user = user.
                setInterestLevel(sessionA, ATTEND).
                setInterestLevel(sessionB, INTEREST);

        assertEquals(2, user.sessionAssociations.size());

        assertEquals(ATTEND, user.sessionAssociations.get(sessionA).some().interestLevel);
        assertEquals(INTEREST, user.sessionAssociations.get(sessionB).some().interestLevel);

        // Change the marker from interest to attendance on sessionB
        user = user.setInterestLevel(sessionB, ATTEND);

        assertEquals(2, user.sessionAssociations.size());

        assertEquals(ATTEND, user.sessionAssociations.get(sessionA).some().interestLevel);
        assertEquals(ATTEND, user.sessionAssociations.get(sessionB).some().interestLevel);
    }
}
