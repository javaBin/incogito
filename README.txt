To run the EMS server:

$ mvn -f incogito-ems-server/pom.xml exec:java

To use an existing EMS database (do this before starting incognito-ems-server):

$ cd incogito-ems-server
$ rm -rf target/ems-home
$ tar zxvf ..
$ cp -r database target/ems-home

To run the Incogito web application:

$ mvn -f incogito-web/pom.xml jetty:run

The application will be available on http://localhost:8096/incogito-web
