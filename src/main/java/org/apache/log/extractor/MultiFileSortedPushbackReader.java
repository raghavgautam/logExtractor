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

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MultiFileSortedPushbackReader implements PushbackLineReader {
    private final List<MyReader> readers;
    List<String> pushQueue = new ArrayList<String>();

    public MultiFileSortedPushbackReader(List<File> fileList) throws IOException {
        List<MyReader> readers = new ArrayList<MyReader>();
        for (File file : fileList) {
            final FileReader fileReader = new FileReader(file.getCanonicalPath());
            final MyReader reader = new MyReader(fileReader);
            readers.add(reader);
        }
        Collections.sort(readers, new Comparator<MyReader>() {
            @Override
            public int compare(MyReader o1, MyReader o2) {
                String l1, l2;
                try {
                    l1 = o1.peekLine();
                    l2 = o2.peekLine();
                } catch (IOException e) {
                    return 0;
                }
                return l1.compareTo(l2);
            }
        });
        this.readers = readers;
    }

    public void pushBack(String str) {
        pushQueue.add(str);
    }

    @Override
    public String readLine() throws IOException {
        //return stuff from push queue if available
        if(pushQueue.size() != 0) {
            return pushQueue.remove(0);
        }

        //pick up next reader if current reader finished
        if (readers.size() == 0) {
            return null;
        }
        MyReader currReader = readers.get(0);
        while (currReader == null) {
            readers.remove(null);
            if (readers.isEmpty()) {
                return null;
            }
            currReader = readers.get(0);
        }

        String result = currReader.readLine();
        //checking if current reader is finished
        if (result == null) {
            currReader.close();
            readers.remove(currReader);
            return readLine();
        }
        return result;
    }
}

class MyReader implements PushbackLineReader {

    List<String> pushQueue = new ArrayList<String>();
    BufferedReader br;

    public MyReader(Reader reader) {
        br = new BufferedReader(reader);
    }

    public String peekLine() throws IOException {
        final String str = readLine();
        pushBack(str);
        return str;
    }

    public void pushBack(String str) {
        pushQueue.add(str);
    }

    public String readLine() throws IOException {
        if(pushQueue.size() != 0) {
            return pushQueue.remove(0);
        }
        final String line = br.readLine();
        if (line != null) {
            return line + "\n";
        }
        return null;
    }

    public void close() {
        IOUtils.closeQuietly(br);
    }
}
