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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Application to extract oozie job logs from oozie service logs
 */
public class App {
    private static Logger logger = Logger.getLogger(App.class);
    private static PropertiesConfiguration config;

    final Map<String, Writer> files = new HashMap<String, Writer>();

    private App() {
        logger.info("***Starting App***");
        try {
            config = new CustomPropertiesConfiguration("logExtractor.properties");
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private Writer getWriter(String id) throws IOException {
        Writer writer = files.get(id);
        if(writer == null) {
            final String fileName = config.getString("log.oozie.write.location") + id + ".txt";
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
        final List<File> logFiles = app.getLogFiles();
        LineReader reader = new MultiFileReader(logFiles);
        final LogReader logReader =
            OozieLogReader.getInstance(reader);
        for (LogRecord record = logReader.readRecord(); record != null; record = logReader.readRecord()) {
            if (!StringUtils.isEmpty(record.getId())) {
                app.getWriter(record.getId()).write(record.toString());
            }
        }
        app.finish();
    }

    private List<File> getLogFiles() throws IOException {
        final String logLocationPropName = "log.oozie.location";
        String[] oozieLogLocations = config.getStringArray(logLocationPropName);
        if (ArrayUtils.isEmpty(oozieLogLocations)) {
            throw new IOException("Please set property: " + logLocationPropName);
        }
        List<File> files = new ArrayList<File>();
        for (String oneLocation : oozieLogLocations) {
            File file = new File(oneLocation);
            if (!file.isFile()) {
                throw new IOException("Specified file does not exit: " + oneLocation);
            }
            files.add(file);
        }
        return files;
    }

    public static PropertiesConfiguration getConfig() {
        return config;
    }
}
