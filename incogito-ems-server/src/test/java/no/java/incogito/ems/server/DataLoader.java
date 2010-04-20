package no.java.incogito.ems.server;

import no.java.ems.dao.*;
import org.springframework.context.support.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DataLoader {
    public static void main(String[] args) {
        File basedir = EmsTestServer.getBasedir(args);

        File emsHome = new File(basedir, "target/ems-home");

        System.setProperty("ems.home", emsHome.getAbsolutePath());

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");

        context.start();

        EventDao eventDao = (EventDao) context.getBean("eventDao", EventDao.class);
        SessionDao sessionDao = (SessionDao) context.getBean("sessionDao", SessionDao.class);

        System.out.println("Loading data");
        new DataGenerator(eventDao, sessionDao).generate1();
        System.out.println("Data loaded");

        context.stop();

        System.exit(0);
    }
}
