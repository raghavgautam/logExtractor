Log Extractor
=============

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/628a70c07ea5437f98d317c7dda4f4d9)](https://www.codacy.com/app/raghavgautam/logExtractor?utm_source=github.com&utm_medium=referral&utm_content=raghavgautam/logExtractor&utm_campaign=badger)

Log extractor utility for extracting oozie job logs from oozie system logs


How to run this utility ?

For running it from java code:

    cd logExtractor
    mvn clean compile exec:java -Dexec.mainClass="org.apache.log.extractor.App" -Dlog.oozie.location=/Users/rgautam/tmp/oozie/oozie.log.2014-09-12,/Users/rgautam/tmp/oozie/oozie.log -Dlog.oozie.write.location="/tmp/test/"

If there is just directory with all the logs, launching this command would look for oozie.logs
recursively and will extract the oozie job logs to /Users/rgautam/test_output/oozie_logs

    cd logExtractor
    mvn clean compile exec:java -Dexec.mainClass="org.apache.log.extractor.App" -Dlog.oozie.location=/Users/rgautam/test_output

If you are using jar artifact with all the libraries, this should suffice.

    java -jar logExtractor.jar -Dlog.oozie.location=/Users/rgautam/test_output

Options
-------
All the properties in logExtractor.properties can be overridden at commandline.
