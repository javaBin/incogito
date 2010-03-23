For the full documentation on Incogito, please go here:

  http://wiki.java.no/display/smia/Incogito

This is a short summary to get started with developing Incogito only. For the
full guide see the above documentation.

To develop Incogito you need:

 1) A working EMS server with data.

    First you have to unpack some data. Get a backup and unpack it like this:

    $ cat ems-20090828-000000.tar.gz | (cd incogito-ems-server/target && tar zxfv - && mv database ems-home)

    We have a special Maven module that embeds a complete EMS server which you
    can run like this:

    $ mvn -f incogito-ems-server/pom.xml install exec:java

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
