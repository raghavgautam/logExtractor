package org.apache;

import java.util.ArrayList;

public class LogRecord extends ArrayList<String> {
    private final String id;
    public LogRecord(String id) {
        super();
        this.id = id;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : this) {
            stringBuilder.append(s + "\n");
        }

        return stringBuilder.toString();
    }

    public String getId() {
        return id;
    }
}
