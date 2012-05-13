#!/bin/sh

set -e
set -x

v=0.51

if [ ! -d target ]
then
  mkdir target
fi

cd target

if [ ! -r voldemort-$v.tar.gz ]
then
  wget -O voldemort-$v.tar.gz http://project-voldemort.googlecode.com/files/voldemort-$v.tar.gz
fi

gunzip -f voldemort-$v.tar.gz
tar=tar
if [ -x "`which gtar`" ]
then
  tar=`which gtar`
fi

$tar xf voldemort-$v.tar

common="-Dpackaging=jar"

jar cf voldemort-$v-sources.jar -C voldemort-$v/src/java .

mvn install:install-file $common \
  -DgroupId=voldemort -DartifactId=voldemort -Dversion=$v \
  -DpomFile=../`dirname $0`/voldemort-pom.xml -Dfile=voldemort-$v/dist/voldemort-$v.jar

mvn install:install-file $common \
  -DgroupId=voldemort -DartifactId=voldemort -Dversion=$v \
  -Dclassifier=sources -Dfile=voldemort-$v-sources.jar

mvn install:install-file $common \
  -DgroupId=voldemort -DartifactId=je -Dversion=3.3.62 \
  -DgeneratePom=true -Dfile=voldemort-$v/lib/je-3.3.62.jar
