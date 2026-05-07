package com.latchdb.query.execution;

import com.latchdb.storage.Record;
import java.util.List;

public class ProjectionIterator implements Iterator {
    private final Iterator child;
    private final List<String> columns;

    public ProjectionIterator(Iterator child, List<String> columns) {
        this.child = child;
        this.columns = columns;
    }

    @Override
    public void open() throws Exception {
        child.open();
    }

    @Override
    public Record next() throws Exception {
        Record record = child.next();
        if (record == null) return null;
        Record projected = new Record();
        for (String col : columns) {
            projected.put(col, record.get(col));
        }
        return projected;
    }

    @Override
    public void close() throws Exception {
        child.close();
    }
}
