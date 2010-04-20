package no.java.incogito.application;

import fj.F;
import fj.P;
import fj.P2;
import fj.Unit;
import fj.data.List;
import fj.data.Option;
import static fj.data.Option.some;
import fj.data.Stream;
import fj.data.TreeMap;
import fj.pre.Ord;
import fj.pre.Show;
import no.java.ems.dao.*;
import no.java.incogito.IO;
import static no.java.incogito.IO.Strings.stringToStream;
import static no.java.incogito.PropertiesF.storePropertiesMap;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.User;
import no.java.incogito.domain.User.UserId;
import static no.java.incogito.domain.User.createPristineUser;
import no.java.incogito.domain.UserSessionAssociation.InterestLevel;
import static no.java.incogito.domain.UserSessionAssociation.InterestLevel.ATTEND;
import no.java.incogito.ems.server.DataGenerator;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.plexus.util.FileUtils;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import voldemort.server.VoldemortServer;

import java.io.File;
import java.net.URL;
import java.util.Random;
import java.util.logging.*;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
//@ContextConfiguration(locations = {"classpath*:applicationContext.xml", "classpath:incogito-application-applicationContext-test.xml"})
//@RunWith(SpringJUnit4ClassRunner.class)
@Ignore
public class IncogitoApplicationIntegrationTest {

    private static final Logger logger;

    static {
        java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }
        SLF4JBridgeHandler.install();
        logger = LoggerFactory.getLogger(IncogitoApplicationIntegrationTest.class);
    }

    @Autowired
    IncogitoApplication incogito;

    @Autowired
    EventDao eventDao;

    @Autowired
    SessionDao sessionDao;

    @Autowired
    VoldemortServer voldemortServer;

    @Autowired
    @Qualifier("incogitoHome")
    File incogitoHome;

    DataGenerator.DataSet1 dataSet;

    // All numbers but zero has twice the probability
    Stream<Integer> numbers = Stream.unfold(new F<Object, Option<P2<Integer, Object>>>() {
        private final Random random = new Random(0);

        public Option<P2<Integer, Object>> f(Object o) {
            return some(P.p(random.nextInt(Integer.MAX_VALUE), o));
        }
    }, 0);

    @BeforeClass
    public static void beforeClass() throws Exception {
        logger.info("=============================================================");
        logger.info("===================== STARTING SETUP ========================");
        logger.info("=============================================================");
        System.out.println(UserClient.SCHEMA);

        URL resource = IncogitoApplicationIntegrationTest.class.getResource("/");
        File basedir = new File(resource.toURI().getPath()).getParentFile().getParentFile().getAbsoluteFile();

        File etc = new File(basedir, "src/test/resources/cluster-it/node-it/etc");
        if (!etc.isDirectory()) {
            assertTrue(etc.mkdirs());
        }

        File emsHome = new File("target/ems.home");
        emsHome.mkdirs();
        System.setProperty("ems.home", emsHome.getAbsolutePath());

        TreeMap<String, String> properties = TreeMap.<String, String>empty(Ord.stringOrd).
            set("baseurl", "http://").
            set("events", "JavaZone 2008");
        System.out.println("new File(etc, \"incogito.properties\").getAbsolutePath() = " + new File(etc, "incogito.properties").getAbsolutePath());
        IO.runFileOutputStream(storePropertiesMap.f(properties), new File(etc, "incogito.properties")).call();
//        System.out.println("properties = " + new java.util.TreeMap<String, String>(properties.toMutableMap()).toString().replace(',', '\n'));
    }

    @Before
    public void setUp() throws Exception {
        logger.info("=============================================================");
        logger.info("================ STARTING DATA GENERATION ===================");
        logger.info("=============================================================");
        dataSet = new DataGenerator(eventDao, sessionDao).generate1();
        logger.info("=============================================================");
        logger.info("======================= SETUP DONE ==========================");
        logger.info("=============================================================");

        incogito.reloadConfiguration();
    }

//    @Test
//    public void testBasic() {
//        final SessionId sessionA = new SessionId("session-a");
//        final SessionId sessionB = new SessionId("session-b");
//
//        final UserId userId = new UserId("trygvis");
//
//        OperationResult<User> userOperationResult = incogito.getUser(userId);
//
//        if (userOperationResult.isNotFound()) {
//            User user = createPristineUser(userId).
//                setInterestLevel(sessionA, ATTEND).
//                setInterestLevel(sessionB, INTEREST);
//            incogito.createUser(user);
//        }
//
//        User user = incogito.getUser(userId).value();
//
//        assertEquals(userId.value, user.id.value);
//        assertEquals(userId, user.id);
//        assertEquals(2, user.sessionAssociations.size());
//
//        OperationResult<Schedule> scheduleOperationResult =
//            incogito.getSchedule(dataSet.javaZone2008.getName(), userId.value);
//        assertEquals(OperationResult.Status.OK, scheduleOperationResult.status);
//    }

    @Test
    @Ignore("We need a way to embed EMS")
    public void testFrontPageTexts() throws Exception {
        // Gah .. since the events are generated dynamically, we have to create a properties file for the app to load

        String text = "Welcome to JavaZone 2008!";

        assertNotNull(incogitoHome);
        assertTrue(incogitoHome.isDirectory());

        File etc = new File(incogitoHome, "etc");
        File incogitoPropertiesFile = new File(etc, "incogito.properties");
        File jz08Directory = new File(etc, "events/" + dataSet.javaZone2008.getName());
        FileUtils.deleteDirectory(jz08Directory);
        assertTrue(jz08Directory.mkdirs());
        //noinspection ResultOfMethodCallIgnored
        jz08Directory.mkdirs();
        File jz08Welcome = new File(jz08Directory, "frontpage.txt");
        File jz08PropertiesFile = new File(jz08Directory, "event.properties");

        TreeMap<String, String> incogitoProperties = TreeMap.<String, String>empty(Ord.stringOrd).
            set("baseurl", "http://poop").
            set("events", dataSet.javaZone2008.getName() + ",");
        IO.runFileOutputStream(storePropertiesMap.f(incogitoProperties), incogitoPropertiesFile).call();

        TreeMap<String, String> jz08Properties = TreeMap.<String, String>empty(Ord.stringOrd).
            set("dates", "2008-09-17, 2008-09-18").
            set("rooms.2008-09-17", "Lab I, Lab II").
            set("rooms.2008-09-18", "Lab I, Lab II, BoF");
        IO.runFileOutputStream(storePropertiesMap.f(jz08Properties), jz08PropertiesFile).call();

        IO.runFileOutputStream(stringToStream.f(text), jz08Welcome).call();

        incogito.reloadConfiguration();

        OperationResult<Event> operationResult = incogito.getEventByName(dataSet.javaZone2008.getName());
        assertTrue(operationResult.isOk());
        Event jz08 = operationResult.value();

        assertTrue(jz08.frontpageContent.isSome());
        assertEquals(text, jz08.frontpageContent.some());
    }

    @Test
    @Ignore("We need a way to embed EMS")
    public void testLots() throws Exception {

        int nUsers = 10;

        List<Event> events = List.iterableList(incogito.getEvents().value());
        int eventCount = events.length();

        // Pre-load all sessions
        TreeMap<Event.EventId, List<Session>> sessionMap = TreeMap.empty(Event.EventId.ord);
        for (Event event : events) {
//            List<Session> sessionList = strategy.parList(incogito.getSessions(event.name).value())._1();
            List<Session> sessionList = incogito.getSessions(event.name).value();
            sessionMap = sessionMap.set(event.id, sessionList);
        }

        // Create all users. This will normally happen when logging in
        for (UserId userId : Stream.range(0, nUsers).map(Show.intShow.showS_()).map(UserId.userId)) {
            OperationResult<Unit> removeResult = incogito.removeUser(userId);
            assertTrue(removeResult.isOk() || removeResult.isNotFound());
            assertEquals(OperationResult.Status.OK, incogito.createUser(User.createPristineUser(userId)).status);
        }

        // For each user
        for (UserId userId : Stream.range(0, nUsers).map(Show.intShow.showS_()).map(UserId.userId)) {

            // Select an event
            Event event = events.index(numbers.head() % eventCount);

            List<Session> sessions = sessionMap.get(event.id).some();

            int nSessions = sessions.length();

            InterestLevel[] interestLevels = InterestLevel.values();

            for (Integer i : Stream.range(0, nSessions)) {
                Session session = sessions.index(i);

                InterestLevel interestLevel = interestLevels[numbers.head() % 3];

                OperationResult result = incogito.setInterestLevel(userId.value, event.name, session.id, interestLevel);

                assertEquals(OperationResult.Status.OK, result.status);
            }
        }
    }

    @Test
    @Ignore("We need a way to embed EMS")
    public void testAttendance() {
        User user = createPristineUser(UserId.fromString("trygvis"));
        OperationResult<User> userOperationResult = incogito.createUser(user);
        assertTrue(userOperationResult.isOk());
        user = userOperationResult.value();

        Event event = incogito.getEvents().value().head();
        Session session = incogito.getSessions(event.name).value().head();

        OperationResult operationResult = incogito.setInterestLevel(user.id.value, event.name, session.id, ATTEND);
        assertTrue(operationResult.isOk());

        OperationResult<Schedule> scheduleOperationResult = incogito.getSchedule(event.name, user.id.value);
        assertTrue(scheduleOperationResult.isOk());
        Schedule schedule = scheduleOperationResult.value();
        List<Session> sessions = schedule.getAttendingSessions();
        Show<List<Session>> sessionShow = Show.listShow(Show.showS(new F<Session, String>() {
            public String f(Session session) {
                return ToStringBuilder.reflectionToString(session, ToStringStyle.MULTI_LINE_STYLE);
            }
        }));
        sessionShow.println(sessions);

        // TODO: Add asserts for change of attendance and removal
    }
}
