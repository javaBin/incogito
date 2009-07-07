package no.java.incogito.cli;

import static fj.data.Option.fromNull;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import static javax.management.ObjectName.getInstance;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoMuninPlugin {

    private static class Connection {
        private final MBeanServerConnection beanServerConnection;
        private final ObjectName incogitoObjectName;

        private Connection() throws Exception {
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://127.0.0.1:1100/jmxrmi");
            JMXConnector connector = JMXConnectorFactory.connect(url, null);
            connector.connect();
            beanServerConnection = connector.getMBeanServerConnection();

            incogitoObjectName = getInstance("incogito.performance:name=Incogito");
        }
    }

    public static void main(String[] args) {

        try {
            IncogitoMuninPlugin plugin = new IncogitoMuninPlugin();

            if (args.length == 1 && args[0].equals("config")) {
                plugin.config();
            } else if (args.length == 1 && args[0].equals("autoconf")) {
                plugin.autoconf();
            } else {
                plugin.read();
            }
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private void read() throws Exception {
        Connection connection = new Connection();
        System.out.println("graph_title Incogito Performance");
        System.out.println("graph_period minute");
        System.out.println("graph_category Incogito");
        System.out.println("graph_info Performance statistics for Incogito instance");

        MBeanInfo beanInfo = connection.beanServerConnection.getMBeanInfo(connection.incogitoObjectName);

        MBeanAttributeInfo[] attributes = beanInfo.getAttributes();
        String[] names = new String[attributes.length];
        for (int i = 0, attributesLength = attributes.length; i < attributesLength; i++) {
            names[i] = attributes[i].getName();
        }

        AttributeList attributeList = connection.beanServerConnection.getAttributes(connection.incogitoObjectName, names);

        for (int i = 0; i < attributeList.size(); i++) {
            String name = names[i];
            String[] strings = ((Attribute) attributeList.get(i)).getValue().toString().split("=|:");

            System.out.println(name + "_lastExecutionTime.value " + (strings[1].equals("-1") ? "U" : strings[1]));
            System.out.println(name + "_invocations.value " + strings[3]);
            System.out.println(name + "_exceptions.value " + strings[5]);
        }
    }

    private void config() throws Exception {
        Connection connection = new Connection();

        System.out.println("graph_category Incogito");
        System.out.println("graph_title Incogito Performance");
        System.out.println("graph_period minute");
        System.out.println("graph_info Performance statistics for Incogito instance");

        MBeanInfo beanInfo = connection.beanServerConnection.getMBeanInfo(connection.incogitoObjectName);
        for (MBeanAttributeInfo attributeInfo : beanInfo.getAttributes()) {
            String name = attributeInfo.getName();
            String description = fromNull(attributeInfo.getDescription()).orSome(name);

            System.out.println(name + "_lastExecutionTime.label " + description + ", last execution time");
            System.out.println(name + "_lastExecutionTime.type DERIVE");
            System.out.println(name + "_lastExecutionTime.min 0");

            System.out.println(name + "_invocations.label " + description + ", invocations");
            System.out.println(name + "_invocations.type COUNTER");
            System.out.println(name + "_invocations.min 0");

            System.out.println(name + "_exceptions.label " + description + ", exceptions");
            System.out.println(name + "_exceptions.type COUNTER");
            System.out.println(name + "_exceptions.min 0");
        }
    }

    private void autoconf() throws Exception {
        try {
            new Connection();
            System.out.println("yes");
        } catch (IOException e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            System.out.println("no (" + root.getMessage() + ")");
            System.exit(1);
        }
    }
}
