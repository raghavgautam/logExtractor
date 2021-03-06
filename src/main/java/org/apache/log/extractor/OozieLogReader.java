/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log.extractor;

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
    PushbackLineReader lineReader;
    final String RECORD_REGEX = App.getConfig().getString("log.oozie.record.regex");
    final Pattern recordRegex = Pattern.compile(RECORD_REGEX);

    private OozieLogReader(PushbackLineReader lineReader) {
        this.lineReader = lineReader;
    }

    public static LogReader getInstance(PushbackLineReader lineReader) {
        return new OozieLogReader(lineReader);
    }

    @Override
    public LogRecord readRecord() throws IOException {
        LogRecord logRecord = null;
        //read beginnings of first log record in readAhead
        String line = lineReader.readLine();
        if (line == null) {
            logger.info("processing finished.");
            return null;
        }
        for (int i = 0; i <= MaxBadRecord; ++i) {
            if (i == MaxBadRecord) {
                logger.warn("Bad record: " + line);
                throw new IOException(
                        "Found " + MaxBadRecord + " bad records. Check readAhead file");
            }
            if (isNewRecord(line)) {
                logRecord = new LogRecord(getJobId(line));
                logRecord.add(line);
                break;
            }
            line = lineReader.readLine();
        }

        //read rest of the log record
        line = lineReader.readLine();
        while (line != null && !isNewRecord(line)) {
            assert logRecord != null;
            logRecord.add(line);
            line = lineReader.readLine();
        }
        lineReader.pushBack(line);
        return logRecord;
    }

    private boolean isNewRecord(String line) {
        final Matcher matcher = recordRegex.matcher(line);
        return matcher.find();
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
