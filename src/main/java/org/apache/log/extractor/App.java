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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Application to extract oozie job logs from oozie service logs
 */
public class App {
    private static Logger logger = Logger.getLogger(App.class);
    private static PropertiesConfiguration config;
    private final List<File> logFiles;
    private String writeLocation;

    final Map<String, Writer> files = new HashMap<String, Writer>();

    private App() throws IOException, ConfigurationException {
        logger.info("***Starting App***");
        config = new CustomPropertiesConfiguration("logExtractor.properties");
        final String[] logLocations = config.getStringArray("log.oozie.location");
        if (ArrayUtils.isEmpty(logLocations)) {
            throw new ConfigurationException("Please set property: " + "log.oozie.location");
        }
        logFiles = getLogFiles(logLocations);
        if (logFiles.size() > config.getInt("log.oozie.max.files")) {
            logger.error("Too many log files: " + logFiles);
            return;
        }
        String writeMayBe = config.getString("log.oozie.write.location");
        if (StringUtils.isEmpty(writeMayBe)) {
            if (logLocations.length == 1 && new File(logLocations[0]).isDirectory()) {
                writeLocation = logLocations[0] + "/oozie_logs/";
            } else {
                throw new ConfigurationException("Please set property: " +
                        "log.oozie.write.location");
            }
        } else {
            writeLocation = writeMayBe;
        }
    }

    private Writer getWriter(String id) throws IOException {
        Writer writer = files.get(id);
        if(writer == null) {
            final String fileName = writeLocation + id + ".txt";
            final File parentFile = new File(fileName).getParentFile();
            if (!parentFile.exists()) {
                logger.warn("Creating directory for output logs: " + parentFile);
                FileUtils.forceMkdir(parentFile);
            }
            writer = new BufferedWriter(new FileWriter(fileName));
            files.put(id, writer);
        }
        return writer;
    }

    private void closeWriters() {
        for (String s : files.keySet()) {
            IOUtils.closeQuietly(files.get(s));
        }

    }

    public void finish() {
        closeWriters();
        logger.info("***App Done***");
    }

    public static void main(String[] args) throws Exception {
        App app = new App();
        app.segregate();
        app.finish();
    }

    private void segregate() throws IOException {
        PushbackLineReader reader = new MultiFileSortedPushbackReader(logFiles);
        final LogReader logReader = OozieLogReader.getInstance(reader);
        for (LogRecord record = logReader.readRecord(); record != null; record = logReader.readRecord()) {
            if (!StringUtils.isEmpty(record.getId())) {
                getWriter(record.getId()).write(record.toString());
            }
        }
    }

    private List<File> getLogFiles(String[] logLocations) throws IOException {
        final String logFileNamePattern = config.getString("log.oozie.filename.pattern");
        Set<File> uniqueFiles = new HashSet<File>();
        for (String oneLocation : logLocations) {
            File file = new File(oneLocation);
            if (!file.isFile()) {
                final Collection listFiles = FileUtils.listFiles(file,
                        new WildcardFileFilter(logFileNamePattern, IOCase.INSENSITIVE),
                        TrueFileFilter.INSTANCE);
                uniqueFiles.addAll(listFiles);
            } else {
                uniqueFiles.add(file);
            }
        }
        return new ArrayList<File>(uniqueFiles);
    }

    public static PropertiesConfiguration getConfig() {
        return config;
    }
}
