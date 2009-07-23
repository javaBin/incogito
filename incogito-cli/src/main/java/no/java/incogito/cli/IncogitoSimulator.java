package no.java.incogito.cli;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import fj.F;
import fj.F3;
import static fj.Function.curry;
import static fj.Function.flip;
import fj.P1;
import fj.control.parallel.Strategy;
import fj.data.Array;
import fj.data.List;
import static fj.data.List.iterableList;
import fj.data.TreeMap;
import fj.pre.Ord;
import static no.java.incogito.cli.RunningTimer.runningTimer;
import no.java.incogito.cli.RunningTimer.Lap;
import no.java.incogito.dto.EventListXml;
import no.java.incogito.dto.EventXml;
import no.java.incogito.dto.SessionListXml;
import no.java.incogito.dto.SessionXml;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoSimulator {
    WebResource baseurl;
    EventListXml eventList;
    TreeMap<String, Array<SessionXml>> events = TreeMap.empty(Ord.stringOrd);
    Client client;

    public static void main(String[] args) {
        new IncogitoSimulator().run();
    }

    private void run() {
        client = Client.create();

        baseurl = client.resource("http://localhost:8096/incogito-web/rest");

        System.out.println("Loading events:");

        eventList = baseurl.path("events").get(EventListXml.class);

        for (EventXml eventXml : eventList.getEvents()) {
            System.out.println("eventXml.getName() = " + eventXml.getName());

            SessionListXml xml = baseurl.path("events").path(eventXml.getName()).path("sessions").
                    get(SessionListXml.class);

            List<SessionXml> list = iterableList(xml.getSessions()).
                    foldLeft(flip(List.<SessionXml>cons()), List.<SessionXml>nil());

            events = events.set(eventXml.getName(), list.toArray(SessionXml[].class));
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Strategy<SessionXml> generatorStrategy = Strategy.executorStrategy(threadPool);
        Strategy<Result> requestStrategy = Strategy.executorStrategy(threadPool);

        try {
            work(generatorStrategy, requestStrategy);
        } finally {
            threadPool.shutdown();
        }
    }

    private void work(Strategy<SessionXml> generatorStrategy, Strategy<Result> requestStrategy) {
        Random random = new Random();

        F<Integer, SessionXml> selector = this.<SessionXml>randomSelector().f(random).f(events.get("JavaZone 2009").some());

        int size = 1000;
        // Do size requests, create chunks of them for each thread to execute
        RunningTimer timer = runningTimer();

        System.err.println("List");

        List<Integer> l = List.nil();
        for (int i = 0; i < size; i++) {
            l = l.cons(i);
        }
        System.err.println("List done: " + timer.lap().interval);

        System.err.println("Generating requests...");
        List<SessionXml> requests = generatorStrategy.parMapList(selector).f(l)._1();
        System.err.println("Generated requests in " + timer.lap().interval);

        // Map the chunks in parallel, and finally join all the lists
        System.err.println("Transforming");
        P1<List<Result>> listP1 = requestStrategy.parMapList(user).f(requests);
        System.err.println("Transformed in " + timer.lap().interval);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {
        }

        timer.lap();

        System.err.println("Starting...");

        List<Result> resultList = listP1._1();
        System.err.println("resultList.length() = " + resultList.length());
        for (Result unit : resultList) {
            System.err.println(unit.thread + " fetched '" + unit.title + "' in " + unit.lap.interval);
        }
        System.err.println("Done in " + timer.lap().interval);
    }

    public static class Result {
        public final String thread;
        public final String title;
        public final Lap lap;

        public Result(String thread, String title, Lap lap) {
            this.thread = thread;
            this.title = title;
            this.lap = lap;
        }
    }

    <T> F<Random, F<Array<T>, F<Integer, T>>> randomSelector() {
        return curry( new F3<Random, Array<T>, Integer, T>() {
            public T f(Random random, Array<T> array, Integer i) {
                return array.get(random.nextInt(array.length()));
            }
        });
    }

    F<SessionXml, Result> user = new F<SessionXml, Result>() {
        public Result f(SessionXml s) {
            RunningTimer timer = runningTimer();
            SessionXml session = client.resource(s.selfUri).get(SessionXml.class);
            return new Result(Thread.currentThread().getName(), session.title, timer.lap());
        }
    };
}
