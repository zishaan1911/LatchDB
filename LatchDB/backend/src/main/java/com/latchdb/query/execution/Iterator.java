package com.latchdb.query.execution;

import com.latchdb.storage.Record;

public interface Iterator {
    void open() throws Exception;
    Record next() throws Exception;
    void close() throws Exception;
}
