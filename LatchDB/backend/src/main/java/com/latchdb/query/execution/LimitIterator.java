package com.latchdb.query.execution;

import com.latchdb.storage.Record;

public class LimitIterator implements Iterator {
    private final Iterator child;
    private final int limit;
    private int count = 0;

    public LimitIterator(Iterator child, int limit) {
        this.child = child;
        this.limit = limit;
    }

    @Override
    public void open() throws Exception {
        child.open();
    }

    @Override
    public Record next() throws Exception {
        if (count < limit) {
            Record r = child.next();
            if (r != null) {
                count++;
                return r;
            }
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        child.close();
    }
}
