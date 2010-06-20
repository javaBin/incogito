For the full documentation on Incogito, please go here:

  http://wiki.java.no/display/smia/Incogito

This is a short summary to get started with developing Incogito only. For the
full guide see the above documentation.

To develop Incogito you need:

 1) A working EMS server with data.

    These instructions assume you're using a EMS 1 backup!

    First you have to unpack some data. Get a backup and unpack it like this:

    $ cat ems-20090828-000000.tar.gz | (cd incogito-ems-server/target && tar zxf - && mv database ems-home && mv ems-home/database/ems ems-home/database/derby && mv ems-home/binaries ems-home/database/binaries)

    We have a special Maven module that embeds a complete EMS server which you
    can run like this:

    $ mvn -f incogito-ems-server/pom.xml jetty:run-war

    Start ij and run the DDL upgrade script:

    $ ./ij -p ij.properties
    ij> run '../ems/ems-server/src/main/resources/ddl/upgrade_1.1_to_2.0.ddl';

    You can skip the "install" part if you know what you're doing.

 2) Download and install Voldemort into your local Maven repository. As
    Voldemort refuses to deploy their artifacts to a Maven repository you have
    to jump a few more hoops to get started.

    If you have a sane platform (which means Linux, Solaris or OS X) running
    this *should* download and install Voldemort:

    $ misc/get-install-voldemort.sh

    If that doesn't do it for you, read the script and execute the similar
    commands for your platform.

  3) Start the Incogito web application:

    $ mvn -f incogito-web/pom.xml jetty:run

    The application will be available on http://localhost:8096/incogito

== Connecting to an EMS Database ==

This assumes you already have unpacked a backup

start ij
> connect 'jdbc:derby:target/ems-home/database/ems';

> connect 'jdbc:derby:incogito-ems-server/target/ems-home/database/derby';
