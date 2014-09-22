package org.apache;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OozieLogReader implements LogReader {
    private static Logger logger = Logger.getLogger(OozieLogReader.class);
    private static final int MaxBadRecord = 1000;
    private static final String JOB_REGEX = App.getConfig().getString("log.oozie.job.regex");
    private static final Pattern jobRegex = Pattern.compile(JOB_REGEX);
    LineReader lineReader;
    LogRecord readAhead;
    final String recordPrefix;

    private OozieLogReader(LineReader lineReader, String recordPrefix) {
        this.lineReader = lineReader;
        this.recordPrefix = recordPrefix;
    }

    public static LogReader getInstance(LineReader lineReader, String recordPrefix) {
        return new OozieLogReader(lineReader, recordPrefix);
    }

    @Override
    public LogRecord readRecord() throws IOException {
        //read beginnings of first log record in readAhead
        if (readAhead == null || readAhead.size() == 0) {
            final String line = lineReader.readLine();
            if (line == null) {
                logger.info("processing finished.");
                return null;
            }
            for(int i = 0; i <= MaxBadRecord; ++i) {
                if( i == MaxBadRecord) {
                    logger.warn("Bad record: " + line);
                    throw new IOException("Found " + MaxBadRecord + " bad records. Check readAhead file");
                }
                if (isNewRecord(line)) {
                    readAhead = new LogRecord(getJobId(line));
                    readAhead.add(line);
                    break;
                }

            }
        }
        //read rest of the log record
        String line = lineReader.readLine();
        while (line != null && !isNewRecord(line)) {
            readAhead.add(line);
            line = lineReader.readLine();
        }
        //save beginning of the next log record
        LogRecord logRecord = readAhead;
        if (line != null) {
            readAhead = new LogRecord(getJobId(line));
            readAhead.add(line);
        } else {
            readAhead = null;
        }
        return logRecord;
    }

    private boolean isNewRecord(String line) {
        return line.startsWith(recordPrefix);
    }

    public static String getJobId(String line) {
        if(StringUtils.isEmpty(line)) {
            return null;
        }
        final Matcher matcher = jobRegex.matcher(line);
        if(matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
