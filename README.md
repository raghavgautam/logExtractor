Log Extractor
=============

Log extractor utility for extracting oozie job logs from oozie system logs


How to run this utility ?

cd logExtractor

mvn clean compile exec:java -Dexec.mainClass="org.apache.log.extractor.App" -Dlog.oozie.location=/Users/rgautam/tmp/oozie/oozie.log.2014-09-12,/Users/rgautam/tmp/oozie/oozie.log -Dlog.oozie.write.location="/tmp/test/"


Options
-------
All the properties in logExtractor.properties can be overridden at commandline.
