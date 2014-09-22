package org.apache;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MultiFileReader implements LineReader {
    private final List<BufferedReader> readers;
    BufferedReader currReader;

    public MultiFileReader(List<File> fileList) throws IOException {
        List<BufferedReader> readers = new ArrayList<BufferedReader>();
        for (File file : fileList) {
            final FileReader fileReader = new FileReader(file.getCanonicalPath());
            final BufferedReader reader = new BufferedReader(fileReader);
            readers.add(reader);
        }
        this.readers = readers;
    }

    @Override
    public String readLine() throws IOException {
        //pick up next reader if current reader finished
        if (currReader == null) {
            if (readers.isEmpty()) {
                return null;
            }
            currReader = readers.remove(0);
        }
        String result = currReader.readLine();
        //checking if current reader is finished
        if (result == null) {
            IOUtils.closeQuietly(currReader);
            currReader = null;
            return readLine();
        }
        return result;
    }
}
