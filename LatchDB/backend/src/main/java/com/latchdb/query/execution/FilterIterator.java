package com.latchdb.query.execution;

import com.latchdb.storage.Record;
import java.util.function.Predicate;

public class FilterIterator implements Iterator {
    private final Iterator child;
    private final Predicate<Record> predicate;

    public FilterIterator(Iterator child, Predicate<Record> predicate) {
        this.child = child;
        this.predicate = predicate;
    }

    @Override
    public void open() throws Exception {
        child.open();
    }

    @Override
    public Record next() throws Exception {
        Record record;
        while ((record = child.next()) != null) {
            if (predicate.test(record)) {
                return record;
            }
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        child.close();
    }
}
