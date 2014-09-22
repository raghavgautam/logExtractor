package org.apache;

import java.io.IOException;

public interface LogReader {
    LogRecord readRecord() throws IOException;
}
