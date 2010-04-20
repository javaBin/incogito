package no.java.incogito.ems.server;

import fj.F;
import fj.data.*;
import static fj.data.Option.fromNull;
import static fj.data.List.nil;
import no.java.ems.dao.*;
import no.java.ems.server.domain.*;
import org.joda.time.*;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogManager;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SessionJavaGenerator {
    public static void main(final String[] args) throws Exception {
        java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }
        SLF4JBridgeHandler.install();

        File basedir = getBasedir(args);

        File emsHome = new File(basedir, "target/ems-home");

        System.setProperty("ems.home", emsHome.getAbsolutePath());

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:incogito-ems-server-applicationContext-test.xml");

        EventDao eventDao = (EventDao) context.getBean("eventDao", EventDao.class);
        SessionDao sessionDao = (SessionDao) context.getBean("sessionDao", SessionDao.class);

        try {
            List<Event> list = eventDao.getEvents();
            System.out.println("Events: (" + list.size() + "):");
            Event event = null;
            for (Event e : list) {
                System.out.println("e.getName() = " + e.getName());
                if (e.getName().equals("Javazone 2008")) {
                    event = e;
                    break;
                }
            }

            if (event == null) {
                return;
            }

            List<Session> sessions = sessionDao.getSessions(event.getId());

            fj.data.List<String> ids = nil();
            for (int i = 0; i < sessions.size(); i++) {
                Session session = sessions.get(i);

                if (!session.isPublished() || session.getTitle() == null) {
                    continue;
                }

                F<String, String> quote = new F<String, String>() {
                    public String f(String s) {
                        return "\"" + s + "\"";
                    }
                };
                F<String, String> fromNull = new F<String, String>() {
                    public String f(String s) {
                        return "Option.fromNull(\"" + s + "\")";
                    }
                };

                F<Room, String> getName = new F<Room, String>() {
                    public String f(Room room) {
                        return room.getName();
                    }
                };

//                System.out.println("toDateTime(session.getTimeslot().getStart()) = " + toDateTime(session.getTimeslot().getStart()));

                F<String, String> addUnderscore = new F<String, String>() {
                    public String f(String s) {
                        return s.replace(' ', '_');
                    }
                };
                Interval timeslot = session.getTimeslot().some();
                System.out.println("public static final Session session" + i + " = new Session(new SessionId(\"" + session.getId() + "\"), " +
                        "Format." + session.getFormat().name() + ", " +
                        "\"" + session.getTitle().replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n") + "\", " +
                        toOption("WikiString", "new WikiString(\"", session.getLead(), "\")") + ", " +
                        toOption("WikiString", "new WikiString(\"", session.getBody(), "\")") + ", " +
                        "Option.some(" + session.getLevel().name() + "), " +
                        "Option.some(new Interval(" + timeslot.getStartMillis() + "L, " + timeslot.getEndMillis() + "L))," +
                        toOption("\"", fromNull(session.getRoom()).map(getName).orSome((String)null), "\"") + ", " +
                        "List.<Label>list(" + show(fj.data.List.iterableList(session.getKeywords()).map(addUnderscore)) + "), " +
                        "List.<Speaker>nil(), " +
                        "List.<Comment>nil()" +
                        ");");
                ids = ids.cons("session" + i);
            }

            System.out.println("public static final List<Session> sessions = List.list(" + show(ids.reverse()) + ");");
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        finally {
            System.out.println("Shutting down");
            context.stop();
            System.exit(0);
        }
    }

    private static String show(fj.data.List<String> list) {
        String s = "";

        while (list.isNotEmpty()) {
            s += list.head();
            list = list.tail();

            if (list.isNotEmpty()) {
                s += ", ";
            }
        }

        return s;
    }

    public static String toDateTime(DateTime dateTime) {
        return "new DateTime(" + dateTime.getYear() + ", " + dateTime.getMonthOfYear() + ", " + dateTime.getDayOfMonth() + ", " + dateTime.getHourOfDay() + ", " + dateTime.getMinuteOfHour() + ", " + dateTime.getSecondOfMinute() + ", " + dateTime.getMillisOfSecond() + ")";
    }

    public static String toStringOption(String s) {
        if(s == null) {
            return "null";
        }

        return s.replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n");
    }

    public static String toOption(String prefix, String s, String suffix) {
        if(s == null) {
            return "Option.none()";
        }

        return "Option.some(" + prefix + s.replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n") + suffix + ")";
    }

    public static String toOption(String type, String prefix, String s, String suffix) {
        if(s == null) {
            return "Option.<" + type + ">none()";
        }

        return "Option.some(" + prefix + s.replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n") + suffix + ")";
    }

    public static File getBasedir(String[] args) {
        File basedir;
        if (args.length > 0) {
            basedir = new File(args[0]);
        } else {
            basedir = new File("").getAbsoluteFile();
        }
        return basedir;

    }
}
