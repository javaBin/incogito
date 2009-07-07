package no.java.incogito.ems.server;

import no.java.ems.server.EmsServices;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

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

        EmsServices emsServices = (EmsServices) context.getBean("emsServices");

        System.out.println("Loading data");
        new DataGenerator(emsServices).generate1();
        System.out.println("Data loaded");

        context.stop();

        System.exit(0);
    }
}
