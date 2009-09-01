package no.java.incogito.domain;

import junit.framework.TestCase;
import no.java.incogito.domain.IncogitoUri.IncogitoRestEventsUri.IncogitoRestEventUri.IncogitoRestSessionUri;
import no.java.incogito.domain.Session.Format;
import static no.java.incogito.domain.IncogitoTestData.session46;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoUriTest extends TestCase {
    public void testBasic() {
        IncogitoUri incogitoUri = new IncogitoUri("http://localhost");

        String what = incogitoUri.events().eventUri("JavaZone 2009").session(session46);

        assertEquals("http://localhost/events/JavaZone%202009/sessions/Dokumentasjon%20uten%20pistol%20-%20g%C3%A5r%20det%20an%3F", what);

        IncogitoRestSessionUri session2RestUri = incogitoUri.restEvents().eventUri("JavaZone 2009").session(IncogitoTestData.session3);

        String s = session2RestUri.toString();

        assertEquals("http://localhost/rest/events/JavaZone%202009/sessions/b8a6034f-573d-4321-9155-a5ed31885958", s);
        assertEquals("http://localhost/rest/events/JavaZone%202009/sessions/b8a6034f-573d-4321-9155-a5ed31885958/speaker-photos/2", session2RestUri.speakerPhoto(2));

        assertEquals("http://localhost/events/JavaZone%202009/sessions/Scala%20%2b%20Wicket%20=%20Match%20made%20in%20heaven%3F",
                incogitoUri.events().eventUri("JavaZone 2009").session(session46.title("Scala + Wicket = Match made in heaven?")));

        assertEquals("http://localhost/events/JavaZone%202009/sessions/%2b=",
                incogitoUri.events().eventUri("JavaZone 2009").session(session46.title("+=")));
    }

//    public void testCrap() {
//        String expr = "^/events/([.&&[^/]]*)/?$";
//        showMatches("/events/JavaZone%202009/", expr);
//        showMatches("/events/JavaZone%202009", expr);
//
//        expr = "^/events/([\\w%0-9])*";
//        showMatches("/events/JavaZone%202009/", expr);
//        showMatches("/events/JavaZone%202009", expr);
//
//        expr = "^/events/(.*)?$";
//        showMatches("/events/JavaZone%202009/", expr);
//        showMatches("/events/JavaZone%202009", expr);
//    }

    private void showMatches(String text, String regexp) {
        List<Integer> starts = new LinkedList<Integer>();
        List<Integer> ends = new LinkedList<Integer>();
        List<String> groups = new LinkedList<String>();

        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
//            System.out.println("start: " + matcher.start() + ", end: " + matcher.end() + ", group: " + matcher.group());
            starts.add(matcher.start());
            ends.add(matcher.end());
            groups.add(matcher.group());
        }

        System.out.print(regexp + " with " + text + " => ");

        for (String group : groups) {
            System.out.println(group);
        }

//        System.out.print("match:  ");
//
//        if (starts.size() == 0) {
//            System.out.println();
//            return;
//        }
//
//        int start = starts.remove(0);
//        int end = ends.remove(0);
//        for (int i = 0; i < text.length(); i++) {
//            if (i == start && start == end - 1) {
//                System.out.print("|");
//            }
//            else if (i == start) {
//                System.out.print("|");
//            } else if (i == end - 1) {
//                System.out.print("|");
//
//                if (starts.size() == 0) {
//                    break;
//                }
//                start = starts.remove(0);
//                end = ends.remove(0);
//            } else if (start < i && i < end) {
//                System.out.print("-");
//            } else {
//                System.out.print(" ");
//            }
//        }
//
//        System.out.println();
        System.out.println();
    }
}
