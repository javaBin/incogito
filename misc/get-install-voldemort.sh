#!/bin/sh

common="-Dpackaging=jar"

set -e
set -x

if [ ! -d target ]
then
  mkdir target
fi

cd target

#wget -O target/voldemort-0.51.tar.gz http://cloud.github.com/downloads/voldemort/voldemort/voldemort-0.51.tar.gz

#tar zxf voldemort-0.51.tar.gz

#mvn install:install-file $common \
#  -DgroupId=voldemort -DartifactId=voldemort -Dversion=0.51 \
#  -DpomFile=../`dirname $0`/voldemort-pom.xml -Dfile=voldemort-0.51/dist/voldemort-0.51.jar

mvn install:install-file $common \
  -DgroupId=voldemort -DartifactId=je -Dversion=3.3.62 \
  -DgeneratePom=true -Dfile=voldemort-0.51/lib/je-3.3.62.jar
