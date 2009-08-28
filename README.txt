For the full documentation on Incogito, please go here:

  http://wiki.github.com/javaBin/incogito


= EMS Server =

== Using an EMS Backup ==

To use an existing EMS database (do this before starting incognito-ems-server):

$ cd incogito-ems-server
$ rm -rf target/ems-home
$ tar zxvf ..
$ cp -r database target/ems-home

== Connecting to an EMS Database ==

This assumes you already have unpacked a backup

start ij
> connect 'jdbc:derby:target/ems-home/database/ems';

== To run the EMS server ==

$ mvn -f incogito-ems-server/pom.xml exec:java

= Web Application =

To run the Incogito web application:

$ mvn -f incogito-web/pom.xml jetty:run

The application will be available on http://localhost:8096/incogito

= How to run a Incogito cluster:

node a$ MAVEN_OPTS="-Dcom.sun.management.jmxremote.port=1100 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false" mvn clean jetty:run -Dvoldemort.clusterId=b -Dvoldemort.nodeId=a -Djetty.port=8096
node b$ MAVEN_OPTS="-Dcom.sun.management.jmxremote.port=1101 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false" mvn clean jetty:run -Dvoldemort.clusterId=b -Dvoldemort.nodeId=b -Djetty.port=8097
node c$ MAVEN_OPTS="-Dcom.sun.management.jmxremote.port=1102 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false" mvn clean jetty:run -Dvoldemort.clusterId=b -Dvoldemort.nodeId=c -Djetty.port=8098

Notes:
 * Make sure to start node a first as the other use it as the bootstrap server.
 * You might want to add this line to your /etc/hosts or equivalent:

   127.0.0.1  node-a.incogito node-b.incogito node-c.incogito
