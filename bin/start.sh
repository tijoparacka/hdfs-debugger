#!/bin/bash
bin_dir=`dirname $0`
#jarpath = `$bin_dir/../lib/simulator-1.1.0-SNAPSHOT-jar-with-dependencies.jar`
jarpath=".:$bin_dir/../lib/"
for file in $bin_dir/../lib/*
do
  jarpath=$jarpath:$file
done
echo "Files in classpath $jarpath"
java -server -Xmx2048m \
-XX:+UseG1GC \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:+ExitOnOutOfMemoryError \
-XX:+PrintGC \
-XX:HeapDumpPath=/tmp/druid-heap.hprof -cp "$jarpath" com.examples.Test <principal> <keytab> <path eg /warehouse>