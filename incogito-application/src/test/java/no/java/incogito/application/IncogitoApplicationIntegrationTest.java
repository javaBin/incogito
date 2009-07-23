package no.java.incogito.application;

import fj.F;
import fj.P;
import fj.P2;
import fj.Unit;
import fj.control.parallel.Strategy;
import fj.data.List;
import fj.data.Option;
import static fj.data.Option.some;
import fj.data.Stream;
import fj.data.TreeMap;
import fj.pre.Show;
import no.java.ems.server.EmsServices;
import no.java.incogito.domain.AttendanceMarker;
import static no.java.incogito.domain.AttendanceMarker.createAttendance;
import static no.java.incogito.domain.AttendanceMarker.createInterest;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.User;
import static no.java.incogito.domain.User.createTransientUser;
import no.java.incogito.domain.UserId;
import no.java.incogito.ems.server.DataGenerator;
import org.apache.log4j.Logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import voldemort.server.VoldemortServer;

import java.util.Random;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@ContextConfiguration(locations = "classpath*:applicationContext.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class IncogitoApplicationIntegrationTest {

    private static final Logger logger = Logger.getLogger(IncogitoApplicationIntegrationTest.class);

    @Autowired
    IncogitoApplication incogito;

    @Autowired
    EmsServices services;    

    @Autowired
    VoldemortServer voldemortServer;

    Stream<Integer> numbers = Stream.unfold(new F<Object, Option<P2<Integer, Object>>>() {

        private final Random random = new Random(0);

        public Option<P2<Integer, Object>> f(Object o) {
            return some(P.p(random.nextInt(), o));
        }
    }, 0);

    @BeforeClass
    public static void beforeClass() {
        logger.info("=============================================================");
        logger.info("===================== STARTING SETUP ========================");
        logger.info("=============================================================");
        System.out.println(UserClient.SCHEMA);
    }

    @Before
    public void setUp() throws Exception {
        logger.info("=============================================================");
        logger.info("================ STARTING DATA GENERATION ===================");
        logger.info("=============================================================");
        new DataGenerator(services).generate1();
        logger.info("=============================================================");
        logger.info("======================= SETUP DONE ==========================");
        logger.info("=============================================================");
    }

    @Test
    public void testBasic() {
        final SessionId sessionA = new SessionId("session-a");
        final SessionId sessionB = new SessionId("session-b");

        final UserId userId = new UserId("trygvis");

        OperationResult<User> userOperationResult = incogito.getUser(userId);

        if(userOperationResult.isNotFound()){
            User user = createTransientUser(userId).
                    markAttendance(sessionA).
                    markInterest(sessionB);
            incogito.createUser(user);
        }

        User user = incogito.getUser(userId).value();

        assertEquals(userId, user.id);
        assertEquals(2, user.attendanceMarkers.length());

        OperationResult<Schedule> scheduleOperationResult = incogito.getSchedule(userId);
        assertEquals(OperationResult.Status.OK, scheduleOperationResult.status);
    }

    @Test
    public void testLots() throws Exception {

        Strategy<Session> strategy = Strategy.executorStrategy(Executors.newFixedThreadPool(5));

        int nUsers = 10;

        List<Event> events = List.iterableList(incogito.getEvents().value());
        int eventCount = events.length();
        System.out.println("eventCount = " + eventCount);

        // Pre-load all sessions
        TreeMap<Event.EventId, List<Session>> sessionMap = TreeMap.empty(Event.EventId.ord);
        for (Event event : events) {
//            List<Session> sessionList = strategy.parList(incogito.getSessions(event.name).value())._1();
            List<Session> sessionList = incogito.getSessions(event.name).value();
            sessionMap = sessionMap.set(event.id, sessionList);
        }

        // Create all users. This will normally happen when logging in
        for (UserId userId : Stream.range(0, nUsers).map(Show.intShow.showS_()).map(UserId.fromString)) {
            OperationResult<Unit> removeResult = incogito.removeUser(userId);
            assertTrue(removeResult.isOk() || removeResult.isNotFound());
            assertEquals(OperationResult.Status.OK, incogito.createUser(User.createTransientUser(userId)).status);
        }

        // For each user
        for (UserId userId : Stream.range(0, nUsers).map(Show.intShow.showS_()).map(UserId.fromString)) {

            // Select an event
            Event event = events.index(numbers.head() % eventCount);

            List<Session> sessions = sessionMap.get(event.id).some();

            int nSessions = sessions.length();
            System.out.println("nSessions = " + nSessions);

            for (Integer i : Stream.range(0, nSessions)) {
                Session session = sessions.index(i);

                AttendanceMarker attendanceMarker = numbers.head() % 2 == 0 ?
                    createInterest(session.id) :
                    createAttendance(session.id);

                OperationResult result = incogito.markAttendance(userId, session.id, attendanceMarker);

                assertEquals(OperationResult.Status.OK, result.status);
            }
        }
    }
}
