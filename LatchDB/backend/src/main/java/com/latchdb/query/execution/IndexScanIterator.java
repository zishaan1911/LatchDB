package com.latchdb.query.execution;

import com.latchdb.index.BPlusTree;
import com.latchdb.index.LeafNode;
import com.latchdb.storage.Record;
import java.io.IOException;

public class IndexScanIterator implements Iterator {
    private final BPlusTree tree;
    private final int startKey;
    private final int endKey;
    private LeafNode currentLeaf;
    private int currentIndex;

    public IndexScanIterator(BPlusTree tree, int startKey, int endKey) {
        this.tree = tree;
        this.startKey = startKey;
        this.endKey = endKey;
    }

    @Override
    public void open() throws IOException {
        currentLeaf = tree.findLeafNode(startKey);
        currentIndex = 0;
        // Skip keys less than startKey
        while (currentLeaf != null) {
            while (currentIndex < currentLeaf.getKeys().size() && currentLeaf.getKeys().get(currentIndex) < startKey) {
                currentIndex++;
            }
            if (currentIndex < currentLeaf.getKeys().size()) {
                break;
            } else {
                if (currentLeaf.getNextLeafPageId() != -1) {
                    currentLeaf = tree.loadLeafNode(currentLeaf.getNextLeafPageId());
                    currentIndex = 0;
                } else {
                    currentLeaf = null;
                }
            }
        }
    }

    @Override
    public Record next() throws IOException {
        if (currentLeaf == null) return null;
        if (currentIndex < currentLeaf.getKeys().size()) {
            int key = currentLeaf.getKeys().get(currentIndex);
            if (key > endKey) {
                currentLeaf = null;
                return null;
            }
            Record record = currentLeaf.getRecords().get(currentIndex);
            currentIndex++;
            if (currentIndex == currentLeaf.getKeys().size()) {
                if (currentLeaf.getNextLeafPageId() != -1) {
                    currentLeaf = tree.loadLeafNode(currentLeaf.getNextLeafPageId());
                    currentIndex = 0;
                } else {
                    currentLeaf = null;
                }
            }
            return record;
        }
        return null;
    }

    @Override
    public void close() {}
}
