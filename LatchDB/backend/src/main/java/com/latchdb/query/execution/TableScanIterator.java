package com.latchdb.query.execution;

import com.latchdb.index.BPlusTree;
import com.latchdb.storage.Record;

public class TableScanIterator implements Iterator {
    private final IndexScanIterator indexScan;

    public TableScanIterator(BPlusTree tree) {
        this.indexScan = new IndexScanIterator(tree, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public void open() throws Exception {
        indexScan.open();
    }

    @Override
    public Record next() throws Exception {
        return indexScan.next();
    }

    @Override
    public void close() throws Exception {
        indexScan.close();
    }
}
